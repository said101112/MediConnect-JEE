-- ============================================
-- MediConnect - Add groupe_sanguin to patients
-- Run this once after init.sql
-- ============================================
ALTER TABLE patients ADD COLUMN IF NOT EXISTS groupe_sanguin VARCHAR(5);

-- Set blood type for the test patient (Pierre Bernard, id=3)
UPDATE patients SET groupe_sanguin = 'O+' WHERE id = 4;

-- Update antecedents and allergies for a richer dossier
UPDATE patients SET
    antecedents = 'Hypertension artérielle (2018) — sous traitement.
Appendicectomie (2015) — opéré au CHU de Casablanca.
Diabète de type 2 (2020) — suivi trimestriel.',
    allergies   = 'Pénicilline — réaction allergique sévère (urticaire).
Aspirine — intolérance gastrique.
Arachides — allergie alimentaire confirmée.'
WHERE id = 4;

-- Verify
SELECT id, nom, prenom, cin, date_naissance, groupe_sanguin, antecedents, allergies
FROM patients WHERE id = 4;
