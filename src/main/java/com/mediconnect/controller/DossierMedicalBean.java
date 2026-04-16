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
import jakarta.faces.application.FacesMessage;

import com.mediconnect.repository.UserRepository;

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
    private Patient patientEdit; // Copy for editing
    private List<Consultation> consultations = new ArrayList<>();
    private final ConsultationService consultationService = new ConsultationService();
    private final UserRepository userRepository = new UserRepository();
    private final com.mediconnect.repository.PatientRepository patientRepository = new com.mediconnect.repository.PatientRepository();

    @PostConstruct
    public void init() {
        if (sessionManager.isLoggedIn() && sessionManager.isPatient()) {
            Patient sessionPatient = (Patient) sessionManager.getCurrentUser();
            // Fetch updated patient data directly from database to include recent medical record edits
            userRepository.findById(sessionPatient.getId()).ifPresent(user -> {
                if (user instanceof Patient) this.patient = (Patient) user;
            });
            if (this.patient == null) {
                this.patient = sessionPatient; // Fallback
            }
            prepareEdit();
            consultations = consultationService.getOrdonnancesByPatient(patient.getId());
        }
    }

    /** Redirect to the dossier PDF download servlet. */
    public void telechargerDossier() throws IOException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String contextPath = ctx.getExternalContext().getRequestContextPath();
        ctx.getExternalContext().redirect(contextPath + "/download/dossier");
    }

    /** Prepare the editing copy. */
    public void prepareEdit() {
        if (patient != null) {
            // Manual copy to avoid modifying the displayed one until save
            patientEdit = new Patient();
            patientEdit.setId(patient.getId());
            patientEdit.setNom(patient.getNom());
            patientEdit.setPrenom(patient.getPrenom());
            patientEdit.setEmail(patient.getEmail());
            patientEdit.setCin(patient.getCin());
            patientEdit.setTelephone(patient.getTelephone());
            patientEdit.setAdresse(patient.getAdresse());
            patientEdit.setDateNaissance(patient.getDateNaissance());
            patientEdit.setPassword(patient.getPassword());
            patientEdit.setActive(patient.getActive());
            patientEdit.setAllergies(patient.getAllergies());
            patientEdit.setAntecedents(patient.getAntecedents());
            patientEdit.setGroupeSanguin(patient.getGroupeSanguin());
        }
    }

    /** Save the profile changes. */
    public void saveProfile() {
        try {
            // Perform update in repository
            patientRepository.update(patientEdit);
            
            // If successful, update the main objects
            this.patient = patientEdit; 
            
            // Sync with SessionManager so other pages (like dashboard) show updated name/email
            sessionManager.setCurrentUser(patient);
            
            prepareEdit(); // Refresh edit copy for next time
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Votre profil a été mis à jour avec succès."));
                    
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("cin") || msg.contains("unique constraint"))) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Ce CIN est déjà utilisé par un autre patient."));
            } else if (msg != null && msg.contains("email")) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Cet email est déjà utilisé."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de mettre à jour le profil. Vérifiez vos données."));
            }
            e.printStackTrace();
        }
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
    public Patient getPatientEdit() { return patientEdit; }
    public void setPatientEdit(Patient patientEdit) { this.patientEdit = patientEdit; }
    public List<Consultation> getConsultations() { return consultations; }
    public int getConsultationCount() { return consultations != null ? consultations.size() : 0; }
}
