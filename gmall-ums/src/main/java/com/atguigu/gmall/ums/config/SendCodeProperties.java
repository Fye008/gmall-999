package com.atguigu.gmall.ums.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aliyun.sms")
@Data
@Component
public class SendCodeProperties {


   private String accessKeyId;
   private String accessSecret;
   private String TemplateCode;
   private String SignName;


}
