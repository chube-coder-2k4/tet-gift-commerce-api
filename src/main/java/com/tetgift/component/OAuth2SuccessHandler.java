package com.tetgift.component;

import com.tetgift.model.Users;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url:https://shophuypro.store}")
    private String frontendUrl;

    @Value("${app.cookie-secure:true}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException {

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            if (email == null || email.isEmpty()) {
                redirectToError(request, response, "email_missing");
                return;
            }

            // User is guaranteed to exist — CustomOAuth2UserService already created it
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("OAuth2 user not found after processing: " + email));

            String jwtToken = jwtService.generateAccessToken(user);

            Cookie cookie = new Cookie("JWT_TOKEN", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(cookieSecure);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            String targetUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/oauth2/redirect")
                    .queryParam("token", jwtToken)
                    .queryParam("email", email)
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            log.error("OAuth2 login error: {}", e.getMessage(), e);
            redirectToError(request, response, "server_error");
        }
    }


    private void redirectToError(HttpServletRequest request,
            HttpServletResponse response,
            String errorCode) throws IOException {
        String errorUrl = frontendUrl + "/login?error=" + errorCode;
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}