package com.JWTSecurity.service;

import com.JWTSecurity.domain.User;
import com.JWTSecurity.dto.LoginUserDto;
import com.JWTSecurity.dto.RegisterUserDto;
import com.JWTSecurity.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDto input) {
        return userRepository.findByEmail(input.getEmail())
                .map(user -> {
                    user.setFullName(input.getFullName());
                    user.setPassword(passwordEncoder.encode(input.getPassword()));
                    return user;
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setFullName(input.getFullName());
                    newUser.setEmail(input.getEmail());
                    newUser.setPassword(passwordEncoder.encode(input.getPassword()));
                    return newUser;
                });
    }

    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }
}
