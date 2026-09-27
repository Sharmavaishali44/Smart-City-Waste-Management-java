-- =======================================================
-- Smart City Waste Collection & Municipal Complaint System
-- Database Schema for MySQL 8.0+ (Nagar Nigam Jaipur)
-- =======================================================

CREATE DATABASE IF NOT EXISTS smart_city_waste CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_city_waste;

-- Drop tables in reverse dependency order if recreating
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS complaint_status_history;
DROP TABLE IF EXISTS complaint_assignments;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS crews;
DROP TABLE IF EXISTS wards;
DROP TABLE IF EXISTS areas;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------------
-- 1. USERS TABLE
-- -------------------------------------------------------
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) NOT NULL,
    role ENUM('CITIZEN', 'ADMIN', 'CREW') NOT NULL DEFAULT 'CITIZEN',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 2. AREAS (MUNICIPAL ZONES) TABLE
-- -------------------------------------------------------
CREATE TABLE areas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    city VARCHAR(100) NOT NULL DEFAULT 'Jaipur',
    population INT DEFAULT 50000,
    is_sensitive BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_areas_name (name)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 3. WARDS TABLE
-- -------------------------------------------------------
CREATE TABLE wards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ward_number VARCHAR(20) NOT NULL,
    ward_name VARCHAR(100) NOT NULL,
    area_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES areas(id) ON DELETE CASCADE,
    INDEX idx_wards_area (area_id),
    INDEX idx_wards_number (ward_number)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 4. CREWS (COLLECTION TEAMS) TABLE
-- -------------------------------------------------------
CREATE TABLE crews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    crew_name VARCHAR(100) NOT NULL,
    leader_name VARCHAR(100) NOT NULL,
    members_count INT DEFAULT 4,
    members_description VARCHAR(255) DEFAULT '4 Field Sanitation Staff',
    phone VARCHAR(20) NOT NULL,
    area_id INT NOT NULL,
    ward_id INT NOT NULL,
    ward_coverage VARCHAR(100) DEFAULT 'All Wards in Zone',
    vehicle_number VARCHAR(30) NOT NULL,
    vehicle_capacity DECIMAL(5,2) DEFAULT 3.50,
    current_workload INT DEFAULT 0,
    availability ENUM('AVAILABLE', 'ON_DUTY', 'OFF_DUTY', 'INACTIVE') DEFAULT 'AVAILABLE',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    user_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES areas(id),
    FOREIGN KEY (ward_id) REFERENCES wards(id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_crews_area (area_id),
    INDEX idx_crews_ward (ward_id),
    INDEX idx_crews_avail (availability)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 5. COMPLAINTS TABLE
-- -------------------------------------------------------
CREATE TABLE complaints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_code VARCHAR(30) NOT NULL UNIQUE,
    citizen_user_id INT NULL,
    citizen_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    area_id INT NOT NULL,
    ward_id INT NOT NULL,
    locality VARCHAR(150) NULL,
    address VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    image_path VARCHAR(255) NULL,
    status ENUM('NEW', 'PENDING_ASSIGNMENT', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED') DEFAULT 'NEW',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    priority_score INT DEFAULT 50,
    priority_reason TEXT NULL,
    is_health_risk BOOLEAN DEFAULT FALSE,
    is_garbage_overflow BOOLEAN DEFAULT FALSE,
    is_sensitive_area BOOLEAN DEFAULT FALSE,
    assigned_crew_id INT NULL,
    expected_resolution_time DATETIME NULL,
    resolution_notes TEXT NULL,
    resolution_image_path VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    FOREIGN KEY (citizen_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (area_id) REFERENCES areas(id),
    FOREIGN KEY (ward_id) REFERENCES wards(id),
    FOREIGN KEY (assigned_crew_id) REFERENCES crews(id) ON DELETE SET NULL,
    INDEX idx_complaints_status (status),
    INDEX idx_complaints_priority (priority),
    INDEX idx_complaints_area (area_id),
    INDEX idx_complaints_ward (ward_id),
    INDEX idx_complaints_created (created_at),
    INDEX idx_complaints_code (complaint_code)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 6. COMPLAINT ASSIGNMENTS TABLE
-- -------------------------------------------------------
CREATE TABLE complaint_assignments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    crew_id INT NOT NULL,
    assigned_by_user_id INT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(255) NULL,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (crew_id) REFERENCES crews(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_assign_complaint (complaint_id),
    INDEX idx_assign_crew (crew_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 7. COMPLAINT STATUS HISTORY (AUDIT TRAIL)
-- -------------------------------------------------------
CREATE TABLE complaint_status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL,
    old_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_by_user_id INT NULL,
    comments VARCHAR(255) NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_history_complaint (complaint_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 8. FEEDBACK TABLE
-- -------------------------------------------------------
CREATE TABLE feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT NOT NULL UNIQUE,
    citizen_user_id INT NULL,
    rating INT NOT NULL,
    comments TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (citizen_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_feedback_rating (rating)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 9. NOTIFICATIONS TABLE
-- -------------------------------------------------------
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    role_target VARCHAR(20) DEFAULT 'ALL',
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_role (role_target)
) ENGINE=InnoDB;

-- -------------------------------------------------------
-- 10. AUDIT LOGS (SECURITY & ACTIONS AUDIT)
-- -------------------------------------------------------
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id INT NULL,
    details TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;
