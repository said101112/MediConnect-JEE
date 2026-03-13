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
    private List<Patient> patients;

    private PatientService patientService;

    @PostConstruct
    public void init() {
        patientService = new PatientService();
        newPatient = new Patient();
        loadPatients();
    }

    public void loadPatients() {
        patients = patientService.getAllPatients();
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

    public Patient getNewPatient() {
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }
}