<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
            padding: 0.75rem 1rem;
            border-radius: 6px;
            font-size: 0.86rem;
            margin-bottom: 1.25rem;
            display: none;
        }
        .msg-ok { background: rgba(22, 101, 52, 0.9); border: 1px solid #86efac; color: #dcfce7; }
        .msg-err { background: rgba(153, 27, 27, 0.9); border: 1px solid #fca5a5; color: #ffffff; }
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
        .dark-input {
            width: 100%;
            padding: 0.65rem 0.85rem;
            border-radius: 6px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            background: var(--navy-950);
            color: #ffffff;
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
                        SMART RESOURCE MATCHING
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
            <c:set var="totalOffered" value="${totalOffered + pl.quantityOffered}"/>
            <c:set var="totalAlloc" value="${totalAlloc + pl.quantityAllocated}"/>
        </c:forEach>

        <section class="kpi-grid">
            <div class="kpi-card">
                <span>Verified Needs Available</span>
                <strong style="color: #38bdf8;">${fn:length(matchedNeeds)}</strong>
            </div>
            <div class="kpi-card">
                <span>My Total Pledges</span>
                <strong>${fn:length(myPledges)}</strong>
            </div>
            <div class="kpi-card">
                <span>Units Pledged</span>
                <strong style="color: #fdba74;">${totalOffered}</strong>
            </div>
            <div class="kpi-card">
                <span>Units Allocated to Citizens</span>
                <strong style="color: #4ade80;">${totalAlloc}</strong>
            </div>
        </section>

        <div id="donorBanner" class="msg-banner"></div>

        <!-- Smart Matched Needs -->
        <section class="panel-card">
            <h2>Smart-Matched Relief Requests</h2>
            <p class="panel-sub">Department-verified needs ranked by urgency level and GPS distance from your location.</p>

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
                                    <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 0.85rem;">
                                        <c:out value="${m.need.description}"/>
                                    </p>
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
                                            Pledge Supplies
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
            <h2>My Resource Pledges</h2>
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
                                    <th>Allocated</th>
                                    <th>Surplus Pool</th>
                                    <th>Status</th>
                                    <th>Coordinate</th>
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
                                        <td><span class="badge score-pill">${p.status}</span></td>
                                        <td>
                                            <button type="button" class="btn-outline-glass chat-open-btn"
                                                    style="padding: 0.38rem 0.75rem; font-size: 0.8rem;"
                                                    data-need-id="${p.needId}"
                                                    data-need-title="Need #${p.needId}">
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
    </main>

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
            }

            document.querySelectorAll(".pledge-btn").forEach((btn) => {
                btn.addEventListener("click", async () => {
                    const needId = btn.dataset.needId;
                    const qtyInput = document.getElementById("pledgeQty_" + needId);
                    const qty = qtyInput ? qtyInput.value : 0;

                    btn.disabled = true;
                    btn.textContent = "Pledging...";

                    try {
                        const res = await fetch(contextPath + "/api/pledges", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: new URLSearchParams({ needId: needId, quantityOffered: qty })
                        });
                        const data = await res.json();
                        if (res.ok) {
                            showDonorBanner("Pledge confirmed! Updating allocations...", false);
                            setTimeout(() => window.location.reload(), 750);
                        } else {
                            showDonorBanner(data.error || "Could not process pledge.", true);
                        }
                    } catch (err) {
                        showDonorBanner("Network error submitting pledge.", true);
                    } finally {
                        btn.disabled = false;
                        btn.textContent = "Pledge Supplies";
                    }
                });
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
                    headerEl.appendChild(senderEl);

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
                    const res = await fetch(contextPath + "/api/messages/" + needId);
                    if (!res.ok) {
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
                        headers: { "Content-Type": "application/x-www-form-urlencoded" },
                        body: new URLSearchParams({ needId: activeNeedId, content: text })
                    });
                    const data = await res.json();
                    if (res.ok) {
                        input.value = "";
                        await loadMessages(activeNeedId, false);
                    } else {
                        alert(data.error || "Failed to send message.");
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