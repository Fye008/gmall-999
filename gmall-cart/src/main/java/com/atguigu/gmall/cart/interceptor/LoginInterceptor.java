package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtConfig;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtConfig.class)
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtConfig jwtConfig;

    //声明线程的局部变量
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在handler处理之前

        //获取登陆头信息
        String userKey = CookieUtils.getCookieValue(request, jwtConfig.getUserKey());
        if (StringUtils.isBlank(userKey)) {
            //cookie中没有userKey,制作userKey
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, jwtConfig.getUserKey(), userKey, jwtConfig.getExpire());
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);

        //判断用户是否登录，登录：取userId
        String token = CookieUtils.getCookieValue(request, jwtConfig.getCookieName());
        if (StringUtils.isNotBlank(token)) {
            //解析token
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtConfig.getPubKey());
            userInfo.setUserId(map.get("userId").toString());
        }
        THREAD_LOCAL.set(userInfo);

        System.out.println("进入到l拦截器....");

        return true;
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }
}
