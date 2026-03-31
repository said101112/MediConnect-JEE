package com.mediconnect.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "medecins")
@DiscriminatorValue("MEDECIN")
public class Medecin extends User {

    private static final long serialVersionUID = 1L;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "specialite", nullable = false, length = 100)
    private String specialite;

    @Column(name = "matricule", unique = true, nullable = false, length = 50)
    private String matricule;

    @Column(name = "heure_debut", length = 10)
    private String heureDebut = "08:30";

    @Column(name = "heure_fin", length = 10)
    private String heureFin = "17:30";

    @Column(name = "jours_travailles", length = 100)
    private String joursTravailles = "Lundi, Mardi, Mercredi, Jeudi, Vendredi";

    // Default Constructor
    public Medecin() {
        super();
    }

    // Getters and Setters

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String getPrenom() {
        return prenom;
    }

    @Override
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Override
    public String getTelephone() {
        return telephone;
    }

    @Override
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String h) { this.heureDebut = h; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String h) { this.heureFin = h; }

    public String getJoursTravailles() { return joursTravailles; }
    public void setJoursTravailles(String j) { this.joursTravailles = j; }
}
