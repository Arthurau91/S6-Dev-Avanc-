-- =====================================================
-- MasterAnnonce - Database Initialization Script
-- PostgreSQL
-- =====================================================

-- Create tables (Hibernate can auto-generate with hbm2ddl=update,
-- but this script provides manual control)

CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS annonce (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    adress VARCHAR(64),
    mail VARCHAR(64),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'DRAFT',
    version BIGINT DEFAULT 0,
    author_id BIGINT REFERENCES users(id),
    category_id BIGINT REFERENCES category(id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_annonce_status ON annonce(status);
CREATE INDEX IF NOT EXISTS idx_annonce_author ON annonce(author_id);
CREATE INDEX IF NOT EXISTS idx_annonce_category ON annonce(category_id);

-- Seed data
INSERT INTO category (label) VALUES ('Informatique') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Immobilier') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Cours particuliers') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Emploi') ON CONFLICT (label) DO NOTHING;
INSERT INTO category (label) VALUES ('Services') ON CONFLICT (label) DO NOTHING;

INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@univ-paris8.fr', 'admin123', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, email, password, role)
VALUES ('testuser', 'test@univ-paris8.fr', 'password123', 'USER')
ON CONFLICT (username) DO NOTHING;
