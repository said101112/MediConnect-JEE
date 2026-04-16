package com.mediconnect.controller;

import com.mediconnect.model.Consultation;
import com.mediconnect.model.Patient;
import com.mediconnect.model.User;
import com.mediconnect.service.ConsultationService;
import com.mediconnect.service.SessionManager;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streams an ordonnance PDF to the browser.
 * Accepts: GET /download/ordonnance?id=<consultationId>
 * Security: verifies the logged-in patient owns the consultation.
 */
@WebServlet("/download/ordonnance")
public class DownloadOrdonnanceServlet extends HttpServlet {

    @Inject
    private SessionManager sessionManager;

    private final ConsultationService consultationService = new ConsultationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // ── Auth guard ──────────────────────────────────────────────────────
        if (!sessionManager.isLoggedIn()) {
            resp.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        // ── Resolve consultation ────────────────────────────────────────────
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre 'id' manquant.");
            return;
        }

        Integer consultationId;
        try {
            consultationId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre 'id' invalide.");
            return;
        }

        Consultation consultation = consultationService.findById(consultationId).orElse(null);
        if (consultation == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Ordonnance introuvable.");
            return;
        }

        // ── Ownership check ─────────────────────────────────────────────────
        boolean authorized = false;
        User currentUser = sessionManager.getCurrentUser();

        if (sessionManager.isPatient()) {
            authorized = consultation.getPatient().getId().equals(currentUser.getId());
        } else if (sessionManager.isMedecin()) {
            authorized = consultation.getMedecin().getId().equals(currentUser.getId());
        } else if (sessionManager.isSecretaire()) {
            authorized = true; // Secretaries can download any for administrative purposes
        }

        if (!authorized) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès refusé.");
            return;
        }

        // ── Resolve PDF bytes ───────────────────────────────────────────────
        byte[] pdfBytes;
        String storedPath = consultation.getCheminPdfOrdonnance();

        if (storedPath != null && !storedPath.isBlank()) {
            // Try to serve the stored file first
            File file = new File(storedPath);
            if (file.exists() && file.isFile()) {
                pdfBytes = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(pdfBytes);
                }
            } else {
                // Fall back to generation if file not found on disk
                pdfBytes = consultationService.generateOrdonnancePdf(consultation);
            }
        } else {
            // Generate on-the-fly
            pdfBytes = consultationService.generateOrdonnancePdf(consultation);
        }

        // ── Stream to client ────────────────────────────────────────────────
        String filename = "ordonnance_" + consultationId + ".pdf";
        resp.setContentType("application/pdf");
        resp.setContentLength(pdfBytes.length);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (OutputStream out = resp.getOutputStream()) {
            out.write(pdfBytes);
            out.flush();
        }
    }
}
