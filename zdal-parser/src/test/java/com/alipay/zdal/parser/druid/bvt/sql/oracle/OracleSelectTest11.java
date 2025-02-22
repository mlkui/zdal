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
 * @version $Id: OracleSelectTest11.java, v 0.1 2012-5-17 上午10:19:54 xiaoqing.zhouxq Exp $
 */
public class OracleSelectTest11 extends OracleTest {

    public void test_0() throws Exception {
        String sql = "SELECT ID, GMT_CREATE, GMT_MODIFIED, COMPANY_ID, MEMBER_ID"
                     + //
                     "    , MEMBER_SEQ, FILE_NAME, DISPLAY_NAME, DISPLAY_NAME_UTF8, FILE_SIZE"
                     + //
                     "    , STATUS, WIDTH, HEIGHT, REFERENCE_COUNT, HASH_CODE"
                     + //
                     "    , GROUP_ID"
                     + //
                     " FROM (SELECT *"
                     + //
                     "    FROM (SELECT row_.*, rownum AS rownum_"
                     + //
                     "        FROM (SELECT rowid AS rid"
                     + //
                     "            FROM IMAGE_REPOSITORY"
                     + //
                     "            WHERE COMPANY_ID = :1 AND STATUS = 'enable' "
                     + //
                     "                 AND MEMBER_SEQ = :2 AND GMT_MODIFIED >= to_date(:3, 'yyyy-mm-dd hh24:mi:ss') "
                     + //
                     "                 AND GMT_MODIFIED < to_date(:4, 'yyyy-mm-dd hh24:mi:ss')" + //
                     "            ORDER BY \"FILE_SIZE\" DESC, \"ID\" DESC" + // //
                     "            ) row_" + //
                     "        WHERE rownum <= :5" + //
                     "        )" + //
                     "    WHERE rownum_ >= :6" + //
                     "    ) a, IMAGE_REPOSITORY b " + //
                     " WHERE a.rid = b.rowid;"; //

        //        sql = "select ID, GMT_CREATE, GMT_MODIFIED, COMPANY_ID, MEMBER_ID, MEMBER_SEQ, FILE_NAME, DISPLAY_NAME, DISPLAY_NAME_UTF8, FILE_SIZE, STATUS, WIDTH, HEIGHT, REFERENCE_COUNT, HASH_CODE, GROUP_ID     from (            select * from (select row_.*, rownum rownum_ from (          select rowid rid     from IMAGE_REPOSITORY     where COMPANY_ID = :1            and STATUS = 'enable'                          and             MEMBER_SEQ = :2                   and                              GMT_MODIFIED >= to_date(:3 , 'yyyy-mm-dd hh24:mi:ss')                               and                              GMT_MODIFIED < to_date(:4 , 'yyyy-mm-dd hh24:mi:ss')                                        order by           \"FILE_SIZE\"      desc     ,      \"ID\"      desc                         )row_ where rownum <= :5  ) where rownum_ >= :6           ) a , IMAGE_REPOSITORY b where a.rid = b.rowid   ";

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

        Assert.assertEquals(1, visitor.getTables().size());

        Assert.assertTrue(visitor.getTables().containsKey(new TableStat.Name("IMAGE_REPOSITORY")));

        Assert.assertEquals(8, visitor.getColumns().size());

        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("IMAGE_REPOSITORY", "rowid")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("IMAGE_REPOSITORY", "COMPANY_ID")));
        Assert.assertTrue(visitor.getColumns().contains(
            new TableStat.Column("IMAGE_REPOSITORY", "STATUS")));
    }
}
