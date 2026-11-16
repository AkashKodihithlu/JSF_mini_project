# Disaster Relief Coordination System
**Prepare • Respond • Recover**

A full-stack emergency response and resource coordination platform built with **Spring Boot**, **JDBC / DAO Pattern**, **JSP / JSTL**, and **MySQL**. The system connects **Affected Citizens**, **Verified Donors**, and **Relief Department Officers** to eliminate duplicated requests, prioritize critical needs, and match nearby relief supplies in real time.

---

## Table of Contents
1. [System Architecture & Features](#1-system-architecture--features)
2. [Prerequisites & Required Software](#2-prerequisites--required-software)
3. [Step-by-Step A–Z Local Setup](#3-step-by-step-az-local-setup)
4. [Complete MySQL Schema & Seed Script](#4-complete-mysql-schema--seed-script)
5. [Application Configuration (`application.properties`)](#5-application-configuration-applicationproperties)
6. [Running the Project](#6-running-the-project)
7. [A–Z End-to-End Testing Walkthrough](#7-az-end-to-end-testing-walkthrough)
8. [Project Folder Structure](#8-project-folder-structure)
9. [Team Git Workflow & Secret Safety Rules](#9-team-git-workflow--secret-safety-rules)
10. [Troubleshooting Common Errors](#10-troubleshooting-common-errors)

---

## 1. System Architecture & Features

* **Passwordless Mobile OTP Authentication:** Users sign in with a 10-digit Indian mobile number and a 6-digit OTP (printed to the Spring Boot server console during local development). Registered users are routed automatically to their role dashboard; unregistered numbers are directed to `/register` with browser GPS auto-detection.
* **Three Role-Based Dashboards:**
  * **Affected Citizen (`/affected/dashboard`):** Submit urgent relief requests (`Food & Rations`, `Clean Drinking Water`, `Medical & First Aid`, `Shelter & Clothing`, `Rescue & Evacuation`), preview AI category/priority/duplicate scores, track fulfillment progress, and chat with donors/officers.
  * **Verified Donor (`/donor/dashboard`):** Browse verified needs ranked by urgency weight and Haversine GPS distance, pledge full or partial quantities, and coordinate delivery via chat.
  * **Department / Relief Officer (`/department/dashboard`):** Audit pending requests (Approve, Flag Duplicate, Reject, or override Urgency), reallocate unallocated donor surplus to other open needs, update delivery stages (`ALLOCATED` → `IN_TRANSIT` → `DELIVERED`), broadcast kilometer-radius geo-alerts on Leaflet maps, view Chart.js analytics, and export CSV / Print-PDF reports.
* **AI Classification & Duplicate Detection:** Connects to an optional Python AI/ML service (`http://localhost:5000`) or Gemini API, with a built-in Jaccard similarity and keyword rule engine fallback so the application works 100% offline as well.

---

## 2. Prerequisites & Required Software

Before starting, make sure every team member has the following installed and added to their system `PATH`:

| Tool | Minimum Version | Verification Command | Download Source |
| :--- | :--- | :--- | :--- |
| **Java JDK** | JDK 17 or 21 | `java -version` | Eclipse Temurin / Oracle JDK |
| **Apache Maven** | 3.8+ | `mvn -version` | `maven.apache.org` (or use `./mvnw`) |
| **MySQL Server** | 8.0+ | `mysql --version` | MySQL Community Server + Workbench |
| **Git** | 2.30+ | `git --version` | `git-scm.com` |
| **IDE (Recommended)** | Latest | — | VS Code (with Extension Pack for Java) or IntelliJ IDEA |

---

## 3. Step-by-Step A–Z Local Setup

### Step A: Clone the Repository & Switch to the Active Branch
Open your terminal (Command Prompt, PowerShell, or Git Bash) and run:
```bash
git clone [https://github.com/AkashKodihithlu/JSF_mini_project.git](https://github.com/AkashKodihithlu/JSF_mini_project.git)
cd JSF_mini_project
git checkout abhi
```
*(If changes have been merged into `main`, run `git checkout main && git pull origin main`.)*

### Step B: Start MySQL & Create the Database
1. Ensure your local **MySQL80** Windows service (or macOS/Linux MySQL daemon) is running.
2. Open **MySQL Workbench** or connect via terminal:
   ```bash
   mysql -u root -p
   ```
3. Run the complete SQL script in **[Section 4](#4-complete-mysql-schema--seed-script)** to create `disaster_relief_db`, all 8 relational tables, and the 3 demo accounts.

### Step C: Configure `application.properties`
Open `src/main/resources/application.properties` and set your local MySQL username and password as shown in **[Section 5](#5-application-configuration-applicationproperties)**.

### Step D: Verify Logo Asset Path
Confirm that the application crest logo exists at:
```text
src/main/webapp/assets/images/disasterManageLogo.png
```
*(If stored as `disasterManageLogo.jpeg`, the JSP pages automatically fall back to `.jpeg`.)*

### Step E: Set Environment Variables (Optional for Live Gemini AI)
If you want to enable live Gemini AI calls instead of the built-in offline classifier:
* **Windows CMD:**
  ```cmd
  set GEMINI_API_KEY=your_gemini_api_key_here
  ```
* **Windows PowerShell:**
  ```powershell
  $env:GEMINI_API_KEY="your_gemini_api_key_here"
  ```
* **macOS / Linux:**
  ```bash
  export GEMINI_API_KEY="your_gemini_api_key_here"
  ```

---

## 4. Complete MySQL Schema & Seed Script

Copy and execute this entire SQL script in MySQL Workbench or the MySQL CLI:

```sql
CREATE DATABASE IF NOT EXISTS disaster_relief_db;
USE disaster_relief_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    mobile_number VARCHAR(15) NOT NULL UNIQUE,
    role ENUM('AFFECTED_PERSON', 'DONOR', 'DEPARTMENT') NOT NULL,
    latitude DOUBLE NOT NULL DEFAULT 13.2150,
    longitude DOUBLE NOT NULL DEFAULT 74.9960,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Disasters Table
CREATE TABLE IF NOT EXISTS disasters (
    disaster_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(60) NOT NULL,
    status ENUM('ACTIVE', 'MONITORING', 'CLOSED') DEFAULT 'ACTIVE',
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Relief Needs Table
CREATE TABLE IF NOT EXISTS needs (
    need_id INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    created_by INT NOT NULL,
    category VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    quantity_required INT NOT NULL,
    quantity_pledged INT NOT NULL DEFAULT 0,
    quantity_allocated INT NOT NULL DEFAULT 0,
    quantity_remaining INT NOT NULL,
    urgency ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    status ENUM('PENDING_VERIFICATION', 'VERIFIED', 'PARTIALLY_FULFILLED', 'FULFILLED', 'REJECTED') DEFAULT 'PENDING_VERIFICATION',
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. Department Verification Audit Table
CREATE TABLE IF NOT EXISTS need_verification (
    verification_id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    department_id INT NOT NULL,
    status ENUM('APPROVED', 'REJECTED', 'FLAGGED_DUPLICATE') NOT NULL,
    remarks VARCHAR(255),
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (need_id) REFERENCES needs(need_id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 5. Donor Pledges Table
CREATE TABLE IF NOT EXISTS pledges (
    pledge_id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    donor_id INT NOT NULL,
    quantity_offered INT NOT NULL,
    quantity_allocated INT NOT NULL DEFAULT 0,
    status ENUM('PLEDGED', 'PARTIALLY_ALLOCATED', 'ALLOCATED', 'CANCELLED') DEFAULT 'PLEDGED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (need_id) REFERENCES needs(need_id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 6. Resource Allocations Table
CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    pledge_id INT NOT NULL,
    need_id INT NOT NULL,
    quantity_allocated INT NOT NULL,
    status ENUM('ALLOCATED', 'IN_TRANSIT', 'DELIVERED', 'REASSIGNED') DEFAULT 'ALLOCATED',
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pledge_id) REFERENCES pledges(pledge_id) ON DELETE CASCADE,
    FOREIGN KEY (need_id) REFERENCES needs(need_id) ON DELETE CASCADE
);

-- 7. Need-Specific Coordination Chat Table
CREATE TABLE IF NOT EXISTS messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    need_id INT NOT NULL,
    sender_id INT NOT NULL,
    receiver_id INT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (need_id) REFERENCES needs(need_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 8. Radius Geo-Alerts Table
CREATE TABLE IF NOT EXISTS alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id INT NOT NULL,
    created_by INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_km DOUBLE NOT NULL DEFAULT 15.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Seed Default Users (Department, Affected Citizen, Donor)
INSERT IGNORE INTO users (user_id, name, mobile_number, role, latitude, longitude) VALUES
(1, 'District Relief Control Room', '9000000001', 'DEPARTMENT', 13.2150, 74.9960),
(2, 'Ramesh Poojary (Citizen)', '9000000002', 'AFFECTED_PERSON', 13.2185, 74.9920),
(3, 'Coastal Care Foundation', '9000000003', 'DONOR', 13.2110, 75.0010);

-- Seed Active Disaster Event
INSERT IGNORE INTO disasters (disaster_id, name, type, status, latitude, longitude) VALUES
(1, 'Coastal Monsoon Flood 2026', 'Flood', 'ACTIVE', 13.2150, 74.9960);

-- Seed Sample Verified & Pending Needs for Immediate Testing
INSERT IGNORE INTO needs (need_id, disaster_id, created_by, category, description, quantity_required, quantity_pledged, quantity_allocated, quantity_remaining, urgency, status, latitude, longitude) VALUES
(1, 1, 2, 'Clean Drinking Water', '50 bottled water crates needed for 18 families at Ward 4 shelter', 50, 0, 0, 50, 'HIGH', 'VERIFIED', 13.2185, 74.9920),
(2, 1, 2, 'Medical & First Aid', 'Urgent insulin vials and basic first-aid kits for elderly residents', 15, 0, 0, 15, 'CRITICAL', 'PENDING_VERIFICATION', 13.2192, 74.9935);
```

---

## 5. Application Configuration (`application.properties`)

Ensure `src/main/resources/application.properties` contains the following configuration (replace `YOUR_MYSQL_PASSWORD` with your local MySQL root password):

```properties
server.port=8080

# JSP View Resolver
spring.mvc.view.prefix=/WEB-INF/jsp/
spring.mvc.view.suffix=.jsp

# MySQL Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/disaster_relief_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# AI Microservice & Environment Variable Key (NEVER hardcode API keys here)
ai.service.url=http://localhost:5000
gemini.api.key=${GEMINI_API_KEY:}
```

---

## 6. Running the Project

From the root folder (`JSF_mini_project`), compile and launch Spring Boot:

```bash
mvn clean spring-boot:run
```

Wait until you see:
```text
Started ReliefApplication in X.XXX seconds
```

Then open your browser and navigate to:
* **Landing Page:** `http://localhost:8080/`
* **Sign In / Register:** `http://localhost:8080/login`

---

## 7. A–Z End-to-End Testing Walkthrough

Follow these steps in order to verify every module works end-to-end:

1. **Test Affected Citizen Flow (`9000000002`):**
   * Open `http://localhost:8080/login`, enter **`9000000002`**, and click **Send OTP now**.
   * Look at your **terminal / IDE console** where Spring Boot is running—copy the 6-digit OTP printed in the log and submit it.
   * On `/affected/dashboard`, fill out **Report Relief Need**, click **AI Preview** to test automatic category/urgency/duplicate detection, and click **Submit Request**.
2. **Test Department Verification & Geo-Alerts (`9000000001`):**
   * Click **Logout**, sign in with **`9000000001`** (copy the new OTP from the console).
   * On `/department/dashboard`, review incoming requests in **Pending Verification Queue**, adjust urgency if needed, add remarks, and click **Approve**.
   * Broadcast a **Radius Geo-Alert** (e.g., `15` km) and verify it appears as a red circle on the **Geospatial Command Map**.
3. **Test Smart Donor Matching & Pledges (`9000000003`):**
   * Click **Logout**, sign in with **`9000000003`** (copy the OTP from the console).
   * On `/donor/dashboard`, view verified requests ranked by match score and GPS distance.
   * Enter a pledge quantity (try pledging *more* than `quantityRemaining` to create a surplus pool!) and click **Pledge Supplies**.
   * Click the **Chat** icon on any request to send coordination messages.
4. **Test Surplus Reallocation, Delivery Stages & Reports (`9000000001`):**
   * Sign back in as Department Officer (`9000000001`).
   * Reallocate surplus stock to another open need, mark allocations as **In Transit** / **Delivered**, inspect the **Chart.js** supply graph, and click **Export CSV** or **Print / PDF**.
5. **Test New User Registration:**
   * Sign in with any unregistered 10-digit number (e.g., `9876543210`), verify the console OTP, and complete `/register` with GPS auto-detect.

---

## 8. Project Folder Structure

```text
JSF_mini_project/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/relief/
    │   ├── ReliefApplication.java
    │   ├── controller/
    │   │   ├── AuthController.java              # /login, /register, OTP flow, /dashboard router
    │   │   ├── DashboardController.java         # /affected/dashboard, /donor/dashboard, /department/dashboard
    │   │   ├── NeedController.java              # /api/needs, AI preview, department verification
    │   │   ├── PledgeAllocationController.java  # /api/matching/needs, /api/pledges, surplus reallocation
    │   │   ├── CommunicationController.java     # /api/messages/{needId}, /api/alerts
    │   │   └── ReportController.java            # /api/reports/summary, /api/reports/export/csv
    │   ├── service/
    │   │   ├── NeedService.java
    │   │   ├── PledgeAllocationService.java
    │   │   └── CommunicationService.java
    │   ├── dao/
    │   │   ├── UserDAO.java
    │   │   ├── DisasterDAO.java
    │   │   ├── NeedDAO.java
    │   │   ├── NeedVerificationDAO.java
    │   │   ├── PledgeDAO.java
    │   │   ├── AllocationDAO.java
    │   │   ├── MessageDAO.java
    │   │   └── AlertDAO.java
    │   └── model/
    │       ├── User.java, Disaster.java, Need.java, NeedVerification.java
    │       └── Pledge.java, Allocation.java, Message.java, Alert.java
    ├── resources/
    │   └── application.properties
    └── webapp/
        ├── assets/
        │   ├── css/style.css
        │   ├── js/app.js
        │   ├── js/cursor-effects.js
        │   └── images/disasterManageLogo.png
        └── WEB-INF/jsp/
            ├── index.jsp
            ├── auth/
            │   ├── login.jsp
            │   └── register.jsp
            └── dashboard/
                ├── affected-dashboard.jsp
                ├── donor-dashboard.jsp
                └── department-dashboard.jsp
```

---

## 9. Team Git Workflow & Secret Safety Rules

1. **Pull before coding:**
   ```bash
   git pull origin abhi
   ```
2. **Never commit API keys or personal MySQL passwords:**
   * GitHub Push Protection (`GH013`) will reject any commit containing a raw `AIza...` Google/Gemini API key anywhere in the repository history—even inside comments.
   * Always keep `gemini.api.key=${GEMINI_API_KEY:}` in `application.properties` and pass actual keys via environment variables.
3. **Commit & push your feature:**
   ```bash
   git status
   git add <your-changed-files>
   git commit -m "Describe your update clearly"
   git push origin abhi
   ```

---

## 10. Troubleshooting Common Errors

* **`Port 8080 was already in use`:**
  Kill the lingering Java process on Windows CMD and restart:
  ```cmd
  for /f "tokens=5" %a in ('netstat -aon ^| findstr :8080') do taskkill /F /PID %a
  mvn spring-boot:run
  ```
* **`Access denied for user 'root'@'localhost'`:**
  Verify `spring.datasource.password` in `src/main/resources/application.properties` matches your local MySQL password and ensure the `disaster_relief_db` database exists.
* **`404 Whitelabel Error` on JSP pages:**
  Make sure you run via `mvn spring-boot:run` from the project root directory so Embedded Tomcat resolves `src/main/webapp/WEB-INF/jsp/` properly, and verify `tomcat-embed-jasper` is present in `pom.xml`.
* **`GH013: Repository rule violations found (Push cannot contain secrets)`:**
  Remove any literal API key from `application.properties`, run `git add src/main/resources/application.properties && git commit --amend --no-edit`, and push again.