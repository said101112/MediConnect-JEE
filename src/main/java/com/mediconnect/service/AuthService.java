package com.mediconnect.service;

import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.util.PasswordUtil;

import java.io.Serializable;
import java.util.Optional;

/**
 * Authentication service for verifying user credentials.
 */
public class AuthService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UserRepository userRepository;
    
    public AuthService() {
        this(new UserRepository());
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticate a user by email and password.
     *
     * @param email    the user's email
     * @param password the plain text password
     * @return the authenticated User, or empty if credentials are invalid
     */
    public Optional<User> authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if user is active
            if (user.getActive() == null || !user.getActive()) {
                return Optional.empty();
            }

            // Verify password
            if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }
}
