package com.mediconnect.controller;

import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.service.RendezVousService;
import com.mediconnect.service.ConsultationService;
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
    private List<RendezVous> filteredAppointments;

    private RendezVousService rendezVousService;
    private ConsultationService consultationService;

    @PostConstruct
    public void init() {
        rendezVousService = new RendezVousService();
        consultationService = new ConsultationService();
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
            // Find the associated consultation if it exists
            var consultationOpt = consultationService.findByRendezVous(rdv.getId());
            if (consultationOpt.isPresent()) {
                consultationService.cloturerConsultation(consultationOpt.get());
            } else {
                // If no consultation is started, just terminate the RDV
                rdv.setStatut(StatutRDV.TERMINE);
                rendezVousService.updateRendezVous(rdv);
            }
            loadDashboardData();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Rendez-vous terminé pour " + rdv.getPatient().getFullName()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Action impossible : " + e.getMessage()));
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
    public void setTodayAppointments(List<RendezVous> todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public List<RendezVous> getFilteredAppointments() {
        return filteredAppointments;
    }

    public void setFilteredAppointments(List<RendezVous> filteredAppointments) {
        this.filteredAppointments = filteredAppointments;
    }
}
