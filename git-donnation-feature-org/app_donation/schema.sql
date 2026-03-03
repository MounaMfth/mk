-- Schema creation script for donation app
-- This creates the basic tables needed for authentication

-- Drop existing tables if they exist
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS organisation CASCADE;
DROP TABLE IF EXISTS secteur CASCADE;
DROP TABLE IF EXISTS organisation_secteur CASCADE;
DROP TABLE IF EXISTS piece_jointe CASCADE;
DROP TABLE IF EXISTS activite CASCADE;
DROP TABLE IF EXISTS projet CASCADE;
DROP TABLE IF EXISTS don CASCADE;
DROP TABLE IF EXISTS don_financier CASCADE;
DROP TABLE IF EXISTS don_nature CASCADE;
DROP TABLE IF EXISTS don_parrainage CASCADE;
DROP TABLE IF EXISTS don_evenementiel CASCADE;
DROP TABLE IF EXISTS commentaire CASCADE;

-- Create secteur table
CREATE TABLE secteur (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL
);

-- Create organisation table
CREATE TABLE organisation (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    email VARCHAR(255),
    telephone VARCHAR(50),
    adresse TEXT,
    site_web VARCHAR(255),
    statut VARCHAR(50),
    numero_agrement VARCHAR(100),
    validation VARCHAR(50),
    date_creation TIMESTAMP,
    logo_url VARCHAR(500)
);

-- Create utilisateur table
CREATE TABLE utilisateur (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    telephone VARCHAR(50),
    adresse TEXT,
    profil VARCHAR(100),
    organisation_id VARCHAR(255),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE SET NULL
);

-- Create user_roles table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES utilisateur(id) ON DELETE CASCADE
);

-- Create organisation_secteur join table
CREATE TABLE organisation_secteur (
    organisation_id VARCHAR(255) NOT NULL,
    secteur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (organisation_id, secteur_id),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE CASCADE,
    FOREIGN KEY (secteur_id) REFERENCES secteur(id) ON DELETE CASCADE
);

-- Create piece_jointe table
CREATE TABLE piece_jointe (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255),
    type VARCHAR(100),
    url VARCHAR(500),
    organisation_id VARCHAR(255),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE CASCADE
);

-- Create activite table
CREATE TABLE activite (
    id VARCHAR(255) PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_activite TIMESTAMP,
    organisation_id VARCHAR(255),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE CASCADE
);

-- Create projet table
CREATE TABLE projet (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    organisation_id VARCHAR(255),
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE CASCADE
);

-- Create don table (base table for all donation types)
CREATE TABLE don (
    id VARCHAR(255) PRIMARY KEY,
    montant DECIMAL(12, 2),
    date_don TIMESTAMP,
    type VARCHAR(50),
    organisation_id VARCHAR(255),
    utilisateur_id BIGINT,
    FOREIGN KEY (organisation_id) REFERENCES organisation(id) ON DELETE SET NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE SET NULL
);

-- Create specific donation type tables
CREATE TABLE don_financier (
    id VARCHAR(255) PRIMARY KEY,
    modalite_paiement VARCHAR(100),
    FOREIGN KEY (id) REFERENCES don(id) ON DELETE CASCADE
);

CREATE TABLE don_nature (
    id VARCHAR(255) PRIMARY KEY,
    type_bien VARCHAR(255),
    FOREIGN KEY (id) REFERENCES don(id) ON DELETE CASCADE
);

CREATE TABLE don_parrainage (
    id VARCHAR(255) PRIMARY KEY,
    duree_mois INTEGER,
    FOREIGN KEY (id) REFERENCES don(id) ON DELETE CASCADE
);

CREATE TABLE don_evenementiel (
    id VARCHAR(255) PRIMARY KEY,
    evenement VARCHAR(255),
    FOREIGN KEY (id) REFERENCES don(id) ON DELETE CASCADE
);

-- Create commentaire table
CREATE TABLE commentaire (
    id BIGSERIAL PRIMARY KEY,
    contenu TEXT,
    date_commentaire TIMESTAMP,
    utilisateur_id BIGINT,
    don_id VARCHAR(255),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) ON DELETE SET NULL,
    FOREIGN KEY (don_id) REFERENCES don(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_utilisateur_username ON utilisateur(username);
CREATE INDEX idx_utilisateur_email ON utilisateur(email);
CREATE INDEX idx_utilisateur_organisation ON utilisateur(organisation_id);
CREATE INDEX idx_don_organisation ON don(organisation_id);
CREATE INDEX idx_don_utilisateur ON don(utilisateur_id);
