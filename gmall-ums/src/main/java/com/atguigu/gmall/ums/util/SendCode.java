package com.atguigu.gmall.ums.util;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gmall.ums.config.SendCodeProperties;
import org.springframework.beans.factory.annotation.Autowired;


public class SendCode {

    @Autowired
    private SendCodeProperties sendCodeProperties;


    public void send(String phone) throws ClientException {

        System.out.println(sendCodeProperties);


        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", sendCodeProperties.getAccessKeyId(), sendCodeProperties.getAccessSecret());
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", sendCodeProperties.getSignName());
        request.putQueryParameter("TemplateCode", sendCodeProperties.getTemplateCode());
        request.putQueryParameter("TemplateParam", "{\"code\":\"${code}\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws ClientException {
        SendCode sendCode = new SendCode();
        sendCode.send("19965361762");
    }
}
