package com.atguigu.gmall.author;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GmallAuthorApplicationTests {

    // 别忘了创建D:\\project\rsa目录
    private static final String pubKeyPath = "F:\\rsa\\rsa.pub";
    private static final String priKeyPath = "F:\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MTQxNTc4MTF9.Uqjmi30eQV6OSrh6cI07p42yvaeN_6_q7vDEU91hIm5wXIaptWtjJvxsqSJeFqpwUnn4BNol4cIraLCkwAqPE87_jlVSflinPjLVqwgv8JA0TN9YjxGEA3PWtnMyqurGjS6lwqmCELkce81vZ3qnwUMx4Riuy9Ek5br0MWjyBxjF14QtI0sSzhkg8R2CV1F2IaPp2bso9HnRZQ90LV3A3fin0Nltkx0HFueNwlwaqBadRGrxmGCGeQ-284WG_wP5qn7_cmO1sBmm0LZ0RmDRHPQkhYyaL_dCoOnxcGbYeC--5AsV5QdXkHH85wMQxHoVASeJ_qpK6mtJR2yE_83SpQ";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }

}
