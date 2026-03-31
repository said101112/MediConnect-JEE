package com.mediconnect.controller;

import com.mediconnect.model.Patient;
import com.mediconnect.service.PatientService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class PatientBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Patient newPatient;
    private Patient selectedPatient;
    private List<Patient> patients;
    private List<com.mediconnect.model.RendezVous> selectedPatientRDVs;
    private List<com.mediconnect.model.Facture> selectedPatientFactures;

    private PatientService patientService;
    private com.mediconnect.service.RendezVousService rendezVousService;
    private com.mediconnect.service.FactureService factureService;

    @PostConstruct
    public void init() {
        patientService = new PatientService();
        rendezVousService = new com.mediconnect.service.RendezVousService();
        factureService = new com.mediconnect.service.FactureService();
        newPatient = new Patient();
        selectedPatient = new Patient();
        loadPatients();
    }

    public void loadPatients() {
        patients = patientService.getAllPatients();
    }

    public void prepareEdit(Patient p) {
        this.selectedPatient = p;
    }

    public void prepareDossier(Patient p) {
        this.selectedPatient = p;
        this.selectedPatientRDVs = rendezVousService.getRendezVousByPatient(p.getId());
        this.selectedPatientFactures = factureService.getFacturesByPatient(p.getId());
    }

    public void prepareNew() {
        this.newPatient = new Patient();
    }

    public void savePatient() {
        try {
            patientService.registerPatient(newPatient, "patient123");

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Patient ajouté avec succès."));

            loadPatients(); // Rafraichir la table
            newPatient = new Patient(); // Reset le form
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Impossible de créer le patient.";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur Système", msg));
            e.printStackTrace();
        }
    }

    public void updatePatient() {
        try {
            patientService.updatePatient(selectedPatient);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Informations patient mises à jour."));
            loadPatients();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Action impossible."));
        }
    }

    public Patient getNewPatient() {
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    public void setSelectedPatient(Patient selectedPatient) {
        this.selectedPatient = selectedPatient;
    }

    public List<com.mediconnect.model.RendezVous> getSelectedPatientRDVs() {
        return selectedPatientRDVs;
    }

    public List<com.mediconnect.model.Facture> getSelectedPatientFactures() {
        return selectedPatientFactures;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }
}
