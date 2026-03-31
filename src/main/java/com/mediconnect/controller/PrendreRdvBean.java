package com.mediconnect.controller;

import com.mediconnect.model.Medecin;
import com.mediconnect.model.RendezVous;
import com.mediconnect.service.RendezVousService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import com.mediconnect.service.SessionManager;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Named
@ViewScoped
public class PrendreRdvBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private ScheduleModel eventModel;
    private RendezVous newRendezVous;
    private List<Medecin> medecins;
    private Long selectedMedecinId;

    @Inject
    private SessionManager sessionManager;

    private RendezVousService rendezVousService;

    // Fields for dialog
    private LocalDate selectedDate;
    private String selectedTimeSlot;
    private String selectedMedecinName;
    private org.primefaces.model.ScheduleEvent<?> selectedEvent;

    @PostConstruct
    public void init() {
        rendezVousService = new RendezVousService();
        eventModel = new DefaultScheduleModel();
        newRendezVous = new RendezVous();
        medecins = rendezVousService.getAllMedecins();

        loadEvents();
    }

    public void loadEvents() {
        eventModel.clear();

        if (selectedMedecinId == null) {
            return;
        }

        List<RendezVous> rdvs = rendezVousService.getRendezVousByMedecin(selectedMedecinId);
        Long currentUserId = sessionManager.getCurrentUser().getId();

        for (RendezVous rdv : rdvs) {
            LocalDateTime start = rdv.getDateHeure();
            if (start != null) {
                LocalDateTime end = start.plusMinutes(rdv.getDureeMinutes() != null ? rdv.getDureeMinutes() : 30);

                boolean isMine = rdv.getPatient() != null && rdv.getPatient().getId().equals(currentUserId);

                String title = isMine ? "Mon RDV" : "Indisponible";
                String styleClass = isMine ? "my-rdv-slot" : "occupied-slot";

                DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                        .id(String.valueOf(rdv.getId())) // Explicitly set ID to fetch later
                        .title(title)
                        .startDate(start)
                        .endDate(end)
                        .styleClass(styleClass)
                        .editable(false)
                        .overlapAllowed(false)
                        .build();

                eventModel.addEvent(event);
            }
        }
    }

    public void onMedecinChange() {
        // Cache the doctor name for the dialog
        medecins.stream().filter(m -> m.getId().equals(selectedMedecinId)).findFirst().ifPresent(m -> {
            selectedMedecinName = "Dr. " + m.getNom() + " " + m.getPrenom();
        });
        loadEvents();
    }

    // Handles clicking an empty slot on the calendar
    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        if (selectedMedecinId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Oups",
                            "Veuillez d'abord choisir un médecin pour voir ses disponibilités."));
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("showDialog", false);
            return;
        }

        LocalDateTime clickedDateTime = selectEvent.getObject();
        LocalDate clickedDate = clickedDateTime.toLocalDate();
        LocalTime clickedTime = clickedDateTime.toLocalTime();

        // Prevent past clicks
        if (clickedDateTime.isBefore(LocalDateTime.now())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention", "Ce créneau est déjà passé."));
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("showDialog", false);
            return;
        }

        // Prevent booking outside of 9h-18h or lunch break
        if (clickedTime.isBefore(LocalTime.of(9, 0)) ||
                (clickedTime.isAfter(LocalTime.of(11, 30)) && clickedTime.isBefore(LocalTime.of(14, 0))) ||
                clickedTime.isAfter(LocalTime.of(17, 30))) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Horaires",
                            "Le médecin ne consulte pas sur ces horaires."));
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("showDialog", false);
            return;
        }

        // Preparation for Dialog
        newRendezVous = new RendezVous();
        newRendezVous.setDureeMinutes(30);
        this.selectedDate = clickedDate;
        this.selectedTimeSlot = String.format("%02d:%02d", clickedTime.getHour(), clickedTime.getMinute());

        org.primefaces.PrimeFaces.current().ajax().addCallbackParam("showDialog", true);
    }

    // Handles clicking an existing event
    public void onEventSelect(SelectEvent<org.primefaces.model.ScheduleEvent<?>> selectEvent) {
        selectedEvent = selectEvent.getObject();

        if ("my-rdv-slot".equals(selectedEvent.getStyleClass())) {
            // It's the patient's own RDV
            this.selectedDate = selectedEvent.getStartDate().toLocalDate();
            this.selectedTimeSlot = String.format("%02d:%02d",
                    selectedEvent.getStartDate().getHour(),
                    selectedEvent.getStartDate().getMinute());

            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("isMine", true);
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Indisponible",
                            "Ce créneau est déjà réservé par un autre patient."));
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("isMine", false);
        }
    }

    public void deleteSelectedEvent() {
        if (selectedEvent != null && selectedEvent.getId() != null) {
            try {
                Integer rdvId = Integer.parseInt(selectedEvent.getId());
                rendezVousService.deleteRendezVous(rdvId);

                loadEvents(); // Refresh UI

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Annulé",
                                "Votre rendez-vous a été annulé avec succès."));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                                "Impossible d'annuler le rendez-vous."));
            }
        }
    }

    public void addEvent() {
        if (selectedMedecinId == null || selectedDate == null || selectedTimeSlot == null) {
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("error", true);
            return;
        }

        try {
            Long currentPatientId = sessionManager.getCurrentUser().getId();

            LocalTime time = LocalTime.parse(selectedTimeSlot);
            LocalDateTime finalDateTime = LocalDateTime.of(selectedDate, time);

            // Check for conflict
            List<RendezVous> rdvs = rendezVousService.getRendezVousByMedecin(selectedMedecinId);
            boolean conflict = rdvs.stream().anyMatch(r -> {
                LocalDateTime start = r.getDateHeure();
                if (start == null)
                    return false;
                LocalDateTime end = start.plusMinutes(r.getDureeMinutes() != null ? r.getDureeMinutes() : 30);
                LocalDateTime newEnd = finalDateTime.plusMinutes(30); // Default duration 30m
                return finalDateTime.isBefore(end) && newEnd.isAfter(start);
            });

            if (conflict) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Créneau Indisponible",
                                "Un autre patient vient tout juste de réserver ce même créneau."));
                org.primefaces.PrimeFaces.current().ajax().addCallbackParam("error", true);
                return;
            }

            newRendezVous.setDateHeure(finalDateTime);

            rendezVousService.planifierRendezVous(newRendezVous, currentPatientId, selectedMedecinId);

            loadEvents(); // Refresh schedule

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Félicitations",
                            "Votre rendez-vous a été confirmé le " + selectedDate + " à " + selectedTimeSlot + "."));

            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("error", false);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                            "Le créneau n'est plus disponible ou une erreur serveur est survenue."));
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("error", true);
        }
    }

    // Getters and Setters

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public void setEventModel(ScheduleModel eventModel) {
        this.eventModel = eventModel;
    }

    public RendezVous getNewRendezVous() {
        return newRendezVous;
    }

    public void setNewRendezVous(RendezVous newRendezVous) {
        this.newRendezVous = newRendezVous;
    }

    public List<Medecin> getMedecins() {
        return medecins;
    }

    public void setMedecins(List<Medecin> medecins) {
        this.medecins = medecins;
    }

    public Long getSelectedMedecinId() {
        return selectedMedecinId;
    }

    public void setSelectedMedecinId(Long selectedMedecinId) {
        this.selectedMedecinId = selectedMedecinId;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public String getSelectedTimeSlot() {
        return selectedTimeSlot;
    }

    public void setSelectedTimeSlot(String selectedTimeSlot) {
        this.selectedTimeSlot = selectedTimeSlot;
    }

    public String getSelectedMedecinName() {
        return selectedMedecinName;
    }

    public void setSelectedMedecinName(String selectedMedecinName) {
        this.selectedMedecinName = selectedMedecinName;
    }
}
