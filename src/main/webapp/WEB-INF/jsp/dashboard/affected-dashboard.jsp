<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="jakarta.tags.core" %> <%@ taglib prefix="fn"
uri="jakarta.tags.functions" %>
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
      .workspace-grid {
        display: grid;
        grid-template-columns: 420px 1fr;
        gap: 1.75rem;
        align-items: start;
      }
      .panel-card {
        background: var(--navy-900);
        border: 1px solid var(--border-glass);
        border-radius: 12px;
        padding: 1.5rem;
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
        display: none;
      }
      .msg-banner {
        padding: 0.75rem 0.95rem;
        border-radius: 6px;
        font-size: 0.85rem;
        margin-bottom: 1rem;
        display: none;
      }
      .msg-ok {
        background: rgba(22, 101, 52, 0.88);
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
          <span>Verified &amp; Matching</span>
          <strong style="color: #38bdf8">${activeCnt}</strong>
        </div>
        <div class="kpi-card">
          <span>Fulfilled Requests</span>
          <strong style="color: #4ade80">${fulfilledCnt}</strong>
        </div>
      </section>

      <div class="workspace-grid">
        <!-- Left: Submit Relief Need Form -->
        <section class="panel-card">
          <h2>Report Relief Need</h2>
          <p class="panel-sub">
            Submit urgent supply requirements. AI can auto-detect category,
            priority, and duplicates.
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

            <div class="form-field">
              <label for="quantityRequired"
                >Quantity Required (Units / Kits / Liters)</label
              >
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
                &#129302; AI Preview
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
        const aiBox = document.getElementById("aiPreviewBox");

        function showBanner(msg, isError) {
          banner.textContent = msg;
          banner.className = "msg-banner " + (isError ? "msg-err" : "msg-ok");
          banner.style.display = "block";
        }

        document
          .getElementById("aiAnalyzeBtn")
          .addEventListener("click", async () => {
            const desc = document.getElementById("description").value.trim();
            const qty = document.getElementById("quantityRequired").value;
            const disasterId = document.getElementById("disasterId").value;

            if (desc.length < 5) {
              showBanner(
                "Enter at least 5 characters in the description first.",
                true
              );
              return;
            }

            aiBox.style.display = "block";
            aiBox.textContent =
              "Running AI classification & duplicate check...";

            try {
              const res = await fetch(contextPath + "/api/needs/ai-analyze", {
                method: "POST",
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded",
                },
                body: new URLSearchParams({
                  description: desc,
                  quantityRequired: qty,
                  disasterId: disasterId,
                }),
              });
              const data = await res.json();
              if (res.ok) {
                const dupFlag = data.isDuplicateSuspected
                  ? "Warning: Potential duplicate detected (Similarity: " +
                    (data.similarityScore * 100).toFixed(0) +
                    "%)"
                  : "Unique request (Similarity: " +
                    (data.similarityScore * 100).toFixed(0) +
                    "%)";
                aiBox.textContent = "";
                const strongEl = document.createElement("strong");
                strongEl.textContent = "AI Recommendation: ";
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
        const chatModal = document.getElementById("chatModal");
        const chatHistory = document.getElementById("chatHistory");

        async function loadMessages(needId) {
          chatHistory.innerHTML =
            "<div style='color:#94a3b8;'>Loading messages...</div>";
          try {
            const res = await fetch(contextPath + "/api/messages/" + needId);
            const data = await res.json();
            chatHistory.innerHTML = "";
            if (!data.messages || data.messages.length === 0) {
              const emptyEl = document.createElement("div");
              emptyEl.style.color = "#94a3b8";
              emptyEl.textContent =
                "No messages yet. Start coordinating below.";
              chatHistory.appendChild(emptyEl);
              return;
            }
            data.messages.forEach((m) => {
              const bubble = document.createElement("div");
              bubble.className =
                "chat-bubble" + (m.senderId === currentUserId ? " mine" : "");

              const senderEl = document.createElement("strong");
              senderEl.textContent = (m.senderName || "User") + ": ";
              bubble.appendChild(senderEl);
              bubble.appendChild(document.createTextNode(m.content || ""));

              chatHistory.appendChild(bubble);
            });
            chatHistory.scrollTop = chatHistory.scrollHeight;
          } catch (err) {
            chatHistory.innerHTML =
              "<div style='color:#fca5a5;'>Unable to load messages.</div>";
          }
        }

        document.querySelectorAll(".chat-open-btn").forEach((btn) => {
          btn.addEventListener("click", () => {
            activeNeedId = btn.dataset.needId;
            document.getElementById("chatModalTitle").textContent =
              "Need #" + activeNeedId + " — " + btn.dataset.needTitle;
            chatModal.style.display = "flex";
            loadMessages(activeNeedId);
          });
        });

        document
          .getElementById("closeChatBtn")
          .addEventListener("click", () => {
            chatModal.style.display = "none";
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
              const res = await fetch(contextPath + "/api/messages", {
                method: "POST",
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded",
                },
                body: new URLSearchParams({
                  needId: activeNeedId,
                  content: text,
                }),
              });
              if (res.ok) {
                input.value = "";
                await loadMessages(activeNeedId);
              }
            } finally {
              sendBtn.disabled = false;
            }
          });
      });
    </script>
  </body>
</html>
