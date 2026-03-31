package com.mediconnect.controller;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mediconnect.model.Consultation;
import com.mediconnect.model.Patient;
import com.mediconnect.service.ConsultationService;
import com.mediconnect.service.SessionManager;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates and streams a complete Dossier Médical PDF for the logged-in patient.
 * Mapped to: GET /download/dossier
 */
@WebServlet("/download/dossier")
public class DownloadDossierServlet extends HttpServlet {

    @Inject
    private SessionManager sessionManager;

    private final ConsultationService consultationService = new ConsultationService();

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    // Brand colours
    private static final DeviceRgb BLUE       = new DeviceRgb(37,  99,  235);
    private static final DeviceRgb GREEN      = new DeviceRgb( 5, 150, 105);
    private static final DeviceRgb RED        = new DeviceRgb(220,  38,  38);
    private static final DeviceRgb PURPLE     = new DeviceRgb(124,  58, 237);
    private static final DeviceRgb GRAY_BG    = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb GRAY_TEXT  = new DeviceRgb(100, 116, 139);
    private static final DeviceRgb BORDER_COL = new DeviceRgb(226, 232, 240);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!sessionManager.isLoggedIn() || !sessionManager.isPatient()) {
            resp.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        Patient patient = (Patient) sessionManager.getCurrentUser();
        List<Consultation> consultations = consultationService.getOrdonnancesByPatient(patient.getId());

        byte[] pdf = buildDossierPdf(patient, consultations);

        String filename = "dossier_medical_" + patient.getNom().toLowerCase() + ".pdf";
        resp.setContentType("application/pdf");
        resp.setContentLength(pdf.length);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (OutputStream out = resp.getOutputStream()) {
            out.write(pdf);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    private byte[] buildDossierPdf(Patient patient, List<Consultation> consultations)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf);
        doc.setMargins(50, 50, 50, 50);

        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, PdfEncodings.CP1252);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1252);
        PdfFont italic  = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE, PdfEncodings.CP1252);

        // ── HEADER ──────────────────────────────────────────────────────────
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth().setMarginBottom(4);

        Cell titleCell = new Cell().setBorder(Border.NO_BORDER);
        titleCell.add(new Paragraph("DOSSIER MÉDICAL")
                .setFont(bold).setFontSize(26).setFontColor(BLUE));
        titleCell.add(new Paragraph("MediConnect — Espace Patient")
                .setFont(regular).setFontSize(11).setFontColor(GRAY_TEXT));
        headerTable.addCell(titleCell);

        Cell dateCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        dateCell.add(new Paragraph("Édité le").setFont(regular).setFontSize(9).setFontColor(GRAY_TEXT));
        dateCell.add(new Paragraph(LocalDate.now().format(DATE_FMT))
                .setFont(bold).setFontSize(11).setFontColor(GRAY_TEXT));
        headerTable.addCell(dateCell);

        doc.add(headerTable);
        doc.add(new LineSeparator(new SolidLine(1.5f)).setMarginBottom(20));

        // ── SECTION: IDENTITÉ ────────────────────────────────────────────────
        addSectionTitle(doc, bold, "👤  Identité du Patient", BLUE);

        Table idTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setBackgroundColor(GRAY_BG)
                .setBorderRadius(new BorderRadius(8))
                .setMarginBottom(20);

        int age = (patient.getDateNaissance() != null)
                ? Period.between(patient.getDateNaissance(), LocalDate.now()).getYears() : 0;

        addInfoCell(idTable, bold, regular, "Nom complet",
                patient.getPrenom() + " " + patient.getNom());
        addInfoCell(idTable, bold, regular, "CIN",
                nvl(patient.getCin()));
        addInfoCell(idTable, bold, regular, "Date de naissance",
                patient.getDateNaissance() != null
                        ? patient.getDateNaissance().format(DATE_FMT) + " (" + age + " ans)" : "—");
        addInfoCell(idTable, bold, regular, "Téléphone",
                nvl(patient.getTelephone()));
        addInfoCell(idTable, bold, regular, "Adresse",
                nvl(patient.getAdresse()));
        addInfoCell(idTable, bold, regular, "Email",
                nvl(patient.getEmail()));

        doc.add(idTable);

        // ── SECTION: PROFIL MÉDICAL ──────────────────────────────────────────
        addSectionTitle(doc, bold, "🩸  Profil Médical", RED);

        // Blood type badge
        String gs = patient.getGroupeSanguin();
        if (gs != null && !gs.isBlank()) {
            DeviceRgb gsColor = bloodTypeColor(gs);
            Cell gsCell = new Cell()
                    .setBorder(new SolidBorder(gsColor, 2))
                    .setBackgroundColor(new DeviceRgb(239, 246, 255))
                    .setPadding(10)
                    .setBorderRadius(new BorderRadius(8))
                    .setWidth(UnitValue.createPointValue(100))
                    .setMarginBottom(12);
            gsCell.add(new Paragraph("Groupe Sanguin").setFont(bold).setFontSize(9)
                    .setFontColor(GRAY_TEXT));
            gsCell.add(new Paragraph(gs).setFont(bold).setFontSize(22)
                    .setFontColor(gsColor));
            doc.add(gsCell);
        }

        // Allergies
        addSubSection(doc, bold, regular, italic,
                "Allergies connues", patient.getAllergies(), RED);

        // Antécédents
        addSubSection(doc, bold, regular, italic,
                "Antécédents médicaux", patient.getAntecedents(), PURPLE);

        // ── SECTION: HISTORIQUE DES CONSULTATIONS ────────────────────────────
        addSectionTitle(doc, bold,
                "📋  Historique des Consultations (" + consultations.size() + ")", GREEN);

        if (consultations.isEmpty()) {
            doc.add(new Paragraph("Aucune consultation enregistrée.")
                    .setFont(italic).setFontSize(11).setFontColor(GRAY_TEXT)
                    .setMarginBottom(20));
        } else {
            for (int i = 0; i < consultations.size(); i++) {
                Consultation c = consultations.get(i);
                addConsultationBlock(doc, bold, regular, italic, c, i + 1, BORDER_COL, GRAY_BG, BLUE);
            }
        }

        // ── FOOTER ───────────────────────────────────────────────────────────
        doc.add(new LineSeparator(new SolidLine(0.5f)).setMarginTop(12).setMarginBottom(8));
        doc.add(new Paragraph("Document généré automatiquement par MediConnect • Confidentiel")
                .setFont(italic).setFontSize(9).setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void addSectionTitle(Document doc, PdfFont bold, String title, DeviceRgb color) {
        doc.add(new Paragraph(title)
                .setFont(bold).setFontSize(14).setFontColor(color)
                .setMarginTop(8).setMarginBottom(10));
    }

    private void addInfoCell(Table table, PdfFont bold, PdfFont regular,
                             String label, String value) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(10);
        cell.add(new Paragraph(label).setFont(bold).setFontSize(9).setFontColor(GRAY_TEXT));
        cell.add(new Paragraph(value).setFont(regular).setFontSize(11));
        table.addCell(cell);
    }

    private void addSubSection(Document doc, PdfFont bold, PdfFont regular,
                               PdfFont italic, String title, String content, DeviceRgb accentColor) {
        doc.add(new Paragraph(title).setFont(bold).setFontSize(12).setFontColor(accentColor)
                .setMarginBottom(4));

        if (content == null || content.isBlank()) {
            doc.add(new Paragraph("Aucune information renseignée.")
                    .setFont(italic).setFontSize(11).setFontColor(GRAY_TEXT).setMarginBottom(12));
        } else {
            Cell c = new Cell().setBorder(Border.NO_BORDER)
                    .setBorderLeft(new SolidBorder(accentColor, 3))
                    .setBackgroundColor(GRAY_BG).setPadding(10)
                    .setMarginBottom(16);
            c.add(new Paragraph(content).setFont(regular).setFontSize(11));
            Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth().setMarginBottom(16);
            t.addCell(c);
            doc.add(t);
        }
    }

    private void addConsultationBlock(Document doc, PdfFont bold, PdfFont regular,
                                      PdfFont italic, Consultation c, int num,
                                      DeviceRgb borderCol, DeviceRgb bgCol, DeviceRgb blue) {
        // Outer card-like table
        Table card = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth().setMarginBottom(12);

        Cell cell = new Cell()
                .setBackgroundColor(bgCol)
                .setBorder(new SolidBorder(borderCol, 1))
                .setBorderLeft(new SolidBorder(blue, 4))
                .setBorderRadius(new BorderRadius(6))
                .setPadding(12);

        // Header row: number + date + doctor
        String dateStr = c.getDateVisite() != null ? c.getDateVisite().format(DATETIME_FMT) : "—";
        String docName = c.getMedecin() != null
                ? "Dr. " + c.getMedecin().getPrenom() + " " + c.getMedecin().getNom()
                + " — " + nvl(c.getMedecin().getSpecialite()) : "—";
        String status  = Boolean.TRUE.equals(c.getCloturee()) ? "✔ Clôturée" : "⏳ En cours";

        cell.add(new Paragraph("Consultation #" + num + "  |  " + dateStr + "  |  " + status)
                .setFont(bold).setFontSize(11).setFontColor(blue).setMarginBottom(4));
        cell.add(new Paragraph(docName).setFont(regular).setFontSize(10)
                .setFontColor(GRAY_TEXT).setMarginBottom(8));

        // Diagnostic
        if (c.getDiagnostic() != null && !c.getDiagnostic().isBlank()) {
            cell.add(new Paragraph("Diagnostic :").setFont(bold).setFontSize(10).setMarginBottom(2));
            cell.add(new Paragraph(c.getDiagnostic()).setFont(regular).setFontSize(10)
                    .setMarginBottom(6));
        }

        // Prescriptions
        if (c.getObservations() != null && !c.getObservations().isBlank()) {
            cell.add(new Paragraph("Prescriptions :").setFont(bold).setFontSize(10).setMarginBottom(2));
            cell.add(new Paragraph(c.getObservations()).setFont(italic).setFontSize(10));
        }

        card.addCell(cell);
        doc.add(card);
    }

    private String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private DeviceRgb bloodTypeColor(String gs) {
        return switch (gs) {
            case "O+", "O-"   -> GREEN;
            case "A+", "A-"   -> BLUE;
            case "B+", "B-"   -> PURPLE;
            case "AB+", "AB-" -> RED;
            default            -> GRAY_TEXT;
        };
    }
}
