<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="jakarta.tags.core" %> <%@ taglib prefix="fn"
uri="jakarta.tags.functions" %> <%@ taglib prefix="fmt" uri="jakarta.tags.fmt"
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Affected Citizen Portal | Disaster Relief Coordination System</title>
    <link
      rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/style.css"
    />
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
        background: linear-gradient(
          90deg,
          rgba(153, 27, 27, 0.88),
          rgba(180, 83, 9, 0.85)
        );
        border: 1px solid #fca5a5;
        border-radius: 10px;
        padding: 1rem 1.25rem;
        margin-bottom: 1.75rem;
      }
      .alert-strip h3 {
        font-size: 1.1rem;
        text-transform: uppercase;
        margin-bottom: 0.35rem;
        color: #ffffff;
      }
      .alert-item {
        font-size: 0.88rem;
        color: #fef2f2;
        padding: 0.3rem 0;
        border-top: 1px solid rgba(255, 255, 255, 0.16);
      }
      .alert-item:first-of-type {
        border-top: none;
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
        font-family: "Rajdhani", sans-serif;
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
        margin-bottom: 0.3rem;
      }
      .panel-sub {
        font-size: 0.85rem;
        color: var(--text-muted);
        margin-bottom: 1.25rem;
      }
      .workspace-grid {
        display: grid;
        grid-template-columns: 420px 1fr;
        gap: 1.75rem;
        align-items: start;
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
        background: linear-gradient(
          90deg,
          #38bdf8 35%,
          rgba(148, 163, 184, 0.22) 100%
        );
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
      .htl-step.done .htl-label {
        color: #86efac;
      }
      .htl-step.active .htl-label {
        color: #38bdf8;
      }
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
      .confirm-received-btn {
        background: linear-gradient(135deg, #16a34a, #15803d);
        border: 1px solid #86efac;
        color: #ffffff;
        padding: 0.5rem 1rem;
        border-radius: 6px;
        font-size: 0.82rem;
        font-weight: 700;
        cursor: pointer;
        box-shadow: 0 4px 14px rgba(22, 163, 74, 0.32);
        transition: transform 0.18s ease, box-shadow 0.18s ease;
      }
      .confirm-received-btn:hover {
        transform: translateY(-1px);
        box-shadow: 0 6px 18px rgba(22, 163, 74, 0.45);
      }
      .confirm-received-btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
        transform: none;
      }

      /* Form & Table Styles */
      .form-field {
        margin-bottom: 1rem;
      }
      .form-field label {
        display: block;
        font-size: 0.83rem;
        font-weight: 600;
        color: #e2e8f0;
        margin-bottom: 0.35rem;
      }
      .dark-input,
      .dark-select,
      .dark-textarea {
        width: 100%;
        padding: 0.68rem 0.85rem;
        border-radius: 6px;
        border: 1px solid rgba(255, 255, 255, 0.2);
        background: var(--navy-950);
        color: #ffffff;
        font-family: inherit;
        font-size: 0.92rem;
      }
      .dark-input:focus,
      .dark-select:focus,
      .dark-textarea:focus {
        outline: none;
        border-color: var(--sky-400);
      }
      .two-col {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0.75rem;
      }
      .ai-box {
        background: rgba(37, 99, 235, 0.14);
        border: 1px solid rgba(56, 189, 248, 0.4);
        border-radius: 8px;
        padding: 0.85rem;
        margin-bottom: 1rem;
        font-size: 0.84rem;
        line-height: 1.45;
        display: none;
      }
      .msg-banner {
        padding: 0.8rem 1rem;
        border-radius: 8px;
        font-size: 0.86rem;
        margin-bottom: 1.15rem;
        display: none;
      }
      .msg-ok {
        background: rgba(22, 101, 52, 0.9);
        border: 1px solid #86efac;
        color: #dcfce7;
      }
      .msg-err {
        background: rgba(153, 27, 27, 0.9);
        border: 1px solid #fca5a5;
        color: #ffffff;
      }
      .table-wrap {
        overflow-x: auto;
      }
      table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.88rem;
      }
      th,
      td {
        text-align: left;
        padding: 0.85rem 0.75rem;
        border-bottom: 1px solid var(--border-glass);
        vertical-align: middle;
      }
      th {
        font-size: 0.75rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--text-muted);
      }
      .badge {
        display: inline-block;
        padding: 0.22rem 0.6rem;
        border-radius: 999px;
        font-size: 0.73rem;
        font-weight: 700;
        text-transform: uppercase;
      }
      .score-pill {
        background: rgba(14, 165, 233, 0.2);
        border: 1px solid rgba(56, 189, 248, 0.45);
        color: #7dd3fc;
      }
      .urg-CRITICAL {
        background: rgba(220, 38, 38, 0.25);
        color: #fca5a5;
        border: 1px solid #f87171;
      }
      .urg-HIGH {
        background: rgba(234, 88, 12, 0.25);
        color: #fdba74;
        border: 1px solid #fb923c;
      }
      .urg-MEDIUM {
        background: rgba(37, 99, 235, 0.25);
        color: #93c5fd;
        border: 1px solid #60a5fa;
      }
      .urg-LOW {
        background: rgba(100, 116, 139, 0.25);
        color: #cbd5e1;
      }
      .st-PENDING_VERIFICATION {
        background: rgba(234, 179, 8, 0.2);
        color: #fde047;
      }
      .st-VERIFIED,
      .st-PARTIALLY_FULFILLED {
        background: rgba(14, 165, 233, 0.22);
        color: #7dd3fc;
      }
      .st-FULFILLED {
        background: rgba(34, 197, 94, 0.22);
        color: #86efac;
      }
      .st-REJECTED {
        background: rgba(239, 68, 68, 0.22);
        color: #fca5a5;
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
      @media (max-width: 960px) {
        .workspace-grid {
          grid-template-columns: 1fr;
        }
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
        .htl-sub {
          max-width: none;
        }
      }
    </style>
  </head>
  <body
    data-context-path="${pageContext.request.contextPath}"
    data-user-id="${user.userId}"
  >
    <header class="dash-nav">
      <div class="dash-nav-inner">
        <a href="${pageContext.request.contextPath}/" class="brand-logo-wrap">
          <img
            src="${pageContext.request.contextPath}/assets/images/disasterManageLogo.png"
            onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/disasterManageLogo.jpeg';"
            alt="Logo"
            class="brand-logo-img"
            style="width: 40px; height: 40px"
          />
          <div>
            <div
              style="
                font-family: 'Rajdhani', sans-serif;
                font-weight: 700;
                font-size: 1.15rem;
              "
            >
              AFFECTED CITIZEN PORTAL
            </div>
            <div
              style="font-size: 0.68rem; color: #fca5a5; letter-spacing: 0.12em"
            >
              PREPARE &bull; RESPOND &bull; RECOVER
            </div>
          </div>
        </a>

        <div
          style="
            display: flex;
            align-items: center;
            gap: 0.85rem;
            flex-wrap: wrap;
          "
        >
          <span class="user-pill">
            &#128100; <c:out value="${user.name}" /> (+91-<c:out
              value="${user.mobileNumber}"
            />)
          </span>
          <a
            href="${pageContext.request.contextPath}/logout"
            class="btn-outline-glass"
            style="padding: 0.45rem 1rem; font-size: 0.84rem"
          >
            Logout
          </a>
        </div>
      </div>
    </header>

    <main class="dash-container">
      <!-- Geo-Targeted Emergency Alerts -->
      <c:if test="${not empty alerts}">
        <div class="alert-strip">
          <h3>&#9888; Active Emergency Alerts in Your Area</h3>
          <c:forEach var="a" items="${alerts}">
            <div class="alert-item">
              <strong><c:out value="${a.title}" />:</strong>
              <c:out value="${a.message}" />
              <span style="opacity: 0.85; font-size: 0.78rem"
                >(Radius: ${a.radiusKm} km)</span
              >
            </div>
          </c:forEach>
        </div>
      </c:if>

      <!-- KPI Summary -->
      <c:set var="pendingCnt" value="0" />
      <c:set var="activeCnt" value="0" />
      <c:set var="fulfilledCnt" value="0" />
      <c:forEach var="item" items="${myNeeds}">
        <c:if test="${item.status == 'PENDING_VERIFICATION'}"
          ><c:set var="pendingCnt" value="${pendingCnt + 1}"
        /></c:if>
        <c:if
          test="${item.status == 'VERIFIED' || item.status == 'PARTIALLY_FULFILLED'}"
          ><c:set var="activeCnt" value="${activeCnt + 1}"
        /></c:if>
        <c:if test="${item.status == 'FULFILLED'}"
          ><c:set var="fulfilledCnt" value="${fulfilledCnt + 1}"
        /></c:if>
      </c:forEach>

      <section class="kpi-grid">
        <div class="kpi-card">
          <span>Total Requests</span>
          <strong>${fn:length(myNeeds)}</strong>
        </div>
        <div class="kpi-card">
          <span>Pending Verification</span>
          <strong style="color: #fde047">${pendingCnt}</strong>
        </div>
        <div class="kpi-card">
          <span>Active Donor Connections</span>
          <strong style="color: #38bdf8">${fn:length(myAllocations)}</strong>
        </div>
        <div class="kpi-card">
          <span>Fulfilled Requests</span>
          <strong style="color: #4ade80">${fulfilledCnt}</strong>
        </div>
      </section>

      <div id="globalCitizenBanner" class="msg-banner"></div>

      <!-- Incoming Relief Connections & Horizontal Delivery Timelines -->
      <section class="panel-card">
        <h2>Incoming Relief &amp; Delivery Timelines</h2>
        <p class="panel-sub">
          Track every connected donor in real time. Once supplies reach you,
          click <strong>Confirm Received</strong> to verify the need is reached.
        </p>

        <c:choose>
          <c:when test="${empty myAllocations}">
            <p
              style="
                color: var(--text-muted);
                text-align: center;
                padding: 1.25rem 0;
              "
            >
              No donors connected yet. Once your request is verified and matched
              with a donor, your live horizontal delivery timeline will appear
              here.
            </p>
          </c:when>
          <c:otherwise>
            <div class="timeline-list">
              <c:forEach var="al" items="${myAllocations}">
                <c:set var="stepNum" value="1" />
                <c:if test="${al.status == 'IN_TRANSIT'}"
                  ><c:set var="stepNum" value="2"
                /></c:if>
                <c:if test="${al.status == 'DELIVERED'}"
                  ><c:set var="stepNum" value="3"
                /></c:if>
                <c:if test="${al.status == 'CONFIRMED'}"
                  ><c:set var="stepNum" value="4"
                /></c:if>
                <c:if test="${al.status == 'CANCELLED'}"
                  ><c:set var="stepNum" value="-1"
                /></c:if>

                <div
                  class="conn-card ${al.status == 'CONFIRMED' ? 'is-confirmed' : (al.status == 'CANCELLED' ? 'is-cancelled' : '')}"
                >
                  <div class="conn-header">
                    <div>
                      <div
                        style="
                          display: flex;
                          align-items: center;
                          gap: 0.6rem;
                          flex-wrap: wrap;
                        "
                      >
                        <strong style="font-size: 1.1rem; color: #ffffff">
                          <c:out value="${al.needCategory}" /> (Need
                          #${al.needId})
                        </strong>
                        <span class="badge score-pill"
                          >${al.quantityAllocated} Units Incoming</span
                        >
                        <c:if test="${not empty al.needUrgency}">
                          <span class="badge urg-${al.needUrgency}"
                            >${al.needUrgency}</span
                          >
                        </c:if>
                      </div>
                      <div class="conn-meta">
                        <span>
                          Donor:
                          <strong style="color: #e2e8f0"
                            ><c:out
                              value="${not empty al.donorName ? al.donorName : 'Relief Donor'}"
                          /></strong>
                          <c:if test="${not empty al.donorMobile}">
                            (+91-<c:out value="${al.donorMobile}" />)</c:if
                          >
                        </span>
                        <c:if test="${not empty al.allocatedAt}">
                          <span
                            >&bull; Connected:
                            <fmt:formatDate
                              value="${al.allocatedAt}"
                              pattern="dd MMM, HH:mm"
                          /></span>
                        </c:if>
                      </div>
                    </div>

                    <div
                      style="display: flex; gap: 0.5rem; align-items: center"
                    >
                      <button
                        type="button"
                        class="btn-outline-glass chat-open-btn"
                        style="padding: 0.42rem 0.85rem; font-size: 0.8rem"
                        data-need-id="${al.needId}"
                        data-need-title="${fn:escapeXml(al.needCategory)}"
                        data-donor-id="${al.donorId}"
                        data-donor-name="${fn:escapeXml(al.donorName)}"
                      >
                        &#128172; Message Donor
                      </button>
                    </div>
                  </div>

                  <c:choose>
                    <c:when test="${al.status == 'CANCELLED'}">
                      <div class="cancel-callout">
                        <strong>&#10006; Donor Cancelled This Delivery</strong>
                        <c:if test="${not empty al.cancelledAt}">
                          (<fmt:formatDate
                            value="${al.cancelledAt}"
                            pattern="dd MMM, HH:mm"
                          />)
                        </c:if>
                        <div style="margin-top: 0.3rem; color: #fee2e2">
                          <strong>Donor's Reason:</strong> &ldquo;<c:out
                            value="${not empty al.cancellationReason ? al.cancellationReason : 'Cancelled by donor'}"
                          />&rdquo;
                        </div>
                        <div
                          style="
                            margin-top: 0.35rem;
                            font-size: 0.78rem;
                            color: #fecaca;
                          "
                        >
                          Your request has been automatically reopened so
                          another nearby donor can fulfill these
                          ${al.quantityAllocated} units.
                        </div>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <!-- 4-Stage Horizontal Stepper -->
                      <div class="htl-track">
                        <div
                          class="htl-step ${stepNum > 1 ? 'done' : (stepNum == 1 ? 'active' : '')}"
                        >
                          <div class="htl-dot">
                            ${stepNum > 1 ? '&#10003;' : '1'}
                          </div>
                          <div class="htl-label">Connected</div>
                          <div class="htl-sub">
                            Donor matched to your request
                          </div>
                        </div>

                        <div
                          class="htl-step ${stepNum > 2 ? 'done' : (stepNum == 2 ? 'active' : '')}"
                        >
                          <div class="htl-dot">
                            ${stepNum > 2 ? '&#10003;' : '2'}
                          </div>
                          <div class="htl-label">In Transit</div>
                          <div class="htl-sub">Donor dispatched supplies</div>
                        </div>

                        <div
                          class="htl-step ${stepNum > 3 ? 'done' : (stepNum == 3 ? 'active' : '')}"
                        >
                          <div class="htl-dot">
                            ${stepNum > 3 ? '&#10003;' : '3'}
                          </div>
                          <div class="htl-label">At Drop-Off</div>
                          <div class="htl-sub">Arrived at your location</div>
                        </div>

                        <div class="htl-step ${stepNum == 4 ? 'done' : ''}">
                          <div class="htl-dot">
                            ${stepNum == 4 ? '&#10003;' : '4'}
                          </div>
                          <div class="htl-label">Need Reached</div>
                          <div class="htl-sub">
                            <c:choose>
                              <c:when test="${stepNum == 4}">
                                Receipt confirmed
                                <c:if test="${not empty al.confirmedAt}">
                                  (<fmt:formatDate
                                    value="${al.confirmedAt}"
                                    pattern="dd MMM, HH:mm"
                                  />)
                                </c:if>
                              </c:when>
                              <c:otherwise>Confirm once received</c:otherwise>
                            </c:choose>
                          </div>
                        </div>
                      </div>
                    </c:otherwise>
                  </c:choose>

                  <div class="conn-footer">
                    <div style="font-size: 0.8rem; color: #94a3b8">
                      <c:choose>
                        <c:when test="${al.status == 'ALLOCATED'}">
                          Donor is preparing your ${al.quantityAllocated} units
                          for dispatch.
                        </c:when>
                        <c:when test="${al.status == 'IN_TRANSIT'}">
                          Supplies are currently in transit to your location.
                        </c:when>
                        <c:when test="${al.status == 'DELIVERED'}">
                          <span style="color: #7dd3fc; font-weight: 600"
                            >Supplies have arrived! Please confirm receipt
                            below.</span
                          >
                        </c:when>
                        <c:when test="${al.status == 'CONFIRMED'}">
                          <span style="color: #4ade80; font-weight: 600"
                            >&#10003; Confirmed: Relief supplies received and
                            need reached.</span
                          >
                        </c:when>
                        <c:when test="${al.status == 'CANCELLED'}">
                          Status: Reopened in donor matching queue.
                        </c:when>
                      </c:choose>
                    </div>

                    <div>
                      <c:if
                        test="${al.status == 'ALLOCATED' || al.status == 'IN_TRANSIT' || al.status == 'DELIVERED'}"
                      >
                        <button
                          type="button"
                          class="confirm-received-btn"
                          data-allocation-id="${al.allocationId}"
                          data-need-id="${al.needId}"
                        >
                          &#10003; Confirm Received (Need Reached)
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

      <div class="workspace-grid">
        <!-- Left: Submit Relief Need Form -->
        <section class="panel-card">
          <h2>Report Relief Need</h2>
          <p class="panel-sub">
            Submit urgent supply requirements. Live Gemini AI auto-classifies
            category, priority, and semantic duplicates.
          </p>

          <div id="needStatusBanner" class="msg-banner"></div>

          <form id="submitNeedForm">
            <div class="form-field">
              <label for="disasterId">Active Disaster Event</label>
              <select
                id="disasterId"
                name="disasterId"
                class="dark-select"
                required
              >
                <c:forEach var="d" items="${disasters}">
                  <option value="${d.disasterId}">
                    ${fn:escapeXml(d.name)} (${fn:escapeXml(d.type)})
                  </option>
                </c:forEach>
                <c:if test="${empty disasters}">
                  <option value="1">Coastal Monsoon Flood 2026 (Flood)</option>
                </c:if>
              </select>
            </div>

            <div class="two-col">
              <div class="form-field">
                <label for="category">Resource Category</label>
                <select id="category" name="category" class="dark-select">
                  <option value="AUTO">AI Auto-Classify</option>
                  <option value="Food &amp; Rations">Food &amp; Rations</option>
                  <option value="Clean Drinking Water">
                    Clean Drinking Water
                  </option>
                  <option value="Medical &amp; First Aid">
                    Medical &amp; First Aid
                  </option>
                  <option value="Shelter &amp; Clothing">
                    Shelter &amp; Clothing
                  </option>
                  <option value="Rescue &amp; Evacuation">
                    Rescue &amp; Evacuation
                  </option>
                  <option value="General Relief Supplies">
                    General Relief Supplies
                  </option>
                </select>
              </div>

              <div class="form-field">
                <label for="urgency">Urgency Level</label>
                <select id="urgency" name="urgency" class="dark-select">
                  <option value="AUTO">AI Priority Score</option>
                  <option value="CRITICAL">CRITICAL</option>
                  <option value="HIGH">HIGH</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="LOW">LOW</option>
                </select>
              </div>
            </div>

            <div class="two-col">
              <div class="form-field">
                <label for="quantityRequired">Quantity Required</label>
                <input
                  type="number"
                  id="quantityRequired"
                  name="quantityRequired"
                  class="dark-input"
                  min="1"
                  value="10"
                  required
                />
              </div>

              <div class="form-field">
                <label for="locationName">Landmark / Ward (Opt.)</label>
                <input
                  type="text"
                  id="locationName"
                  name="locationName"
                  class="dark-input"
                  placeholder="e.g. Ward 4 Hall"
                />
              </div>
            </div>

            <div class="two-col">
              <div class="form-field">
                <label for="affectedPeopleCount">Total People Affected</label>
                <input
                  type="number"
                  id="affectedPeopleCount"
                  name="affectedPeopleCount"
                  class="dark-input"
                  min="0"
                  placeholder="e.g. 15"
                />
              </div>

              <div class="form-field">
                <label for="criticalPeopleCount"
                  >Critical / Elderly / Infants</label
                >
                <input
                  type="number"
                  id="criticalPeopleCount"
                  name="criticalPeopleCount"
                  class="dark-input"
                  min="0"
                  placeholder="e.g. 3"
                />
              </div>
            </div>

            <div class="form-field">
              <label for="description">Detailed Need Description</label>
              <textarea
                id="description"
                name="description"
                rows="3"
                class="dark-textarea"
                placeholder="Describe items needed, number of family members, landmarks..."
                required
              ></textarea>
            </div>

            <div id="aiPreviewBox" class="ai-box"></div>

            <div class="form-field">
              <label>GPS Coordinates (Pre-filled from Profile)</label>
              <div class="two-col">
                <input
                  type="number"
                  step="any"
                  id="latitude"
                  name="latitude"
                  class="dark-input"
                  value="${user.latitude}"
                  required
                />
                <input
                  type="number"
                  step="any"
                  id="longitude"
                  name="longitude"
                  class="dark-input"
                  value="${user.longitude}"
                  required
                />
              </div>
            </div>

            <div style="display: flex; gap: 0.65rem; flex-wrap: wrap">
              <button
                type="button"
                id="aiAnalyzeBtn"
                class="btn-outline-glass"
                style="flex: 1"
              >
                &#10024; AI Preview
              </button>
              <button
                type="submit"
                id="submitNeedBtn"
                class="btn-relief"
                style="flex: 2"
              >
                Submit Request &rarr;
              </button>
            </div>
          </form>
        </section>

        <!-- Right: My Submitted Needs Table -->
        <section class="panel-card">
          <h2>My Relief Requests</h2>
          <p class="panel-sub">
            Track verification progress, donor allocations, and coordinate via
            direct messages.
          </p>

          <c:choose>
            <c:when test="${empty myNeeds}">
              <p
                style="
                  color: var(--text-muted);
                  padding: 1.5rem 0;
                  text-align: center;
                "
              >
                No relief requests submitted yet. Use the form on the left to
                report an urgent need.
              </p>
            </c:when>
            <c:otherwise>
              <div class="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Category &amp; Details</th>
                      <th>Fulfillment</th>
                      <th>Urgency</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="n" items="${myNeeds}">
                      <tr>
                        <td><strong>#${n.needId}</strong></td>
                        <td>
                          <div style="font-weight: 700; color: #ffffff">
                            <c:out value="${n.category}" />
                          </div>
                          <div
                            style="font-size: 0.82rem; color: var(--text-muted)"
                          >
                            <c:out value="${n.description}" />
                          </div>
                        </td>
                        <td>
                          <div>
                            <strong>${n.quantityAllocated}</strong> /
                            ${n.quantityRequired} allocated
                          </div>
                          <div style="font-size: 0.78rem; color: #7dd3fc">
                            Remaining: ${n.quantityRemaining}
                          </div>
                        </td>
                        <td>
                          <span class="badge urg-${n.urgency}"
                            >${n.urgency}</span
                          >
                        </td>
                        <td>
                          <span class="badge st-${n.status}">${n.status}</span>
                        </td>
                        <td>
                          <button
                            type="button"
                            class="btn-outline-glass chat-open-btn"
                            style="padding: 0.4rem 0.75rem; font-size: 0.8rem"
                            data-need-id="${n.needId}"
                            data-need-title="${fn:escapeXml(n.category)}"
                          >
                            &#128172; Chat
                          </button>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </c:otherwise>
          </c:choose>
        </section>
      </div>
    </main>

    <!-- Coordination Chat Modal -->
    <div id="chatModal" class="modal-backdrop">
      <div class="modal-card">
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <h3 id="chatModalTitle" style="font-size: 1.25rem">
            Need Coordination Chat
          </h3>
          <button
            type="button"
            id="closeChatBtn"
            class="btn-outline-glass"
            style="padding: 0.3rem 0.7rem"
          >
            &times;
          </button>
        </div>
        <div id="chatHistory" class="chat-history"></div>
        <div
          id="replyingToBanner"
          style="
            display: none;
            font-size: 0.8rem;
            color: #7dd3fc;
            margin-bottom: 0.4rem;
            padding: 0.3rem 0.6rem;
            background: rgba(56, 189, 248, 0.12);
            border: 1px solid rgba(56, 189, 248, 0.3);
            border-radius: 6px;
            align-items: center;
            justify-content: space-between;
          "
        >
          <span>Replying to: <strong id="replyingToName"></strong></span>
          <button
            type="button"
            id="cancelReplyBtn"
            style="
              background: none;
              border: none;
              color: #fca5a5;
              cursor: pointer;
              font-size: 0.75rem;
              text-decoration: underline;
            "
          >
            &times; Clear (Send to all)
          </button>
        </div>
        <form id="sendChatForm" style="display: flex; gap: 0.5rem">
          <input
            type="text"
            id="chatInput"
            class="dark-input"
            placeholder="Write message to donors / relief team..."
            required
          />
          <button
            type="submit"
            id="sendChatBtn"
            class="btn-relief"
            style="padding: 0.65rem 1.1rem"
          >
            Send
          </button>
        </form>
      </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/cursor-effects.js"></script>
    <script>
      document.addEventListener("DOMContentLoaded", () => {
        const contextPath = document.body.dataset.contextPath || "";
        const currentUserId = Number(document.body.dataset.userId || 0);
        const banner = document.getElementById("needStatusBanner");
        const globalBanner = document.getElementById("globalCitizenBanner");
        const aiBox = document.getElementById("aiPreviewBox");

        function showBanner(msg, isError) {
          banner.textContent = msg;
          banner.className = "msg-banner " + (isError ? "msg-err" : "msg-ok");
          banner.style.display = "block";
        }

        function showGlobalBanner(msg, isError) {
          globalBanner.textContent = msg;
          globalBanner.className =
            "msg-banner " + (isError ? "msg-err" : "msg-ok");
          globalBanner.style.display = "block";
          globalBanner.scrollIntoView({ behavior: "smooth", block: "nearest" });
        }

        // Citizen Confirm Received (Need Reached)
        document.querySelectorAll(".confirm-received-btn").forEach((btn) => {
          btn.addEventListener("click", async () => {
            const allocationId = btn.dataset.allocationId;
            if (
              !confirm(
                "Confirm that you have physically received these relief supplies?"
              )
            ) {
              return;
            }
            btn.disabled = true;
            btn.textContent = "Confirming...";

            try {
              const res = await fetch(
                contextPath +
                  "/api/allocations/" +
                  allocationId +
                  "/confirm-received",
                { method: "POST" }
              );
              const data = await res.json();
              if (res.ok) {
                showGlobalBanner(
                  data.message ||
                    "Receipt confirmed! Relief timeline marked as Need Reached.",
                  false
                );
                setTimeout(() => window.location.reload(), 850);
              } else {
                showGlobalBanner(
                  data.error || "Could not confirm receipt.",
                  true
                );
                btn.disabled = false;
                btn.textContent = "\u2713 Confirm Received (Need Reached)";
              }
            } catch (err) {
              showGlobalBanner("Network error confirming receipt.", true);
              btn.disabled = false;
              btn.textContent = "\u2713 Confirm Received (Need Reached)";
            }
          });
        });

        // Live Gemini AI Preview
        document
          .getElementById("aiAnalyzeBtn")
          .addEventListener("click", async () => {
            const desc = document.getElementById("description").value.trim();
            const qty = document.getElementById("quantityRequired").value;
            const disasterId = document.getElementById("disasterId").value;
            const affectedCnt = document.getElementById(
              "affectedPeopleCount"
            ).value;
            const criticalCnt = document.getElementById(
              "criticalPeopleCount"
            ).value;

            if (desc.length < 5) {
              showBanner(
                "Enter at least 5 characters in the description first.",
                true
              );
              return;
            }

            aiBox.style.display = "block";
            aiBox.textContent =
              "Running live AI triage, priority scoring & semantic duplicate check...";

            const params = new URLSearchParams({
              description: desc,
              quantityRequired: qty,
              disasterId: disasterId,
            });
            if (affectedCnt !== "")
              params.append("affectedPeopleCount", affectedCnt);
            if (criticalCnt !== "")
              params.append("criticalPeopleCount", criticalCnt);

            try {
              const res = await fetch(contextPath + "/api/needs/ai-analyze", {
                method: "POST",
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded",
                },
                body: params,
              });
              const data = await res.json();
              if (res.ok) {
                const simPct = ((data.similarityScore || 0) * 100).toFixed(0);
                const dupFlag = data.isDuplicateSuspected
                  ? "Warning: Potential duplicate detected (Similarity: " +
                    simPct +
                    "%)"
                  : "Unique request (Similarity: " + simPct + "%)";

                aiBox.textContent = "";
                const strongEl = document.createElement("strong");
                strongEl.textContent =
                  data.aiSource === "GEMINI_LIVE"
                    ? "\u2728 Gemini AI Triage: "
                    : "AI Triage: ";
                aiBox.appendChild(strongEl);
                aiBox.appendChild(
                  document.createTextNode(
                    "Category = " +
                      data.suggestedCategory +
                      " | Urgency = " +
                      data.suggestedUrgency
                  )
                );
                aiBox.appendChild(document.createElement("br"));
                aiBox.appendChild(document.createTextNode(dupFlag));

                if (data.aiReasoning) {
                  aiBox.appendChild(document.createElement("br"));
                  const reasonSpan = document.createElement("span");
                  reasonSpan.style.color = "#bae6fd";
                  reasonSpan.textContent = "Insight: " + data.aiReasoning;
                  aiBox.appendChild(reasonSpan);
                }
              } else {
                aiBox.textContent = data.error || "AI analysis unavailable.";
              }
            } catch (err) {
              aiBox.textContent = "Could not reach AI service.";
            }
          });

        document
          .getElementById("submitNeedForm")
          .addEventListener("submit", async (e) => {
            e.preventDefault();
            const btn = document.getElementById("submitNeedBtn");
            btn.disabled = true;
            btn.textContent = "Submitting...";

            const params = new URLSearchParams({
              disasterId: document.getElementById("disasterId").value,
              category: document.getElementById("category").value,
              description: document.getElementById("description").value,
              quantityRequired:
                document.getElementById("quantityRequired").value,
              urgency: document.getElementById("urgency").value,
              latitude: document.getElementById("latitude").value,
              longitude: document.getElementById("longitude").value,
            });
            const locName = document
              .getElementById("locationName")
              .value.trim();
            const affCnt = document.getElementById("affectedPeopleCount").value;
            const critCnt = document.getElementById(
              "criticalPeopleCount"
            ).value;
            if (locName) params.append("locationName", locName);
            if (affCnt !== "") params.append("affectedPeopleCount", affCnt);
            if (critCnt !== "") params.append("criticalPeopleCount", critCnt);

            try {
              const res = await fetch(contextPath + "/api/needs", {
                method: "POST",
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded",
                },
                body: params,
              });
              const data = await res.json();
              if (res.ok) {
                showBanner("Request submitted! Reloading dashboard...", false);
                setTimeout(() => window.location.reload(), 750);
              } else {
                showBanner(data.error || "Failed to submit request.", true);
              }
            } catch (err) {
              showBanner("Network error submitting request.", true);
            } finally {
              btn.disabled = false;
              btn.textContent = "Submit Request \u2192";
            }
          });

        // Chat Modal Logic
        let activeNeedId = null;
        let activeReceiverId = null;
        let chatPollTimer = null;
        const chatModal = document.getElementById("chatModal");
        const chatHistory = document.getElementById("chatHistory");
        const replyingBanner = document.getElementById("replyingToBanner");
        const replyingName = document.getElementById("replyingToName");
        const cancelReplyBtn = document.getElementById("cancelReplyBtn");

        function setReplyRecipient(id, name) {
          activeReceiverId = id;
          if (replyingBanner && replyingName) {
            replyingName.textContent = name;
            replyingBanner.style.display = "flex";
          }
          const input = document.getElementById("chatInput");
          if (input) {
            input.placeholder = "Replying directly to " + name + "...";
            input.focus();
          }
        }

        function clearReplyRecipient() {
          activeReceiverId = null;
          if (replyingBanner) {
            replyingBanner.style.display = "none";
          }
          const input = document.getElementById("chatInput");
          if (input) {
            input.placeholder = "Write message to donors / relief team...";
          }
        }

        if (cancelReplyBtn) {
          cancelReplyBtn.addEventListener("click", clearReplyRecipient);
        }

        function renderMessages(messages) {
          chatHistory.innerHTML = "";
          if (!messages || messages.length === 0) {
            const emptyEl = document.createElement("div");
            emptyEl.style.color = "#94a3b8";
            emptyEl.style.textAlign = "center";
            emptyEl.style.padding = "1.5rem 0";
            emptyEl.textContent = "No messages yet. Start coordinating below.";
            chatHistory.appendChild(emptyEl);
            return;
          }
          messages.forEach((m) => {
            const bubble = document.createElement("div");
            bubble.className =
              "chat-bubble" + (m.senderId === currentUserId ? " mine" : "");

            const headerEl = document.createElement("div");
            headerEl.style.fontSize = "0.72rem";
            headerEl.style.marginBottom = "0.2rem";
            headerEl.style.display = "flex";
            headerEl.style.justifyContent = "space-between";
            headerEl.style.gap = "0.5rem";

            const senderEl = document.createElement("strong");
            senderEl.textContent =
              m.senderId === currentUserId
                ? "You"
                : m.senderName || "User #" + m.senderId;

            const senderWrap = document.createElement("div");
            senderWrap.style.display = "flex";
            senderWrap.style.alignItems = "center";
            senderWrap.style.gap = "0.35rem";
            senderWrap.appendChild(senderEl);

            if (m.messageType === "BROADCAST") {
              const bBadge = document.createElement("span");
              bBadge.textContent = "BROADCAST";
              bBadge.style.cssText =
                "font-size:0.6rem; background:rgba(234,179,8,0.2); color:#fde047; padding:1px 5px; border-radius:4px; font-weight:600; border:1px solid rgba(234,179,8,0.35);";
              senderWrap.appendChild(bBadge);
            }
            headerEl.appendChild(senderWrap);

            const metaRight = document.createElement("div");
            metaRight.style.display = "flex";
            metaRight.style.alignItems = "center";
            metaRight.style.gap = "0.4rem";

            if (m.senderId !== currentUserId) {
              const replyBtn = document.createElement("button");
              replyBtn.type = "button";
              replyBtn.textContent = "\u21a9 Reply";
              replyBtn.style.cssText =
                "background:rgba(255,255,255,0.08); border:none; color:#38bdf8; font-size:0.68rem; padding:1px 6px; border-radius:4px; cursor:pointer;";
              replyBtn.title = "Direct reply to this sender";
              replyBtn.onclick = () => {
                setReplyRecipient(
                  m.senderId,
                  m.senderName || "User #" + m.senderId
                );
              };
              metaRight.appendChild(replyBtn);
            }

            if (m.sentAt) {
              const timeEl = document.createElement("span");
              timeEl.style.color = "#94a3b8";
              timeEl.style.fontSize = "0.68rem";
              try {
                const d = new Date(m.sentAt);
                timeEl.textContent = d.toLocaleTimeString([], {
                  hour: "2-digit",
                  minute: "2-digit",
                });
              } catch (e) {}
              metaRight.appendChild(timeEl);
            }
            headerEl.appendChild(metaRight);
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
            chatHistory.innerHTML =
              "<div style='color:#94a3b8; text-align:center; padding:1.5rem 0;'>Loading messages...</div>";
          }
          try {
            const res = await fetch(contextPath + "/api/messages/" + needId, {
              headers: { "X-Expected-User-Id": currentUserId },
            });
            if (!res.ok) {
              if (
                res.status === 401 ||
                res.status === 403 ||
                res.status === 409
              ) {
                stopChatPolling();
                const reason =
                  res.status === 401
                    ? "Session expired. Please log in again."
                    : res.status === 409
                    ? "Session user mismatch. Please refresh your page."
                    : "You are not authorized to participate in this conversation.";
                chatHistory.innerHTML =
                  "<div style='color:#fca5a5; text-align:center; padding:1rem 0; font-size:0.85rem;'>" +
                  reason +
                  "</div>";
                return;
              }
              if (!isPolling) {
                chatHistory.innerHTML =
                  "<div style='color:#fca5a5; text-align:center;'>Unable to load messages (Status " +
                  res.status +
                  ").</div>";
              }
              return;
            }
            const data = await res.json();
            renderMessages(data.messages || []);
          } catch (err) {
            if (!isPolling) {
              chatHistory.innerHTML =
                "<div style='color:#fca5a5; text-align:center;'>Network error loading messages.</div>";
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
          clearReplyRecipient();
          activeNeedId = null;
        }

        document.querySelectorAll(".chat-open-btn").forEach((btn) => {
          btn.addEventListener("click", () => {
            activeNeedId = btn.dataset.needId;
            clearReplyRecipient();
            if (btn.dataset.donorId && btn.dataset.donorName) {
              setReplyRecipient(
                Number(btn.dataset.donorId),
                btn.dataset.donorName
              );
            }
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

        document
          .getElementById("closeChatBtn")
          .addEventListener("click", closeChatModal);
        chatModal.addEventListener("click", (e) => {
          if (e.target === chatModal) closeChatModal();
        });
        document.addEventListener("keydown", (e) => {
          if (e.key === "Escape" && chatModal.style.display === "flex")
            closeChatModal();
        });

        document
          .getElementById("sendChatForm")
          .addEventListener("submit", async (e) => {
            e.preventDefault();
            if (!activeNeedId) return;
            const input = document.getElementById("chatInput");
            const sendBtn = document.getElementById("sendChatBtn");
            const text = input.value.trim();
            if (!text) return;

            sendBtn.disabled = true;
            try {
              const bodyParams = {
                needId: activeNeedId,
                content: text,
                messageType: activeReceiverId ? "DIRECT" : "BROADCAST",
              };
              if (activeReceiverId) {
                bodyParams.receiverId = activeReceiverId;
              }
              const res = await fetch(contextPath + "/api/messages", {
                method: "POST",
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded",
                  "X-Expected-User-Id": currentUserId,
                },
                body: new URLSearchParams(bodyParams),
              });
              const data = await res.json();
              if (res.ok) {
                input.value = "";
                await loadMessages(activeNeedId, false);
              } else {
                if (
                  res.status === 401 ||
                  res.status === 403 ||
                  res.status === 409
                ) {
                  stopChatPolling();
                }
                alert(
                  data.error ||
                    "Failed to send message (Status " + res.status + ")."
                );
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
