package com.alipay.zdal.parser.druid.bvt.sql.db2.visitor;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.oracle.parser.OracleStatementParser;
import com.alipay.zdal.parser.sql.stat.TableStat.Column;
import com.alipay.zdal.parser.visitor.ZdalDB2SchemaStatVisitor;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: OracleSchemaStatVisitorTest_Update.java, v 0.1 2012-5-17 上午10:23:27 xiaoqing.zhouxq Exp $
 */
public class DB2SchemaStatVisitorTest_Update extends TestCase {

    public void test_0() throws Exception {
        String sql = "update users set c1 = ?, c2 = 'nihao' where c3 = ? and  c4=now()";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        ZdalDB2SchemaStatVisitor visitor = new ZdalDB2SchemaStatVisitor();
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
        System.out.println("bindColumns : " + visitor.getBindVarConditions());
        System.out.println("noBindConditions : " + visitor.getNoBindVarConditions());
        System.out.println("--------------------------------");

        Assert.assertEquals(1, visitor.getTables().size());
        Assert.assertEquals(true, visitor.containsTable("users"));

        Assert.assertEquals(4, visitor.getColumns().size());
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "c1")));
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "c2")));
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "c3")));
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "c4")));

    }

}
