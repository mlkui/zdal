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
 * @version $Id: OracleSchemaStatVisitorTest_Subquery2.java, v 0.1 2012-5-17 上午10:23:24 xiaoqing.zhouxq Exp $
 */
public class DB2SchemaStatVisitorTest_Subquery2 extends TestCase {

    public void test_0() throws Exception {
        String sql = "SELECT a.id, a.name, b.name groupName FROM (select id, name, groupId from users WHERE ROWNUM < 10) a inner join groups b on a.groupId = b.id";

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
        System.out.println("noBindConditions : " + visitor.getBindVarConditions());
        System.out.println("--------------------------------");

        Assert.assertEquals(2, visitor.getTables().size());
        Assert.assertEquals(true, visitor.containsTable("users"));
        Assert.assertEquals(true, visitor.containsTable("groups"));

        Assert.assertEquals(5, visitor.getColumns().size());
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "id")));
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "groupId")));
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("users", "name")));
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("groups", "id")));
        Assert.assertEquals(true, visitor.getColumns().contains(new Column("groups", "name")));

    }

}
