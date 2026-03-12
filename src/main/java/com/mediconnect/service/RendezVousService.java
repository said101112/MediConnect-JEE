package com.mediconnect.service;

import com.mediconnect.model.Medecin;
import com.mediconnect.model.Patient;
import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.repository.MedecinRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.RendezVousRepository;

import java.io.Serializable;
import java.util.List;

public class RendezVousService implements Serializable {
    private static final long serialVersionUID = 1L;

    private final RendezVousRepository rendezVousRepository;
    private final MedecinRepository medecinRepository;
    private final PatientRepository patientRepository;

    public RendezVousService() {
        this.rendezVousRepository = new RendezVousRepository();
        this.medecinRepository = new MedecinRepository();
        this.patientRepository = new PatientRepository();
    }

    public List<RendezVous> getAllRendezVous() {
        return rendezVousRepository.findAll();
    }

    public List<RendezVous> getRendezVousByMedecin(Long medecinId) {
        return rendezVousRepository.findByMedecin(medecinId);
    }

    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public void planifierRendezVous(RendezVous rdv, Long patientId, Long medecinId) throws Exception {
        if (patientId == null) {
            throw new Exception("Veuillez sélectionner un patient.");
        }
        if (medecinId == null) {
            throw new Exception("Veuillez sélectionner un médecin.");
        }

        Patient p = new Patient();
        p.setId(patientId);
        rdv.setPatient(p);

        Medecin m = new Medecin();
        m.setId(medecinId);
        rdv.setMedecin(m);

        if (rdv.getStatut() == null) {
            rdv.setStatut(StatutRDV.PLANIFIE);
        }

        rendezVousRepository.save(rdv);
    }
}
