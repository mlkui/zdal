package com.alipay.zdal.parser.druid.bvt.sql.oracle;

import java.util.List;

import junit.framework.Assert;

import com.alipay.zdal.parser.druid.sql.OracleTest;
import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.oracle.parser.OracleStatementParser;
import com.alipay.zdal.parser.sql.stat.TableStat;
import com.alipay.zdal.parser.visitor.ZdalOracleSchemaStatVisitor;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: OracleUpdateTest1.java, v 0.1 2012-5-17 上午10:22:37 xiaoqing.zhouxq Exp $
 */
public class OracleUpdateTest1 extends OracleTest {

    public void test_0() throws Exception {
        String sql = "UPDATE table1 t_alias1 " + //
                     "    SET column = " + //
                     "        (SELECT expr " + //
                     "            FROM table2 t_alias2 " + //
                     "            WHERE t_alias1.column = t_alias2.column); ";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);
        print(statementList);

        Assert.assertEquals(1, statementList.size());

        ZdalOracleSchemaStatVisitor visitor = new ZdalOracleSchemaStatVisitor();
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

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("table1")));
        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("table2")));

        Assert.assertEquals(2, visitor.getTables().size());
        Assert.assertEquals(3, visitor.getColumns().size());

        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("table1", "column")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("table2", "expr")));
        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("table2", "column")));
    }

}
