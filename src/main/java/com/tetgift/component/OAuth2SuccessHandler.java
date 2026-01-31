package com.tetgift.component;

import com.tetgift.model.Role;
import com.tetgift.model.Users;
import com.tetgift.repository.jpa.RoleRepository;
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
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

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
            Thread.sleep(100);

            Users user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        return createUserFromOAuth2(oAuth2User);
                    });

            String jwtToken = jwtService.generateAccessToken(user);

            Cookie cookie = new Cookie("JWT_TOKEN", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
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
            redirectToError(request, response, "server_error");
        }
    }

    private Users createUserFromOAuth2(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");
        String fullName = name != null ? name :
                (givenName != null && familyName != null ?
                        givenName + " " + familyName : email);
        Set<Role> role = roleRepository.findByName("USER");
        Users newUser = Users.builder()
                .email(email)
                .fullName(fullName)
                .username(email.split("@")[0])
                .roles(role)
                .build();
        return userRepository.save(newUser);
    }

    private void redirectToError(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String errorCode) throws IOException {
        String errorUrl = frontendUrl + "/login?error=" + errorCode;
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}