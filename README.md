# Disaster Relief Coordination System

## Tech Stack
- **Backend:** Java 17, Spring Boot 3.3 (Jakarta EE), Spring MVC, Spring JDBC, WebSocket
- **Frontend:** JSP, JSTL, HTML5, CSS3, JavaScript, Leaflet.js (OpenStreetMap + Nominatim), Chart.js
- **Database:** MySQL 8+
- **AI/ML Service:** Python (Flask + scikit-learn + Gemini API)
- **Reports:** OpenPDF (PDF), Apache POI (Excel), OpenCSV (CSV)

## Quick Setup Guide (For All 4 Team Members)

### 1. Database Setup (MySQL)
Open MySQL Workbench or terminal and run:
```sql
SOURCE database/schema.sql;
```

### 2. Local Configuration (Keep Private - Ignored by Git)
Copy `src/main/resources/application-local.properties.example` to `src/main/resources/application-local.properties` and enter your own MySQL password and Gemini API key:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
gemini.api.key=YOUR_GEMINI_API_KEY
ai.service.url=http://localhost:5000
```

### 3. Run the Java Spring Boot App
Using Maven:
```bash
mvn spring-boot:run
```
Or open the folder in **VS Code**, install the recommended extensions, and click **Run** on `ReliefApplication.java`.
Visit: `http://localhost:8080/`

### 4. Run the Python AI/ML Microservice
```bash
cd ai-service
python -m venv venv
# Windows: venv\Scripts\activate | Mac/Linux: source venv/bin/activate
pip install -r requirements.txt
python app.py
```
Runs on: `http://localhost:5000/`

## Git Branching Workflow
- Never push directly to `main`.
- Create your module branch:
  - `git checkout -b feature/auth-needs`
  - `git checkout -b feature/pledge-allocation`
  - `git checkout -b feature/maps-alerts-chat`
  - `git checkout -b feature/ai-analytics-reports`
