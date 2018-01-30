/*
package com.sunyuki.framework.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

*/
/**
 * 重定向 同意界面
 *//*==

@Controller
@SessionAttributes("authorizationRequest")
public class OAuth2ApprovalController {


    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(Map<String, Object> model, HttpServletRequest request)
            throws Exception {
        if (request.getAttribute("_csrf") != null) {
            model.put("_csrf", request.getAttribute("_csrf"));
        }
        return new ModelAndView("login", model);
    }
}
*/
