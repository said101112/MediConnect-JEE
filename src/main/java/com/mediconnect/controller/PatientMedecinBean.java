package com.mediconnect.controller;

import com.mediconnect.model.Medecin;
import com.mediconnect.model.Patient;
import com.mediconnect.service.MedecinService;
import com.mediconnect.service.SessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class PatientMedecinBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManager sessionManager;

    private List<Patient> patients;
    private MedecinService medecinService;

    @PostConstruct
    public void init() {
        medecinService = new MedecinService();
        Medecin medecin = (Medecin) sessionManager.getCurrentUser();
        if (medecin != null) {
            patients = medecinService.getPatientsByMedecin(medecin.getId());
        }
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }
}
