package com.sunyuki.framework.oauth;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

//@EnableAuthorizationServer 注解来配置OAuth2.0 授权服务机制
@SpringBootApplication
@EnableAuthorizationServer
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).web(true).run(args);
    }

}
