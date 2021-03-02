package com.atguigu.gmall.cart.config;


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
    private String cookieName;
    private Integer expire;

    private String userKey;


    private PublicKey pubKey;


    @PostConstruct
    public void init() {

        try {
            this.pubKey = RsaUtils.getPublicKey(pubKeyPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
