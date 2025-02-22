package com.alipay.zdal.parser.druid.bvt.sql.oracle;

import java.util.List;

import junit.framework.Assert;

import com.alipay.zdal.parser.druid.sql.OracleTest;
import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.oracle.parser.OracleStatementParser;
import com.alipay.zdal.parser.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alipay.zdal.parser.sql.stat.TableStat;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: OracleInsertTest2.java, v 0.1 2012-5-17 上午10:17:30 xiaoqing.zhouxq Exp $
 */
public class OracleInsertTest2 extends OracleTest {

    public void test_0() throws Exception {
        String sql = "INSERT INTO employees " + //
                     "      (employee_id, last_name, email, hire_date, job_id, salary)" + //
                     "   VALUES " + //
                     "   (employees_seq.nextval, 'Doe', 'john.doe@example.com', " + //
                     "       SYSDATE, 'SH_CLERK', 2400) " + //
                     "   RETURNING salary*12, job_id INTO :bnd1, :bnd2; ";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);
        print(statementList);

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

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("employees")));

        Assert.assertEquals(1, visitor.getTables().size());
        Assert.assertEquals(6, visitor.getColumns().size());

        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "employee_id")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "last_name")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "email")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "hire_date")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "job_id")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("employees", "salary")));
    }

}
