package com.alipay.zdal.parser.druid.bvt.sql.oracle;

import java.util.List;

import junit.framework.Assert;

import com.alipay.zdal.parser.druid.sql.OracleTest;
import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.oracle.parser.OracleStatementParser;
import com.alipay.zdal.parser.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: OracleBlockTest3.java, v 0.1 2012-5-17 上午10:14:56 xiaoqing.zhouxq Exp $
 */
public class OracleBlockTest3 extends OracleTest {

    public void test_0() throws Exception {
        String sql = "DECLARE " + //
                     "  job BINARY_INTEGER := :job; " + //
                     "  next_date DATE := :mydate;  " + //
                     "  broken BOOLEAN := FALSE; " + //
                     "BEGIN " + //
                     "  get_promo_product_search; " + //
                     "  :mydate := next_date; " + //
                     "  IF broken THEN :b := 1; " + //
                     "  ELSE :b := 0; " + //
                     "  END IF; " + //
                     "END; ";

        OracleStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement statemen = statementList.get(0);
        print(statementList);

        Assert.assertEquals(1, statementList.size());

        OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
        statemen.accept(visitor);

        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
        System.out.println("coditions : " + visitor.getConditions());
        System.out.println("relationships : " + visitor.getRelationships());
        System.out.println("orderBy : " + visitor.getOrderByColumns());

        Assert.assertEquals(0, visitor.getTables().size());

        //        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("employees")));

        Assert.assertEquals(0, visitor.getColumns().size());

        //        Assert.assertTrue(visitor.getColumns().contains(new TableStat.Column("departments", "department_id")));
    }
}
