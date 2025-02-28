package com.JWTSecurity.dto;

import lombok.Data;

@Data
public class GitHubUser {
    private String login;
    private String name;
    private String email;
    private String avatar_url;
}
