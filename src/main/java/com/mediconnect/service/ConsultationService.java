package com.mediconnect.service;

import com.mediconnect.model.Consultation;
import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.repository.ConsultationRepository;
import com.mediconnect.repository.RendezVousRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class ConsultationService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ConsultationRepository consultationRepository;
    private final RendezVousRepository rendezVousRepository;

    public ConsultationService() {
        this.consultationRepository = new ConsultationRepository();
        this.rendezVousRepository = new RendezVousRepository();
    }

    public Consultation ouvrirConsultation(Integer rdvId) throws Exception {
        Optional<RendezVous> rdvOpt = rendezVousRepository.findById(rdvId);
        if (rdvOpt.isEmpty()) {
            throw new Exception("Rendez-vous introuvable.");
        }
        RendezVous rdv = rdvOpt.get();

        Optional<Consultation> existante = consultationRepository.findByRendezVous(rdvId);
        if (existante.isPresent()) {
            return existante.get();
        }

        Consultation consultation = new Consultation();
        consultation.setRendezVous(rdv);
        consultation.setPatient(rdv.getPatient());
        consultation.setMedecin(rdv.getMedecin());
        consultation.setCloturee(false);
        consultationRepository.save(consultation);

        rendezVousRepository.updateStatut(rdvId, StatutRDV.EN_COURS);
        return consultation;
    }

    public void sauvegarderConsultation(Consultation consultation) throws Exception {
        if (consultation.getCloturee()) {
            throw new Exception("Cette consultation est déjà clôturée.");
        }
        consultationRepository.update(consultation);
    }

    public void cloturerConsultation(Consultation consultation) throws Exception {
        if (consultation.getCloturee()) {
            throw new Exception("Cette consultation est déjà clôturée.");
        }
        consultation.setCloturee(true);
        consultationRepository.update(consultation);
        if (consultation.getRendezVous() != null) {
            rendezVousRepository.updateStatut(
                    consultation.getRendezVous().getId(), StatutRDV.TERMINE);
        }
    }

    public void enregistrerOrdonnancePdf(Integer consultationId, String cheminPdf) throws Exception {
        Optional<Consultation> opt = consultationRepository.findById(consultationId);
        if (opt.isEmpty()) throw new Exception("Consultation introuvable.");
        Consultation c = opt.get();
        c.setCheminPdfOrdonnance(cheminPdf);
        consultationRepository.update(c);
    }

    public List<Consultation> getHistoriqueByMedecin(Long medecinId) {
        return consultationRepository.findByMedecin(medecinId);
    }

    public List<Consultation> getHistoriqueByPatient(Long patientId) {
        return consultationRepository.findByPatient(patientId);
    }

    public Optional<Consultation> findById(Integer id) {
        return consultationRepository.findById(id);
    }

    public long getNombreConsultations(Long medecinId) {
        return consultationRepository.countByMedecin(medecinId);
    }
}