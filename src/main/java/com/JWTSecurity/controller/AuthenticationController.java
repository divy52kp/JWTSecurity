package com.JWTSecurity.controller;

import com.JWTSecurity.domain.User;
import com.JWTSecurity.dto.GitHubUser;
import com.JWTSecurity.dto.LoginResponse;
import com.JWTSecurity.dto.LoginUserDto;
import com.JWTSecurity.dto.RegisterUserDto;
import com.JWTSecurity.exceptions.InvalidGithubCodeException;
import com.JWTSecurity.exceptions.InvalidGoogleTokenException;
import com.JWTSecurity.service.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {
    private final JWTService jwtService;

    private final AuthenticationService authenticationService;

    private final UserDetailsService userDetailsService;

    private final UserService userService;

    private final GoogleService googleService;

    private final GitHubService gitHubService;

    public AuthenticationController(JWTService jwtService, AuthenticationService authenticationService, UserDetailsService userDetailsService, UserService userService, GoogleService googleService, GitHubService gitHubService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.googleService = googleService;
        this.gitHubService = gitHubService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("token");

        GoogleIdToken.Payload payload = googleService.verifyToken(idToken);
        if (payload == null) {
            throw new InvalidGoogleTokenException("Invalid Google token");
        }
        String email = payload.getEmail();

        userService.createNewUser(payload);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String jwtToken = jwtService.generateToken(userDetails);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());


        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/github")
    public ResponseEntity<LoginResponse> githubLogin(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.isEmpty()) {
            throw new InvalidGithubCodeException("Invalid Github code");
        }

        String accessToken = gitHubService.exchangeCodeForAccessToken(code);

        GitHubUser user = gitHubService.getGitHubUser(accessToken);

        User authenticatedUser = userService.findOrCreateGitHubUser(user);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}
