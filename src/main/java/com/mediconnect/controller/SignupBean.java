package com.mediconnect.controller;

import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@RequestScoped
public class SignupBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fullName;
    private String email;
    private String password;
    private Role role;
    private boolean termsAccepted;

    public String signup() {
        if (!termsAccepted) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                            "Vous devez accepter les conditions d'utilisation."));
            return null;
        }

        // Parse Full Name into Nom and Prenom
        String nom = "";
        String prenom = "";
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            prenom = parts[0];
            if (parts.length > 1) {
                nom = parts[1];
            } else {
                nom = parts[0]; // Fallback if only one name is provided
            }
        }

        Patient newUser = new Patient();
        newUser.setPrenom(prenom);
        newUser.setNom(nom);
        newUser.setEmail(email.trim().toLowerCase());

        // Provide dummy data for required database fields
        newUser.setCin("PENDING-" + UUID.randomUUID().toString().substring(0, 8));
        newUser.setDateNaissance(LocalDate.of(2000, 1, 1));

        try {
            com.mediconnect.service.PatientService patientService = new com.mediconnect.service.PatientService();
            patientService.registerPatient(newUser, password);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès",
                            "Compte créé ! Vous pouvez maintenant vous connecter."));

            // Allow messages to survive the redirect
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

            return "login?faces-redirect=true";
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Impossible de créer le compte.";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", msg));
            e.printStackTrace();
            return null;
        }
    }

    // Getters and Setters

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    // Provider for dropdown
    public Role[] getRoles() {
        return Role.values();
    }
}
