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
                            "Une erreur s'est produite lors de l'encaissement."));
            e.printStackTrace();
        }
    }

    public List<Facture> getFactures() {
        return factures;
    }

    public void setFactures(List<Facture> factures) {
        this.factures = factures;
    }
}
