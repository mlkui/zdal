package com.alipay.zdal.test.ut.sqlparser.oracle;

import java.util.List;

import org.junit.Test;

import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.oracle.parser.OracleStatementParser;
import com.alipay.zdal.parser.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alipay.zdal.parser.sql.stat.TableStat;

import junit.framework.Assert;


public class OracleSelectTest7   {
@Test
    public void test_0() throws Exception {
        String sql = "SELECT * FROM employees " + //
                     "   WHERE department_id NOT IN " + //
                     "   (SELECT department_id FROM departments " + //
                     "       WHERE location_id = 1700)" + //
                     "   ORDER BY last_name;";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);


        Assert.assertEquals(1, statementList.size());

        OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
        statemen.accept(visitor);

        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("alias : " + visitor.getAliasMap());
        System.out.println("conditions : " + visitor.getConditions());
        System.out.println("orderBy : " + visitor.getOrderByColumns());
        System.out.println("groupBy : " + visitor.getGroupByColumns());
        System.out.println("variant : " + visitor.getVariants());
        System.out.println("relationShip : " + visitor.getRelationships());
        System.out.println("--------------------------------");

        Assert.assertEquals(2, visitor.getTables().size());

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("departments")));
        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("employees")));

        Assert.assertEquals(5, visitor.getColumns().size());

        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("departments", "department_id")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "last_name")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("departments", "location_id")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "*")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "department_id")));

    }
}
