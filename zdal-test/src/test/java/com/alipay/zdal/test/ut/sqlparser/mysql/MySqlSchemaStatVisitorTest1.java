package com.alipay.zdal.test.ut.sqlparser.mysql;

import java.util.List;

import org.junit.Test;

import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alipay.zdal.parser.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alipay.zdal.parser.sql.stat.TableStat.Column;
import com.alipay.zdal.parser.visitor.ZdalMySqlSchemaStatVisitor;

import junit.framework.Assert;


public class MySqlSchemaStatVisitorTest1   {
@Test
    public void test_0() throws Exception {
        String sql = "select a.name, b.name FROM users a, usergroups b on a.groupId = b.id";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        ZdalMySqlSchemaStatVisitor visitor = new ZdalMySqlSchemaStatVisitor();
        statemen.accept(visitor);

        System.out.println(sql);
        System.out.println("current table : " + visitor.getCurrentTable());
        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("alias : " + visitor.getAliasMap());
        System.out.println("conditions : " + visitor.getConditions());
        System.out.println("orderBy : " + visitor.getOrderByColumns());
        System.out.println("groupBy : " + visitor.getGroupByColumns());
        System.out.println("variant : " + visitor.getVariants());
        System.out.println("relationShip : " + visitor.getRelationships());
        System.out.println("limit : " + visitor.getLimits());

        Assert.assertEquals(2, visitor.getTables().size());
        Assert.assertEquals(true, visitor.containsTable("users"));
        Assert.assertEquals(true, visitor.containsTable("usergroups"));

        Assert.assertEquals(4, visitor.getColumns().size());
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("users", "groupId")));
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("users", "name")));
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("usergroups", "id")));
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("usergroups", "name")));

    }
@Test
    public void test_1() throws Exception {
        String sql = "select a.name, b.name FROM users a, usergroups b on a.groupId = b.id where a.groupID = ?";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        statemen.accept(visitor);

        System.out.println(sql);
        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("alias : " + visitor.getAliasMap());
        System.out.println("conditions : " + visitor.getConditions());
        System.out.println("orderBy : " + visitor.getOrderByColumns());
        System.out.println("groupBy : " + visitor.getGroupByColumns());
        System.out.println("variant : " + visitor.getVariants());
        System.out.println("relationShip : " + visitor.getRelationships());

        Assert.assertEquals(2, visitor.getTables().size());
        Assert.assertEquals(true, visitor.containsTable("users"));
        Assert.assertEquals(true, visitor.containsTable("usergroups"));

        Assert.assertEquals(4, visitor.getColumns().size());
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("users", "groupId")));
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("users", "name")));
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("usergroups", "id")));
        Assert.assertEquals(true, visitor.getColumns().contains(
            new Column("usergroups", "name")));

    }
}
