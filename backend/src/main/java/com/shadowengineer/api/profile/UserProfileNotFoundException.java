package com.shadowengineer.api.profile;

import java.util.UUID;

public class UserProfileNotFoundException extends RuntimeException {

    public UserProfileNotFoundException(UUID id) {
        super("User profile not found: " + id);
    }
}
