package org.example.controller;

import org.example.service.CustomOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class OAuth2LoginController {

    private final CustomOAuth2UserService oauth2UserService;

    public OAuth2LoginController(CustomOAuth2UserService oauth2UserService) {
        this.oauth2UserService = oauth2UserService;
    }

    @GetMapping("/login/oauth2/code/google")
    public String oauth2LoginSuccess(OAuth2UserRequest userRequest, @AuthenticationPrincipal OAuth2User principal) {
        oauth2UserService.loadUser(userRequest);
        return "redirect:/swagger-ui/index.html";
    }
}

