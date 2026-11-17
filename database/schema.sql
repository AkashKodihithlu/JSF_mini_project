DROP DATABASE IF EXISTS disaster_relief_db;

CREATE DATABASE disaster_relief_db
CHARACTER
SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE disaster_relief_db;

-- 1. Users Table
CREATE TABLE users
(
    user_id INT
    AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR
    (120) NOT NULL,
    mobile_number VARCHAR
    (15) NOT NULL UNIQUE,
    role ENUM
    ('AFFECTED_PERSON', 'DONOR', 'DEPARTMENT') NOT NULL,
    latitude DOUBLE NOT NULL DEFAULT 13.2150,
    longitude DOUBLE NOT NULL DEFAULT 74.9960,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

    -- 2. Disasters Table
    CREATE TABLE disasters
    (
        disaster_id INT
        AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR
        (150) NOT NULL,
    type VARCHAR
        (60) NOT NULL,
    description TEXT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    status ENUM
        ('ACTIVE', 'CONTAINED', 'MONITORING', 'CLOSED') DEFAULT 'ACTIVE',
    latitude DOUBLE NOT NULL DEFAULT 13.2150,
    longitude DOUBLE NOT NULL DEFAULT 74.9960,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

        -- 3. Relief Needs Table
        CREATE TABLE needs
        (
            need_id INT
            AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    created_by INT NOT NULL,
    category VARCHAR
            (80) NOT NULL,
    description TEXT NOT NULL,
    quantity_required INT NOT NULL,
    quantity_pledged INT NOT NULL DEFAULT 0,
    quantity_allocated INT NOT NULL DEFAULT 0,
    quantity_remaining INT NOT NULL,
    urgency ENUM
            ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    status ENUM
            ('PENDING_VERIFICATION', 'VERIFIED', 'PARTIALLY_FULFILLED', 'FULFILLED', 'REJECTED') DEFAULT 'PENDING_VERIFICATION',
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    affected_people_count INT NULL,
    critical_people_count INT NULL,
    location_name VARCHAR
            (255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_needs_disaster FOREIGN KEY
            (disaster_id) REFERENCES disasters
            (disaster_id) ON
            DELETE CASCADE,
    CONSTRAINT fk_needs_user FOREIGN KEY
            (created_by) REFERENCES users
            (user_id) ON
            DELETE CASCADE
) ENGINE=InnoDB;

            -- 4. Department Verification Audit Table
            CREATE TABLE need_verification
            (
                verification_id INT
                AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    department_id INT NOT NULL,
    status ENUM
                ('APPROVED', 'REJECTED', 'FLAGGED_DUPLICATE') NOT NULL,
    remarks VARCHAR
                (255) NULL,
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_verif_need FOREIGN KEY
                (need_id) REFERENCES needs
                (need_id) ON
                DELETE CASCADE,
    CONSTRAINT fk_verif_dept FOREIGN KEY
                (department_id) REFERENCES users
                (user_id) ON
                DELETE CASCADE
) ENGINE=InnoDB;

                -- 5. Donor Pledges Table
                CREATE TABLE pledges
                (
                    pledge_id INT
                    AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    donor_id INT NOT NULL,
    quantity_offered INT NOT NULL,
    quantity_allocated INT NOT NULL DEFAULT 0,
    status ENUM
                    ('PLEDGED', 'PARTIALLY_ALLOCATED', 'ALLOCATED', 'CANCELLED', 'WITHDRAWN') DEFAULT 'PLEDGED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pledges_need FOREIGN KEY
                    (need_id) REFERENCES needs
                    (need_id) ON
                    DELETE CASCADE,
    CONSTRAINT fk_pledges_donor FOREIGN KEY
                    (donor_id) REFERENCES users
                    (user_id) ON
                    DELETE CASCADE
) ENGINE=InnoDB;

                    -- 6. Resource Allocations Table
                    CREATE TABLE allocations
                    (
                        allocation_id INT
                        AUTO_INCREMENT PRIMARY KEY,
    pledge_id INT NOT NULL,
    need_id INT NOT NULL,
    quantity_allocated INT NOT NULL,
    status ENUM
                        ('ALLOCATED', 'IN_TRANSIT', 'DELIVERED', 'REASSIGNED', 'CANCELLED') DEFAULT 'ALLOCATED',
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alloc_pledge FOREIGN KEY
                        (pledge_id) REFERENCES pledges
                        (pledge_id) ON
                        DELETE CASCADE,
    CONSTRAINT fk_alloc_need FOREIGN KEY
                        (need_id) REFERENCES needs
                        (need_id) ON
                        DELETE CASCADE
) ENGINE=InnoDB;

                        CREATE TABLE need_participants
                        (
                            participant_id INT
                            AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL,
    participation_status ENUM
                            ('ACTIVE', 'WITHDRAWN') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_part_need FOREIGN KEY
                            (need_id) REFERENCES needs
                            (need_id) ON
                            DELETE CASCADE,
    CONSTRAINT fk_part_user FOREIGN KEY
                            (user_id) REFERENCES users
                            (user_id) ON
                            DELETE CASCADE
) ENGINE=InnoDB;

                            -- 8. Need-Specific Coordination Chat Table (BROADCAST & DIRECT)
                            CREATE TABLE messages
                            (
                                message_id INT
                                AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    sender_id INT NOT NULL,
    receiver_id INT NULL,
    message_type ENUM
                                ('BROADCAST', 'DIRECT') NOT NULL DEFAULT 'BROADCAST',
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_need FOREIGN KEY
                                (need_id) REFERENCES needs
                                (need_id) ON
                                DELETE CASCADE,
    CONSTRAINT fk_msg_sender FOREIGN KEY
                                (sender_id) REFERENCES users
                                (user_id) ON
                                DELETE CASCADE,
    CONSTRAINT fk_msg_receiver FOREIGN KEY
                                (receiver_id) REFERENCES users
                                (user_id) ON
                                DELETE
                                SET NULL
                                ) ENGINE=InnoDB;

                                -- 9. Radius Geo-Alerts Table
                                CREATE TABLE alerts
                                (
                                    alert_id INT
                                    AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    created_by INT NOT NULL,
    title VARCHAR
                                    (150) NOT NULL,
    message TEXT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_km DOUBLE NOT NULL DEFAULT 15.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerts_disaster FOREIGN KEY
                                    (disaster_id) REFERENCES disasters
                                    (disaster_id) ON
                                    DELETE CASCADE,
    CONSTRAINT fk_alerts_user FOREIGN KEY
                                    (created_by) REFERENCES users
                                    (user_id) ON
                                    DELETE CASCADE
) ENGINE=InnoDB;

                                    -- 10. Area Safety Assessments Table (Geospatial Command Map Overlays)
                                    CREATE TABLE area_safety_assessments
                                    (
                                        assessment_id INT
                                        AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    department_id INT NOT NULL,
    area_name VARCHAR
                                        (150) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_km DOUBLE NOT NULL DEFAULT 3.0,
    safety_status ENUM
                                        ('SAFE', 'MONITORING', 'UNSAFE', 'UNKNOWN') NOT NULL DEFAULT 'MONITORING',
    flood_level_meters DOUBLE NULL,
    assessment_notes TEXT NULL,
    assessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_safety_disaster FOREIGN KEY
                                        (disaster_id) REFERENCES disasters
                                        (disaster_id) ON
                                        DELETE CASCADE,
    CONSTRAINT fk_safety_dept FOREIGN KEY
                                        (department_id) REFERENCES users
                                        (user_id) ON
                                        DELETE CASCADE
) ENGINE=InnoDB;

                                        ALTER TABLE allocations
    MODIFY COLUMN status ENUM
                                        ('ALLOCATED', 'IN_TRANSIT', 'DELIVERED', 'CONFIRMED', 'REASSIGNED', 'CANCELLED') NOT NULL DEFAULT 'ALLOCATED',
                                        ADD COLUMN cancellation_reason TEXT NULL AFTER status,
                                        ADD COLUMN cancelled_at TIMESTAMP NULL AFTER cancellation_reason,
                                        ADD COLUMN confirmed_at TIMESTAMP NULL AFTER cancelled_at;

                                        ALTER TABLE pledges
    MODIFY COLUMN status ENUM
                                        ('PLEDGED', 'PARTIALLY_ALLOCATED', 'ALLOCATED', 'CANCELLED', 'WITHDRAWN') NOT NULL DEFAULT 'PLEDGED',
                                        ADD COLUMN cancellation_reason TEXT NULL AFTER status,
                                        ADD COLUMN cancelled_at TIMESTAMP NULL AFTER cancellation_reason;

                                        -- ============================================================================
                                        -- SEED DATA FOR IMMEDIATE LOCAL TESTING
                                        -- ============================================================================

                                        INSERT INTO users
                                            (user_id, name, mobile_number, role, latitude, longitude)
                                        VALUES
                                            (1, 'District Relief Control Room', '9000000001', 'DEPARTMENT', 13.2150, 74.9960),
                                            (2, 'Ramesh Poojary (Citizen)', '9000000002', 'AFFECTED_PERSON', 13.2185, 74.9920),
                                            (3, 'Coastal Care Foundation', '9000000003', 'DONOR', 13.2110, 75.0010);

                                        INSERT INTO disasters
                                            (disaster_id, name, type, description, start_date, status, latitude, longitude)
                                        VALUES
                                            (1, 'Coastal Monsoon Flood 2026', 'Flood', 'Severe monsoon waterlogging across low-lying coastal wards and riverbanks.', '2026-11-15', 'ACTIVE', 13.2150, 74.9960);

                                        INSERT INTO needs
                                            (
                                            need_id, disaster_id, created_by, category, description,
                                            quantity_required, quantity_pledged, quantity_allocated, quantity_remaining,
                                            urgency, status, latitude, longitude,
                                            affected_people_count, critical_people_count, location_name
                                            )
                                        VALUES
                                            (1, 1, 2, 'Clean Drinking Water', '50 bottled water crates needed for 18 families at Ward 4 shelter', 50, 0, 0, 50, 'HIGH', 'VERIFIED', 13.2185, 74.9920, 72, 6, 'Ward 4 Community Hall'),
                                            (2, 1, 2, 'Medical & First Aid', 'Urgent insulin vials and basic first-aid kits for elderly residents', 15, 0, 0, 15, 'CRITICAL', 'PENDING_VERIFICATION', 13.2192, 74.9935, 24, 9, 'Riverbank North Colony');

                                        INSERT INTO area_safety_assessments
                                            (
                                            assessment_id, disaster_id, department_id, area_name, latitude, longitude,
                                            radius_km, safety_status, flood_level_meters, assessment_notes
                                            )
                                        VALUES
                                            (1, 1, 1, 'Riverbank North Sector', 13.2192, 74.9935, 2.0, 'UNSAFE', 1.65, 'Water level rising near bridge; evacuation boats deployed.'),
                                            (2, 1, 1, 'Central Relief Camp Zone', 13.2120, 75.0005, 2.5, 'SAFE', 0.00, 'Elevated ground; main supply staging hub operational.');