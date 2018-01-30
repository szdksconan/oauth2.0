#### SPRING BOOT + OAUTH2.0 + jdbc
###### o(╯□╰)o
搞了几天springboot的security 下的 oauth2.0,之前没接触过springboot 和security,spring boot OAUTH2.0 官方文档也解释的不是很到位,这里写了个demo备忘一下,OAUTH2.0的概念就不再阐述了:
- 协议文档:https://github.com/jeansfish/RFC6749.zh-cn
- 官方demo:https://github.com/spring-guides/tut-spring-boot-oauth2

#### sql脚本

``` sql

CREATE SCHEMA IF NOT EXISTS `test-oauth` DEFAULT CHARACTER SET utf8 ;
		USE `test-oauth` ;

		-- -----------------------------------------------------
		-- Table `test-oauth`.`clientdetails`
		-- -----------------------------------------------------
		CREATE TABLE IF NOT EXISTS `test-oauth`.`clientdetails` (
		`appId` VARCHAR(128) NOT NULL,
		`resourceIds` VARCHAR(256) NULL DEFAULT NULL,
		`appSecret` VARCHAR(256) NULL DEFAULT NULL,
		`scope` VARCHAR(256) NULL DEFAULT NULL,
		`grantTypes` VARCHAR(256) NULL DEFAULT NULL,
		`redirectUrl` VARCHAR(256) NULL DEFAULT NULL,
		`authorities` VARCHAR(256) NULL DEFAULT NULL,
		`access_token_validity` INT(11) NULL DEFAULT NULL,
		`refresh_token_validity` INT(11) NULL DEFAULT NULL,
		`additionalInformation` VARCHAR(4096) NULL DEFAULT NULL,
		`autoApproveScopes` VARCHAR(256) NULL DEFAULT NULL,
		PRIMARY KEY (`appId`))
		ENGINE = InnoDB
		DEFAULT CHARACTER SET = utf8;


		-- -----------------------------------------------------
		-- Table `test-oauth`.`oauth_access_token`
		-- -----------------------------------------------------
		CREATE TABLE IF NOT EXISTS `test-oauth`.`oauth_access_token` (
		`token_id` VARCHAR(256) NULL DEFAULT NULL,
		`token` BLOB NULL DEFAULT NULL,
		`authentication_id` VARCHAR(128) NOT NULL,
		`user_name` VARCHAR(256) NULL DEFAULT NULL,
		`client_id` VARCHAR(256) NULL DEFAULT NULL,
		`authentication` BLOB NULL DEFAULT NULL,
		`refresh_token` VARCHAR(256) NULL DEFAULT NULL,
		PRIMARY KEY (`authentication_id`))
		ENGINE = InnoDB
		DEFAULT CHARACTER SET = utf8;


		-- -----------------------------------------------------
		-- Table `test-oauth`.`oauth_approvals`
		-- -----------------------------------------------------
		CREATE TABLE IF NOT EXISTS `test-oauth`.`oauth_approvals` (
		`userId` VARCHAR(256) NULL DEFAULT NULL,
		`clientId` VARCHAR(256) NULL DEFAULT NULL,
		`scope` VARCHAR(256) NULL DEFAULT NULL,
		`status` VARCHAR(10) NULL DEFAULT NULL,
		`expiresAt` DATETIME NULL DEFAULT NULL,
		`lastModifiedAt` DATETIME NULL DEFAULT NULL)
		ENGINE = InnoDB
		DEFAULT CHARACTER SET = utf8;


		-- -----------------------------------------------------
		-- Table `test-oauth`.`oauth_client_details`
		-- -----------------------------------------------------
		CREATE TABLE IF NOT EXISTS `test-oauth`.`oauth_client_details` (
		`client_id` VARCHAR(128) NOT NULL,
		`resource_ids` VARCHAR(256) NULL DEFAULT NULL,
		`client_secret` VARCHAR(256) NULL DEFAULT NULL,
		`scope` VARCHAR(256) NULL DEFAULT NULL,
		`authorized_grant_types` VARCHAR(256) NULL DEFAULT NULL,
		`web_server_redirect_uri` VARCHAR(256) NULL DEFAULT NULL,
		`authorities` VARCHAR(256) NULL DEFAULT NULL,
		`access_token_validity` INT(11) NULL DEFAULT NULL,
		`refresh_token_validity` INT(11) NULL DEFAULT NULL,
		`additional_information` VARCHAR(4096) NULL DEFAULT NULL,
		`autoapprove` VARCHAR(256) NULL DEFAULT NULL,
		PRIMARY KEY (`client_id`))
		ENGINE = InnoDB
		DEFAULT CHARACTER SET = utf8;


		-- -----------------------------------------------------
		-- Table `test-oauth`.`oauth_client_token`
		-- -----------------------------------------------------
		CREATE TABLE IF NOT EXISTS `test-oauth`.`oauth_client_token` (
		`token_id` VARCHAR(256) NULL DEFAULT NULL,
		`token` BLOB NULL DEFAULT NULL,
		`authentication_id` VARCHAR(128) NOT NULL,
		`user_name` VARCHAR(256) NULL DEFAULT NULL,
		`client_id` VARCHAR(256) NULL DEFAULT NULL,
		PRIMARY KEY (`authentication_id`))
		ENGINE = InnoDB
		DEFAULT CHARACTER SET = utf8;


		-- -----------------------------------------------------
		-- Table `test-oauth`.`oauth_code`
		-- -----------------------------------------------------
		CREATE TABLE IF NOT EXISTS `test-oauth`.`oauth_code` (
		`code` VARCHAR(256) NULL DEFAULT NULL,
		`authentication` BLOB NULL DEFAULT NULL)
		ENGINE = InnoDB
		DEFAULT CHARACTER SET = utf8;


		-- -----------------------------------------------------
		-- Table `test-oauth`.`oauth_refresh_token`
		-- -----------------------------------------------------
		CREATE TABLE IF NOT EXISTS `test-oauth`.`oauth_refresh_token` (
		`token_id` VARCHAR(256) NULL DEFAULT NULL,
		`token` BLOB NULL DEFAULT NULL,
		`authentication` BLOB NULL DEFAULT NULL)
		ENGINE = InnoDB
		DEFAULT CHARACTER SET = utf8;



```


