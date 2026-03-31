package com.mediconnect.service;

import com.mediconnect.model.Facture;
import com.mediconnect.model.StatutPaiement;
import com.mediconnect.repository.FactureRepository;

import java.io.Serializable;
import java.util.List;

public class FactureService implements Serializable {
    private static final long serialVersionUID = 1L;

    private final FactureRepository factureRepository;

    public FactureService() {
        this.factureRepository = new FactureRepository();
    }

    public List<Facture> getAllFactures() {
        return factureRepository.findAll();
    }

    public void encaisserFacture(Facture facture) throws Exception {
        if (facture.getStatutPaiement() == StatutPaiement.PAYE) {
            return;
        }

        facture.setStatutPaiement(StatutPaiement.PAYE);
        factureRepository.update(facture);
    }

    public List<Facture> getFacturesByPatient(Long patientId) {
        return factureRepository.findByPatient(patientId);
    }

    public void updateMontant(Facture facture, java.math.BigDecimal nouveauMontant) throws Exception {
        if (facture.getStatutPaiement() == StatutPaiement.PAYE) {
            throw new Exception("Impossible de modifier une facture déjà payée.");
        }
        facture.setMontantTotal(nouveauMontant);
        factureRepository.update(facture);
    }
}
