package com.mediconnect.controller;

import com.mediconnect.model.Consultation;
import com.mediconnect.model.Patient;
import com.mediconnect.service.ConsultationService;
import com.mediconnect.service.SessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for the patient's Dossier Médical page.
 */
@Named
@ViewScoped
public class DossierMedicalBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManager sessionManager;

    private Patient patient;
    private List<Consultation> consultations = new ArrayList<>();
    private final ConsultationService consultationService = new ConsultationService();

    @PostConstruct
    public void init() {
        if (sessionManager.isLoggedIn() && sessionManager.isPatient()) {
            patient = (Patient) sessionManager.getCurrentUser();
            consultations = consultationService.getOrdonnancesByPatient(patient.getId());
        }
    }

    /** Redirect to the dossier PDF download servlet. */
    public void telechargerDossier() throws IOException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String contextPath = ctx.getExternalContext().getRequestContextPath();
        ctx.getExternalContext().redirect(contextPath + "/download/dossier");
    }

    /** Format a LocalDate for display. */
    public String formatDate(java.time.LocalDate date) {
        if (date == null) return "—";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Format a LocalDateTime for display. */
    public String formatDateTime(java.time.LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm"));
    }

    /** Helper to get first letter of prenom for avatar. */
    public String getInitiale(String text) {
        if (text == null || text.isBlank()) return "?";
        return text.substring(0, 1).toUpperCase();
    }

    /** Compute patient age from dateNaissance. */
    public int getAge() {
        if (patient == null || patient.getDateNaissance() == null) return 0;
        return Period.between(patient.getDateNaissance(), LocalDate.now()).getYears();
    }

    /** CSS color class for blood type badge. */
    public String getGroupeSanguinColor() {
        if (patient == null || patient.getGroupeSanguin() == null) return "#6b7280";
        return switch (patient.getGroupeSanguin()) {
            case "O+", "O-" -> "#059669";
            case "A+", "A-" -> "#2563eb";
            case "B+", "B-" -> "#7c3aed";
            case "AB+", "AB-" -> "#dc2626";
            default -> "#6b7280";
        };
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public Patient getPatient() { return patient; }
    public List<Consultation> getConsultations() { return consultations; }
    public int getConsultationCount() { return consultations != null ? consultations.size() : 0; }
}
