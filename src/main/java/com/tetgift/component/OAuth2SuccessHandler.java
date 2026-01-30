package com.tetgift.component;

import com.tetgift.model.Users;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,Authentication authentication)
            throws IOException, ServletException {

        log.info("OAuth2 authentication successful for user: {}", authentication.getName());

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String username = (String) attributes.get("preferred_username");
        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");

        Optional<Users> userOpt = userRepository.findByEmail(email);
        Users user = null;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = Users.builder()
                    .username(username)
                    .email(email)
                    .fullName(firstName + " " + lastName)
                    .build();
            userRepository.save(user);
        }
        String jwtToken = jwtService.generateAccessToken(user);
        Cookie cookie = new Cookie("JWT_TOKEN", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);


        super.onAuthenticationSuccess(request, response, authentication);
    }


}
