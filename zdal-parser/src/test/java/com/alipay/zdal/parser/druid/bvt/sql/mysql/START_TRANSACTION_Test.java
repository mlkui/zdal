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
 * @version $Id: START_TRANSACTION_Test.java, v 0.1 2012-5-17 上午10:08:15 xiaoqing.zhouxq Exp $
 */
public class START_TRANSACTION_Test extends TestCase {

    public void test_0() throws Exception {
        String sql = "START TRANSACTION;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("START TRANSACTION;", text);
    }

    public void test_1() throws Exception {
        String sql = "START TRANSACTION WITH CONSISTENT SNAPSHOT;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("START TRANSACTION WITH CONSISTENT SNAPSHOT;", text);
    }

    public void test_2() throws Exception {
        String sql = "START TRANSACTION BEGIN;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("START TRANSACTION BEGIN;", text);
    }

    public void test_3() throws Exception {
        String sql = "START TRANSACTION BEGIN WORK;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("START TRANSACTION BEGIN WORK;", text);
    }

    public void test_4() throws Exception {
        String sql = "COMMIT;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("COMMIT;", text);
    }

    public void test_5() throws Exception {
        String sql = "COMMIT WORK;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("COMMIT WORK;", text);
    }

    public void test_6() throws Exception {
        String sql = "ROLLBACK;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("ROLLBACK;", text);
    }

    public void test_7() throws Exception {
        String sql = "SET autocommit = 0;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SET @@autocommit = 0;", text);
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
