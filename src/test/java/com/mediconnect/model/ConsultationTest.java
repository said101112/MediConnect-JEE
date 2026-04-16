package com.mediconnect.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ConsultationTest {

    @Test
    public void testDateFormatting() {
        Consultation consultation = new Consultation();
        LocalDateTime dt = LocalDateTime.of(2023, 10, 5, 14, 30);
        consultation.setDateVisite(dt);
        
        assertEquals("05/10/2023 14:30", consultation.getDateVisiteFormatee());
    }

    @Test
    public void testClotureStatus() {
        Consultation consultation = new Consultation();
        consultation.setCloturee(false);
        assertFalse(consultation.getCloturee());
        
        consultation.setCloturee(true);
        assertTrue(consultation.getCloturee());
    }

    @Test
    public void testNullDate() {
        Consultation consultation = new Consultation();
        consultation.setDateVisite(null);
        
        assertEquals("—", consultation.getDateVisiteFormatee());
    }
}
