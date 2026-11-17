CREATE DATABASE
IF NOT EXISTS disaster_relief_db;
USE disaster_relief_db;

-- 1. USERS
CREATE TABLE
IF NOT EXISTS users
(
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    mobile_number VARCHAR
(15) NOT NULL UNIQUE,
    name VARCHAR
(100) NOT NULL,
    role ENUM
('AFFECTED_PERSON', 'DONOR', 'DEPARTMENT') NOT NULL,
    latitude DECIMAL
(10, 7),
    longitude DECIMAL
(10, 7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. DISASTERS
CREATE TABLE
IF NOT EXISTS disasters
(
    disaster_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR
(150) NOT NULL,
    type VARCHAR
(80) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    status ENUM
('ACTIVE', 'CONTAINED', 'CLOSED') DEFAULT 'ACTIVE'
);

-- 3. NEEDS
CREATE TABLE
IF NOT EXISTS needs
(
    need_id INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    created_by INT NOT NULL,
    category VARCHAR
(80) NOT NULL,
    description TEXT NOT NULL,
    quantity_required INT NOT NULL,
    quantity_pledged INT DEFAULT 0,
    quantity_allocated INT DEFAULT 0,
    quantity_remaining INT NOT NULL,
    urgency ENUM
('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    status ENUM
('PENDING_VERIFICATION', 'VERIFIED', 'REJECTED', 'PARTIALLY_FULFILLED', 'FULFILLED') DEFAULT 'PENDING_VERIFICATION',
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    affected_people_count INT NULL DEFAULT NULL,
    critical_people_count INT NULL DEFAULT NULL,
    location_name VARCHAR(150) NULL DEFAULT NULL,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id),
    FOREIGN KEY
(created_by) REFERENCES users
(user_id)
);

-- 4. NEED_VERIFICATION
CREATE TABLE
IF NOT EXISTS need_verification
(
    verification_id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    department_id INT NOT NULL,
    status ENUM
('APPROVED', 'REJECTED', 'FLAGGED_DUPLICATE') NOT NULL,
    remarks TEXT,
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(need_id) REFERENCES needs
(need_id),
    FOREIGN KEY
(department_id) REFERENCES users
(user_id)
);

-- 5. PLEDGES
CREATE TABLE
IF NOT EXISTS pledges
(
    pledge_id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    donor_id INT NOT NULL,
    quantity_offered INT NOT NULL,
    quantity_allocated INT DEFAULT 0,
    status ENUM
('PLEDGED', 'PARTIALLY_ALLOCATED', 'ALLOCATED', 'CANCELLED') DEFAULT 'PLEDGED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(need_id) REFERENCES needs
(need_id),
    FOREIGN KEY
(donor_id) REFERENCES users
(user_id)
);

-- 6. ALLOCATIONS
CREATE TABLE
IF NOT EXISTS allocations
(
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    pledge_id INT NOT NULL,
    need_id INT NOT NULL,
    quantity_allocated INT NOT NULL,
    status ENUM
('ALLOCATED', 'IN_TRANSIT', 'DELIVERED', 'REASSIGNED') DEFAULT 'ALLOCATED',
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(pledge_id) REFERENCES pledges
(pledge_id),
    FOREIGN KEY
(need_id) REFERENCES needs
(need_id)
);

-- 7. MESSAGES
CREATE TABLE IF NOT EXISTS messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    sender_id INT NOT NULL,
    receiver_id INT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_status BOOLEAN DEFAULT FALSE,
    message_type ENUM('DIRECT', 'BROADCAST') NOT NULL DEFAULT 'DIRECT',
    FOREIGN KEY (need_id) REFERENCES needs(need_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 7b. NEED PARTICIPANTS (Historical Stakeholder Tracking)
CREATE TABLE IF NOT EXISTS need_participants (
    participant_id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL,
    participation_status ENUM('ACTIVE', 'WITHDRAWN') DEFAULT 'ACTIVE',
    FOREIGN KEY (need_id) REFERENCES needs(need_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_np_need_user (need_id, user_id),
    INDEX idx_np_status (participation_status)
);

-- 8. ALERTS
CREATE TABLE
IF NOT EXISTS alerts
(
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    department_id INT NOT NULL,
    title VARCHAR
(150) NOT NULL,
    message TEXT NOT NULL,
    latitude DECIMAL
(10, 7) NOT NULL,
    longitude DECIMAL
(10, 7) NOT NULL,
    radius_km DECIMAL
(6, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
(department_id) REFERENCES users
(user_id)
);

-- 9. AREA_SAFETY_ASSESSMENTS (Historical Hazard & Safety Tracking)
CREATE TABLE IF NOT EXISTS area_safety_assessments (
    assessment_id INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    department_id INT NOT NULL,
    area_name VARCHAR(150) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    radius_km DECIMAL(6, 2) NOT NULL,
    safety_status ENUM('SAFE', 'UNSAFE', 'MONITORING') NOT NULL DEFAULT 'MONITORING',
    flood_level_meters DECIMAL(4, 2) NULL DEFAULT NULL,
    assessment_notes TEXT NULL,
    assessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_asa_disaster_area (disaster_id, area_name),
    INDEX idx_asa_coords (latitude, longitude),
    INDEX idx_asa_status (safety_status),
    INDEX idx_asa_assessed_at (assessed_at DESC)
);

-- Sample Seed Data for Testing
INSERT IGNORE
INTO users
(mobile_number, name, role, latitude, longitude) VALUES
('9000000001', 'District Relief Officer', 'DEPARTMENT', 13.2150000, 74.9960000),
('9000000002', 'Ramesh Kumar', 'AFFECTED_PERSON', 13.2180000, 74.9980000),
('9000000003', 'Coastal Relief Foundation', 'DONOR', 13.2100000, 74.9900000);

INSERT INTO disasters
    (name, type, description, start_date, status)
VALUES
    ('Coastal Monsoon Flood 2026', 'Flood', 'Heavy rainfall causing low-lying waterlogging and supply disruption', '2026-07-15', 'ACTIVE');
