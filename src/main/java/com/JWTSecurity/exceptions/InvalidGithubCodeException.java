package com.JWTSecurity.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidGithubCodeException extends RuntimeException {
    public InvalidGithubCodeException(String message) {
        super(message);
    }
}
