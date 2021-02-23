package com.atguigu.gmall.ums;

import com.aliyuncs.exceptions.ClientException;
import com.atguigu.gmall.ums.util.SendCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallUmsApplicationTests {



    @Test
    void contextLoads() throws ClientException {

        SendCode sendCode = new SendCode();

        sendCode.send("19965361762");
    }

}
