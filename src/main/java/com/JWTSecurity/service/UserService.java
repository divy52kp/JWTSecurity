package com.JWTSecurity.service;

import com.JWTSecurity.domain.User;
import com.JWTSecurity.dto.GitHubUser;
import com.JWTSecurity.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public UserDetails createNewUser(GoogleIdToken.Payload payload) {

        Optional<User> existingUser = userRepository.findByEmail(payload.getEmail());

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = new User();
        newUser.setEmail(payload.getEmail());
        newUser.setFullName((String) payload.get("name"));
        newUser.setPassword("");
        return userRepository.save(newUser);

    }

    public User findOrCreateGitHubUser(GitHubUser gitHubUser) {
        Optional<User> existingUser = userRepository.findByEmail(gitHubUser.getLogin());

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = new User();
        newUser.setEmail(gitHubUser.getLogin());
        newUser.setFullName(gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin());
        newUser.setPassword("");

        return userRepository.save(newUser);
    }
}