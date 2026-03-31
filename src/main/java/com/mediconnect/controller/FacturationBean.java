package com.mediconnect.controller;

import com.mediconnect.model.Facture;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class FacturationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Facture> factures;
    private Facture selectedFacture;
    private com.mediconnect.service.FactureService factureService;

    @PostConstruct
    public void init() {
        factureService = new com.mediconnect.service.FactureService();
        loadFactures();
    }

    public void loadFactures() {
        factures = factureService.getAllFactures();
    }

    public void encaisser(Facture facture) {
        try {
            factureService.encaisserFacture(facture);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Facture encaissée."));

            loadFactures(); // re-load just to ensure UI reflects real DB state
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                            "Une erreur s'est produite lors de l'encaissement: " + e.getMessage()));
        }
    }

    public void modifierMontant(Facture facture) {
        try {
            factureService.updateMontant(facture, facture.getMontantTotal());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Montant mis à jour."));
            loadFactures();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", e.getMessage()));
        }
    }

    public List<Facture> getFactures() {
        return factures;
    }

    public void setFactures(List<Facture> factures) {
        this.factures = factures;
    }

    public Facture getSelectedFacture() {
        return selectedFacture;
    }

    public void setSelectedFacture(Facture selectedFacture) {
        this.selectedFacture = selectedFacture;
    }
}
