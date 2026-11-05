-- MySQL / Relational Database Schema for AI-Based Resume Screening System
CREATE DATABASE IF NOT EXISTS ai_resume_screening;
USE ai_resume_screening;

CREATE TABLE IF NOT EXISTS admin_users (
    id VARCHAR(16) PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_openings (
    id VARCHAR(16) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    department VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    min_experience INT NOT NULL DEFAULT 0,
    required_education VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS candidates (
    id VARCHAR(16) PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(40),
    education VARCHAR(120),
    experience_years DECIMAL(4,1) DEFAULT 0.0,
    file_name VARCHAR(200),
    raw_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
