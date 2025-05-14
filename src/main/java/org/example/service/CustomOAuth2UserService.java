package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new IllegalArgumentException("Email not found from OAuth2 provider");
        }
        System.out.println("Loading user with email: " + email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : "NoName");
            user.setPassword("");

            user.setRole("ROLE_USER");

            if (email.equals("aylok2018@gmail.com")) {
                user.setRole("ROLE_ADMIN");
            }
            System.out.println("User role before saving: " + user.getRole());

            userRepository.save(user);

            System.out.println("Saved user with role: " + user.getRole());
        }
        String role = user.getRole();

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(role)),
                oAuth2User.getAttributes(),
                "email"
        );

    }
}
