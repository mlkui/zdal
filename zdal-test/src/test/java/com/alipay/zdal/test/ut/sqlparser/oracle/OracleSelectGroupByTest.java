package com.alipay.zdal.test.ut.sqlparser.oracle;

import java.util.List;

import org.junit.Test;

import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.oracle.parser.OracleStatementParser;
import com.alipay.zdal.parser.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;

import junit.framework.Assert;


public class OracleSelectGroupByTest   {
@Test
    public void test_0() throws Exception {
        String sql = "SELECT e.employee_id FROM employees e, orders o   WHERE e.employee_id = o.sales_rep_id   GROUP BY e.employee_id; ";

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

        //        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("orders")));

        Assert.assertEquals(2, visitor.getTables().size());
        Assert.assertEquals(2, visitor.getColumns().size());

        //        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("bonuses", "employee_id")));
    }

}
