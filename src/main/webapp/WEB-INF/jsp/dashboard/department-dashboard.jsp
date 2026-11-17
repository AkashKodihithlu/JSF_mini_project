<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relief Command Center | Disaster Relief Coordination System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        .dash-nav {
            background: rgba(8, 20, 43, 0.94);
            backdrop-filter: blur(12px);
            border-bottom: 1px solid var(--border-glass);
            padding: 0.85rem 4%;
            position: sticky;
            top: 0;
            z-index: 1000;
        }
        .dash-nav-inner {
            max-width: 1380px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 1rem;
            flex-wrap: wrap;
        }
        .user-pill {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.4rem 0.9rem;
            border-radius: 999px;
            background: rgba(217, 72, 30, 0.2);
            border: 1px solid rgba(251, 146, 60, 0.45);
            font-size: 0.84rem;
            color: #ffedd5;
        }
        .dash-container {
            max-width: 1380px;
            margin: 0 auto;
            padding: 2rem 4% 4rem;
        }
        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
            gap: 1.15rem;
            margin-bottom: 2rem;
        }
        .kpi-card {
            background: var(--navy-900);
            border: 1px solid var(--border-glass);
            border-radius: 10px;
            padding: 1.25rem;
        }
        .kpi-card span {
            display: block;
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: var(--text-muted);
        }
        .kpi-card strong {
            display: block;
            font-family: 'Rajdhani', sans-serif;
            font-size: 2rem;
            color: #ffffff;
            margin-top: 0.25rem;
        }
        .panel-card {
            background: var(--navy-900);
            border: 1px solid var(--border-glass);
            border-radius: 12px;
            padding: 1.5rem;
            margin-bottom: 2rem;
        }
        .panel-card h2 {
            font-size: 1.45rem;
            text-transform: uppercase;
            margin-bottom: 0.25rem;
        }
        .panel-sub {
            font-size: 0.85rem;
            color: var(--text-muted);
            margin-bottom: 1.25rem;
        }
        .two-col-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1.75rem;
            margin-bottom: 2rem;
        }
        .dark-input, .dark-select, .dark-textarea {
            width: 100%;
            padding: 0.62rem 0.8rem;
            border-radius: 6px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            background: var(--navy-950);
            color: #ffffff;
            font-family: inherit;
            font-size: 0.88rem;
        }
        .form-field { margin-bottom: 0.9rem; }
        .form-field label {
            display: block;
            font-size: 0.8rem;
            font-weight: 600;
            color: #e2e8f0;
            margin-bottom: 0.3rem;
        }
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 0.86rem; }
        th, td {
            text-align: left;
            padding: 0.8rem 0.65rem;
            border-bottom: 1px solid var(--border-glass);
            vertical-align: middle;
        }
        th {
            font-size: 0.74rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: var(--text-muted);
        }
        .badge {
            display: inline-block;
            padding: 0.2rem 0.58rem;
            border-radius: 999px;
            font-size: 0.72rem;
            font-weight: 700;
            text-transform: uppercase;
        }
        .urg-CRITICAL { background: rgba(220, 38, 38, 0.25); color: #fca5a5; border: 1px solid #f87171; }
        .urg-HIGH { background: rgba(234, 88, 12, 0.25); color: #fdba74; border: 1px solid #fb923c; }
        .urg-MEDIUM { background: rgba(37, 99, 235, 0.25); color: #93c5fd; border: 1px solid #60a5fa; }
        .urg-LOW { background: rgba(100, 116, 139, 0.25); color: #cbd5e1; }
        .st-PENDING_VERIFICATION { background: rgba(234, 179, 8, 0.2); color: #fde047; }
        .st-VERIFIED, .st-PARTIALLY_FULFILLED, .st-ALLOCATED, .st-IN_TRANSIT { background: rgba(14, 165, 233, 0.22); color: #7dd3fc; }
        .st-FULFILLED, .st-DELIVERED { background: rgba(34, 197, 94, 0.22); color: #86efac; }
        .st-REJECTED, .st-REASSIGNED { background: rgba(239, 68, 68, 0.22); color: #fca5a5; }
        .btn-sm {
            padding: 0.38rem 0.72rem;
            font-size: 0.78rem;
            border-radius: 5px;
            border: none;
            font-weight: 700;
            cursor: pointer;
            transition: opacity 0.2s ease;
        }
        .btn-sm:disabled { opacity: 0.55; cursor: not-allowed; }
        .btn-approve { background: #16a34a; color: #ffffff; }
        .btn-dup { background: #d97706; color: #ffffff; }
        .btn-reject { background: #dc2626; color: #ffffff; }
        .btn-transit { background: #0284c7; color: #ffffff; }
        .msg-banner {
            padding: 0.75rem 1rem;
            border-radius: 6px;
            font-size: 0.86rem;
            margin-bottom: 1.25rem;
            display: none;
        }
        .msg-ok { background: rgba(22, 101, 52, 0.9); border: 1px solid #86efac; color: #dcfce7; }
        .msg-err { background: rgba(153, 27, 27, 0.9); border: 1px solid #fca5a5; color: #ffffff; }
        .chart-box {
            position: relative;
            height: 260px;
            width: 100%;
        }
        #commandMap {
            height: 480px;
            border-radius: 8px;
            border: 1px solid rgba(255, 255, 255, 0.15);
            z-index: 1;
        }
        .map-kpi-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
            gap: 0.85rem;
            margin-bottom: 1.25rem;
        }
        .map-kpi-card {
            background: rgba(13, 33, 73, 0.65);
            border: 1px solid rgba(255, 255, 255, 0.12);
            border-radius: 8px;
            padding: 0.85rem;
        }
        .map-kpi-card span {
            font-size: 0.72rem;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            display: block;
        }
        .map-kpi-card strong {
            font-family: 'Rajdhani', sans-serif;
            font-size: 1.6rem;
            color: #ffffff;
            display: block;
            margin-top: 0.2rem;
        }
        .map-legend {
            display: flex;
            gap: 1rem;
            flex-wrap: wrap;
            align-items: center;
            font-size: 0.78rem;
            color: #cbd5e1;
            margin-top: 0.75rem;
            padding: 0.5rem 0.75rem;
            background: rgba(15, 23, 42, 0.6);
            border-radius: 6px;
        }
        .legend-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            display: inline-block;
            margin-right: 4px;
        }
        .modal-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(3, 8, 18, 0.8);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 2000;
            padding: 1rem;
        }
        .modal-card {
            background: var(--navy-900);
            border: 1px solid var(--sky-400);
            border-radius: 12px;
            width: 100%;
            max-width: 520px;
            padding: 1.4rem;
        }
        .chat-history {
            height: 240px;
            overflow-y: auto;
            background: var(--navy-950);
            border: 1px solid var(--border-glass);
            border-radius: 8px;
            padding: 0.85rem;
            margin: 0.85rem 0;
            display: flex;
            flex-direction: column;
            gap: 0.6rem;
        }
        .chat-bubble {
            padding: 0.6rem 0.85rem;
            border-radius: 8px;
            background: rgba(13, 33, 73, 0.8);
            font-size: 0.86rem;
            word-break: break-word;
        }
        .chat-bubble.mine {
            background: rgba(217, 72, 30, 0.28);
            border: 1px solid rgba(234, 88, 12, 0.45);
            align-self: flex-end;
        }
        @media (max-width: 980px) {
            .two-col-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-user-id="${user.userId}"
      data-user-lat="${user.latitude}"
      data-user-lng="${user.longitude}">

    <header class="dash-nav">
        <div class="dash-nav-inner">
            <a href="${pageContext.request.contextPath}/" class="brand-logo-wrap">
                <img src="${pageContext.request.contextPath}/assets/images/disasterManageLogo.png"
                     onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/disasterManageLogo.jpeg';"
                     alt="Logo" class="brand-logo-img" style="width: 40px; height: 40px;">
                <div>
                    <div style="font-family: 'Rajdhani', sans-serif; font-weight: 700; font-size: 1.15rem;">
                        RELIEF COMMAND CENTER
                    </div>
                    <div style="font-size: 0.68rem; color: #fdba74; letter-spacing: 0.12em;">
                        VERIFICATION &bull; ALLOCATION &bull; GEO-ALERTS
                    </div>
                </div>
            </a>

            <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/api/reports/export/pdf" class="btn-relief"
                   style="padding: 0.45rem 0.95rem; font-size: 0.82rem; text-decoration: none;"
                   title="Download Disaster Relief Coordination PDF Report">
                    &#128196; Download PDF Report
                </a>
                <a href="${pageContext.request.contextPath}/api/reports/export/csv" class="btn-outline-glass"
                   style="padding: 0.45rem 0.9rem; font-size: 0.82rem;">
                    &#11015; Export CSV
                </a>
                <button type="button" onclick="window.print()" class="btn-outline-glass"
                        style="padding: 0.45rem 0.9rem; font-size: 0.82rem;">
                    &#128424; Print View
                </button>
                <span class="user-pill">&#128737; <c:out value="${user.name}"/></span>
                <a href="${pageContext.request.contextPath}/logout" class="btn-relief"
                   style="padding: 0.45rem 0.95rem; font-size: 0.82rem;">
                    Logout
                </a>
            </div>
        </div>
    </header>

    <main class="dash-container">
        <!-- KPI Strip -->
        <section class="kpi-grid">
            <div class="kpi-card">
                <span>Total Reported Needs</span>
                <strong>${fn:length(allNeeds)}</strong>
            </div>
            <div class="kpi-card">
                <span>Awaiting Verification</span>
                <strong style="color: #fde047;">${fn:length(pendingNeeds)}</strong>
            </div>
            <div class="kpi-card">
                <span>Surplus Pledges Pool</span>
                <strong style="color: #fdba74;">${fn:length(surplusPledges)}</strong>
            </div>
            <div class="kpi-card">
                <span>Active Allocations</span>
                <strong style="color: #4ade80;">${fn:length(allocations)}</strong>
            </div>
        </section>

        <div id="cmdBanner" class="msg-banner"></div>

        <!-- 1. Pending Verification Queue -->
        <section class="panel-card">
            <h2>Pending Verification Queue</h2>
            <p class="panel-sub">Audit incoming requests, override urgency priority if needed, and approve or reject duplicates.</p>

            <c:choose>
                <c:when test="${empty pendingNeeds}">
                    <p style="color: var(--text-muted); text-align: center; padding: 1.25rem 0;">
                        No pending requests in queue. All incoming citizen reports have been reviewed.
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Category &amp; Description</th>
                                    <th>Qty</th>
                                    <th>Urgency Priority</th>
                                    <th>Officer Remarks</th>
                                    <th>Decision</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="n" items="${pendingNeeds}">
                                    <tr>
                                        <td><strong>#${n.needId}</strong></td>
                                        <td>
                                            <div style="font-weight: 700;"><c:out value="${n.category}"/></div>
                                            <div style="font-size: 0.82rem; color: var(--text-muted);"><c:out value="${n.description}"/></div>
                                        </td>
                                        <td><strong>${n.quantityRequired}</strong></td>
                                        <td>
                                            <select id="urgSelect_${n.needId}" class="dark-select" style="width: 120px;">
                                                <option value="CRITICAL" ${n.urgency == 'CRITICAL' ? 'selected' : ''}>CRITICAL</option>
                                                <option value="HIGH" ${n.urgency == 'HIGH' ? 'selected' : ''}>HIGH</option>
                                                <option value="MEDIUM" ${n.urgency == 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
                                                <option value="LOW" ${n.urgency == 'LOW' ? 'selected' : ''}>LOW</option>
                                            </select>
                                        </td>
                                        <td>
                                            <input type="text" id="remarks_${n.needId}" class="dark-input"
                                                   placeholder="Audit remarks..." style="min-width: 160px;">
                                        </td>
                                        <td>
                                            <div class="action-group" style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
                                                <button type="button" class="btn-sm btn-approve verify-btn"
                                                        data-need-id="${n.needId}" data-decision="APPROVED">Approve</button>
                                                <button type="button" class="btn-sm btn-dup verify-btn"
                                                        data-need-id="${n.needId}" data-decision="FLAGGED_DUPLICATE">Duplicate</button>
                                                <button type="button" class="btn-sm btn-reject verify-btn"
                                                        data-need-id="${n.needId}" data-decision="REJECTED">Reject</button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- 2. Analytics Chart & Surplus Reallocation -->
        <div class="two-col-grid">
            <section class="panel-card" style="margin-bottom: 0;">
                <h2>Supply &amp; Fulfillment Analytics</h2>
                <p class="panel-sub">Real-time breakdown of required, pledged, allocated, and remaining units.</p>
                <div class="chart-box">
                    <canvas id="stockChart"></canvas>
                </div>
            </section>

            <section class="panel-card" style="margin-bottom: 0;">
                <h2>Surplus Stock Reallocation</h2>
                <p class="panel-sub">Reassign unallocated donor pledge surplus to active verified needs.</p>

                <c:set var="hasOpenVerifiedNeed" value="false"/>
                <c:forEach var="chk" items="${allNeeds}">
                    <c:if test="${chk.quantityRemaining > 0 && (chk.status == 'VERIFIED' || chk.status == 'PARTIALLY_FULFILLED')}">
                        <c:set var="hasOpenVerifiedNeed" value="true"/>
                    </c:if>
                </c:forEach>

                <c:choose>
                    <c:when test="${empty surplusPledges}">
                        <p style="color: var(--text-muted); padding: 1.5rem 0; text-align: center;">
                            No unallocated surplus pledges available right now.
                        </p>
                    </c:when>
                    <c:when test="${not hasOpenVerifiedNeed}">
                        <p style="color: var(--text-muted); padding: 1.5rem 0; text-align: center;">
                            Surplus stock is available, but there are currently no open verified needs requiring units.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <form id="reallocateForm">
                            <div class="form-field">
                                <label for="pledgeId">Select Surplus Pledge</label>
                                <select id="pledgeId" class="dark-select" required>
                                    <c:forEach var="sp" items="${surplusPledges}">
                                        <option value="${sp.pledgeId}" data-max-surplus="${sp.quantitySurplus}">
                                            Pledge #${sp.pledgeId} (Surplus: ${sp.quantitySurplus} units available)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-field">
                                <label for="targetNeedId">Target Verified Need</label>
                                <select id="targetNeedId" class="dark-select" required>
                                    <c:forEach var="tn" items="${allNeeds}">
                                        <c:if test="${tn.quantityRemaining > 0 && (tn.status == 'VERIFIED' || tn.status == 'PARTIALLY_FULFILLED')}">
                                            <option value="${tn.needId}">
                                                Need #${tn.needId} &mdash; ${fn:escapeXml(tn.category)} (Needs ${tn.quantityRemaining})
                                            </option>
                                        </c:if>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-field">
                                <label for="reallocQty">Quantity to Reallocate</label>
                                <input type="number" id="reallocQty" class="dark-input" min="1" value="1" required>
                            </div>

                            <button type="submit" id="reallocSubmitBtn" class="btn-relief" style="width: 100%;">
                                Reallocate Surplus Stock &rarr;
                            </button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </section>
        </div>

        <!-- 3. Geospatial Command & Control Map -->
        <section class="panel-card">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 0.85rem;">
                <div>
                    <h2>Geospatial Command Map</h2>
                    <p class="panel-sub" style="margin-bottom: 0;">Real-time spatial visualization of active relief needs and department safety assessment perimeters.</p>
                </div>
                <div style="display: flex; gap: 0.75rem; align-items: center;">
                    <span id="mapLastUpdated" style="font-size: 0.78rem; color: var(--text-muted);">Polling active...</span>
                    <button type="button" id="refreshMapBtn" class="btn-outline-glass" style="padding: 0.35rem 0.8rem; font-size: 0.82rem;">
                        &#8635; Refresh Map
                    </button>
                </div>
            </div>

            <!-- Map Summary KPI Cards -->
            <div class="map-kpi-grid">
                <div class="map-kpi-card">
                    <span>Active Needs</span>
                    <strong id="mapMetricActiveNeeds">0</strong>
                </div>
                <div class="map-kpi-card">
                    <span>Critical People</span>
                    <strong id="mapMetricCritical" style="color: #fca5a5;">--</strong>
                </div>
                <div class="map-kpi-card">
                    <span>Est. Unique Affected</span>
                    <strong id="mapMetricUniqueAffected" style="color: #7dd3fc;">--</strong>
                </div>
                <div class="map-kpi-card">
                    <span>Unsafe Zones</span>
                    <strong id="mapMetricUnsafe" style="color: #ef4444;">0</strong>
                </div>
                <div class="map-kpi-card">
                    <span>Monitoring Zones</span>
                    <strong id="mapMetricMonitoring" style="color: #f59e0b;">0</strong>
                </div>
                <div class="map-kpi-card">
                    <span>Unassessed Needs</span>
                    <strong id="mapMetricUnknown" style="color: #94a3b8;">0</strong>
                </div>
            </div>

            <div id="commandMap"></div>

            <div class="map-legend">
                <span><strong>Legend:</strong></span>
                <span><span class="legend-dot" style="background: #ef4444;"></span> Critical Need</span>
                <span><span class="legend-dot" style="background: #f97316;"></span> High Urgency</span>
                <span><span class="legend-dot" style="background: #eab308;"></span> Medium Urgency</span>
                <span><span class="legend-dot" style="background: #3b82f6;"></span> Low Urgency</span>
                <span style="margin: 0 4px; color: #475569;">|</span>
                <span><span class="legend-dot" style="background: #dc2626; border: 1px solid #ef4444;"></span> Unsafe Zone</span>
                <span><span class="legend-dot" style="background: #d97706; border: 1px solid #f59e0b;"></span> Monitoring Zone</span>
                <span><span class="legend-dot" style="background: #059669; border: 1px solid #10b981;"></span> Safe Zone</span>
                <span style="margin-left: auto; font-style: italic; color: #94a3b8;">💡 Click anywhere on the map to auto-set assessment coordinates</span>
            </div>
        </section>

        <!-- 4. Area Safety Assessment & Geo-Alert Actions -->
        <div class="two-col-grid">
            <section class="panel-card" style="margin-bottom: 0;">
                <h2>Record Area Safety Assessment</h2>
                <p class="panel-sub">Evaluate district sectors as Safe, Monitoring, or Unsafe. Creates an immutable historical audit record.</p>

                <form id="safetyAssessmentForm">
                    <div class="form-field">
                        <label for="safetyDisasterId">Disaster Event</label>
                        <select id="safetyDisasterId" class="dark-select">
                            <c:forEach var="d" items="${disasters}">
                                <option value="${d.disasterId}">${fn:escapeXml(d.name)}</option>
                            </c:forEach>
                            <c:if test="${empty disasters}">
                                <option value="1">Coastal Monsoon Flood 2026</option>
                            </c:if>
                        </select>
                    </div>
                    <div class="form-field">
                        <label for="safetyAreaName">Area / Sector Name</label>
                        <input type="text" id="safetyAreaName" class="dark-input" placeholder="e.g. Velachery West Sector" required>
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.6rem; margin-bottom: 0.9rem;">
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Latitude</label>
                            <input type="number" step="any" id="safetyLat" class="dark-input"
                                   value="${user.latitude != 0 ? user.latitude : 13.0827}" required>
                        </div>
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Longitude</label>
                            <input type="number" step="any" id="safetyLng" class="dark-input"
                                   value="${user.longitude != 0 ? user.longitude : 80.2707}" required>
                        </div>
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Radius (km)</label>
                            <input type="number" step="any" id="safetyRadius" class="dark-input" min="0.1" max="100" value="3.0" required>
                        </div>
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.6rem; margin-bottom: 0.9rem;">
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Safety Status</label>
                            <select id="safetyStatus" class="dark-select" required>
                                <option value="MONITORING">MONITORING (Caution)</option>
                                <option value="UNSAFE">UNSAFE (Hazard / Inundated)</option>
                                <option value="SAFE">SAFE (Verified Clear)</option>
                            </select>
                        </div>
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Flood Depth (metres, opt.)</label>
                            <input type="number" step="0.01" min="0" max="30" id="safetyFloodDepth" class="dark-input" placeholder="e.g. 1.8">
                        </div>
                    </div>
                    <div class="form-field">
                        <label for="safetyNotes">Assessment Notes / Field Guidance</label>
                        <textarea id="safetyNotes" rows="2" class="dark-textarea" placeholder="Field report, breach status, shelter capacity, route access..."></textarea>
                    </div>
                    <button type="submit" id="safetySubmitBtn" class="btn-relief" style="width: 100%;">
                        &#128737; Save Area Safety Assessment
                    </button>
                </form>
            </section>

            <section class="panel-card" style="margin-bottom: 0;">
                <h2>Broadcast Radius Geo-Alert</h2>
                <p class="panel-sub">Send targeted emergency alerts to citizens and donors within a kilometer radius.</p>

                <form id="alertForm">
                    <div class="form-field">
                        <label for="alertDisasterId">Disaster Event</label>
                        <select id="alertDisasterId" class="dark-select">
                            <c:forEach var="d" items="${disasters}">
                                <option value="${d.disasterId}">${fn:escapeXml(d.name)}</option>
                            </c:forEach>
                            <c:if test="${empty disasters}">
                                <option value="1">Coastal Monsoon Flood 2026</option>
                            </c:if>
                        </select>
                    </div>
                    <div class="form-field">
                        <label for="alertTitle">Alert Headline</label>
                        <input type="text" id="alertTitle" class="dark-input" placeholder="e.g., Flash Flood Warning — Low-Lying Wards" required>
                    </div>
                    <div class="form-field">
                        <label for="alertMessage">Alert Instructions</label>
                        <textarea id="alertMessage" rows="2" class="dark-textarea" placeholder="Evacuation route, relief camp address, helpline..." required></textarea>
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.6rem; margin-bottom: 0.9rem;">
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Latitude</label>
                            <input type="number" step="any" id="alertLat" class="dark-input"
                                   value="${user.latitude != 0 ? user.latitude : 13.2150}" required>
                        </div>
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Longitude</label>
                            <input type="number" step="any" id="alertLng" class="dark-input"
                                   value="${user.longitude != 0 ? user.longitude : 74.9960}" required>
                        </div>
                        <div>
                            <label style="font-size: 0.76rem; color: #cbd5e1;">Radius (km)</label>
                            <input type="number" step="any" id="alertRadius" class="dark-input" min="0.5" value="15" required>
                        </div>
                    </div>
                    <button type="submit" id="alertSubmitBtn" class="btn-relief" style="width: 100%;">
                        &#128225; Broadcast Geo-Alert
                    </button>
                </form>
            </section>
        </div>

        <!-- 4. Resource Allocations & Delivery Tracking -->
        <section class="panel-card">
            <h2>Resource Allocations &amp; Delivery Status</h2>
            <p class="panel-sub">Update field dispatch status from Allocated to In-Transit and Delivered.</p>

            <c:choose>
                <c:when test="${empty allocations}">
                    <p style="color: var(--text-muted); text-align: center; padding: 1rem 0;">
                        No resource allocations recorded yet. Allocations appear here once donors pledge supplies.
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>Allocation ID</th>
                                    <th>Pledge ID</th>
                                    <th>Target Need</th>
                                    <th>Units Allocated</th>
                                    <th>Delivery Status</th>
                                    <th>Update Stage</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="al" items="${allocations}">
                                    <tr>
                                        <td><strong>#${al.allocationId}</strong></td>
                                        <td>Pledge #${al.pledgeId}</td>
                                        <td>Need #${al.needId}</td>
                                        <td><strong>${al.quantityAllocated}</strong></td>
                                        <td><span class="badge st-${al.status}">${al.status}</span></td>
                                        <td>
                                            <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
                                                <button type="button" class="btn-sm btn-transit alloc-status-btn"
                                                        data-alloc-id="${al.allocationId}" data-status="IN_TRANSIT">In Transit</button>
                                                <button type="button" class="btn-sm btn-approve alloc-status-btn"
                                                        data-alloc-id="${al.allocationId}" data-status="DELIVERED">Delivered</button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- 5. Master Needs Ledger -->
        <section class="panel-card">
            <h2>Master Needs Ledger</h2>
            <p class="panel-sub">Monitor all reported needs across stages and open direct coordination chats.</p>

            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>Need ID</th>
                            <th>Category</th>
                            <th>Description</th>
                            <th>Req / Alloc / Rem</th>
                            <th>Urgency</th>
                            <th>Status</th>
                            <th>Chat</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="n" items="${allNeeds}">
                            <tr class="need-map-row"
                                data-need-id="${n.needId}"
                                data-category="${fn:escapeXml(n.category)}"
                                data-status="${fn:escapeXml(n.status)}"
                                data-urgency="${fn:escapeXml(n.urgency)}"
                                data-lat="${n.latitude}"
                                data-lng="${n.longitude}">
                                <td><strong>#${n.needId}</strong></td>
                                <td><c:out value="${n.category}"/></td>
                                <td><c:out value="${n.description}"/></td>
                                <td>${n.quantityRequired} / <strong style="color:#4ade80;">${n.quantityAllocated}</strong> / <span style="color:#38bdf8;">${n.quantityRemaining}</span></td>
                                <td><span class="badge urg-${n.urgency}">${n.urgency}</span></td>
                                <td><span class="badge st-${n.status}">${n.status}</span></td>
                                <td>
                                    <button type="button" class="btn-outline-glass chat-open-btn"
                                            style="padding: 0.35rem 0.7rem; font-size: 0.78rem;"
                                            data-need-id="${n.needId}"
                                            data-need-title="${fn:escapeXml(n.category)}">
                                        &#128172; Chat
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>

        <!-- Hidden Geo-Alert Data Elements for Leaflet Circles -->
        <div id="alertMapData" style="display: none;">
            <c:forEach var="alertItem" items="${alerts}">
                <div class="alert-map-item"
                     data-title="${fn:escapeXml(alertItem.title)}"
                     data-radius="${alertItem.radiusKm}"
                     data-lat="${alertItem.latitude}"
                     data-lng="${alertItem.longitude}"></div>
            </c:forEach>
        </div>
    </main>

    <!-- Chat Modal -->
    <div id="chatModal" class="modal-backdrop">
        <div class="modal-card">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <h3 id="chatModalTitle" style="font-size: 1.25rem;">Command Coordination Chat</h3>
                <button type="button" id="closeChatBtn" class="btn-outline-glass" style="padding: 0.3rem 0.7rem;">&times;</button>
            </div>
            <div id="chatHistory" class="chat-history"></div>
            <form id="sendChatForm" style="display: flex; gap: 0.5rem;">
                <input type="text" id="chatInput" class="dark-input" placeholder="Send command update..." required>
                <button type="submit" id="sendChatBtn" class="btn-relief" style="padding: 0.65rem 1.1rem;">Send</button>
            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cursor-effects.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", () => {
            const bodyData = document.body.dataset;
            const contextPath = bodyData.contextPath || "";
            const currentUserId = Number(bodyData.userId || 0);
            const rawLat = parseFloat(bodyData.userLat);
            const rawLng = parseFloat(bodyData.userLng);
            const centerLat = (!isNaN(rawLat) && rawLat !== 0) ? rawLat : 13.2150;
            const centerLng = (!isNaN(rawLng) && rawLng !== 0) ? rawLng : 74.9960;

            const cmdBanner = document.getElementById("cmdBanner");

            function showCmdBanner(msg, isError) {
                cmdBanner.textContent = msg;
                cmdBanner.className = "msg-banner " + (isError ? "msg-err" : "msg-ok");
                cmdBanner.style.display = "block";
                cmdBanner.scrollIntoView({ behavior: "smooth", block: "nearest" });
            }

            // 1. Verify / Reject / Flag Duplicate Buttons
            document.querySelectorAll(".verify-btn").forEach((btn) => {
                btn.addEventListener("click", async () => {
                    const needId = btn.dataset.needId;
                    const decision = btn.dataset.decision;
                    const urgEl = document.getElementById("urgSelect_" + needId);
                    const remEl = document.getElementById("remarks_" + needId);
                    const urgency = urgEl ? urgEl.value : "MEDIUM";
                    const remarks = remEl ? remEl.value : "";

                    const rowButtons = btn.closest(".action-group").querySelectorAll("button");
                    rowButtons.forEach((b) => { b.disabled = true; });

                    try {
                        const res = await fetch(contextPath + "/api/needs/" + needId + "/verify", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: new URLSearchParams({
                                decision: decision,
                                urgency: urgency,
                                remarks: remarks
                            })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showCmdBanner("Need #" + needId + " marked as " + decision + ". Refreshing...", false);
                            setTimeout(() => window.location.reload(), 700);
                        } else {
                            showCmdBanner(data.error || "Verification update failed.", true);
                            rowButtons.forEach((b) => { b.disabled = false; });
                        }
                    } catch (err) {
                        showCmdBanner("Network error during verification.", true);
                        rowButtons.forEach((b) => { b.disabled = false; });
                    }
                });
            });

            // 2. Surplus Reallocation Form
            const reallocForm = document.getElementById("reallocateForm");
            const pledgeSelect = document.getElementById("pledgeId");
            const reallocQtyInput = document.getElementById("reallocQty");

            function syncMaxSurplus() {
                if (!pledgeSelect || !reallocQtyInput) return;
                const selectedOpt = pledgeSelect.options[pledgeSelect.selectedIndex];
                const maxSurplus = Number(selectedOpt ? selectedOpt.dataset.maxSurplus : 1);
                reallocQtyInput.max = maxSurplus;
                if (Number(reallocQtyInput.value) > maxSurplus) {
                    reallocQtyInput.value = maxSurplus;
                }
            }

            if (pledgeSelect) {
                syncMaxSurplus();
                pledgeSelect.addEventListener("change", syncMaxSurplus);
            }

            if (reallocForm) {
                reallocForm.addEventListener("submit", async (e) => {
                    e.preventDefault();
                    const submitBtn = document.getElementById("reallocSubmitBtn");
                    submitBtn.disabled = true;

                    try {
                        const res = await fetch(contextPath + "/api/allocations/reallocate", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: new URLSearchParams({
                                pledgeId: pledgeSelect.value,
                                targetNeedId: document.getElementById("targetNeedId").value,
                                quantity: reallocQtyInput.value
                            })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showCmdBanner("Surplus stock reallocated! Reloading...", false);
                            setTimeout(() => window.location.reload(), 700);
                        } else {
                            showCmdBanner(data.error || "Reallocation failed.", true);
                            submitBtn.disabled = false;
                        }
                    } catch (err) {
                        showCmdBanner("Network error during reallocation.", true);
                        submitBtn.disabled = false;
                    }
                });
            }

            // 3. Broadcast Geo-Alert Form
            const alertForm = document.getElementById("alertForm");
            if (alertForm) {
                alertForm.addEventListener("submit", async (e) => {
                    e.preventDefault();
                    const submitBtn = document.getElementById("alertSubmitBtn");
                    submitBtn.disabled = true;

                    try {
                        const res = await fetch(contextPath + "/api/alerts", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: new URLSearchParams({
                                disasterId: document.getElementById("alertDisasterId").value,
                                title: document.getElementById("alertTitle").value,
                                message: document.getElementById("alertMessage").value,
                                latitude: document.getElementById("alertLat").value,
                                longitude: document.getElementById("alertLng").value,
                                radiusKm: document.getElementById("alertRadius").value
                            })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showCmdBanner(data.message || "Geo-alert broadcasted!", false);
                            setTimeout(() => window.location.reload(), 750);
                        } else {
                            showCmdBanner(data.error || "Failed to broadcast alert.", true);
                            submitBtn.disabled = false;
                        }
                    } catch (err) {
                        showCmdBanner("Network error broadcasting alert.", true);
                        submitBtn.disabled = false;
                    }
                });
            }

            // 4. Allocation Delivery Status Buttons
            document.querySelectorAll(".alloc-status-btn").forEach((btn) => {
                btn.addEventListener("click", async () => {
                    const allocId = btn.dataset.allocId;
                    const newStatus = btn.dataset.status;
                    btn.disabled = true;

                    try {
                        const res = await fetch(contextPath + "/api/allocations/" + allocId + "/status", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: new URLSearchParams({ status: newStatus })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showCmdBanner("Allocation #" + allocId + " updated to " + newStatus + ".", false);
                            setTimeout(() => window.location.reload(), 650);
                        } else {
                            showCmdBanner(data.error || "Failed to update allocation status.", true);
                            btn.disabled = false;
                        }
                    } catch (err) {
                        showCmdBanner("Network error updating allocation status.", true);
                        btn.disabled = false;
                    }
                });
            });

            // 5. Analytics Chart
            async function renderAnalyticsChart() {
                const canvas = document.getElementById("stockChart");
                if (!canvas || typeof Chart === "undefined") return;

                try {
                    const res = await fetch(contextPath + "/api/reports/summary");
                    const data = await res.json();
                    if (!res.ok) return;

                    new Chart(canvas.getContext("2d"), {
                        type: "bar",
                        data: {
                            labels: ["Required", "Pledged", "Allocated", "Remaining"],
                            datasets: [{
                                label: "Resource Units",
                                data: [
                                    data.totalQuantityRequired || 0,
                                    data.totalQuantityPledged || 0,
                                    data.totalQuantityAllocated || 0,
                                    data.totalQuantityRemaining || 0
                                ],
                                backgroundColor: ["#38bdf8", "#fb923c", "#4ade80", "#f87171"],
                                borderRadius: 6
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: { legend: { display: false } },
                            scales: {
                                x: { ticks: { color: "#cbd5e1" }, grid: { color: "rgba(255,255,255,0.08)" } },
                                y: { beginAtZero: true, ticks: { color: "#cbd5e1" }, grid: { color: "rgba(255,255,255,0.08)" } }
                            }
                        }
                    });
                } catch (e) {
                    console.error("Chart load error:", e);
                }
            }
            renderAnalyticsChart();

            // 6. Interactive Geospatial Command Map with Auto-Polling & Safety Overlays
            if (typeof L !== "undefined" && document.getElementById("commandMap")) {
                const cmdMap = L.map("commandMap").setView([centerLat, centerLng], 11);
                L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
                    attribution: "&copy; OpenStreetMap contributors"
                }).addTo(cmdMap);

                // Dedicated LayerGroups for clean updates without duplication
                const needLayerGroup = L.layerGroup().addTo(cmdMap);
                const safetyLayerGroup = L.layerGroup().addTo(cmdMap);
                const alertLayerGroup = L.layerGroup().addTo(cmdMap);

                // Interactive map click to auto-fill latitude & longitude in forms
                cmdMap.on("click", (e) => {
                    const latVal = e.latlng.lat.toFixed(6);
                    const lngVal = e.latlng.lng.toFixed(6);
                    const safetyLat = document.getElementById("safetyLat");
                    const safetyLng = document.getElementById("safetyLng");
                    if (safetyLat && safetyLng) {
                        safetyLat.value = latVal;
                        safetyLng.value = lngVal;
                    }
                    const alertLat = document.getElementById("alertLat");
                    const alertLng = document.getElementById("alertLng");
                    if (alertLat && alertLng) {
                        alertLat.value = latVal;
                        alertLng.value = lngVal;
                    }
                });

                // Render previous Geo-Alert circles into alertLayerGroup
                document.querySelectorAll(".alert-map-item").forEach((item) => {
                    const lat = parseFloat(item.dataset.lat);
                    const lng = parseFloat(item.dataset.lng);
                    const radiusKm = parseFloat(item.dataset.radius) || 10;
                    if (!isNaN(lat) && !isNaN(lng) && (lat !== 0 || lng !== 0)) {
                        const popupDiv = document.createElement("div");
                        const titleEl = document.createElement("strong");
                        titleEl.textContent = "Broadcast Alert: " + item.dataset.title;
                        popupDiv.appendChild(titleEl);
                        popupDiv.appendChild(document.createElement("br"));
                        popupDiv.appendChild(document.createTextNode("Broadcast Radius: " + radiusKm + " km"));

                        L.circle([lat, lng], {
                            color: "#ef4444",
                            fillColor: "#dc2626",
                            fillOpacity: 0.14,
                            radius: radiusKm * 1000,
                            dashArray: "5, 5",
                            weight: 1.5
                        }).addTo(alertLayerGroup).bindPopup(popupDiv);
                    }
                });

                function makePopupRow(label, value, valueColor) {
                    const r = document.createElement("div");
                    r.style.margin = "2px 0";
                    const lbl = document.createElement("span");
                    lbl.style.fontWeight = "600";
                    lbl.textContent = label + ": ";
                    r.appendChild(lbl);
                    const val = document.createElement("span");
                    val.textContent = value;
                    if (valueColor) val.style.color = valueColor;
                    r.appendChild(val);
                    return r;
                }

                function createNeedPopup(need) {
                    const div = document.createElement("div");
                    div.style.minWidth = "230px";
                    div.style.fontSize = "0.85rem";
                    div.style.color = "#0f172a";
                    div.style.lineHeight = "1.35";

                    const header = document.createElement("strong");
                    header.textContent = "Need #" + need.needId + ": " + (need.category || "Relief Need");
                    header.style.display = "block";
                    header.style.fontSize = "0.95rem";
                    header.style.marginBottom = "4px";
                    header.style.color = "#0369a1";
                    div.appendChild(header);

                    if (need.description) {
                        const desc = document.createElement("div");
                        desc.style.fontSize = "0.78rem";
                        desc.style.color = "#475569";
                        desc.style.marginBottom = "6px";
                        desc.textContent = need.description;
                        div.appendChild(desc);
                    }

                    div.appendChild(makePopupRow("Urgency", need.urgency || "Unknown"));
                    div.appendChild(makePopupRow("Status", need.status || "Unknown"));
                    div.appendChild(makePopupRow("Location", need.locationName ? need.locationName : "Not reported"));

                    const affectedText = (need.affectedPeopleCount !== null && need.affectedPeopleCount !== undefined)
                            ? need.affectedPeopleCount + " people"
                            : "Not reported";
                    div.appendChild(makePopupRow("Affected Count", affectedText));

                    const criticalText = (need.criticalPeopleCount !== null && need.criticalPeopleCount !== undefined)
                            ? need.criticalPeopleCount + " people"
                            : "Not reported";
                    div.appendChild(makePopupRow("Critical Count", criticalText, "#b91c1c"));

                    const req = need.quantityRequired || 0;
                    const alloc = need.quantityAllocated || 0;
                    const rem = need.quantityRemaining !== undefined ? need.quantityRemaining : (req - alloc);
                    div.appendChild(makePopupRow("Req / Alloc / Rem", req + " / " + alloc + " / " + rem));

                    div.appendChild(makePopupRow("Disaster", (need.disasterName ? need.disasterName : "Event #" + (need.disasterId || 1))));

                    const updatedStr = need.createdAt ? need.createdAt.replace("T", " ").substring(0, 16) : "Recent";
                    div.appendChild(makePopupRow("Last Updated", updatedStr));

                    return div;
                }

                function createSafetyPopup(a) {
                    const div = document.createElement("div");
                    div.style.minWidth = "230px";
                    div.style.fontSize = "0.85rem";
                    div.style.color = "#0f172a";
                    div.style.lineHeight = "1.35";

                    const header = document.createElement("strong");
                    header.textContent = "Area: " + a.areaName;
                    header.style.display = "block";
                    header.style.fontSize = "0.95rem";
                    header.style.marginBottom = "4px";
                    header.style.color = "#0f172a";
                    div.appendChild(header);

                    let statusColor = "#16a34a";
                    if (a.safetyStatus === "UNSAFE") statusColor = "#dc2626";
                    else if (a.safetyStatus === "MONITORING") statusColor = "#d97706";

                    div.appendChild(makePopupRow("Safety Status", a.safetyStatus, statusColor));

                    const floodText = (a.floodLevelMeters !== null && a.floodLevelMeters !== undefined)
                            ? a.floodLevelMeters + " m"
                            : "Not reported";
                    div.appendChild(makePopupRow("Flood Depth", floodText));

                    div.appendChild(makePopupRow("Radius", (a.radiusKm || 0) + " km"));
                    div.appendChild(makePopupRow("Assessed By", a.departmentOfficerName || "Department Officer"));

                    const timeStr = a.assessedAt ? a.assessedAt.replace("T", " ").substring(0, 16) : "Recent";
                    div.appendChild(makePopupRow("Assessed At", timeStr));

                    if (a.assessmentNotes) {
                        div.appendChild(makePopupRow("Notes", a.assessmentNotes));
                    }

                    return div;
                }

                function renderNeedMarkers(needs) {
                    needLayerGroup.clearLayers();
                    const urgencyStyles = {
                        "CRITICAL": { color: "#ffffff", fillColor: "#ef4444", radius: 9 },
                        "HIGH": { color: "#ffffff", fillColor: "#f97316", radius: 8 },
                        "MEDIUM": { color: "#ffffff", fillColor: "#eab308", radius: 7 },
                        "LOW": { color: "#ffffff", fillColor: "#3b82f6", radius: 6 }
                    };

                    needs.forEach((need) => {
                        const lat = parseFloat(need.latitude);
                        const lng = parseFloat(need.longitude);
                        if (!isNaN(lat) && !isNaN(lng) && (lat !== 0 || lng !== 0)) {
                            const style = urgencyStyles[need.urgency] || urgencyStyles["MEDIUM"];
                            const marker = L.circleMarker([lat, lng], {
                                radius: style.radius,
                                color: style.color,
                                fillColor: style.fillColor,
                                fillOpacity: 0.9,
                                weight: 2
                            });
                            marker.bindPopup(createNeedPopup(need));
                            needLayerGroup.addLayer(marker);
                        }
                    });
                }

                function renderSafetyCircles(assessments) {
                    safetyLayerGroup.clearLayers();
                    assessments.forEach((a) => {
                        if (a.safetyStatus === "UNKNOWN") return; // Never render circle for UNKNOWN

                        const lat = parseFloat(a.latitude);
                        const lng = parseFloat(a.longitude);
                        const radiusMeters = (parseFloat(a.radiusKm) || 1) * 1000;

                        if (isNaN(lat) || isNaN(lng) || (lat === 0 && lng === 0)) return;

                        let strokeColor = "#10b981";
                        let fillColor = "#059669";
                        let fillOpacity = 0.18;

                        if (a.safetyStatus === "UNSAFE") {
                            strokeColor = "#ef4444";
                            fillColor = "#dc2626";
                            fillOpacity = 0.22;
                        } else if (a.safetyStatus === "MONITORING") {
                            strokeColor = "#f59e0b";
                            fillColor = "#d97706";
                            fillOpacity = 0.18;
                        }

                        const circle = L.circle([lat, lng], {
                            radius: radiusMeters,
                            color: strokeColor,
                            fillColor: fillColor,
                            fillOpacity: fillOpacity,
                            weight: 2
                        });
                        circle.bindPopup(createSafetyPopup(a));
                        safetyLayerGroup.addLayer(circle);
                    });
                }

                function updateMapSummaryCards(metrics) {
                    if (!metrics) return;
                    const elActive = document.getElementById("mapMetricActiveNeeds");
                    if (elActive) elActive.textContent = metrics.totalActiveNeeds ?? 0;

                    const elCrit = document.getElementById("mapMetricCritical");
                    if (elCrit) {
                        elCrit.textContent = (metrics.reportedCriticalPeople !== null && metrics.reportedCriticalPeople !== undefined)
                                ? metrics.reportedCriticalPeople
                                : "Not reported";
                    }

                    const elAff = document.getElementById("mapMetricUniqueAffected");
                    if (elAff) {
                        elAff.textContent = (metrics.estimatedUniqueAffectedPeople !== null && metrics.estimatedUniqueAffectedPeople !== undefined)
                                ? metrics.estimatedUniqueAffectedPeople + " est."
                                : "Not reported";
                    }

                    const elUnsafe = document.getElementById("mapMetricUnsafe");
                    if (elUnsafe) elUnsafe.textContent = metrics.unsafeAreasCount ?? 0;

                    const elMon = document.getElementById("mapMetricMonitoring");
                    if (elMon) elMon.textContent = metrics.monitoringAreasCount ?? 0;

                    const elUnk = document.getElementById("mapMetricUnknown");
                    if (elUnk) elUnk.textContent = metrics.unknownSafetyNeedsCount ?? 0;
                }

                async function fetchMapDataAndOverlays() {
                    try {
                        const disasterSelect = document.getElementById("safetyDisasterId");
                        const disasterId = disasterSelect ? (disasterSelect.value || 1) : 1;

                        const [needsRes, safetyRes, metricsRes] = await Promise.all([
                            fetch(contextPath + "/api/needs"),
                            fetch(contextPath + "/api/safety-assessments?disasterId=" + disasterId),
                            fetch(contextPath + "/api/safety-assessments/metrics?disasterId=" + disasterId)
                        ]);

                        if (needsRes.ok) {
                            const needsData = await needsRes.json();
                            const needs = Array.isArray(needsData) ? needsData : (needsData.needs || []);
                            renderNeedMarkers(needs);
                        }

                        if (safetyRes.ok) {
                            const safetyData = await safetyRes.json();
                            const assessments = safetyData.assessments || [];
                            renderSafetyCircles(assessments);
                        }

                        if (metricsRes.ok) {
                            const metrics = await metricsRes.json();
                            updateMapSummaryCards(metrics);
                        }

                        const now = new Date();
                        const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
                        const updatedEl = document.getElementById("mapLastUpdated");
                        if (updatedEl) {
                            updatedEl.textContent = "Live — Polled at " + timeStr;
                        }
                    } catch (err) {
                        console.warn("Command map polling error:", err);
                    }
                }

                // Initial fetch
                fetchMapDataAndOverlays();

                // Periodic REST polling every 12 seconds
                const mapPollTimer = setInterval(fetchMapDataAndOverlays, 12000);

                // Refresh button listener
                const refreshBtn = document.getElementById("refreshMapBtn");
                if (refreshBtn) {
                    refreshBtn.addEventListener("click", () => {
                        fetchMapDataAndOverlays();
                        showCmdBanner("Command map refreshed.", false);
                    });
                }

                // Disaster selector change listener
                const disasterSelect = document.getElementById("safetyDisasterId");
                if (disasterSelect) {
                    disasterSelect.addEventListener("change", () => {
                        fetchMapDataAndOverlays();
                    });
                }

                // Area Safety Assessment Form Submission Handler
                const safetyForm = document.getElementById("safetyAssessmentForm");
                if (safetyForm) {
                    safetyForm.addEventListener("submit", async (e) => {
                        e.preventDefault();
                        const submitBtn = document.getElementById("safetySubmitBtn");
                        submitBtn.disabled = true;

                        try {
                            const disasterIdVal = document.getElementById("safetyDisasterId").value;
                            const areaNameVal = document.getElementById("safetyAreaName").value;
                            const latVal = document.getElementById("safetyLat").value;
                            const lngVal = document.getElementById("safetyLng").value;
                            const radiusVal = document.getElementById("safetyRadius").value;
                            const statusVal = document.getElementById("safetyStatus").value;
                            const floodVal = document.getElementById("safetyFloodDepth").value;
                            const notesVal = document.getElementById("safetyNotes").value;

                            const params = new URLSearchParams({
                                disasterId: disasterIdVal,
                                areaName: areaNameVal,
                                latitude: latVal,
                                longitude: lngVal,
                                radiusKm: radiusVal,
                                safetyStatus: statusVal
                            });
                            if (floodVal !== "") {
                                params.append("floodLevelMeters", floodVal);
                            }
                            if (notesVal !== "") {
                                params.append("assessmentNotes", notesVal);
                            }

                            const res = await fetch(contextPath + "/api/safety-assessments", {
                                method: "POST",
                                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                                body: params
                            });
                            const data = await res.json();
                            if (res.ok) {
                                showCmdBanner("Area safety assessment for '" + areaNameVal + "' recorded successfully!", false);
                                safetyForm.reset();
                                fetchMapDataAndOverlays();
                            } else {
                                showCmdBanner(data.message || data.error || "Failed to record safety assessment.", true);
                            }
                        } catch (err) {
                            showCmdBanner("Network error submitting safety assessment.", true);
                        } finally {
                            submitBtn.disabled = false;
                        }
                    });
                }
            }

            // 7. Coordination Chat Modal
            let activeNeedId = null;
            let chatPollTimer = null;
            const chatModal = document.getElementById("chatModal");
            const chatHistory = document.getElementById("chatHistory");

            function renderMessages(messages) {
                chatHistory.innerHTML = "";
                if (!messages || messages.length === 0) {
                    const emptyEl = document.createElement("div");
                    emptyEl.style.color = "#94a3b8";
                    emptyEl.style.textAlign = "center";
                    emptyEl.style.padding = "1.5rem 0";
                    emptyEl.textContent = "No messages on this need yet.";
                    chatHistory.appendChild(emptyEl);
                    return;
                }
                messages.forEach((m) => {
                    const bubble = document.createElement("div");
                    bubble.className = "chat-bubble" + (m.senderId === currentUserId ? " mine" : "");

                    const headerEl = document.createElement("div");
                    headerEl.style.fontSize = "0.72rem";
                    headerEl.style.marginBottom = "0.2rem";
                    headerEl.style.display = "flex";
                    headerEl.style.justifyContent = "space-between";
                    headerEl.style.gap = "0.5rem";

                    const senderEl = document.createElement("strong");
                    senderEl.textContent = (m.senderId === currentUserId) ? "You" : (m.senderName || "User");
                    
                    const senderWrap = document.createElement("div");
                    senderWrap.style.display = "flex";
                    senderWrap.style.alignItems = "center";
                    senderWrap.style.gap = "0.35rem";
                    senderWrap.appendChild(senderEl);

                    if (m.messageType === "BROADCAST") {
                        const bBadge = document.createElement("span");
                        bBadge.textContent = "BROADCAST";
                        bBadge.style.cssText = "font-size:0.6rem; background:rgba(234,179,8,0.2); color:#fde047; padding:1px 5px; border-radius:4px; font-weight:600; border:1px solid rgba(234,179,8,0.35);";
                        senderWrap.appendChild(bBadge);
                    }
                    headerEl.appendChild(senderWrap);

                    if (m.sentAt) {
                        const timeEl = document.createElement("span");
                        timeEl.style.color = "#94a3b8";
                        timeEl.style.fontSize = "0.68rem";
                        try {
                            const d = new Date(m.sentAt);
                            timeEl.textContent = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                        } catch (e) {}
                        headerEl.appendChild(timeEl);
                    }
                    bubble.appendChild(headerEl);

                    const contentEl = document.createElement("div");
                    contentEl.textContent = m.content || "";
                    bubble.appendChild(contentEl);

                    chatHistory.appendChild(bubble);
                });
                chatHistory.scrollTop = chatHistory.scrollHeight;
            }

            async function loadMessages(needId, isPolling = false) {
                if (!needId) return;
                if (!isPolling && chatHistory.children.length === 0) {
                    chatHistory.innerHTML = "<div style='color:#94a3b8; text-align:center; padding:1.5rem 0;'>Loading messages...</div>";
                }
                try {
                    const res = await fetch(contextPath + "/api/messages/" + needId, {
                        headers: { "X-Expected-User-Id": currentUserId }
                    });
                    if (!res.ok) {
                        if (res.status === 401 || res.status === 403 || res.status === 409) {
                            stopChatPolling();
                            const reason = res.status === 401
                                ? "Session expired. Please log in again."
                                : res.status === 409
                                ? "Session user mismatch. Please refresh your page."
                                : "You are not authorized to view messages for this need.";
                            chatHistory.innerHTML = "<div style='color:#fca5a5; text-align:center; padding:1rem 0; font-size:0.85rem;'>" + reason + "</div>";
                            return;
                        }
                        if (!isPolling) {
                            chatHistory.innerHTML = "<div style='color:#fca5a5; text-align:center;'>Unable to load messages (Status " + res.status + ").</div>";
                        }
                        return;
                    }
                    const data = await res.json();
                    renderMessages(data.messages || []);
                } catch (err) {
                    if (!isPolling) {
                        chatHistory.innerHTML = "<div style='color:#fca5a5; text-align:center;'>Network error loading messages.</div>";
                    }
                }
            }

            function startChatPolling(needId) {
                stopChatPolling();
                chatPollTimer = setInterval(() => {
                    if (activeNeedId && chatModal.style.display !== "none") {
                        loadMessages(activeNeedId, true);
                    } else {
                        stopChatPolling();
                    }
                }, 2500);
            }

            function stopChatPolling() {
                if (chatPollTimer) {
                    clearInterval(chatPollTimer);
                    chatPollTimer = null;
                }
            }

            function closeChatModal() {
                chatModal.style.display = "none";
                stopChatPolling();
                activeNeedId = null;
            }

            document.querySelectorAll(".chat-open-btn").forEach((btn) => {
                btn.addEventListener("click", () => {
                    activeNeedId = btn.dataset.needId;
                    document.getElementById("chatModalTitle").textContent =
                        "Need #" + activeNeedId + " — " + btn.dataset.needTitle;
                    chatModal.style.display = "flex";
                    loadMessages(activeNeedId, false);
                    startChatPolling(activeNeedId);
                    setTimeout(() => {
                        const input = document.getElementById("chatInput");
                        if (input) input.focus();
                    }, 100);
                });
            });

            document.getElementById("closeChatBtn").addEventListener("click", closeChatModal);
            chatModal.addEventListener("click", (e) => {
                if (e.target === chatModal) closeChatModal();
            });
            document.addEventListener("keydown", (e) => {
                if (e.key === "Escape" && chatModal.style.display === "flex") closeChatModal();
            });

            document.getElementById("sendChatForm").addEventListener("submit", async (e) => {
                e.preventDefault();
                if (!activeNeedId) return;
                const input = document.getElementById("chatInput");
                const sendBtn = document.getElementById("sendChatBtn");
                const text = input.value.trim();
                if (!text) return;

                sendBtn.disabled = true;
                try {
                    const res = await fetch(contextPath + "/api/messages", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded",
                            "X-Expected-User-Id": currentUserId
                        },
                        body: new URLSearchParams({ needId: activeNeedId, content: text, messageType: "BROADCAST" })
                    });
                    const data = await res.json();
                    if (res.ok) {
                        input.value = "";
                        await loadMessages(activeNeedId, false);
                    } else {
                        if (res.status === 401 || res.status === 403 || res.status === 409) {
                            stopChatPolling();
                        }
                        alert(data.error || ("Failed to send message (Status " + res.status + ")."));
                    }
                } catch (err) {
                    alert("Network error sending message.");
                } finally {
                    sendBtn.disabled = false;
                    input.focus();
                }
            });
        });
    </script>
</body>
</html>