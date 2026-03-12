package com.mediconnect.controller;

import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.util.PasswordUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Optional;

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

        UserRepository repo = new UserRepository();
        Optional<User> existingUser = repo.findByEmail(email.trim().toLowerCase());
        if (existingUser.isPresent()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Cet email est déjà utilisé."));
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

        User newUser = new User();
        newUser.setPrenom(prenom);
        newUser.setNom(nom);
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPassword(PasswordUtil.hashPassword(password));
        newUser.setRole(role != null ? role : Role.PATIENT);
        newUser.setActive(true);

        try {
            repo.save(newUser);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès",
                            "Compte créé ! Vous pouvez maintenant vous connecter."));

            // Allow messages to survive the redirect
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

            return "login?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur système", "Impossible de créer le compte."));
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
