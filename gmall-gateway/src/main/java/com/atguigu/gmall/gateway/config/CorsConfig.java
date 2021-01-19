package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("GET");

        config.addAllowedHeader("*");
        //允许那些域名可以访问
        config.addAllowedOrigin("http://manager.gmall.com");
        //允许携带cookie
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);

        CorsWebFilter corsWebFilter = new CorsWebFilter(source);
        return corsWebFilter;
    }

}
