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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLReplaceable;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class SQLJoinTableSource extends SQLTableSourceImpl implements SQLReplaceable {

    protected SQLTableSource      left;
    protected JoinType            joinType;
    protected SQLTableSource      right;
    protected SQLExpr             condition;
    protected final List<SQLExpr> using = new ArrayList<SQLExpr>();


    protected boolean             natural = false;

    public SQLJoinTableSource(String alias){
        super(alias);
    }

    public SQLJoinTableSource(){

    }

    public SQLJoinTableSource(SQLTableSource left, JoinType joinType, SQLTableSource right, SQLExpr condition){
        this.setLeft(left);
        this.setJoinType(joinType);
        this.setRight(right);
        this.setCondition(condition);
    }

    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.left);
            acceptChild(visitor, this.right);
            acceptChild(visitor, this.condition);
            acceptChild(visitor, this.using);
        }

        visitor.endVisit(this);
    }

    public JoinType getJoinType() {
        return this.joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public SQLTableSource getLeft() {
        return this.left;
    }

    public void setLeft(SQLTableSource left) {
        if (left != null) {
            left.setParent(this);
        }
        this.left = left;
    }

    public SQLTableSource getRight() {
        return this.right;
    }

    public void setRight(SQLTableSource right) {
        if (right != null) {
            right.setParent(this);
        }
        this.right = right;
    }

    public SQLExpr getCondition() {
        return this.condition;
    }

    public void setCondition(SQLExpr condition) {
        if (condition != null) {
            condition.setParent(this);
        }
        this.condition = condition;
    }

    public List<SQLExpr> getUsing() {
        return this.using;
    }

    public boolean isNatural() {
        return natural;
    }

    public void setNatural(boolean natural) {
        this.natural = natural;
    }

    public void output(StringBuffer buf) {
        this.left.output(buf);
        buf.append(' ');
        buf.append(JoinType.toString(this.joinType));
        buf.append(' ');
        this.right.output(buf);

        if (this.condition != null) {
            buf.append(" ON ");
            this.condition.output(buf);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLJoinTableSource that = (SQLJoinTableSource) o;

        if (natural != that.natural) return false;
        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (joinType != that.joinType) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;
        if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;
        return using != null ? using.equals(that.using) : that.using == null;
    }

    @Override
    public boolean replace(SQLExpr expr, SQLExpr target) {
        if (condition == expr) {
            setCondition(target);
            return true;
        }

        return false;
    }

    public static enum JoinType {
        COMMA(","), //
        JOIN("JOIN"), //
        INNER_JOIN("INNER JOIN"), //
        CROSS_JOIN("CROSS JOIN"), //
        NATURAL_JOIN("NATURAL JOIN"), //
        NATURAL_INNER_JOIN("NATURAL INNER JOIN"), //
        LEFT_OUTER_JOIN("LEFT JOIN"), //
        RIGHT_OUTER_JOIN("RIGHT JOIN"), //
        FULL_OUTER_JOIN("FULL JOIN"),//
        STRAIGHT_JOIN("STRAIGHT_JOIN"), //
        OUTER_APPLY("OUTER APPLY"),//
        CROSS_APPLY("CROSS APPLY");

        public final String name;
        public final String name_lcase;

        JoinType(String name){
            this.name = name;
            this.name_lcase = name.toLowerCase();
        }

        public static String toString(JoinType joinType) {
            return joinType.name;
        }
    }


    public void cloneTo(SQLJoinTableSource x) {
        x.alias = alias;

        if (left != null) {
            x.setLeft(left.clone());
        }

        x.joinType = joinType;

        if (right != null) {
            x.setRight(right.clone());
        }

        for (SQLExpr item : using) {
            SQLExpr item2 = item.clone();
            item2.setParent(x);
            x.using.add(item2);
        }

        x.natural = natural;
    }

    public SQLJoinTableSource clone() {
        SQLJoinTableSource x = new SQLJoinTableSource();
        cloneTo(x);
        return x;
    }
}
