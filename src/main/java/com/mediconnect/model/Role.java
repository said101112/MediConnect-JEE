package com.mediconnect.model;

/**
 * Enum representing user roles in the MediConnect system.
 */
public enum Role {
    MEDECIN("Médecin"),
    SECRETAIRE("Secrétaire"),
    PATIENT("Patient");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
