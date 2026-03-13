package com.mediconnect.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rdv", unique = true)
    private RendezVous rendezVous;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_patient", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medecin", nullable = false)
    private Medecin medecin;

    @Column(name = "date_visite", insertable = false, updatable = false)
    private LocalDateTime dateVisite;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "diagnostic", columnDefinition = "TEXT")
    private String diagnostic;

    @Column(name = "chemin_pdf_ordonnance", length = 255)
    private String cheminPdfOrdonnance;

    @Column(name = "cloturee")
    private Boolean cloturee = false;

    public Consultation() {}

    public String getDateVisiteFormatee() {
        if (dateVisite == null) return "—";
        return String.format("%02d/%02d/%d %02d:%02d",
                dateVisite.getDayOfMonth(),
                dateVisite.getMonthValue(),
                dateVisite.getYear(),
                dateVisite.getHour(),
                dateVisite.getMinute());
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public RendezVous getRendezVous() { return rendezVous; }
    public void setRendezVous(RendezVous rendezVous) { this.rendezVous = rendezVous; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Medecin getMedecin() { return medecin; }
    public void setMedecin(Medecin medecin) { this.medecin = medecin; }

    public LocalDateTime getDateVisite() { return dateVisite; }
    public void setDateVisite(LocalDateTime dateVisite) { this.dateVisite = dateVisite; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public String getCheminPdfOrdonnance() { return cheminPdfOrdonnance; }
    public void setCheminPdfOrdonnance(String cheminPdfOrdonnance) { this.cheminPdfOrdonnance = cheminPdfOrdonnance; }

    public Boolean getCloturee() { return cloturee; }
    public void setCloturee(Boolean cloturee) { this.cloturee = cloturee; }
}