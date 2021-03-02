package com.atguigu.gmall.author.config;


import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private String cookieName;
    private Integer expire;
    private String unick;


    private PublicKey pubKey;
    private PrivateKey priKey;

    @PostConstruct
    public void init() {

        try {
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);

            if (!pubFile.exists() || !priFile.exists()) {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            this.pubKey = RsaUtils.getPublicKey(pubKeyPath);
            this.priKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
