-- ============================================
-- MediConnect - PostgreSQL Database Init Script
-- ============================================

-- Drop existing tables to start fresh
DROP TABLE IF EXISTS factures CASCADE;
DROP TABLE IF EXISTS consultations CASCADE;
DROP TABLE IF EXISTS rendez_vous CASCADE;
DROP TABLE IF EXISTS disponibilites CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS medecins CASCADE;
DROP TABLE IF EXISTS secretaires CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;

-- 1. Table des Comptes (Authentification et Sécurité)
CREATE TABLE users (
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(150) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(20)  NOT NULL CHECK (role IN ('MEDECIN', 'SECRETAIRE', 'PATIENT')),
    active    BOOLEAN      DEFAULT TRUE,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- 2. Profils Spécifiques (Héritage)
CREATE TABLE secretaires (
    id         BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    nom        VARCHAR(100) NOT NULL,
    prenom     VARCHAR(100) NOT NULL,
    telephone  VARCHAR(20)
);

CREATE TABLE medecins (
    id          BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100) NOT NULL,
    telephone   VARCHAR(20),
    specialite  VARCHAR(100) NOT NULL,
    matricule   VARCHAR(50)  UNIQUE NOT NULL
);

CREATE TABLE patients (
    id              BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    telephone       VARCHAR(20),
    cin             VARCHAR(20) UNIQUE NOT NULL,
    date_naissance  DATE NOT NULL,
    adresse         VARCHAR(255),
    antecedents     TEXT,
    allergies       TEXT
);

-- 3. Gestion de l'Agenda (Disponibilités des médecins)
CREATE TABLE disponibilites (
    id          SERIAL PRIMARY KEY,
    id_medecin  BIGINT NOT NULL REFERENCES medecins(id) ON DELETE CASCADE,
    jour_semaine INT NOT NULL, -- 1=Lundi, 2=Mardi ... 7=Dimanche
    heure_debut TIME NOT NULL,
    heure_fin   TIME NOT NULL
);

-- 4. Gestion des Rendez-vous (L'occupation)
CREATE TABLE rendez_vous (
    id             SERIAL PRIMARY KEY,
    id_patient     BIGINT NOT NULL REFERENCES patients(id),
    id_medecin     BIGINT NOT NULL REFERENCES medecins(id),
    date_heure     TIMESTAMP NOT NULL,
    duree_minutes  INT DEFAULT 30, -- Durée du créneau
    motif          VARCHAR(255),
    statut         VARCHAR(30) DEFAULT 'PLANIFIE' -- PLANIFIE, ANNULE, TERMINE
);

-- 5. Module Médical (Consultation + Ordonnance PDF)
CREATE TABLE consultations (
    id                  SERIAL PRIMARY KEY,
    id_rdv              INT UNIQUE REFERENCES rendez_vous(id),
    id_patient          BIGINT NOT NULL REFERENCES patients(id),
    id_medecin          BIGINT NOT NULL REFERENCES medecins(id),
    date_visite         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observations        TEXT,
    diagnostic          TEXT,
    chemin_pdf_ordonnance VARCHAR(255),
    cloturee            BOOLEAN DEFAULT FALSE
);

-- 6. Module Financier (Facturation)
CREATE TABLE factures (
    id              SERIAL PRIMARY KEY,
    id_consultation INT UNIQUE NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    montant_total   DECIMAL(10,2) NOT NULL,
    date_facture    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut_paiement VARCHAR(20) DEFAULT 'NON_PAYE' -- PAYE, NON_PAYE
);


-- ============================================
-- Insert sample users (passwords are BCrypt hashed)
-- Password for all: "password123"
-- ============================================
INSERT INTO users (id, email, password, role, active) VALUES
    (1, 'medecin@mediconnect.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkJboF0M8Lx7eAB6mH9TlG2aK', 'MEDECIN', true),
    (2, 'secretaire@mediconnect.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkJboF0M8Lx7eAB6mH9TlG2aK', 'SECRETAIRE', true),
    (3, 'patient@mediconnect.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkJboF0M8Lx7eAB6mH9TlG2aK', 'PATIENT', true);

SELECT setval('users_id_seq', 3);

INSERT INTO medecins (id, nom, prenom, telephone, specialite, matricule) VALUES
    (1, 'Dupont', 'Jean', '0601020304', 'Généraliste', 'MED-12345');

INSERT INTO secretaires (id, nom, prenom, telephone) VALUES
    (2, 'Martin', 'Sophie', '0605060708');

INSERT INTO patients (id, nom, prenom, telephone, cin, date_naissance) VALUES
    (3, 'Bernard', 'Pierre', '0611121314', 'AB123456', '1990-01-01');

