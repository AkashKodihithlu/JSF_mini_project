<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Donor Coordination Portal | Disaster Relief Coordination System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
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
            max-width: 1320px;
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
            background: rgba(13, 33, 73, 0.85);
            border: 1px solid rgba(56, 189, 248, 0.35);
            font-size: 0.84rem;
            color: #e0f2fe;
        }
        .dash-container {
            max-width: 1320px;
            margin: 0 auto;
            padding: 2rem 4% 4rem;
        }
        .alert-strip {
            background: linear-gradient(90deg, rgba(153, 27, 27, 0.88), rgba(180, 83, 9, 0.85));
            border: 1px solid #fca5a5;
            border-radius: 10px;
            padding: 1rem 1.25rem;
            margin-bottom: 1.75rem;
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
            font-size: 0.86rem;
            color: var(--text-muted);
            margin-bottom: 1.25rem;
        }

        /* Horizontal Connection Timeline Cards */
        .timeline-list {
            display: flex;
            flex-direction: column;
            gap: 1.15rem;
        }
        .conn-card {
            background: var(--navy-950);
            border: 1px solid var(--border-glass);
            border-radius: 12px;
            padding: 1.25rem 1.4rem;
            transition: border-color 0.2s ease;
        }
        .conn-card.is-confirmed {
            border-color: rgba(74, 222, 128, 0.45);
        }
        .conn-card.is-cancelled {
            border-color: rgba(248, 113, 113, 0.45);
            background: rgba(15, 23, 42, 0.75);
        }
        .conn-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 1rem;
            flex-wrap: wrap;
            margin-bottom: 1.1rem;
        }
        .conn-meta {
            display: flex;
            align-items: center;
            gap: 0.55rem;
            flex-wrap: wrap;
            font-size: 0.82rem;
            color: var(--text-muted);
            margin-top: 0.3rem;
        }
        .htl-track {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            position: relative;
            margin: 1.15rem 0 0.85rem;
            padding: 0 0.5rem;
        }
        .htl-step {
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            z-index: 1;
        }
        .htl-step:not(:last-child)::after {
            content: "";
            position: absolute;
            top: 15px;
            left: 50%;
            width: 100%;
            height: 3px;
            background: rgba(148, 163, 184, 0.22);
            z-index: -1;
        }
        .htl-step.done:not(:last-child)::after {
            background: linear-gradient(90deg, #38bdf8, #4ade80);
        }
        .htl-step.active:not(:last-child)::after {
            background: linear-gradient(90deg, #38bdf8 35%, rgba(148, 163, 184, 0.22) 100%);
        }
        .htl-dot {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.82rem;
            font-weight: 700;
            background: #0f172a;
            border: 2px solid rgba(148, 163, 184, 0.35);
            color: #94a3b8;
            margin-bottom: 0.45rem;
        }
        .htl-step.done .htl-dot {
            background: rgba(22, 163, 74, 0.25);
            border-color: #4ade80;
            color: #4ade80;
        }
        .htl-step.active .htl-dot {
            background: rgba(14, 165, 233, 0.25);
            border-color: #38bdf8;
            color: #e0f2fe;
            box-shadow: 0 0 0 4px rgba(56, 189, 248, 0.18);
        }
        .htl-label {
            font-size: 0.8rem;
            font-weight: 700;
            color: #cbd5e1;
        }
        .htl-step.done .htl-label { color: #86efac; }
        .htl-step.active .htl-label { color: #38bdf8; }
        .htl-sub {
            font-size: 0.71rem;
            color: #94a3b8;
            margin-top: 0.15rem;
            max-width: 150px;
        }
        .cancel-callout {
            background: rgba(153, 27, 27, 0.22);
            border: 1px solid rgba(248, 113, 113, 0.45);
            border-radius: 8px;
            padding: 0.75rem 1rem;
            margin: 0.65rem 0;
            font-size: 0.84rem;
            color: #fecaca;
        }
        .conn-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 0.75rem;
            flex-wrap: wrap;
            padding-top: 0.85rem;
            border-top: 1px solid rgba(255, 255, 255, 0.08);
            margin-top: 0.65rem;
        }
        .step-advance-link {
            background: rgba(14, 165, 233, 0.15);
            border: 1px solid rgba(56, 189, 248, 0.4);
            color: #7dd3fc;
            padding: 0.42rem 0.85rem;
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .step-advance-link:hover {
            background: rgba(14, 165, 233, 0.28);
            color: #ffffff;
        }
        .cancel-conn-btn {
            background: rgba(220, 38, 38, 0.15);
            border: 1px solid rgba(248, 113, 113, 0.45);
            color: #fca5a5;
            padding: 0.42rem 0.85rem;
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .cancel-conn-btn:hover {
            background: rgba(220, 38, 38, 0.3);
            color: #ffffff;
        }

        /* Matched Needs Grid */
        .matches-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
            gap: 1.25rem;
        }
        .match-card {
            background: var(--navy-950);
            border: 1px solid var(--border-glass);
            border-radius: 10px;
            padding: 1.25rem;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            transition: transform 0.25s ease, border-color 0.25s ease;
        }
        .match-card:hover {
            transform: translateY(-4px);
            border-color: var(--sky-400);
        }
        .ai-brief-box {
            background: rgba(14, 165, 233, 0.1);
            border-left: 3px solid #38bdf8;
            padding: 0.5rem 0.7rem;
            border-radius: 4px;
            font-size: 0.79rem;
            color: #bae6fd;
            margin-bottom: 0.85rem;
        }
        .badge {
            display: inline-block;
            padding: 0.22rem 0.6rem;
            border-radius: 999px;
            font-size: 0.72rem;
            font-weight: 700;
            text-transform: uppercase;
        }
        .urg-CRITICAL { background: rgba(220, 38, 38, 0.25); color: #fca5a5; border: 1px solid #f87171; }
        .urg-HIGH { background: rgba(234, 88, 12, 0.25); color: #fdba74; border: 1px solid #fb923c; }
        .urg-MEDIUM { background: rgba(37, 99, 235, 0.25); color: #93c5fd; border: 1px solid #60a5fa; }
        .urg-LOW { background: rgba(100, 116, 139, 0.25); color: #cbd5e1; }
        .score-pill {
            background: rgba(14, 165, 233, 0.2);
            border: 1px solid rgba(56, 189, 248, 0.45);
            color: #7dd3fc;
        }
        .pledge-row {
            display: flex;
            gap: 0.5rem;
            margin-top: 1rem;
        }
        .qty-input {
            width: 100px;
            padding: 0.55rem 0.7rem;
            border-radius: 5px;
            border: 1px solid rgba(255, 255, 255, 0.25);
            background: var(--navy-900);
            color: #ffffff;
            font-weight: 700;
        }
        .msg-banner {
            padding: 0.85rem 1.1rem;
            border-radius: 8px;
            font-size: 0.88rem;
            margin-bottom: 1.25rem;
            display: none;
            line-height: 1.45;
        }
        .msg-ok { background: rgba(22, 101, 52, 0.92); border: 1px solid #86efac; color: #dcfce7; }
        .msg-err { background: rgba(153, 27, 27, 0.92); border: 1px solid #fca5a5; color: #ffffff; }
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
        th, td {
            text-align: left;
            padding: 0.85rem 0.75rem;
            border-bottom: 1px solid var(--border-glass);
        }
        th {
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 0.08em;
            color: var(--text-muted);
        }
        .modal-backdrop {
            position: fixed;
            inset: 0;
            background: rgba(3, 8, 18, 0.82);
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
            height: 250px;
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
        .dark-input {
            width: 100%;
            padding: 0.65rem 0.85rem;
            border-radius: 6px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            background: var(--navy-950);
            color: #ffffff;
            font-family: inherit;
        }
        @media (max-width: 640px) {
            .htl-track {
                grid-template-columns: 1fr;
                gap: 0.85rem;
            }
            .htl-step:not(:last-child)::after {
                display: none;
            }
            .htl-step {
                flex-direction: row;
                text-align: left;
                gap: 0.75rem;
            }
            .htl-sub { max-width: none; }
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}"
      data-user-id="${user.userId}">

    <header class="dash-nav">
        <div class="dash-nav-inner">
            <a href="${pageContext.request.contextPath}/" class="brand-logo-wrap">
                <img src="${pageContext.request.contextPath}/assets/images/disasterManageLogo.png"
                     onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/disasterManageLogo.jpeg';"
                     alt="Logo" class="brand-logo-img" style="width: 40px; height: 40px;">
                <div>
                    <div style="font-family: 'Rajdhani', sans-serif; font-weight: 700; font-size: 1.15rem;">
                        DONOR COORDINATION PORTAL
                    </div>
                    <div style="font-size: 0.68rem; color: #7dd3fc; letter-spacing: 0.12em;">
                        SMART RESOURCE MATCHING &amp; LIVE TIMELINE
                    </div>
                </div>
            </a>

            <div style="display: flex; align-items: center; gap: 0.85rem; flex-wrap: wrap;">
                <span class="user-pill">
                    &#129309; <c:out value="${user.name}"/> &bull; GPS (${user.latitude}, ${user.longitude})
                </span>
                <a href="${pageContext.request.contextPath}/logout" class="btn-outline-glass" style="padding: 0.45rem 1rem; font-size: 0.84rem;">
                    Logout
                </a>
            </div>
        </div>
    </header>

    <main class="dash-container">
        <c:if test="${not empty alerts}">
            <div class="alert-strip">
                <h3 style="font-size: 1.05rem; margin-bottom: 0.35rem;">&#128225; Active Zone Alerts</h3>
                <c:forEach var="a" items="${alerts}">
                    <div style="font-size: 0.86rem; color: #fef2f2;">
                        <strong><c:out value="${a.title}"/>:</strong> <c:out value="${a.message}"/>
                    </div>
                </c:forEach>
            </div>
        </c:if>

        <!-- KPI Strip -->
        <c:set var="totalOffered" value="0"/>
        <c:set var="totalAlloc" value="0"/>
        <c:forEach var="pl" items="${myPledges}">
            <c:if test="${pl.status != 'CANCELLED'}">
                <c:set var="totalOffered" value="${totalOffered + pl.quantityOffered}"/>
                <c:set var="totalAlloc" value="${totalAlloc + pl.quantityAllocated}"/>
            </c:if>
        </c:forEach>

        <section class="kpi-grid">
            <div class="kpi-card">
                <span>Verified Needs Available</span>
                <strong style="color: #38bdf8;">${fn:length(matchedNeeds)}</strong>
            </div>
            <div class="kpi-card">
                <span>Active Connections</span>
                <strong>${fn:length(myAllocations)}</strong>
            </div>
            <div class="kpi-card">
                <span>Active Units Pledged</span>
                <strong style="color: #fdba74;">${totalOffered}</strong>
            </div>
            <div class="kpi-card">
                <span>Units Connected to Citizens</span>
                <strong style="color: #4ade80;">${totalAlloc}</strong>
            </div>
        </section>

        <div id="donorBanner" class="msg-banner"></div>

        <!-- Horizontal Delivery Timelines (Donor <-> Affected Person Connections) -->
        <section class="panel-card">
            <h2>Active Citizen Connections &amp; Delivery Timelines</h2>
            <p class="panel-sub">
                Live horizontal progress synchronized with the affected citizen and Relief Department. Final completion locks once the recipient confirms receipt.
            </p>

            <c:choose>
                <c:when test="${empty myAllocations}">
                    <p style="color: var(--text-muted); text-align: center; padding: 1.25rem 0;">
                        No active citizen connections yet. Pledge supplies to any verified request below to start a live delivery timeline.
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="timeline-list">
                        <c:forEach var="al" items="${myAllocations}">
                            <c:set var="stepNum" value="1"/>
                            <c:if test="${al.status == 'IN_TRANSIT'}"><c:set var="stepNum" value="2"/></c:if>
                            <c:if test="${al.status == 'DELIVERED'}"><c:set var="stepNum" value="3"/></c:if>
                            <c:if test="${al.status == 'CONFIRMED'}"><c:set var="stepNum" value="4"/></c:if>
                            <c:if test="${al.status == 'CANCELLED'}"><c:set var="stepNum" value="-1"/></c:if>

                            <div class="conn-card ${al.status == 'CONFIRMED' ? 'is-confirmed' : (al.status == 'CANCELLED' ? 'is-cancelled' : '')}">
                                <div class="conn-header">
                                    <div>
                                        <div style="display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap;">
                                            <strong style="font-size: 1.1rem; color: #ffffff;">
                                                Connection #${al.allocationId} &bull; <c:out value="${al.needCategory}"/>
                                            </strong>
                                            <span class="badge score-pill">${al.quantityAllocated} Units</span>
                                            <c:if test="${not empty al.needUrgency}">
                                                <span class="badge urg-${al.needUrgency}">${al.needUrgency}</span>
                                            </c:if>
                                        </div>
                                        <div class="conn-meta">
                                            <span>Recipient: <strong style="color: #e2e8f0;"><c:out value="${not empty al.affectedPersonName ? al.affectedPersonName : 'Citizen'}"/></strong> (Need #${al.needId})</span>
                                            <c:if test="${not empty al.allocatedAt}">
                                                <span>&bull; Connected: <fmt:formatDate value="${al.allocatedAt}" pattern="dd MMM, HH:mm"/></span>
                                            </c:if>
                                        </div>
                                    </div>

                                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                                        <button type="button" class="btn-outline-glass chat-open-btn"
                                                style="padding: 0.42rem 0.85rem; font-size: 0.8rem;"
                                                data-need-id="${al.needId}"
                                                data-need-title="${fn:escapeXml(al.needCategory)}">
                                            &#128172; Coordinate Chat
                                        </button>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${al.status == 'CANCELLED'}">
                                        <div class="cancel-callout">
                                            <strong>&#10006; Cancelled by Donor</strong>
                                            <c:if test="${not empty al.cancelledAt}">
                                                (<fmt:formatDate value="${al.cancelledAt}" pattern="dd MMM, HH:mm"/>)
                                            </c:if>
                                            <div style="margin-top: 0.3rem; color: #fee2e2;">
                                                <strong>Reason:</strong> &ldquo;<c:out value="${not empty al.cancellationReason ? al.cancellationReason : 'Cancelled by donor'}"/>&rdquo;
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- 4-Stage Horizontal Stepper -->
                                        <div class="htl-track">
                                            <div class="htl-step ${stepNum > 1 ? 'done' : (stepNum == 1 ? 'active' : '')}">
                                                <div class="htl-dot">${stepNum > 1 ? '&#10003;' : '1'}</div>
                                                <div class="htl-label">Connected</div>
                                                <div class="htl-sub">Supplies committed &amp; matched</div>
                                            </div>

                                            <div class="htl-step ${stepNum > 2 ? 'done' : (stepNum == 2 ? 'active' : '')}">
                                                <div class="htl-dot">${stepNum > 2 ? '&#10003;' : '2'}</div>
                                                <div class="htl-label">In Transit</div>
                                                <div class="htl-sub">Dispatched toward recipient</div>
                                            </div>

                                            <div class="htl-step ${stepNum > 3 ? 'done' : (stepNum == 3 ? 'active' : '')}">
                                                <div class="htl-dot">${stepNum > 3 ? '&#10003;' : '3'}</div>
                                                <div class="htl-label">At Drop-Off</div>
                                                <div class="htl-sub">Awaiting citizen confirmation</div>
                                            </div>

                                            <div class="htl-step ${stepNum == 4 ? 'done' : ''}">
                                                <div class="htl-dot">${stepNum == 4 ? '&#10003;' : '4'}</div>
                                                <div class="htl-label">Need Reached</div>
                                                <div class="htl-sub">
                                                    <c:choose>
                                                        <c:when test="${stepNum == 4}">
                                                            Confirmed by citizen
                                                            <c:if test="${not empty al.confirmedAt}">
                                                                (<fmt:formatDate value="${al.confirmedAt}" pattern="dd MMM, HH:mm"/>)
                                                            </c:if>
                                                        </c:when>
                                                        <c:otherwise>Recipient confirms receipt</c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <div class="conn-footer">
                                    <div style="font-size: 0.8rem; color: #94a3b8;">
                                        <c:choose>
                                            <c:when test="${al.status == 'ALLOCATED'}">
                                                Next step: Dispatch supplies and update timeline progress.
                                            </c:when>
                                            <c:when test="${al.status == 'IN_TRANSIT'}">
                                                Supplies are on the way. Mark when delivered at drop-off point.
                                            </c:when>
                                            <c:when test="${al.status == 'DELIVERED'}">
                                                Waiting for <strong style="color: #e2e8f0;"><c:out value="${al.affectedPersonName}"/></strong> to confirm receipt on their dashboard.
                                            </c:when>
                                            <c:when test="${al.status == 'CONFIRMED'}">
                                                <span style="color: #4ade80; font-weight: 600;">&#10003; Completed: Citizen confirmed that relief supplies reached them.</span>
                                            </c:when>
                                            <c:when test="${al.status == 'CANCELLED'}">
                                                Units were automatically returned to the open relief queue.
                                            </c:when>
                                        </c:choose>
                                    </div>

                                    <div style="display: flex; gap: 0.55rem; align-items: center; flex-wrap: wrap;">
                                        <c:if test="${al.status == 'ALLOCATED'}">
                                            <button type="button" class="step-advance-link timeline-step-btn"
                                                    data-allocation-id="${al.allocationId}"
                                                    data-next-status="IN_TRANSIT">
                                                Advance to In Transit &rarr;
                                            </button>
                                        </c:if>
                                        <c:if test="${al.status == 'IN_TRANSIT'}">
                                            <button type="button" class="step-advance-link timeline-step-btn"
                                                    data-allocation-id="${al.allocationId}"
                                                    data-next-status="DELIVERED">
                                                Mark Arrived at Drop-Off &rarr;
                                            </button>
                                        </c:if>
                                        <c:if test="${al.status == 'ALLOCATED' || al.status == 'IN_TRANSIT'}">
                                            <button type="button" class="cancel-conn-btn open-cancel-modal-btn"
                                                    data-cancel-type="ALLOCATION"
                                                    data-target-id="${al.allocationId}"
                                                    data-target-label="Connection #${al.allocationId} (${fn:escapeXml(al.needCategory)})">
                                                Cancel Connection
                                            </button>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- Smart Matched Needs -->
        <section class="panel-card">
            <h2>Smart-Matched Relief Requests</h2>
            <p class="panel-sub">Department-verified needs ranked by urgency level, GPS proximity, and live Gemini AI logistics insights.</p>

            <c:choose>
                <c:when test="${empty matchedNeeds}">
                    <p style="color: var(--text-muted); text-align: center; padding: 1.5rem 0;">
                        All currently verified relief requests have been fulfilled. Check back soon!
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="matches-grid">
                        <c:forEach var="m" items="${matchedNeeds}">
                            <div class="match-card">
                                <div>
                                    <div style="display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; margin-bottom: 0.75rem;">
                                        <span class="badge urg-${m.need.urgency}">${m.need.urgency}</span>
                                        <span class="badge score-pill">Match ${m.matchScore} &bull; ${m.distanceKm} km</span>
                                    </div>
                                    <h3 style="font-size: 1.25rem; margin-bottom: 0.35rem;">
                                        #${m.need.needId} &mdash; <c:out value="${m.need.category}"/>
                                    </h3>
                                    <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 0.75rem;">
                                        <c:out value="${m.need.description}"/>
                                    </p>
                                    <c:if test="${not empty m.aiLogisticsBrief}">
                                        <div class="ai-brief-box">
                                            <strong>&#10024; AI Insight:</strong> <c:out value="${m.aiLogisticsBrief}"/>
                                        </div>
                                    </c:if>
                                    <div style="font-size: 0.83rem; color: #e2e8f0;">
                                        Remaining Needed: <strong style="color: #38bdf8;">${m.need.quantityRemaining}</strong>
                                        of ${m.need.quantityRequired} units
                                    </div>
                                </div>

                                <div>
                                    <div class="pledge-row">
                                        <input type="number" class="qty-input" id="pledgeQty_${m.need.needId}"
                                               min="1" value="${m.need.quantityRemaining}">
                                        <button type="button" class="btn-relief pledge-btn" style="flex: 1; padding: 0.55rem 0.9rem;"
                                                data-need-id="${m.need.needId}">
                                            Pledge &amp; Connect
                                        </button>
                                        <button type="button" class="btn-outline-glass chat-open-btn"
                                                style="padding: 0.55rem 0.75rem;"
                                                data-need-id="${m.need.needId}"
                                                data-need-title="${fn:escapeXml(m.need.category)}"
                                                title="Chat with Recipient">
                                            &#128172;
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <!-- My Pledges History -->
        <section class="panel-card">
            <h2>My Resource Pledges &amp; Surplus Pool</h2>
            <p class="panel-sub">Any excess units pledged beyond immediate demand remain available for department reallocation.</p>

            <c:choose>
                <c:when test="${empty myPledges}">
                    <p style="color: var(--text-muted); text-align: center; padding: 1rem 0;">
                        You have not submitted any pledges yet.
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>Pledge ID</th>
                                    <th>Need ID</th>
                                    <th>Offered</th>
                                    <th>Connected</th>
                                    <th>Surplus Pool</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${myPledges}">
                                    <tr>
                                        <td><strong>#${p.pledgeId}</strong></td>
                                        <td>Need #${p.needId}</td>
                                        <td>${p.quantityOffered}</td>
                                        <td style="color: #4ade80; font-weight: 700;">${p.quantityAllocated}</td>
                                        <td style="color: #fdba74;">${p.quantitySurplus}</td>
                                        <td>
                                            <span class="badge score-pill">${p.status}</span>
                                            <c:if test="${p.status == 'CANCELLED' && not empty p.cancellationReason}">
                                                <div style="font-size: 0.75rem; color: #fca5a5; margin-top: 0.25rem;">
                                                    Reason: <c:out value="${p.cancellationReason}"/>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 0.4rem; align-items: center;">
                                                <button type="button" class="btn-outline-glass chat-open-btn"
                                                        style="padding: 0.38rem 0.75rem; font-size: 0.8rem;"
                                                        data-need-id="${p.needId}"
                                                        data-need-title="Need #${p.needId}">
                                                    &#128172; Chat
                                                </button>
                                                <c:if test="${p.status != 'CANCELLED' && p.status != 'WITHDRAWN'}">
                                                    <button type="button" class="cancel-conn-btn open-cancel-modal-btn"
                                                            style="padding: 0.38rem 0.65rem; font-size: 0.78rem;"
                                                            data-cancel-type="PLEDGE"
                                                            data-target-id="${p.pledgeId}"
                                                            data-target-label="Pledge #${p.pledgeId} (Need #${p.needId})">
                                                        Cancel Pledge
                                                    </button>
                                                </c:if>
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
    </main>

    <!-- Mandatory Reason Cancellation Modal -->
    <div id="cancelModal" class="modal-backdrop">
        <div class="modal-card" style="border-color: #f87171;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                <h3 id="cancelModalTitle" style="font-size: 1.25rem; color: #fca5a5;">Cancel Delivery Connection</h3>
                <button type="button" id="closeCancelModalBtn" class="btn-outline-glass" style="padding: 0.3rem 0.7rem;">&times;</button>
            </div>
            <p style="font-size: 0.85rem; color: #cbd5e1; margin-bottom: 0.85rem;">
                Cancelling <strong id="cancelTargetName" style="color: #ffffff;"></strong> will immediately reopen the relief request for other donors and notify the citizen and Relief Department.
            </p>
            <form id="cancelReasonForm">
                <label for="cancelReasonInput" style="display: block; font-size: 0.82rem; font-weight: 700; margin-bottom: 0.4rem; color: #e2e8f0;">
                    Cancellation Description / Reason (Required) *
                </label>
                <textarea id="cancelReasonInput" class="dark-input" rows="3" minlength="5" required
                          placeholder="Explain why you need to cancel (e.g., vehicle breakdown on NH-66, stock damaged, road blocked)..."></textarea>
                <div style="display: flex; justify-content: flex-end; gap: 0.6rem; margin-top: 1rem;">
                    <button type="button" id="abortCancelBtn" class="btn-outline-glass" style="padding: 0.55rem 1rem;">Keep Active</button>
                    <button type="submit" id="confirmCancelSubmitBtn" class="btn-relief" style="background: #dc2626; padding: 0.55rem 1.1rem;">
                        Confirm Cancellation
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Chat Modal -->
    <div id="chatModal" class="modal-backdrop">
        <div class="modal-card">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <h3 id="chatModalTitle" style="font-size: 1.25rem;">Need Coordination Chat</h3>
                <button type="button" id="closeChatBtn" class="btn-outline-glass" style="padding: 0.3rem 0.7rem;">&times;</button>
            </div>
            <div id="chatHistory" class="chat-history"></div>
            <form id="sendChatForm" style="display: flex; gap: 0.5rem;">
                <input type="text" id="chatInput" class="dark-input" placeholder="Coordinate drop-off time & location..." required>
                <button type="submit" id="sendChatBtn" class="btn-relief" style="padding: 0.65rem 1.1rem;">Send</button>
            </form>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/cursor-effects.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", () => {
            const contextPath = document.body.dataset.contextPath || "";
            const currentUserId = Number(document.body.dataset.userId || 0);
            const donorBanner = document.getElementById("donorBanner");

            function showDonorBanner(msg, isError) {
                donorBanner.textContent = msg;
                donorBanner.className = "msg-banner " + (isError ? "msg-err" : "msg-ok");
                donorBanner.style.display = "block";
                donorBanner.scrollIntoView({ behavior: "smooth", block: "nearest" });
            }

            // Pledge & Connect
            document.querySelectorAll(".pledge-btn").forEach((btn) => {
                btn.addEventListener("click", async () => {
                    const needId = btn.dataset.needId;
                    const qtyInput = document.getElementById("pledgeQty_" + needId);
                    const qty = qtyInput ? qtyInput.value : 0;

                    btn.disabled = true;
                    btn.textContent = "Connecting...";

                    try {
                        const res = await fetch(contextPath + "/api/pledges", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: new URLSearchParams({ needId: needId, quantityOffered: qty })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showDonorBanner("Connected with citizen! Live horizontal delivery timeline created.", false);
                            setTimeout(() => window.location.reload(), 800);
                        } else {
                            showDonorBanner(data.error || "Could not process pledge.", true);
                        }
                    } catch (err) {
                        showDonorBanner("Network error submitting pledge.", true);
                    } finally {
                        btn.disabled = false;
                        btn.textContent = "Pledge & Connect";
                    }
                });
            });

            // Advance Horizontal Timeline Stage (ALLOCATED -> IN_TRANSIT -> DELIVERED)
            document.querySelectorAll(".timeline-step-btn").forEach((btn) => {
                btn.addEventListener("click", async () => {
                    const allocationId = btn.dataset.allocationId;
                    const nextStatus = btn.dataset.nextStatus;
                    btn.disabled = true;
                    try {
                        const res = await fetch(contextPath + "/api/allocations/" + allocationId + "/status", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: new URLSearchParams({ status: nextStatus })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showDonorBanner(data.message || "Delivery timeline updated.", false);
                            setTimeout(() => window.location.reload(), 650);
                        } else {
                            showDonorBanner(data.error || "Could not update timeline.", true);
                            btn.disabled = false;
                        }
                    } catch (err) {
                        showDonorBanner("Network error updating timeline.", true);
                        btn.disabled = false;
                    }
                });
            });

            // Mandatory Cancellation Reason Modal
            const cancelModal = document.getElementById("cancelModal");
            const cancelReasonForm = document.getElementById("cancelReasonForm");
            const cancelReasonInput = document.getElementById("cancelReasonInput");
            let pendingCancelType = null; // "ALLOCATION" or "PLEDGE"
            let pendingCancelId = null;

            function closeCancelModal() {
                cancelModal.style.display = "none";
                pendingCancelType = null;
                pendingCancelId = null;
                cancelReasonInput.value = "";
            }

            document.querySelectorAll(".open-cancel-modal-btn").forEach((btn) => {
                btn.addEventListener("click", () => {
                    pendingCancelType = btn.dataset.cancelType;
                    pendingCancelId = btn.dataset.targetId;
                    document.getElementById("cancelTargetName").textContent = btn.dataset.targetLabel;
                    document.getElementById("cancelModalTitle").textContent =
                        pendingCancelType === "ALLOCATION" ? "Cancel Delivery Connection" : "Cancel Resource Pledge";
                    cancelModal.style.display = "flex";
                    setTimeout(() => cancelReasonInput.focus(), 80);
                });
            });

            document.getElementById("closeCancelModalBtn").addEventListener("click", closeCancelModal);
            document.getElementById("abortCancelBtn").addEventListener("click", closeCancelModal);
            cancelModal.addEventListener("click", (e) => {
                if (e.target === cancelModal) closeCancelModal();
            });

            cancelReasonForm.addEventListener("submit", async (e) => {
                e.preventDefault();
                const reason = cancelReasonInput.value.trim();
                if (reason.length < 5) {
                    alert("Please enter a clear cancellation description (at least 5 characters).");
                    return;
                }

                const submitBtn = document.getElementById("confirmCancelSubmitBtn");
                submitBtn.disabled = true;
                submitBtn.textContent = "Cancelling...";

                const endpoint = pendingCancelType === "ALLOCATION"
                    ? contextPath + "/api/allocations/" + pendingCancelId + "/cancel"
                    : contextPath + "/api/pledges/" + pendingCancelId + "/cancel";

                try {
                    const res = await fetch(endpoint, {
                        method: "POST",
                        headers: { "Content-Type": "application/x-www-form-urlencoded" },
                        body: new URLSearchParams({ reason: reason })
                    });
                    const data = await res.json();
                    if (res.ok) {
                        closeCancelModal();
                        const aiNote = data.cancellationSummary && data.cancellationSummary.aiRecommendation
                            ? " | AI Logistics Note: " + data.cancellationSummary.aiRecommendation
                            : "";
                        showDonorBanner((data.message || "Cancelled successfully.") + aiNote, false);
                        setTimeout(() => window.location.reload(), 1800);
                    } else {
                        alert(data.error || "Cancellation failed.");
                    }
                } catch (err) {
                    alert("Network error processing cancellation.");
                } finally {
                    submitBtn.disabled = false;
                    submitBtn.textContent = "Confirm Cancellation";
                }
            });

            // Chat Modal
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
                    emptyEl.textContent = "No messages yet. Message the recipient to coordinate delivery.";
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
                                : "You are not authorized to participate in this conversation.";
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
                if (e.key === "Escape") {
                    if (chatModal.style.display === "flex") closeChatModal();
                    if (cancelModal.style.display === "flex") closeCancelModal();
                }
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
                        body: new URLSearchParams({ needId: activeNeedId, content: text, messageType: "DIRECT" })
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