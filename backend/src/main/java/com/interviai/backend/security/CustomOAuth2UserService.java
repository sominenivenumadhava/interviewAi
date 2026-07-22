package com.interviai.backend.security;

import com.interviai.backend.module.user.entity.AuthProvider;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        String email = null;
        String name = null;
        String providerId = null;
        
        if (authProvider == AuthProvider.GOOGLE) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("sub");
        } else if (authProvider == AuthProvider.GITHUB) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = String.valueOf((Integer) oAuth2User.getAttribute("id"));
            if (email == null) {
                email = oAuth2User.getAttribute("login") + "@github.com";
            }
        }
        
        if (email == null) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getAuthProvider().equals(authProvider) && !user.getAuthProvider().equals(AuthProvider.LOCAL)) {
                throw new RuntimeException("Looks like you're signed up with " +
                        user.getAuthProvider() + " account. Please use your " + user.getAuthProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, name, providerId, authProvider);
        } else {
            user = registerNewUser(oAuth2UserRequest, email, name, providerId, authProvider);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, String email, String name, String providerId, AuthProvider authProvider) {
        String firstName = name;
        String lastName = "";
        if (name != null && name.contains(" ")) {
            firstName = name.substring(0, name.indexOf(" "));
            lastName = name.substring(name.indexOf(" ") + 1);
        } else if (name == null) {
            firstName = "User";
        }

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .authProvider(authProvider)
                .providerId(providerId)
                .emailVerified(true)
                .role(User.UserRole.USER)
                .loginAttempts(0)
                .timezone("UTC")
                .language("en")
                .marketingEmailsEnabled(true)
                .notificationEmailsEnabled(true)
                .build();

        user.activate(); // sets isActive = true

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, String name, String providerId, AuthProvider authProvider) {
        String firstName = name;
        String lastName = "";
        if (name != null && name.contains(" ")) {
            firstName = name.substring(0, name.indexOf(" "));
            lastName = name.substring(name.indexOf(" ") + 1);
        }
        
        if (name != null) {
            existingUser.setFirstName(firstName);
            existingUser.setLastName(lastName);
        }
        
        existingUser.setAuthProvider(authProvider);
        existingUser.setProviderId(providerId);
        
        return userRepository.save(existingUser);
    }
}
