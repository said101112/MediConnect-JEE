package com.mediconnect.controller;

import com.mediconnect.model.Medecin;
import com.mediconnect.model.Patient;
import com.mediconnect.model.RendezVous;
import com.mediconnect.service.RendezVousService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class AgendaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private ScheduleModel eventModel;
    private RendezVous newRendezVous;
    private List<Medecin> medecins;
    private List<Patient> patients;
    private Long selectedMedecinId;

    private RendezVousService rendezVousService;

    // Fields for free hours feature
    private List<String> availableTimeSlots;
    private String selectedTimeSlot;
    private LocalDate selectedDate;

    @PostConstruct
    public void init() {
        rendezVousService = new RendezVousService();
        eventModel = new DefaultScheduleModel();
        newRendezVous = new RendezVous();
        newRendezVous.setPatient(new Patient());
        availableTimeSlots = new ArrayList<>();

        medecins = rendezVousService.getAllMedecins();
        patients = rendezVousService.getAllPatients();
        loadEvents();
    }

    public void loadEvents() {
        eventModel.clear();
        List<RendezVous> rdvs;
        if (selectedMedecinId != null) {
            rdvs = rendezVousService.getRendezVousByMedecin(selectedMedecinId);
        } else {
            rdvs = rendezVousService.getAllRendezVous();
        }

        for (RendezVous rdv : rdvs) {
            String title = "";
            if (rdv.getPatient() != null && rdv.getPatient().getFullName() != null) {
                title += rdv.getPatient().getFullName();
            } else {
                title += "Patient Inconnu";
            }
            if (rdv.getMotif() != null && !rdv.getMotif().isEmpty()) {
                title += " - " + rdv.getMotif();
            }

            LocalDateTime start = rdv.getDateHeure();
            if (start != null) {
                LocalDateTime end = start.plusMinutes(rdv.getDureeMinutes() != null ? rdv.getDureeMinutes() : 30);
                
                String styleClass = "occupied-slot";
                if (rdv.getStatut() == com.mediconnect.model.StatutRDV.ANNULE) styleClass = "cancelled-slot";
                else if (rdv.getStatut() == com.mediconnect.model.StatutRDV.EN_ATTENTE) styleClass = "waiting-slot";

                DefaultScheduleEvent<?> event = DefaultScheduleEvent.builder()
                        .title(title)
                        .startDate(start)
                        .endDate(end)
                        .data(rdv)
                        .styleClass(styleClass)
                        .build();
                eventModel.addEvent(event);
            }
        }
    }

    public void onEventSelect(SelectEvent<org.primefaces.model.ScheduleEvent<?>> selectEvent) {
        org.primefaces.model.ScheduleEvent<?> event = selectEvent.getObject();
        newRendezVous = (RendezVous) event.getData();
        this.selectedDate = newRendezVous.getDateHeure().toLocalDate();
        this.selectedTimeSlot = String.format("%02d:%02d", 
                newRendezVous.getDateHeure().getHour(), 
                newRendezVous.getDateHeure().getMinute());
        
        // Refresh patients and physicians to ensure latest data is available
        patients = rendezVousService.getAllPatients();
        medecins = rendezVousService.getAllMedecins();

        updateAvailableTimeSlots();
        // Add current slot to available if it's an edit
        if (!availableTimeSlots.contains(selectedTimeSlot)) {
            availableTimeSlots.add(0, selectedTimeSlot);
        }
    }

    public void saveEvent() {
        if (newRendezVous.getId() == null) {
            addEvent();
        } else {
            updateEvent();
        }
    }

    public void updateEvent() {
        try {
            LocalTime time = LocalTime.parse(selectedTimeSlot);
            newRendezVous.setDateHeure(LocalDateTime.of(selectedDate, time));
            rendezVousService.updateRendezVous(newRendezVous);
            loadEvents();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Rendez-vous mis à jour."));
        } catch (Exception e) {
             FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Modification impossible."));
        }
    }

    public void cancelEvent() {
        try {
            rendezVousService.cancelRendezVous(newRendezVous.getId());
            loadEvents();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Rendez-vous annulé."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Annulation impossible."));
        }
    }

    public void onMedecinChange() {
        loadEvents();
        updateAvailableTimeSlots();
    }

    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        if (selectedMedecinId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention",
                            "Veuillez d'abord sélectionner un médecin."));
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("showDialog", false);
            return;
        }

        LocalDate clickedDate = selectEvent.getObject().toLocalDate();
        if (clickedDate.isBefore(LocalDate.now())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention",
                            "Vous ne pouvez pas planifier de rendez-vous dans le passé."));
            org.primefaces.PrimeFaces.current().ajax().addCallbackParam("showDialog", false);
            return;
        }

        newRendezVous = new RendezVous();
        newRendezVous.setPatient(new Patient());

        // Setup default duration
        newRendezVous.setDureeMinutes(30);

        // Capture selected date
        this.selectedDate = clickedDate;
        this.selectedTimeSlot = null; // Reset selection

        // Refresh patients and physicians to ensure latest data is available
        patients = rendezVousService.getAllPatients();
        medecins = rendezVousService.getAllMedecins();

        // Calculate free hours
        updateAvailableTimeSlots();

        org.primefaces.PrimeFaces.current().ajax().addCallbackParam("showDialog", true);
    }

    public void onManualDateSelect() {
        this.selectedTimeSlot = null;
        updateAvailableTimeSlots();
    }

    public void updateAvailableTimeSlots() {
        availableTimeSlots = new ArrayList<>();
        if (selectedMedecinId == null || selectedDate == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        if (selectedDate.isBefore(today)) {
            return; // No slots available for past dates
        }

        List<LocalTime> allSlots = new ArrayList<>();
        // Morning
        for (int h = 9; h < 12; h++) {
            allSlots.add(LocalTime.of(h, 0));
            allSlots.add(LocalTime.of(h, 30));
        }
        // Afternoon
        for (int h = 14; h < 18; h++) {
            allSlots.add(LocalTime.of(h, 0));
            allSlots.add(LocalTime.of(h, 30));
        }

        List<RendezVous> rdvs = rendezVousService.getRendezVousByMedecin(selectedMedecinId);
        List<RendezVous> rdvsForDay = rdvs.stream()
                .filter(r -> r.getDateHeure() != null && r.getDateHeure().toLocalDate().equals(selectedDate))
                .collect(Collectors.toList());

        LocalTime now = LocalTime.now();

        for (LocalTime slot : allSlots) {
            // Filter out past times for today
            if (selectedDate.isEqual(today) && slot.isBefore(now)) {
                continue;
            }

            boolean isFree = true;
            LocalTime slotEnd = slot.plusMinutes(30);

            for (RendezVous r : rdvsForDay) {
                LocalTime rStart = r.getDateHeure().toLocalTime();
                int duree = r.getDureeMinutes() != null ? r.getDureeMinutes() : 30;
                LocalTime rEnd = rStart.plusMinutes(duree);

                if (slot.isBefore(rEnd) && slotEnd.isAfter(rStart)) {
                    isFree = false;
                    break;
                }
            }
            if (isFree) {
                availableTimeSlots.add(String.format("%02d:%02d", slot.getHour(), slot.getMinute()));
            }
        }
    }

    public void addEvent() {
        if (selectedMedecinId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Veuillez sélectionner un médecin."));
            return;
        }

        try {
            if (selectedDate == null || selectedTimeSlot == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                                "Veuillez sélectionner une date et une heure disponible."));
                return;
            }

            LocalTime time = LocalTime.parse(selectedTimeSlot);
            LocalDateTime finalDateTime = LocalDateTime.of(selectedDate, time);
            newRendezVous.setDateHeure(finalDateTime);

            rendezVousService.planifierRendezVous(newRendezVous,
                    newRendezVous.getPatient() != null ? newRendezVous.getPatient().getId() : null,
                    selectedMedecinId);

            loadEvents();
            updateAvailableTimeSlots(); // Reflect immediately
            selectedTimeSlot = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Rendez-vous ajouté."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                            "Impossible d'ajouter le rendez-vous: " + e.getMessage()));
            e.printStackTrace();
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

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public Long getSelectedMedecinId() {
        return selectedMedecinId;
    }

    public void setSelectedMedecinId(Long selectedMedecinId) {
        this.selectedMedecinId = selectedMedecinId;
    }

    public List<String> getAvailableTimeSlots() {
        return availableTimeSlots;
    }

    public void setAvailableTimeSlots(List<String> availableTimeSlots) {
        this.availableTimeSlots = availableTimeSlots;
    }

    public String getSelectedTimeSlot() {
        return selectedTimeSlot;
    }

    public void setSelectedTimeSlot(String selectedTimeSlot) {
        this.selectedTimeSlot = selectedTimeSlot;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }
}
