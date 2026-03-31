package com.mediconnect.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mediconnect.model.Consultation;
import com.mediconnect.repository.ConsultationRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultationService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    private final ConsultationRepository repository;

    public ConsultationService() {
        this.repository = new ConsultationRepository();
    }

    /** Return all consultations for a patient (only those with medical content). */
    public List<Consultation> getOrdonnancesByPatient(Long patientId) {
        return repository.findByPatientId(patientId);
    }

    /** Find a single consultation (with ownership verified by caller). */
    public Consultation findById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Generate a styled PDF for a given consultation and return it as a byte array.
     * Uses iText 7.
     */
    public byte[] generateOrdonnancePdf(Consultation c) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);
        doc.setMargins(50, 50, 50, 50);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, PdfEncodings.CP1252);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1252);

        DeviceRgb primaryBlue = new DeviceRgb(37, 99, 235);
        DeviceRgb lightGray = new DeviceRgb(248, 250, 252);

        // ── Header ──────────────────────────────────────────────────────────────
        Paragraph title = new Paragraph("ORDONNANCE MÉDICALE")
                .setFont(bold)
                .setFontSize(22)
                .setFontColor(primaryBlue)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(title);

        doc.add(new Paragraph("MediConnect — Cabinet Médical")
                .setFont(regular)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new LineSeparator(new SolidLine(1f)).setMarginTop(8).setMarginBottom(16));

        // ── Date ────────────────────────────────────────────────────────────────
        String dateStr = (c.getDateVisite() != null)
                ? c.getDateVisite().format(DATETIME_FMT)
                : "N/A";
        doc.add(new Paragraph("Date : " + dateStr)
                .setFont(regular).setFontSize(11).setTextAlignment(TextAlignment.RIGHT));

        // ── Doctor / Patient info table ──────────────────────────────────────
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginTop(12).setMarginBottom(12);

        // Doctor cell
        Cell doctorCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(lightGray)
                .setPadding(12)
                .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(8));

        doctorCell.add(new Paragraph("Médecin")
                .setFont(bold).setFontSize(10).setFontColor(ColorConstants.GRAY));

        String medecinName = (c.getMedecin() != null)
                ? "Dr. " + c.getMedecin().getPrenom() + " " + c.getMedecin().getNom()
                : "N/A";
        String specialite = (c.getMedecin() != null && c.getMedecin().getSpecialite() != null)
                ? c.getMedecin().getSpecialite() : "";

        doctorCell.add(new Paragraph(medecinName).setFont(bold).setFontSize(13));
        doctorCell.add(new Paragraph(specialite).setFont(regular).setFontSize(11)
                .setFontColor(ColorConstants.GRAY));

        // Patient cell
        Cell patientCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(lightGray)
                .setPadding(12)
                .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(8))
                .setMarginLeft(8);

        patientCell.add(new Paragraph("Patient(e)")
                .setFont(bold).setFontSize(10).setFontColor(ColorConstants.GRAY));

        String patientName = (c.getPatient() != null)
                ? c.getPatient().getPrenom() + " " + c.getPatient().getNom()
                : "N/A";
        String cin = (c.getPatient() != null && c.getPatient().getCin() != null)
                ? "CIN : " + c.getPatient().getCin() : "";

        patientCell.add(new Paragraph(patientName).setFont(bold).setFontSize(13));
        patientCell.add(new Paragraph(cin).setFont(regular).setFontSize(11)
                .setFontColor(ColorConstants.GRAY));

        infoTable.addCell(doctorCell);
        infoTable.addCell(patientCell);
        doc.add(infoTable);

        doc.add(new LineSeparator(new SolidLine(0.5f)).setMarginBottom(16));

        // ── Diagnostic ───────────────────────────────────────────────────────
        if (c.getDiagnostic() != null && !c.getDiagnostic().isBlank()) {
            doc.add(new Paragraph("Diagnostic")
                    .setFont(bold).setFontSize(13).setFontColor(primaryBlue).setMarginBottom(4));
            doc.add(new Paragraph(c.getDiagnostic())
                    .setFont(regular).setFontSize(11).setMarginBottom(16));
        }

        // ── Ordonnance / Prescriptions ────────────────────────────────────────
        if (c.getObservations() != null && !c.getObservations().isBlank()) {
            doc.add(new Paragraph("Prescriptions / Observations")
                    .setFont(bold).setFontSize(13).setFontColor(primaryBlue).setMarginBottom(4));
            doc.add(new Paragraph(c.getObservations())
                    .setFont(regular).setFontSize(11).setMarginBottom(16));
        }

        // ── Signature block ──────────────────────────────────────────────────
        doc.add(new LineSeparator(new SolidLine(0.5f)).setMarginTop(24).setMarginBottom(12));

        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();
        sigTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("Cachet du médecin").setFont(regular).setFontSize(10)
                        .setFontColor(ColorConstants.GRAY)));
        sigTable.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("Signature").setFont(regular).setFontSize(10)
                        .setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.RIGHT)));
        doc.add(sigTable);

        doc.close();
        return baos.toByteArray();
    }
}
