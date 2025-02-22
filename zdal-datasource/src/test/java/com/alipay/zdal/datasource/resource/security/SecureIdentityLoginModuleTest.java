package com.alipay.zdal.datasource.resource.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * SecureIdentityLoginModule测试用例
 * 
 * @author liangjie.li
 * @version $Id: SecureIdentityLoginModuleTest.java, v 0.1 2012-8-8 下午4:34:31 liangjie.li Exp $
 */
public class SecureIdentityLoginModuleTest {
    private static final String PASSWORD     = "mysql";
    private static final String ENC_PASSWORD = "-76079f94c1e11c89";

    @Test
    public void testDecodedPassword() throws Exception {

        assertEquals(PASSWORD, new String(SecureIdentityLoginModule.decode(ENC_PASSWORD)));
    }
}
