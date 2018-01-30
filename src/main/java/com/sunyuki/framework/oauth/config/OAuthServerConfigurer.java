package com.sunyuki.framework.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;


/**
 * AuthorizationServerConfigurer 需要配置三个配置-重写几个方法：
 * ClientDetailsServiceConfigurer：用于配置客户详情服务，指定存储位置
 * AuthorizationServerSecurityConfigurer：定义安全约束
 * AuthorizationServerEndpointsConfigurer：定义认证和token服务
 *
 */
@Configuration
public class OAuthServerConfigurer extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private DataSource dataSource;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //指定认证管理器
        endpoints.authenticationManager(authenticationManager);
        //指定token存储位置
        endpoints.tokenStore(tokenStore());

        // 自定义token生成方式 可以根据 业务信息生成
        //TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        //tokenEnhancerChain.setTokenEnhancers(Arrays.asList(customerEnhancer(), accessTokenConverter()));
        //endpoints.tokenEnhancer(tokenEnhancerChain);


        // 配置TokenServices参数
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(endpoints.getTokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setClientDetailsService(endpoints.getClientDetailsService());
        tokenServices.setTokenEnhancer(endpoints.getTokenEnhancer());
        tokenServices.setAccessTokenValiditySeconds( (int) TimeUnit.DAYS.toSeconds(3000)); // 3000天
        endpoints.tokenServices(tokenServices);
        super.configure(endpoints);

    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("permitAll()");
        security.allowFormAuthenticationForClients();//允许client使用form的方式进行authentication的授权
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        ClientDetailsService clientDetailsService = clientDetails();
        clients.withClientDetails(clientDetailsService);

    }

    /**
     * 定义clientDetails存储的方式，注入DataSource
     * @return
     */
    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }


    /**
     * 指定token存储的位置
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
       // return new InMemoryTokenStore();
        //return new RedisTokenStore(redisConnectionFactory);
        return new JdbcTokenStore(dataSource);
    }


    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

}
