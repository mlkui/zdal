/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.zdal.parser.druid.bvt.sql.mysql;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alipay.zdal.parser.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alipay.zdal.parser.sql.parser.SQLStatementParser;

/**
 * 
 * @author xiaoqing.zhouxq
 * @version $Id: CastFunctionsAndOperatorsTest.java, v 0.1 2012-5-17 上午10:03:51 xiaoqing.zhouxq Exp $
 */
public class CastFunctionsAndOperatorsTest extends TestCase {

    public void test_0() throws Exception {
        String sql = "SELECT 'a' = 'A'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'a' = 'A';", text);
    }

    public void test_1() throws Exception {
        String sql = "SELECT BINARY 'a' = 'A'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT BINARY 'a' = 'A';", text);
    }

    public void test_2() throws Exception {
        String sql = "SELECT CONVERT('abc' USING utf8)";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CONVERT('abc' USING utf8);", text);
    }

    public void test_3() throws Exception {
        String sql = "SELECT 'A' LIKE CONVERT(blob_col USING latin1) FROM tbl_name;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'A' LIKE CONVERT(blob_col USING latin1)\nFROM tbl_name;", text);
    }

    public void test_4() throws Exception {
        String sql = "SELECT 'A' LIKE CONVERT(blob_col USING latin1) COLLATE latin1_german1_ci FROM tbl_name;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert
            .assertEquals(
                "SELECT 'A' LIKE CONVERT(blob_col USING latin1) COLLATE latin1_german1_ci\nFROM tbl_name;",
                text);
    }

    public void test_5() throws Exception {
        String sql = "SET @str = BINARY 'New York';";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SET @str = BINARY 'New York';", text);
    }

    public void test_6() throws Exception {
        String sql = "SELECT LOWER(@str), LOWER(CONVERT(@str USING latin1))";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT LOWER(@str), LOWER(CONVERT(@str USING latin1));", text);
    }

    public void test_7() throws Exception {
        String sql = "SELECT enum_col FROM tbl_name ORDER BY CAST(enum_col AS CHAR);";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT enum_col\nFROM tbl_name\nORDER BY CAST(enum_col AS CHAR);",
            text);
    }

    public void test_8() throws Exception {
        String sql = "SELECT CONCAT('hello you ',2);";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CONCAT('hello you ', 2);", text);
    }

    public void test_9() throws Exception {
        String sql = "SELECT CAST(1-2 AS UNSIGNED);";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CAST(1 - 2 AS UNSIGNED);", text);
    }

    public void test_10() throws Exception {
        String sql = "SELECT CAST(CAST(1-2 AS UNSIGNED) AS SIGNED);";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CAST(CAST(1 - 2 AS UNSIGNED) AS SIGNED);", text);
    }

    public void test_11() throws Exception {
        String sql = "SELECT CAST(1 AS UNSIGNED) - 2.0;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CAST(1 AS UNSIGNED) - 2.0;", text);
    }

    private String output(List<SQLStatement> stmtList) {
        StringBuilder out = new StringBuilder();

        for (SQLStatement stmt : stmtList) {
            stmt.accept(new MySqlOutputVisitor(out));
            out.append(";");
        }

        return out.toString();
    }
}
