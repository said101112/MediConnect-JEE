package com.mediconnect.controller;

import com.mediconnect.model.Consultation;
import com.mediconnect.model.Patient;
import com.mediconnect.service.ConsultationService;
import com.mediconnect.service.SessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for the patient's ordonnance history page.
 * Loads all consultations that belong to the currently logged-in patient.
 */
@Named
@ViewScoped
public class OrdonnancesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManager sessionManager;

    private List<Consultation> ordonnances = new ArrayList<>();
    private final ConsultationService consultationService = new ConsultationService();

    @PostConstruct
    public void init() {
        if (sessionManager.isLoggedIn() && sessionManager.isPatient()) {
            Patient patient = (Patient) sessionManager.getCurrentUser();
            ordonnances = consultationService.getOrdonnancesByPatient(patient.getId());
        }
    }

    /**
     * Redirects to the PDF download servlet for the given consultation id.
     */
    public void telecharger(Integer consultationId) throws IOException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String contextPath = ctx.getExternalContext().getRequestContextPath();
        ctx.getExternalContext().redirect(contextPath + "/download/ordonnance?id=" + consultationId);
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public List<Consultation> getOrdonnances() {
        return ordonnances;
    }

    public void setOrdonnances(List<Consultation> ordonnances) {
        this.ordonnances = ordonnances;
    }

    /** Number of ordonnances — used in EL instead of fn:length. */
    public int getCount() {
        return ordonnances != null ? ordonnances.size() : 0;
    }

    /** Format a LocalDateTime for display in the XHTML. */
    public String formatDate(LocalDateTime dt) {
        if (dt == null) return "Date inconnue";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm"));
    }

    /** @deprecated kept only for backward compat — use formatDate() instead */
    public DateTimeFormatter getDateFormatter() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    }
}
