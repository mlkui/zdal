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
 * @version $Id: RegularExpressionsTest.java, v 0.1 2012-5-17 上午10:07:34 xiaoqing.zhouxq Exp $
 */
public class RegularExpressionsTest extends TestCase {

    public void test_0() throws Exception {
        String sql = "SELECT 'Monty!' REGEXP '.*'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'Monty!' REGEXP '.*';", text);
    }

    public void test_1() throws Exception {
        String sql = "SELECT 'new*\n*line' REGEXP 'new\\\\*.\\\\*line'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'new*\n*line' REGEXP 'new\\*.\\*line';", text);
    }

    public void test_2() throws Exception {
        String sql = "SELECT 'a' REGEXP 'A', 'a' REGEXP BINARY 'A'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'a' REGEXP 'A', 'a' REGEXP BINARY 'A';", text);
    }

    public void test_3() throws Exception {
        String sql = "SELECT 'a' REGEXP '^[a-d]'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'a' REGEXP '^[a-d]';", text);
    }

    public void test_4() throws Exception {
        String sql = "SELECT 'fo\nfo' REGEXP '^fo$'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'fo\nfo' REGEXP '^fo$';", text);
    }

    public void test_5() throws Exception {
        String sql = "SELECT 'fofo' REGEXP '^fo'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT 'fofo' REGEXP '^fo';", text);
    }

    public void test_6() throws Exception {
        String sql = "SELECT '~' REGEXP '[[.tilde.]]'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT '~' REGEXP '[[.tilde.]]';", text);
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
