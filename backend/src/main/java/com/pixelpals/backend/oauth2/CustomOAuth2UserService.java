package com.pixelpals.backend.oauth2;

import com.pixelpals.backend.enumeration.AuthProvider;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" o "discord"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = null;
        String username = null;
        String avatarUrl = null;

        if (registrationId.equals("google")) {
            email = (String) attributes.get("email");
            username = (String) attributes.get("name");
            avatarUrl = (String) attributes.get("picture");

        } else if (registrationId.equals("discord")) {
            email = (String) attributes.get("email");
            username = (String) attributes.get("username");

            String id = (String) attributes.get("id");
            String avatar = (String) attributes.get("avatar");
            if (id != null && avatar != null) {
                avatarUrl = "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".png";
            }
        }

        if (email == null) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(username); // aggiornabile
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setAuthProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
            newUser.setRole("USER"); // oppure usa default in DB se preferisci
            userRepository.save(newUser);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                attributes,
                "email" // attributo chiave univoca
        );
    }
}
