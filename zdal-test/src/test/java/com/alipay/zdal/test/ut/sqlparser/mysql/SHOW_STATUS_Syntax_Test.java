package com.alipay.zdal.test.ut.sqlparser.mysql;

import java.util.List;

import org.junit.Test;

import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alipay.zdal.parser.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alipay.zdal.parser.sql.parser.SQLStatementParser;

import junit.framework.Assert;

public class SHOW_STATUS_Syntax_Test {
	@Test
	 public void test_0() throws Exception {
	        String sql = "SHOW STATUS LIKE 'Key%'";

	        SQLStatementParser parser = new MySqlStatementParser(sql);
	        List<SQLStatement> stmtList = parser.parseStatementList();

	        String text = output(stmtList);

	        Assert.assertEquals("SHOW STATUS LIKE 'Key%';", text);
	    }
	@Test
	    public void test_1() throws Exception {
	        String sql = "SHOW GLOBAL STATUS LIKE 'Key%'";

	        SQLStatementParser parser = new MySqlStatementParser(sql);
	        List<SQLStatement> stmtList = parser.parseStatementList();

	        String text = output(stmtList);

	        Assert.assertEquals("SHOW GLOBAL STATUS LIKE 'Key%';", text);
	    }
	@Test
	    public void test_2() throws Exception {
	        String sql = "SHOW SESSION STATUS LIKE 'Key%'";

	        SQLStatementParser parser = new MySqlStatementParser(sql);
	        List<SQLStatement> stmtList = parser.parseStatementList();

	        String text = output(stmtList);

	        Assert.assertEquals("SHOW SESSION STATUS LIKE 'Key%';", text);
	    }
	@Test
	    public void test_3() throws Exception {
	        String sql = "SHOW SESSION STATUS";

	        SQLStatementParser parser = new MySqlStatementParser(sql);
	        List<SQLStatement> stmtList = parser.parseStatementList();

	        String text = output(stmtList);

	        Assert.assertEquals("SHOW SESSION STATUS;", text);
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
