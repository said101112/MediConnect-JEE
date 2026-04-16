package com.mediconnect.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class RendezVousTest {

    @Test
    public void testDateFormatting() {
        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(LocalDateTime.of(2023, 12, 25, 10, 0));
        
        assertEquals("25/12/2023 10:00", rdv.getDateHeureFormatee());
    }

    @Test
    public void testStatutAssignment() {
        RendezVous rdv = new RendezVous();
        rdv.setStatut(StatutRDV.PLANIFIE);
        assertEquals(StatutRDV.PLANIFIE, rdv.getStatut());
        
        rdv.setStatut(StatutRDV.ANNULE);
        assertEquals(StatutRDV.ANNULE, rdv.getStatut());
    }

    @Test
    public void testDefaultStatut() {
        RendezVous rdv = new RendezVous();
        assertEquals(StatutRDV.PLANIFIE, rdv.getStatut(), "Le statut par défaut devrait être PLANIFIE");
    }

    @Test
    public void testHeureFormatee() {
        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(LocalDateTime.of(2024, 3, 15, 9, 5));
        
        assertEquals("09:05", rdv.getHeureFormatee());
        assertEquals("15/03/2024", rdv.getDateFormatee());
    }
}
