package com.mediconnect.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testGetFullName() {
        Patient patient = new Patient();
        patient.setNom("Dupont");
        patient.setPrenom("Jean");

        assertEquals("Jean Dupont", patient.getFullName());
    }

    @Test
    public void testEmptyNames() {
        Patient patient = new Patient();
        patient.setNom("");
        patient.setPrenom("");

        assertEquals(" ", patient.getFullName());
    }

    @Test
    public void testActiveByDefault() {
        Patient patient = new Patient();

        assertTrue(patient.getActive(), "Un utilisateur devrait être actif par défaut");
    }

    
}
