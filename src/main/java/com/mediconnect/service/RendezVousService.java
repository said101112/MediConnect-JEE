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

    public void deleteRendezVous(Integer id) {
        rendezVousRepository.deleteById(id);
    }

    public void updateRendezVous(RendezVous rdv) {
        rendezVousRepository.update(rdv);
    }

    public void cancelRendezVous(Integer id) {
        List<RendezVous> all = rendezVousRepository.findAll();
        all.stream()
           .filter(r -> r.getId().equals(id))
           .findFirst()
           .ifPresent(r -> {
               r.setStatut(StatutRDV.ANNULE);
               rendezVousRepository.update(r);
           });
    }

    public List<RendezVous> getRendezVousByPatient(Long patientId) {
        return rendezVousRepository.findByPatient(patientId);
    }

    public List<RendezVous> getTodayRendezVous() {
        return rendezVousRepository.findAll().stream()
            .filter(r -> r.getDateHeure() != null && r.getDateHeure().toLocalDate().equals(java.time.LocalDate.now()))
            .sorted(java.util.Comparator.comparing(RendezVous::getDateHeure))
            .collect(java.util.stream.Collectors.toList());
    }
}
