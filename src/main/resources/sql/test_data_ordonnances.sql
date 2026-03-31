-- ============================================
-- MediConnect - Test Data for Ordonnances
-- Run this AFTER init.sql (tables + base users already exist)
-- ============================================
-- Existing accounts (from init.sql):
--   Patient : patient@mediconnect.com  / password123  (id=3)
--   Médecin : medecin@mediconnect.com  / password123  (id=1)
-- ============================================

-- 1. Add a rendez-vous (TERMINE) between Dr. Dupont and patient Bernard
INSERT INTO rendez_vous (id_patient, id_medecin, date_heure, duree_minutes, motif, statut)
VALUES
    (4, 1, '2026-03-10 09:00:00', 30, 'Douleurs abdominales', 'TERMINE'),
    (4, 1, '2026-03-20 14:30:00', 30, 'Fièvre et toux persistante', 'TERMINE'),
    (4, 1, '2026-03-28 10:00:00', 30, 'Contrôle général', 'TERMINE');

-- 2. Add consultations linked to those rendez-vous (ordonnances)
-- Get the IDs of the newly inserted rendez-vous
DO $$
DECLARE
    rdv1_id INT;
    rdv2_id INT;
    rdv3_id INT;
BEGIN
    SELECT id INTO rdv1_id FROM rendez_vous WHERE motif = 'Douleurs abdominales' AND id_patient = 4 LIMIT 1;
    SELECT id INTO rdv2_id FROM rendez_vous WHERE motif = 'Fièvre et toux persistante' AND id_patient = 4 LIMIT 1;
    SELECT id INTO rdv3_id FROM rendez_vous WHERE motif = 'Contrôle général' AND id_patient = 4 LIMIT 1;

    INSERT INTO consultations (id_rdv, id_patient, id_medecin, date_visite, observations, diagnostic, cloturee)
    VALUES
        (
            rdv1_id, 4, 1,
            '2026-03-10 09:15:00',
            'Prendre SPASFON 80mg — 1 comprimé 3 fois par jour pendant 5 jours.
Eviter les repas lourds et les graisses.
Revenir en consultation si les douleurs persistent au-delà de 5 jours.',
            'Spasmes intestinaux. Pas d''urgence chirurgicale détectée.',
            TRUE
        ),
        (
            rdv2_id, 4, 1,
            '2026-03-20 14:45:00',
            'AMOXICILLINE 1g — 1 comprimé matin et soir pendant 7 jours.
DOLIPRANE 1000mg — En cas de fièvre supérieure à 38.5°C, max 3 fois/jour.
RHINOFLUX — 2 pulvérisations dans chaque narine matin et soir.
Repos recommandé, hydratation abondante.',
            'Rhinopharyngite bactérienne avec fièvre. Prescription d''antibiotique justifiée.',
            TRUE
        ),
        (
            rdv3_id, 4, 1,
            '2026-03-28 10:10:00',
            'VITAMINE D3 1000 UI — 1 capsule par jour pendant 3 mois.
MAGNÉSIUM 300mg — 1 comprimé le soir au coucher.
Activité physique modérée conseillée (30 min de marche par jour).
Prochain contrôle dans 3 mois.',
            'Bilan de santé annuel satisfaisant. Légère carence en vitamine D.',
            FALSE
        );
END $$;

-- ============================================
-- Verify the data was inserted correctly
-- ============================================
SELECT
    c.id                              AS consultation_id,
    c.date_visite,
    c.diagnostic,
    c.cloturee,
    p_user.email                      AS patient_email,
    pat.nom || ' ' || pat.prenom      AS patient_name,
    m.nom   || ' ' || m.prenom        AS medecin_name,
    m.specialite
FROM consultations c
JOIN patients pat  ON pat.id  = c.id_patient
JOIN users p_user  ON p_user.id = pat.id
JOIN medecins m    ON m.id    = c.id_medecin
WHERE c.id_patient = 4
ORDER BY c.date_visite DESC;
