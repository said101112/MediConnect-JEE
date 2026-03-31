package com.mediconnect.controller;

import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.service.AuthService;
import com.mediconnect.service.SessionManager;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

@Named("loginBean")
@RequestScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

    @Inject
    private SessionManager sessionManager;

    private final AuthService authService;

    public LoginBean() {
        this.authService = new AuthService();
    }

    public void login() {
        Optional<User> userOpt = authService.authenticate(email, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            sessionManager.setCurrentUser(user);

            String redirectUrl = getDashboardUrl(user.getRole());
            redirect(redirectUrl);
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Échec de connexion",
                            "Email ou mot de passe incorrect."));
        }
    }

    public void logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        sessionManager.logout();
        externalContext.invalidateSession();

        redirect(externalContext.getRequestContextPath() + "/login.xhtml");
    }

    private String getDashboardUrl(Role role) {
        ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
        String contextPath = ext.getRequestContextPath();

        switch (role) {
            case MEDECIN:    return contextPath + "/views/medecin/dashboard.xhtml";
            case SECRETAIRE: return contextPath + "/views/secretaire/dashboard.xhtml";
            case PATIENT:    return contextPath + "/views/patient/dashboard.xhtml";
            default:         return contextPath + "/login.xhtml";
        }
    }

    private void redirect(String url) {
        try {
            ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
            ext.redirect(url);
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", "Impossible de rediriger."));
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}