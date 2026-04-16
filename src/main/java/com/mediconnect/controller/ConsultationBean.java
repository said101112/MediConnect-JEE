package com.mediconnect.controller;

import com.mediconnect.model.Consultation;
import com.mediconnect.model.Medecin;
import com.mediconnect.model.Patient;
import com.mediconnect.service.ConsultationService;
import com.mediconnect.service.SessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class ConsultationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManager sessionManager;

    private Consultation consultationEnCours;
    private List<Consultation> historiqueConsultations;
    private Medecin medecinConnecte;
    private Patient patientSelectionne;
    private String medicamentsOrdonnance;
    private String posologie;

    private ConsultationService consultationService;

    @PostConstruct
    public void init() {
        consultationService = new ConsultationService();

        if (sessionManager.getCurrentUser() instanceof Medecin) {
            medecinConnecte = (Medecin) sessionManager.getCurrentUser();
        }

        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();
        String rdvIdStr = params.get("rdvId");

        if (rdvIdStr != null && !rdvIdStr.isEmpty()) {
            try {
                Integer rdvId = Integer.parseInt(rdvIdStr);
                consultationEnCours = consultationService.ouvrirConsultation(rdvId);
                patientSelectionne = consultationEnCours.getPatient();
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erreur", "Impossible d'ouvrir la consultation : " + e.getMessage()));
            }
        }

        if (medecinConnecte != null) {
            historiqueConsultations = consultationService.getHistoriqueByMedecin(
                    medecinConnecte.getId());
        }
    }

    public void sauvegarder() {
        try {
            consultationService.sauvegarderConsultation(consultationEnCours);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Consultation sauvegardée."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage()));
        }
    }

    public void sauvegarderDossierMedical() {
        try {
            consultationService.updatePatient(patientSelectionne);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Dossier médical mis à jour avec succès."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Mise à jour du dossier impossible."));
        }
    }

    public String cloturerConsultation() {
        try {
            consultationService.cloturerConsultation(consultationEnCours);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Consultation clôturée avec succès."));
            return "/views/medecin/planning.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage()));
            return null;
        }
    }

    public void genererOrdonnance() {
        try {
            if (medicamentsOrdonnance == null || medicamentsOrdonnance.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Attention", "Veuillez saisir les médicaments."));
                return;
            }
            String cheminPdf = "ordonnances/ordonnance_" +
                    consultationEnCours.getId() + "_" +
                    System.currentTimeMillis() + ".pdf";

            consultationService.enregistrerOrdonnancePdf(
                    consultationEnCours.getId(), cheminPdf);
            consultationEnCours.setCheminPdfOrdonnance(cheminPdf);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Ordonnance générée : " + cheminPdf));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", "Impossible de générer l'ordonnance : " + e.getMessage()));
        }
    }

    // ===== Getters & Setters =====
    public Consultation getConsultationEnCours() { return consultationEnCours; }
    public void setConsultationEnCours(Consultation c) { this.consultationEnCours = c; }

    public List<Consultation> getHistoriqueConsultations() { return historiqueConsultations; }

    public Patient getPatientSelectionne() { return patientSelectionne; }
    public void setPatientSelectionne(Patient p) { this.patientSelectionne = p; }

    public String getMedicamentsOrdonnance() { return medicamentsOrdonnance; }
    public void setMedicamentsOrdonnance(String m) { this.medicamentsOrdonnance = m; }

    public String getPosologie() { return posologie; }
    public void setPosologie(String p) { this.posologie = p; }

    public Medecin getMedecinConnecte() { return medecinConnecte; }
}