package com.mediconnect.controller;

import com.mediconnect.model.Medecin;
import com.mediconnect.service.MedecinService;
import com.mediconnect.service.SessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class SettingsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManager sessionManager;

    private Medecin medecinEdit;
    private MedecinService medecinService;
    private java.util.List<String> selectedJours;

    @PostConstruct
    public void init() {
        medecinService = new MedecinService();
        if (sessionManager.getCurrentUser() instanceof Medecin) {
            // Load a fresh copy to avoid direct session modification until save
            Medecin current = (Medecin) sessionManager.getCurrentUser();
            medecinEdit = medecinService.findById(current.getId()).orElse(current);
            if (medecinEdit.getJoursTravailles() != null && !medecinEdit.getJoursTravailles().isEmpty()) {
                selectedJours = new java.util.ArrayList<>(java.util.Arrays.asList(medecinEdit.getJoursTravailles().split(", ")));
            } else {
                selectedJours = new java.util.ArrayList<>();
            }
        }
    }

    public void saveSettings() {
        try {
            if (selectedJours != null && !selectedJours.isEmpty()) {
                medecinEdit.setJoursTravailles(String.join(", ", selectedJours));
            } else {
                medecinEdit.setJoursTravailles("");
            }
            medecinService.update(medecinEdit);
            // Refresh session user if needed (SessionManager implementation dependent)
            // But usually the session user is the same instance if L2 cache is off or manually managed.
            // For now, just show success.
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Paramètres enregistrés avec succès."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de sauvegarder : " + e.getMessage()));
        }
    }

    public Medecin getMedecinEdit() { return medecinEdit; }
    public void setMedecinEdit(Medecin m) { this.medecinEdit = m; }

    public java.util.List<String> getSelectedJours() { return selectedJours; }
    public void setSelectedJours(java.util.List<String> s) { this.selectedJours = s; }
}
