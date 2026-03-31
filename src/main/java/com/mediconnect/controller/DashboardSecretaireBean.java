package com.mediconnect.controller;

import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.service.RendezVousService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class DashboardSecretaireBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<RendezVous> todayAppointments;
    private long totalToday;
    private long waitingCount;
    private long completedToday;

    private RendezVousService rendezVousService;

    @PostConstruct
    public void init() {
        rendezVousService = new RendezVousService();
        loadDashboardData();
    }

    public void loadDashboardData() {
        todayAppointments = rendezVousService.getTodayRendezVous();
        totalToday = todayAppointments.size();
        waitingCount = todayAppointments.stream()
                .filter(r -> r.getStatut() == StatutRDV.EN_ATTENTE)
                .count();
        completedToday = todayAppointments.stream()
                .filter(r -> r.getStatut() == StatutRDV.TERMINE)
                .count();
    }

    public void marquerArrive(RendezVous rdv) {
        try {
            rdv.setStatut(StatutRDV.EN_ATTENTE);
            rendezVousService.updateRendezVous(rdv);
            loadDashboardData();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", rdv.getPatient().getFullName() + " est en salle d'attente."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Action impossible."));
        }
    }

    public void marquerTermine(RendezVous rdv) {
        try {
            rdv.setStatut(StatutRDV.TERMINE);
            rendezVousService.updateRendezVous(rdv);
            loadDashboardData();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Consultation terminée pour " + rdv.getPatient().getFullName()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Action impossible."));
        }
    }

    public List<RendezVous> getTodayAppointments() {
        return todayAppointments;
    }

    public long getTotalToday() {
        return totalToday;
    }

    public long getWaitingCount() {
        return waitingCount;
    }

    public long getCompletedToday() {
        return completedToday;
    }
}
