package com.mediconnect.controller;

import com.mediconnect.model.Patient;
import com.mediconnect.model.RendezVous;
import com.mediconnect.model.Consultation;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.service.SessionManager;
import com.mediconnect.service.RendezVousService;
import com.mediconnect.service.ConsultationService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;

@Named
@ViewScoped
public class DashboardPatientBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManager sessionManager;

    private Patient patient;
    private RendezVous nextAppointment;
    private List<Consultation> recentConsultations;
    private long appointmentCount;
    private long prescriptionCount;

    private RendezVousService rdvService;
    private ConsultationService consultationService;

    @PostConstruct
    public void init() {
        rdvService = new RendezVousService();
        consultationService = new ConsultationService();
        
        if (sessionManager.getCurrentUser() instanceof Patient) {
            patient = (Patient) sessionManager.getCurrentUser();
            loadDashboardData();
        }
    }

    private void loadDashboardData() {
        List<RendezVous> allRdvs = rdvService.getRendezVousByPatient(patient.getId());
        appointmentCount = allRdvs.size();
        
        // Find next appointment (upcoming and not cancelled)
        nextAppointment = allRdvs.stream()
                .filter(r -> r.getDateHeure().isAfter(LocalDateTime.now()))
                .filter(r -> r.getStatut() != StatutRDV.ANNULE)
                .min(Comparator.comparing(RendezVous::getDateHeure))
                .orElse(null);

        recentConsultations = consultationService.getHistoriqueByPatient(patient.getId());
        prescriptionCount = recentConsultations.stream()
                .filter(c -> c.getCheminPdfOrdonnance() != null && !c.getCheminPdfOrdonnance().isEmpty())
                .count();
    }

    // Getters
    public Patient getPatient() { return patient; }
    public RendezVous getNextAppointment() { return nextAppointment; }
    public List<Consultation> getRecentConsultations() { return recentConsultations; }
    public long getAppointmentCount() { return appointmentCount; }
    public long getPrescriptionCount() { return prescriptionCount; }
}
