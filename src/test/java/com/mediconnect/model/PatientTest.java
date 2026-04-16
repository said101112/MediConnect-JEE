package com.mediconnect.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class PatientTest {

    @Test
    public void testGetDateNaissanceFormatee_NullDate_ShouldReturnEmptyString() {
        Patient patient = new Patient();
        patient.setDateNaissance(null);
        
        assertEquals("", patient.getDateNaissanceFormatee());
    }

    @Test
    public void testGetDateNaissanceFormatee_ValidDate_ShouldReturnFormattedString() {
        Patient patient = new Patient();
        patient.setDateNaissance(LocalDate.of(1990, 5, 15));
        
        // Expected format: %02d/%02d/%d -> 15/05/1990
        assertEquals("15/05/1990", patient.getDateNaissanceFormatee());
    }

    @Test
    public void testGetFullName_ShouldReturnConcatenatedNames() {
        Patient patient = new Patient();
        patient.setPrenom("Jean");
        patient.setNom("Dupont");
        
        assertEquals("Jean Dupont", patient.getFullName(), "Full name should combine prenom and nom.");
    }
}
