package com.sunyuki.framework.oauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@RestController
public class TokenStoreController {

    @Autowired
    TokenStore tokenStore;

    @Value("${oauth.removeTokenCode}")
    String removeTokenCode;

    @RequestMapping(value = "/oauth/remove_token", method = RequestMethod.POST)
    public void removeToken(@RequestParam(value = "clientId", required = true) String clientId,
                            @RequestParam(value = "username", required = true) String username,
                            @RequestParam(value = "removeTokenCode", required = true) String removeTokenCode) {
        if (this.removeTokenCode.equals(removeTokenCode)) {
            Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(clientId, username);
            for (OAuth2AccessToken accessToken : tokens) {
                tokenStore.removeAccessToken(accessToken);
                if (accessToken.getRefreshToken() != null) {
                    tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                }
            }
        }
    }


}
