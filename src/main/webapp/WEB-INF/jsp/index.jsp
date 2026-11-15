<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${projectName}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
</head>
<body>
    <header class="navbar">
        <h1>${projectName}</h1>
        <span class="badge">DB: ${dbStatus} | Users: ${userCount}</span>
    </header>

    <main class="container">
        <section class="card">
            <h2>Project Setup Verified</h2>
            <p>Spring Boot 3 (Jakarta EE) + JSP + Spring JDBC + Leaflet.js is up and running.</p>
            <div class="role-grid">
                <div class="role-box">
                    <h3>Affected Person</h3>
                    <p>Report urgent relief needs with geolocation.</p>
                </div>
                <div class="role-box">
                    <h3>Donor</h3>
                    <p>Match with verified needs, pledge & track allocations.</p>
                </div>
                <div class="role-box">
                    <h3>Relief Department</h3>
                    <p>Verify requests, send radius alerts & export reports.</p>
                </div>
            </div>
        </section>

        <section class="card">
            <h2>Relief Map Preview (Leaflet + OpenStreetMap)</h2>
            <div id="map" style="height: 320px; border-radius: 8px;"></div>
        </section>
    </main>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
</body>
</html>
