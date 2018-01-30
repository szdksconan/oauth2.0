package com.sunyuki.framework.oauth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 必须实现的接口 否则 系统会给出一个默认的密码
 */
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


        System.out.println("loadUserByUsername="+username);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        User userDetails = new User(username,"sunyuki@123",true,true,true,true,authorities);
        //GrantedAuthorityImpl grantedAuthority = new GrantedAuthorityImpl();
        //grantedAuthority.setAuthority("P1F1");
        //authorities.add(grantedAuthority);
        //userDetails.setAuthorities(authorities);
        return userDetails;
    }


}
