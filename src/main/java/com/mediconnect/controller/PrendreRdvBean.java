package com.mediconnect.controller;

import com.mediconnect.model.Medecin;
import com.mediconnect.model.RendezVous;
import com.mediconnect.model.StatutRDV;
import com.mediconnect.service.RendezVousService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import com.mediconnect.service.SessionManager;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class PrendreRdvBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Medecin> medecins;
    private Long selectedMedecinId;
    private LocalDate selectedDate;
    private List<String> availableSlots;
    private List<RendezVous> myUpcomingAppointments;
    private String selectedMedecinName;
    private List<Integer> disabledDays;

    @Inject
    private SessionManager sessionManager;

    private RendezVousService rendezVousService;

    @PostConstruct
    public void init() {
        rendezVousService = new RendezVousService();
        medecins = rendezVousService.getAllMedecins();
        selectedDate = LocalDate.now();
        availableSlots = new ArrayList<>();
        disabledDays = new ArrayList<>();
        disabledDays.add(0);
        disabledDays.add(6);
        loadMyUpcomingAppointments();
    }

    public void loadMyUpcomingAppointments() {
        if (sessionManager.getCurrentUser() != null) {
            Long currentUserId = sessionManager.getCurrentUser().getId();
            List<RendezVous> allMyRdvs = rendezVousService.getRendezVousByPatient(currentUserId);
            myUpcomingAppointments = allMyRdvs.stream()
                .filter(r -> r.getDateHeure() != null && r.getDateHeure().isAfter(LocalDateTime.now()))
                .filter(r -> r.getStatut() != StatutRDV.ANNULE)
                .sorted(java.util.Comparator.comparing(RendezVous::getDateHeure))
                .collect(java.util.stream.Collectors.toList());
        }
    }

    public void onMedecinOrDateChange() {
        if (selectedMedecinId == null || selectedDate == null) {
            availableSlots = new ArrayList<>();
            return;
        }

        medecins.stream().filter(m -> m.getId().equals(selectedMedecinId)).findFirst().ifPresent(m -> {
            selectedMedecinName = "Dr. " + m.getNom() + " " + m.getPrenom();
            availableSlots = new ArrayList<>();

            // Block bookings on weekends
            java.time.DayOfWeek day = selectedDate.getDayOfWeek();
            if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY) {
                return;
            }
            
            // Get doctor bounds
            String dStart = m.getHeureDebut() != null ? m.getHeureDebut() : "09:00";
            String dEnd = m.getHeureFin() != null ? m.getHeureFin() : "17:00";
            
            LocalTime startTime = LocalTime.parse(dStart.length() > 5 ? dStart.substring(0, 5) : dStart);
            LocalTime endTime = LocalTime.parse(dEnd.length() > 5 ? dEnd.substring(0, 5) : dEnd);

            List<RendezVous> doctorRdvs = rendezVousService.getRendezVousByMedecin(selectedMedecinId);

            LocalTime current = startTime;

            while (current.isBefore(endTime)) {
                LocalDateTime slotDateTime = LocalDateTime.of(selectedDate, current);
                
                // Skip past times
                if (slotDateTime.isBefore(LocalDateTime.now())) {
                    current = current.plusMinutes(30);
                    continue;
                }

                // Check conflict
                boolean isConflict = doctorRdvs.stream().anyMatch(r -> {
                    if (r.getDateHeure() == null || r.getStatut() == StatutRDV.ANNULE) return false;
                    LocalDateTime rStart = r.getDateHeure();
                    LocalDateTime rEnd = rStart.plusMinutes(r.getDureeMinutes() != null ? r.getDureeMinutes() : 30);
                    LocalDateTime slotEnd = slotDateTime.plusMinutes(30);
                    return slotDateTime.isBefore(rEnd) && slotEnd.isAfter(rStart);
                });

                if (!isConflict) {
                    availableSlots.add(String.format("%02d:%02d", current.getHour(), current.getMinute()));
                }

                current = current.plusMinutes(30);
            }
        });
    }

    public void bookAppointment(String timeSlot) {
        if (selectedMedecinId == null || selectedDate == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Veuillez choisir un médecin et une date."));
            return;
        }

        try {
            Long currentPatientId = sessionManager.getCurrentUser().getId();
            LocalTime time = LocalTime.parse(timeSlot);
            LocalDateTime finalDateTime = LocalDateTime.of(selectedDate, time);

            RendezVous newRendezVous = new RendezVous();
            newRendezVous.setDureeMinutes(30);
            newRendezVous.setDateHeure(finalDateTime);

            rendezVousService.planifierRendezVous(newRendezVous, currentPatientId, selectedMedecinId);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Félicitations",
                            "Votre rendez-vous a été confirmé le " + selectedDate + " à " + timeSlot + "."));

            // Refresh UI
            onMedecinOrDateChange();
            loadMyUpcomingAppointments();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                            "Impossible de réserver ce créneau."));
        }
    }

    public void cancelAppointment(Integer rdvId) {
        try {
            rendezVousService.deleteRendezVous(rdvId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Annulé", "Le rendez-vous a été annulé."));
            onMedecinOrDateChange();
            loadMyUpcomingAppointments();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible d'annuler le rendez-vous."));
        }
    }

    // Getters and Setters

    public List<Medecin> getMedecins() { return medecins; }
    public void setMedecins(List<Medecin> medecins) { this.medecins = medecins; }

    public Long getSelectedMedecinId() { return selectedMedecinId; }
    public void setSelectedMedecinId(Long selectedMedecinId) { this.selectedMedecinId = selectedMedecinId; }

    public LocalDate getSelectedDate() { return selectedDate; }
    public void setSelectedDate(LocalDate selectedDate) { this.selectedDate = selectedDate; }

    public List<String> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<String> availableSlots) { this.availableSlots = availableSlots; }

    public List<RendezVous> getMyUpcomingAppointments() { return myUpcomingAppointments; }
    public void setMyUpcomingAppointments(List<RendezVous> myUpcomingAppointments) { this.myUpcomingAppointments = myUpcomingAppointments; }

    public String getSelectedMedecinName() { return selectedMedecinName; }
    public void setSelectedMedecinName(String selectedMedecinName) { this.selectedMedecinName = selectedMedecinName; }

    public List<Integer> getDisabledDays() { return disabledDays; }
}
