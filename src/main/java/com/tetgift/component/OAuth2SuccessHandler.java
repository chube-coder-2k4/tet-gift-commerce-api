package com.tetgift.component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,Authentication authentication)
            throws IOException, ServletException {

        log.info("OAuth2 authentication successful for user: {}", authentication.getName());

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        if ("google".equalsIgnoreCase(registrationId)) {
            googleLogin(attributes);
        } else if ("github".equalsIgnoreCase(registrationId)) {
            githubLogin(attributes);
        } else {
            log.warn("Unsupported OAuth2 provider: {}", registrationId);
        }

        setDefaultTargetUrl("/login-success");
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void googleLogin(Map<String,Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String avatar = (String) attributes.get("picture");

        log.info("Google Login - Email: {}, Name: {}, First Name: {}, Last Name: {}, Avatar: {}",
                email, name, firstName, lastName, avatar);
    }

    private void githubLogin(Map<String,Object> attributes) {
        String username = (String) attributes.get("login");
        String avatar = (String) attributes.get("avatar_url");
        String email = (String) attributes.get("email");
        String address = (String) attributes.get("location");
        String name = (String) attributes.get("name");
        log.info("GitHub Login - Username: {}, Name: {}, Email: {}, Address: {}, Avatar: {}",
                username, name, email, address, avatar);
    }

}
