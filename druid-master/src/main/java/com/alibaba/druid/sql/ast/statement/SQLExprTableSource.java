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
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.repository.SchemaObject;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class SQLExprTableSource extends SQLTableSourceImpl {

    protected SQLExpr     expr;
    private List<SQLName> partitions;
    private SchemaObject  schemaObject;

    public SQLExprTableSource(){

    }

    public SQLExprTableSource(SQLExpr expr){
        this(expr, null);
    }

    public SQLExprTableSource(SQLExpr expr, String alias){
        this.setExpr(expr);
        this.setAlias(alias);
    }

    public SQLExpr getExpr() {
        return this.expr;
    }

    public void setExpr(SQLExpr expr) {
        if (expr != null) {
            expr.setParent(this);
        }
        this.expr = expr;
    }

    public List<SQLName> getPartitions() {
        if (this.partitions == null) {
            this.partitions = new ArrayList<SQLName>(2);
        }
        
        return partitions;
    }
    
    public int getPartitionSize() {
        if (this.partitions == null) {
            return 0;
        }
        return this.partitions.size();
    }

    public void addPartition(SQLName partition) {
        if (partition != null) {
            partition.setParent(this);
        }
        
        if (this.partitions == null) {
            this.partitions = new ArrayList<SQLName>(2);
        }
        this.partitions.add(partition);
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.expr);
        }
        visitor.endVisit(this);
    }

    public void output(StringBuffer buf) {
        this.expr.output(buf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLExprTableSource that = (SQLExprTableSource) o;

        if (expr != null ? !expr.equals(that.expr) : that.expr != null) return false;
        return partitions != null ? partitions.equals(that.partitions) : that.partitions == null;
    }

    @Override
    public int hashCode() {
        int result = expr != null ? expr.hashCode() : 0;
        result = 31 * result + (partitions != null ? partitions.hashCode() : 0);
        return result;
    }

    public String computeAlias() {
        String alias = this.getAlias();

        if (alias == null) {
            if (expr instanceof SQLName) {
                alias =((SQLName) expr).getSimpleName();
            }
        }

        return SQLUtils.normalize(alias);
    }

    public SQLExprTableSource clone() {
        SQLExprTableSource x = new SQLExprTableSource();
        cloneTo(x);
        return x;
    }

    public void cloneTo(SQLExprTableSource x) {
        x.alias = alias;

        if (expr != null) {
            x.expr = expr.clone();
        }

        if (partitions != null) {
            for (SQLName p : partitions) {
                SQLName p1 = p.clone();
                x.addPartition(p1);
            }
        }
    }

    public SchemaObject getSchemaObject() {
        return schemaObject;
    }

    public void setSchemaObject(SchemaObject schemaObject) {
        this.schemaObject = schemaObject;
    }
}
