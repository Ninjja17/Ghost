package com.shadowengineer.api.github;

import org.springframework.http.HttpStatus;

public class GitHubSourceException extends RuntimeException {

    private final HttpStatus status;

    public GitHubSourceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
