package com.mediconnect.controller;

import com.mediconnect.model.Medecin;
import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.service.MedecinService;
import com.mediconnect.service.SessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Named
@ViewScoped
public class MedecinBean implements Serializable {


    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private MedecinService medecinService; // ✅ injection CDI correcte

    private Medecin medecinConnecte;
    private List<RendezVous> rdvDuJour;
    private List<RendezVous> rdvDeLaSemaine;
    private long nombreRdvDuJour;
    private long nombreConsultations;
    private LocalDate semaineSelectionnee;
    private LocalDate dateSelectionnee;

    @PostConstruct
    public void init() {
        semaineSelectionnee = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        dateSelectionnee = LocalDate.now();

        try {
            if (sessionManager != null && sessionManager.getCurrentUser() instanceof Medecin) {
                medecinConnecte = (Medecin) sessionManager.getCurrentUser();
                chargerDonnees();
            } else {
                System.out.println("⚠️ Aucun médecin connecté.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chargerDonnees() {
        try {
            rdvDuJour = medecinService.getPlanningDuJour(medecinConnecte.getId());
            rdvDeLaSemaine = medecinService.getPlanningDeLaSemaine(
                    medecinConnecte.getId(), semaineSelectionnee);
            nombreRdvDuJour = medecinService.getNombreRdvDuJour(medecinConnecte.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void semainePrecedente() {
        semaineSelectionnee = semaineSelectionnee.minusWeeks(1);
        chargerDonnees();
    }

    public void semaineSuivante() {
        semaineSelectionnee = semaineSelectionnee.plusWeeks(1);
        chargerDonnees();
    }

    public void demarrerConsultation(Integer rdvId) {
        try {
            medecinService.demarrerConsultation(rdvId);
            chargerDonnees();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Consultation démarrée."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage()));
        }
    }

    public void annulerRdv(Integer rdvId) {
        try {
            medecinService.annulerRdv(rdvId);
            chargerDonnees();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Info", "Rendez-vous annulé."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage()));
        }
    }

    public String ouvrirConsultation(Integer rdvId) {
        return "/medecin/consultation.xhtml?rdvId=" + rdvId + "&faces-redirect=true";
    }

    public String getCouleurStatut(StatutRDV statut) {
        if (statut == null) return "gray";
        if (statut == StatutRDV.PLANIFIE) return "blue";
        if (statut == StatutRDV.EN_COURS) return "orange";
        if (statut == StatutRDV.TERMINE)  return "green";
        if (statut == StatutRDV.ANNULE)   return "red";
        return "gray";
    }

    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getSemaineLabel() {
        LocalDate fin = semaineSelectionnee.plusDays(6);
        return formatDate(semaineSelectionnee) + " – " + formatDate(fin);
    }

// ===== Getters & Setters =====

    public Medecin getMedecinConnecte() { return medecinConnecte; }
    public void setMedecinConnecte(Medecin m) { this.medecinConnecte = m; }

    public List<RendezVous> getRdvDuJour() { return rdvDuJour; }
    public List<RendezVous> getRdvDeLaSemaine() { return rdvDeLaSemaine; }

    public long getNombreRdvDuJour() { return nombreRdvDuJour; }
    public long getNombreConsultations() { return nombreConsultations; }

    public LocalDate getSemaineSelectionnee() { return semaineSelectionnee; }
    public void setSemaineSelectionnee(LocalDate s) { this.semaineSelectionnee = s; }

    public LocalDate getDateSelectionnee() { return dateSelectionnee; }
    public void setDateSelectionnee(LocalDate d) { this.dateSelectionnee = d; }


}
