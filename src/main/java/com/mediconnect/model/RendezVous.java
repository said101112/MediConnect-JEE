package com.mediconnect.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous")
public class RendezVous implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_patient", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medecin", nullable = false)
    private Medecin medecin;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes = 30;

    @Column(name = "motif", length = 255)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 30)
    private StatutRDV statut = StatutRDV.PLANIFIE;

    public RendezVous() {}

    public String getHeureFormatee() {
        if (dateHeure == null) return "";
        return String.format("%02d:%02d", dateHeure.getHour(), dateHeure.getMinute());
    }

    public String getDateFormatee() {
        if (dateHeure == null) return "";
        return String.format("%02d/%02d/%d",
                dateHeure.getDayOfMonth(),
                dateHeure.getMonthValue(),
                dateHeure.getYear());
    }

    public String getDateHeureFormatee() {
        if (dateHeure == null) return "";
        return String.format("%02d/%02d/%d %02d:%02d",
                dateHeure.getDayOfMonth(),
                dateHeure.getMonthValue(),
                dateHeure.getYear(),
                dateHeure.getHour(),
                dateHeure.getMinute());
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Medecin getMedecin() { return medecin; }
    public void setMedecin(Medecin medecin) { this.medecin = medecin; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public StatutRDV getStatut() { return statut; }
    public void setStatut(StatutRDV statut) { this.statut = statut; }
}