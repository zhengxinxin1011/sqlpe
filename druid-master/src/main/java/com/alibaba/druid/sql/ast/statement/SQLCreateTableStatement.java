/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.ast.statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLStatementImpl;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.lang.Consumer;

public class SQLCreateTableStatement extends SQLStatementImpl implements SQLDDLStatement {

    protected boolean            ifNotExiists = false;
    protected Type               type;
    protected SQLExprTableSource tableSource;

    protected List<SQLTableElement> tableElementList = new ArrayList<SQLTableElement>();

    // for postgresql
    private SQLExprTableSource inherits;

    protected SQLSelect select;

    public SQLCreateTableStatement(){

    }

    public SQLCreateTableStatement(String dbType){
        super(dbType);
    }

    public SQLName getName() {
        if (tableSource == null) {
            return null;
        }

        return (SQLName) tableSource.getExpr();
    }

    public void setName(SQLName name) {
        this.setTableSource(new SQLExprTableSource(name));
    }

    public SQLExprTableSource getTableSource() {
        return tableSource;
    }

    public void setTableSource(SQLExprTableSource tableSource) {
        if (tableSource != null) {
            tableSource.setParent(this);
        }
        this.tableSource = tableSource;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static enum Type {
                             GLOBAL_TEMPORARY, LOCAL_TEMPORARY
    }

    public List<SQLTableElement> getTableElementList() {
        return tableElementList;
    }

    public boolean isIfNotExiists() {
        return ifNotExiists;
    }

    public void setIfNotExiists(boolean ifNotExiists) {
        this.ifNotExiists = ifNotExiists;
    }

    public SQLExprTableSource getInherits() {
        return inherits;
    }

    public void setInherits(SQLExprTableSource inherits) {
        if (inherits != null) {
            inherits.setParent(this);
        }
        this.inherits = inherits;
    }

    public SQLSelect getSelect() {
        return select;
    }

    public void setSelect(SQLSelect select) {
        this.select = select;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            this.acceptChild(visitor, tableSource);
            this.acceptChild(visitor, tableElementList);
            this.acceptChild(visitor, inherits);
            this.acceptChild(visitor, select);
        }
        visitor.endVisit(this);
    }
    
    @SuppressWarnings("unchecked")
    public void addBodyBeforeComment(List<String> comments) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>(1);
        }
        
        List<String> attrComments = (List<String>) attributes.get("format.body_before_comment");
        if (attrComments == null) {
            attributes.put("format.body_before_comment", comments);
        } else {
            attrComments.addAll(comments);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getBodyBeforeCommentsDirect() {
        if (attributes == null) {
            return null;
        }
        
        return (List<String>) attributes.get("format.body_before_comment");
    }
    
    public boolean hasBodyBeforeComment() {
        List<String> comments = getBodyBeforeCommentsDirect();
        if (comments == null) {
            return false;
        }
        
        return !comments.isEmpty();
    }

    public String computeName() {
        if (tableSource == null) {
            return null;
        }

        SQLExpr expr = tableSource.getExpr();
        if (expr instanceof SQLName) {
            String name = ((SQLName) expr).getSimpleName();
            return SQLUtils.normalize(name);
        }

        return null;
    }

    public SQLColumnDefinition findColumn(String columName) {
        for (SQLTableElement element : tableElementList) {
            if (element instanceof SQLColumnDefinition) {
                SQLColumnDefinition column = (SQLColumnDefinition) element;
                String name = column.computeAlias();
                if (columName.equalsIgnoreCase(name)) {
                    return column;
                }
            }
        }

        return null;
    }

    public SQLTableElement findIndex(String columnName) {
        for (SQLTableElement element : tableElementList) {
            List<SQLExpr> keyColumns = null;
            if (element instanceof SQLUniqueConstraint) {
                SQLUniqueConstraint unique = (SQLUniqueConstraint) element;
                keyColumns = unique.getColumns();
            } else if (element instanceof MySqlTableIndex) {
                keyColumns = ((MySqlTableIndex) element).getColumns();
            }

            if (keyColumns == null) {
                continue;
            }

            for (SQLExpr columnExpr : keyColumns) {
                if (columnExpr instanceof SQLIdentifierExpr) {
                    String keyColumName = ((SQLIdentifierExpr) columnExpr).getName();
                    keyColumName = SQLUtils.normalize(keyColumName);
                    if (keyColumName.equalsIgnoreCase(columnName)) {
                        return element;
                    }
                }
            }
        }

        return null;
    }

    public void forEachColumn(Consumer<SQLColumnDefinition> columnConsumer) {
        if (columnConsumer == null) {
            return;
        }

        for (SQLTableElement element : this.tableElementList) {
            if (element instanceof SQLColumnDefinition) {
                columnConsumer.accept((SQLColumnDefinition) element);
            }
        }
    }
}
