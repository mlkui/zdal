package com.alipay.zdal.test.ut.sqlparser.oracle;

import java.util.List;

import org.junit.Test;

import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.oracle.parser.OracleStatementParser;
import com.alipay.zdal.parser.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alipay.zdal.parser.sql.stat.TableStat;

import junit.framework.Assert;


public class OracleSelectTest29   {
@Test
    public void test_0() throws Exception {
        String sql = //
        "(select id,super_category_id_1 from category where super_category_id_1 is not null and super_category_id_1 != '-1')    union    (select id,super_category_id_2 from category where super_category_id_2 is not null and super_category_id_2 != '-1')     union    (select id,super_category_id_3 from category where super_category_id_3 is not null and super_category_id_3 != '-1')    union    (select id,super_category_id_4 from category where super_category_id_4 is not null and super_category_id_4 != '-1')    union    (select id,super_category_id_5 from category where super_category_id_5 is not null and super_category_id_5 != '-1')";

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

        Assert.assertEquals(1, visitor.getTables().size());

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("category")));

        Assert.assertEquals(6, visitor.getColumns().size());

        // Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("pivot_table", "*")));
        // Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("pivot_table", "YEAR")));
        // Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("pivot_table", "order_mode")));
    }
}
