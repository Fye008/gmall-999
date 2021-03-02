package com.atguigu.gmall.author.service;

import com.alibaba.nacos.client.utils.IPUtil;
import com.atguigu.gmall.author.config.JwtConfig;
import com.atguigu.gmall.author.feign.GmallumsClient;

import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties(JwtConfig.class)
public class AuthService {

    @Autowired
    private GmallumsClient gmallumsClient;

    @Autowired
    private JwtConfig jwtConfig;


    public void checkUser(String loginName, String password, HttpServletRequest request, HttpServletResponse response){

        //验证用户密码是否正有对应的用户
        UserEntity userEntity = gmallumsClient.queryUser(loginName, password).getData();
        if (userEntity == null){
            throw new RuntimeException("用户不存在");
        }

        Map<String,Object> map = new HashMap<>();
        map.put("userId",userEntity.getId());
        map.put("userName",userEntity.getUsername());
        /**
         * 获取ip地址
         * 需要在nginx中配置：（否则，获取的是nginx服务器的ip地址）
         *      proxy_set_header X-real-ip $remote_addr;
         *      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         */
        map.put("ip", IpUtil.getIpAddressAtService(request));

        try {
            //生成jwt
            String token = JwtUtils.generateToken(map, jwtConfig.getPriKey(), jwtConfig.getExpire());
            //将token写入cookie
            CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),token,jwtConfig.getExpire() * 60);
            //为了方便展示用户的登录信息，需要写入unick
            CookieUtils.setCookie(request,response,jwtConfig.getUnick(),userEntity.getNickname(),jwtConfig.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
