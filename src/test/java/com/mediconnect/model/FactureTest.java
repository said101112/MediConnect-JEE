package com.mediconnect.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class FactureTest {

    @Test
    public void testPaiementStatus() {
        Facture facture = new Facture();
        assertEquals(StatutPaiement.NON_PAYE, facture.getStatutPaiement());
        
        facture.setStatutPaiement(StatutPaiement.PAYE);
        assertEquals(StatutPaiement.PAYE, facture.getStatutPaiement());
    }

    @Test
    public void testMontant() {
        Facture facture = new Facture();
        BigDecimal montant = new BigDecimal("450.50");
        facture.setMontantTotal(montant);
        
        assertEquals(0, montant.compareTo(facture.getMontantTotal()));
    }
}
