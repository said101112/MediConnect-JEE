package com.mediconnect.service;

import com.mediconnect.model.Medecin;
import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.repository.MedecinRepository;
import com.mediconnect.repository.RendezVousRepository;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MedecinService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final MedecinRepository medecinRepository;
    private final RendezVousRepository rendezVousRepository;

    public MedecinService() {
        this.medecinRepository = new MedecinRepository();
        this.rendezVousRepository = new RendezVousRepository();
    }

    public Optional<Medecin> findById(Long id) {
        return medecinRepository.findById(id);
    }

    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    // Planning du jour
    public List<RendezVous> getPlanningDuJour(Long medecinId) {
        return rendezVousRepository.findByMedecinAndDate(medecinId, LocalDate.now());
    }

    // Planning d'une semaine
    public List<RendezVous> getPlanningDeLaSemaine(Long medecinId, LocalDate debutSemaine) {
        return rendezVousRepository.findByMedecinAndWeek(medecinId, debutSemaine);
    }

    // Nombre de RDV du jour
    public long getNombreRdvDuJour(Long medecinId) {
        return rendezVousRepository.countTodayByMedecin(medecinId);
    }

    // Faire passer un RDV "en cours"
    public void demarrerConsultation(Integer rdvId) {
        rendezVousRepository.updateStatut(rdvId, StatutRDV.EN_COURS);
    }

    // Marquer un RDV "terminé"
    public void terminerRdv(Integer rdvId) {
        rendezVousRepository.updateStatut(rdvId, StatutRDV.TERMINE);
    }

    // Annuler un RDV
    public void annulerRdv(Integer rdvId) {
        rendezVousRepository.updateStatut(rdvId, StatutRDV.ANNULE);
    }

    public void update(Medecin medecin) {
        medecinRepository.update(medecin);
    }
}