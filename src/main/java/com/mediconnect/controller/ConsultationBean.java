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

    @Inject
    private ConsultationService consultationService; // ✅ injection correcte

    private Consultation consultationEnCours;
    private List<Consultation> historiqueConsultations;
    private Medecin medecinConnecte;
    private Patient patientSelectionne;

    private String medicamentsOrdonnance;
    private String posologie;

    @PostConstruct
    public void init() {

        try {
            // 🔹 récupérer médecin connecté
            if (sessionManager != null && sessionManager.getCurrentUser() instanceof Medecin) {
                medecinConnecte = (Medecin) sessionManager.getCurrentUser();
            }

            // 🔹 récupérer paramètre rdvId depuis URL
            Map<String, String> params = FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestParameterMap();

            String rdvIdStr = params.get("rdvId");

            if (rdvIdStr != null && !rdvIdStr.isEmpty()) {
                Integer rdvId = Integer.parseInt(rdvIdStr);

                // 🔥 ouvrir ou créer consultation
                consultationEnCours = consultationService.ouvrirConsultation(rdvId);

                if (consultationEnCours != null) {
                    patientSelectionne = consultationEnCours.getPatient();

                    // 🔥 historique du patient (CORRECT)
                    historiqueConsultations =
                            consultationService.getHistoriqueByPatient(
                                    patientSelectionne.getId());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", "Erreur lors de l'initialisation : " + e.getMessage()));
        }
    }

// ================= ACTIONS =================

    public void sauvegarder() {
        try {
            consultationService.sauvegarderConsultation(consultationEnCours);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Consultation sauvegardée."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", e.getMessage()));
        }
    }

    public String cloturerConsultation() {
        try {
            consultationService.cloturerConsultation(consultationEnCours);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Consultation clôturée."));

            return "/views/medecin/planning.xhtml?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", e.getMessage()));
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

            // 🔥 chemin PDF simulé (à remplacer par vrai PDF plus tard)
            String cheminPdf = "ordonnances/ordonnance_" +
                    consultationEnCours.getId() + "_" +
                    System.currentTimeMillis() + ".pdf";

            consultationService.enregistrerOrdonnancePdf(
                    consultationEnCours.getId(), cheminPdf);

            consultationEnCours.setCheminPdfOrdonnance(cheminPdf);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Ordonnance générée."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", e.getMessage()));
        }
    }

// ================= GETTERS =================

    public Consultation getConsultationEnCours() {
        return consultationEnCours;
    }

    public List<Consultation> getHistoriqueConsultations() {
        return historiqueConsultations;
    }

    public Patient getPatientSelectionne() {
        return patientSelectionne;
    }

    public Medecin getMedecinConnecte() {
        return medecinConnecte;
    }

    public String getMedicamentsOrdonnance() {
        return medicamentsOrdonnance;
    }

    public void setMedicamentsOrdonnance(String medicamentsOrdonnance) {
        this.medicamentsOrdonnance = medicamentsOrdonnance;
    }

    public String getPosologie() {
        return posologie;
    }

    public void setPosologie(String posologie) {
        this.posologie = posologie;
    }


}
