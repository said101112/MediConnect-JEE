package com.mediconnect.service;

import com.mediconnect.model.Role;
import com.mediconnect.model.User;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Session-scoped managed bean that stores the currently authenticated user.
 * Provides methods to check authentication state and user role.
 */
@Named("sessionManager")
@SessionScoped
public class SessionManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private User currentUser;

    /**
     * Check if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get the current authenticated user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Set the current authenticated user (called after successful login).
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the role of the current user.
     */
    public Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * Check if the current user has a specific role.
     */
    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    /**
     * Check if the current user is a MEDECIN.
     */
    public boolean isMedecin() {
        return hasRole(Role.MEDECIN);
    }

    /**
     * Check if the current user is a SECRETAIRE.
     */
    public boolean isSecretaire() {
        return hasRole(Role.SECRETAIRE);
    }

    /**
     * Check if the current user is a PATIENT.
     */
    public boolean isPatient() {
        return hasRole(Role.PATIENT);
    }

    /**
     * Get the full name of the current user.
     */
    public String getFullName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }

    /**
     * Invalidate the session (logout).
     */
    public void logout() {
        this.currentUser = null;
    }
}