#### authorization server

```
//EnableAuthorizationServer注解 来表示授权服务
@SpringBootApplication
@EnableAuthorizationServer
public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).web(true).run(args);
    }

}

```

重写AuthorizationServerConfigurerAdapter类 配置token相关参数 初始化 tokenStore authenticationManager clientDetailsService,这里都是用的框架提供的模板,也可查看模板源码,根据其接口自己实现持久层，

```

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
    private DataSource dataSource;

    @Autowired
    @Qualifier("clientDetailsServiceImpl")
    private ClientDetailsService clientDetailsService;

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

```

实现 UserDetailsService 接口,spring security 登录验证需要,具体源码位于AbstractUserDetailsAuthenticationProvider类authenticate方法
而其又在org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter中被调用,这里不再多阐述可以自己去了解spring security


```
@Service
public class UserDetailsServiceImpl implements UserDetailsService {


    @Autowired
    DataSource dataSource;

    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init(){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * 验证用户是否存在 成功返回其权限
     * 可以根据username 实现应用成面的的用户认证 如无需认证 则可以写死password password是必须存在的
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据username 获取 user信息 返回给框架验证 这里方便测试写死

        System.out.println("loadUserByUsername="+username);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        User userDetails = new User(username,"sunyuki@123",true,true,true,true,authorities);
        //GrantedAuthorityImpl grantedAuthority = new GrantedAuthorityImpl();
        //grantedAuthority.setAuthority("P1F1");
        //authorities.add(grantedAuthority);
        //userDetails.setAuthorities(authorities);
        return userDetails;
    }
    
```

重写WebSecurityConfigurerAdapter类 自定义过滤机制
这里如果要用授权码模式demo 请开启httpBasic
```

@Configuration
@EnableWebSecurity
public class OAuthWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/oauth/remove_token").permitAll()
                .anyRequest().authenticated().and().csrf().disable();
                //.httpBasic(); //开起Basic认证 授权码使用
    }
}

```
#### test


这里使用密码授权(grant_type : password refresh_token) 测试 这里的clientId password 自行在数据库里添加
这里说一下 token 和 user相关 会序列化后 以二进制的情况存入数据库、所以资源服务器 在验证的时候请注意反序列化的对象，这里都是用的spring框架的所以不会存在问题，如果要使用authorization_code demo 请允许httpBasic,client_details表里有个authorized_grant_types字段.表示client支持授权类型,如需多种请以逗号隔开 如:password,refresh_token 

![image](https://github.com/szdksconan/oauth2.0/raw/master/img/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202018-01-30%20%E4%B8%8A%E5%8D%8811.33.21.png)
![image](https://github.com/szdksconan/oauth2.0/raw/master/img/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202018-01-30%20%E4%B8%8A%E5%8D%8811.33.27.png)
![image](https://github.com/szdksconan/oauth2.0/raw/master/img/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202018-01-30%20%E4%B8%8B%E5%8D%882.39.01.png)
![image](https://github.com/szdksconan/oauth2.0/raw/master/img/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202018-01-30%20%E4%B8%8B%E5%8D%882.39.08.png)



### resource server

上面获取了token 下面说下资源服务器

```
//同authorization server @EnableResourceServer标示资源服务器 导入相关jar包 请参照 server 
@SpringBootApplication
@EnableResourceServer
public class Application {
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(Application.class).web(true).run(args);
    }
}

```


重写ResourceServerConfigurerAdapter 定义过滤规则和tokenStore 

```

@Configuration
public class ResourceConfig extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "my_rest_api";

    @Autowired
    @Qualifier("tokenDataSource")
    DataSource tokenDataSource;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore());
        resources.resourceId(RESOURCE_ID).stateless(false);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
       //http.authorizeRequests().antMatchers("/**").permitAll().anyRequest().authenticated();
        super.configure(http);
    }

    /**
     * 指定token存储的位置
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(tokenDataSource);
    }

}
```

测试url如：
```
http://secure.syk.com:8081/acct/v3/payment/pay_code/record/read/47?access_token=75949cc7-21cf-43f9-bb3f-1b6e769c7967
```

这里资源服务器因为公司业务 就不上传更多的资源服务器的代码


authorization server github:https://github.com/szdksconan/oauth2.0

ps:很多细节没有仔细研究 、感觉使用学习成本真不如自己写一个 


