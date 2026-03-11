-- ============================================
-- MediConnect - PostgreSQL Database Init Script
-- ============================================

-- Create the database (run this separately if needed)
-- CREATE DATABASE mediconnect;

-- Connect to the database
-- \c mediconnect;

-- Create enum type for roles
CREATE TYPE user_role AS ENUM ('MEDECIN', 'SECRETAIRE', 'PATIENT');

-- Create the users table
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('MEDECIN', 'SECRETAIRE', 'PATIENT')),
    nom             VARCHAR(100)    NOT NULL,
    prenom          VARCHAR(100)    NOT NULL,
    telephone       VARCHAR(20),
    date_naissance  DATE,
    adresse         VARCHAR(255),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    active          BOOLEAN         DEFAULT TRUE
);

-- Create index on email for fast lookup
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ============================================
-- Insert sample users (passwords are BCrypt hashed)
-- Password for all: "password123"
-- ============================================
INSERT INTO users (email, password, role, nom, prenom, telephone) VALUES
    ('medecin@mediconnect.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkJboF0M8Lx7eAB6mH9TlG2aK', 'MEDECIN',    'Dupont',   'Jean',     '0601020304'),
    ('secretaire@mediconnect.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkJboF0M8Lx7eAB6mH9TlG2aK', 'SECRETAIRE', 'Martin',   'Sophie',   '0605060708'),
    ('patient@mediconnect.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBrkJboF0M8Lx7eAB6mH9TlG2aK', 'PATIENT',    'Bernard',  'Pierre',   '0611121314')
ON CONFLICT (email) DO NOTHING;
