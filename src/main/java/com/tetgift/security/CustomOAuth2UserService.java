package com.tetgift.security;

import com.tetgift.model.Users;
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
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            processOAuth2User(oauth2User);
            return oauth2User;
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("Failed to load user: " + e.getMessage());
        }
    }

    @Transactional
    public void processOAuth2User(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        Users user = userRepository.findByEmail(email).orElseGet(() -> {
            String fullName = name != null ? name :
                    (givenName != null && familyName != null ?
                            givenName + " " + familyName : email);
            Users newUser = Users.builder()
                    .email(email)
                    .fullName(fullName)
                    .username(email.split("@")[0])
                    .build();
            Users saved = userRepository.save(newUser);
            userRepository.flush();
            return saved;
        });
    }
}