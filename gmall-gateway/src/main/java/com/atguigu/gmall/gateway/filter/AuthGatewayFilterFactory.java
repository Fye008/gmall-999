package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtConfig;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResizableByteArrayOutputStream;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
@EnableConfigurationProperties(JwtConfig.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    JwtConfig jwtConfig;


    //告知父类，使用PathConfig接收配置信息
    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    //配置类中字段类型
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    //配置类中字段顺序
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                System.out.println("来到了局部过滤器...");

                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                // 判断当前请求 在不在拦截名单
                List<String> paths = config.paths; //我们配置的拦截路径
                String curPath = request.getURI().getPath();//请求的路径

                //如果配置的拦截路径为空，或者请求路径没有以拦截路径开头，就放行
                if (CollectionUtils.isEmpty(paths) || !paths.stream().anyMatch(path -> StringUtils.startsWith(curPath, path))) {
                    return chain.filter(exchange);
                }
                //从请求头中或者cookie中获取token
                String token = request.getHeaders().getFirst("token");
                if (StringUtils.isBlank(token)) {
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (cookies != null && cookies.containsKey(jwtConfig.getCookieName())) {
                        token = cookies.getFirst(jwtConfig.getCookieName()).getValue();
                    }
                }
                //判断token是否为空，没有就跳转到登录页面
                if (StringUtils.isBlank(token)) {
                    //token为空，重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://auth.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }

                try {
                    //如果解析token出现异常，说明token是伪造的
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtConfig.getPubKey());

                    //通过ip判断有没有被盗用
                    String ip = map.get("ip").toString(); //载荷中的ip
                    String curIp = IpUtil.getIpAddressAtGateway(request); //此时发起请求客户端的ip

                    if (!StringUtils.equals(ip, curIp)) {
                        //ip不一致，重定向到登录页面
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://auth.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        return response.setComplete();
                    }

                    //将token信息传递给后续服务
                    Consumer<HttpHeaders> headers = header -> {
                        header.set("userId", map.get("userId").toString());
                        header.set("userName", map.get("userName").toString());
                    };
                    request.mutate().headers(headers).build();
                    exchange.mutate().request(request).build();
                    //放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();

                    //出现异常，重定向到登陆页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://auth.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }
            }
        };
    }


    // 配置类
    @Data
    public static class PathConfig {

        private List<String> paths;
    }


}
