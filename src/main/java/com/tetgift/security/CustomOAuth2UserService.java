package com.tetgift.security;

import com.tetgift.model.Role;
import com.tetgift.model.Users;
import com.tetgift.repository.jpa.RoleRepository;
import com.tetgift.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            processOAuth2User(oauth2User);
            return oauth2User;
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process OAuth2 user: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Failed to load user: " + e.getMessage());
        }
    }

    @Transactional
    public void processOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Only create user if not exists — actual user creation is in
        // OAuth2SuccessHandler
        if (userRepository.findByEmail(email).isEmpty()) {
            String name = (String) attributes.get("name");
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");

            String fullName = name != null ? name
                    : (givenName != null && familyName != null ? givenName + " " + familyName : email);

            Role userRole = roleRepository.findByName("USER").orElse(null);

            Users newUser = Users.builder()
                    .email(email)
                    .fullName(fullName)
                    .username(email.split("@")[0])
                    .role(userRole)
                    .isVerify(true)
                    .isActive(true)
                    .build();

            userRepository.save(newUser);
            userRepository.flush();
            log.info("Created new OAuth2 user: {}", email);
        }
    }
}