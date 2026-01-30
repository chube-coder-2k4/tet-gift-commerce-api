package com.tetgift.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest
                .getClientRegistration()
                .getRegistrationId(); // google | github

        String email;
        String name;

        if (provider.equals("google")) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else if (provider.equals("github")) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("login");
        } else if (provider.equals("keycloak")) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("preferred_username");
        } else {
            throw new OAuth2AuthenticationException("Login with " + provider + " is not supported yet.");
        }

        //check user tồn tại


        //nếu chưa tạo user


        //return CustomOAuth2User


        return oAuth2User;
    }
}
