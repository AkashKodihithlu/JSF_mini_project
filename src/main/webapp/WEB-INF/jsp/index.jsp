<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>
      Disaster Relief Coordination System | Prepare &bull; Respond &bull;
      Recover
    </title>
    <link
      rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/style.css"
    />
    <link
      rel="stylesheet"
      href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
    />
    <style>
      /* Top Glass Navbar */
      .main-nav {
        position: sticky;
        top: 0;
        z-index: 1000;
        background: rgba(5, 13, 29, 0.85);
        backdrop-filter: blur(12px);
        border-bottom: 1px solid var(--border-glass);
        padding: 0.85rem 5%;
      }

      .nav-inner {
        max-width: 1240px;
        margin: 0 auto;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 1rem;
      }

      .nav-links {
        display: flex;
        align-items: center;
        gap: 1.75rem;
      }

      .nav-links a {
        color: var(--text-muted);
        text-decoration: none;
        font-size: 0.9rem;
        font-weight: 600;
        transition: color 0.2s ease;
      }

      .nav-links a:hover {
        color: #ffffff;
      }

      .status-chip {
        display: inline-flex;
        align-items: center;
        gap: 0.45rem;
        padding: 0.35rem 0.8rem;
        border-radius: 999px;
        background: rgba(13, 33, 73, 0.8);
        border: 1px solid rgba(56, 189, 248, 0.3);
        font-size: 0.78rem;
        color: #e0f2fe;
      }

      .pulse-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #22c55e;
        box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7);
        animation: pulseRing 1.8s infinite;
      }

      @keyframes pulseRing {
        0% {
          box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7);
        }
        70% {
          box-shadow: 0 0 0 8px rgba(34, 197, 94, 0);
        }
        100% {
          box-shadow: 0 0 0 0 rgba(34, 197, 94, 0);
        }
      }

      /* Hero Banner */
      .hero-section {
        position: relative;
        padding: 5.5rem 5% 5rem;
        background: radial-gradient(
            circle at 82% 28%,
            rgba(220, 38, 38, 0.24),
            transparent 45%
          ),
          radial-gradient(
            circle at 18% 72%,
            rgba(37, 99, 235, 0.28),
            transparent 50%
          ),
          linear-gradient(
            135deg,
            rgba(5, 13, 29, 0.93) 0%,
            rgba(8, 20, 43, 0.84) 100%
          ),
          url("https://images.unsplash.com/photo-1547683905-f686c993aae5?auto=format&fit=crop&w=1920&q=80")
            center center / cover no-repeat;
        border-bottom: 1px solid var(--border-glass);
      }

      .hero-grid {
        max-width: 1240px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: 1.2fr 0.8fr;
        gap: 3rem;
        align-items: center;
      }

      .eyebrow-badge {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.38rem 0.95rem;
        border-radius: 999px;
        background: rgba(220, 38, 38, 0.16);
        border: 1px solid rgba(248, 113, 113, 0.4);
        color: #fecaca;
        font-family: "Rajdhani", sans-serif;
        font-size: 0.88rem;
        font-weight: 700;
        letter-spacing: 0.14em;
        text-transform: uppercase;
        margin-bottom: 1.15rem;
      }

      .hero-title {
        font-size: clamp(2.6rem, 5vw, 4.2rem);
        font-weight: 700;
        line-height: 1.05;
        text-transform: uppercase;
        margin-bottom: 1.15rem;
        color: #ffffff;
      }

      .hero-title span {
        background: linear-gradient(90deg, #38bdf8, #f97316);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      .hero-subtitle {
        font-size: 1.05rem;
        color: var(--text-muted);
        max-width: 580px;
        margin-bottom: 2rem;
      }

      .hero-cta-group {
        display: flex;
        gap: 1rem;
        flex-wrap: wrap;
      }

      .hero-emblem-card {
        background: rgba(13, 33, 73, 0.45);
        backdrop-filter: blur(14px);
        border: 1px solid var(--border-glass);
        border-radius: 16px;
        padding: 2rem;
        text-align: center;
        box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
        transition: transform 0.35s ease, border-color 0.35s ease;
      }

      .hero-emblem-card:hover {
        transform: translateY(-6px);
        border-color: rgba(234, 88, 12, 0.55);
      }

      .hero-emblem-card img {
        width: 210px;
        height: 210px;
        object-fit: contain;
        border-radius: 50%;
        margin-bottom: 1rem;
        filter: drop-shadow(0 10px 25px rgba(0, 0, 0, 0.6));
      }

      .hero-stats-strip {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 0.75rem;
        margin-top: 1.25rem;
        padding-top: 1.25rem;
        border-top: 1px solid var(--border-glass);
      }

      .stat-item strong {
        display: block;
        font-family: "Rajdhani", sans-serif;
        font-size: 1.45rem;
        color: #ffffff;
      }

      .stat-item span {
        font-size: 0.75rem;
        color: var(--text-muted);
        text-transform: uppercase;
        letter-spacing: 0.06em;
      }

      /* Section Containers */
      .section-wrap {
        max-width: 1240px;
        margin: 0 auto;
        padding: 4.5rem 5%;
      }

      .section-header {
        text-align: center;
        max-width: 680px;
        margin: 0 auto 3rem;
      }

      .section-header h2 {
        font-size: 2.35rem;
        text-transform: uppercase;
        margin-bottom: 0.5rem;
      }

      .section-header p {
        color: var(--text-muted);
        font-size: 0.98rem;
      }

      /* Role Cards with Imagery & Spotlight Hover */
      .roles-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(310px, 1fr));
        gap: 1.75rem;
      }

      .role-card {
        position: relative;
        background: var(--navy-900);
        border: 1px solid var(--border-glass);
        border-radius: 12px;
        overflow: hidden;
        transition: transform 0.3s ease, border-color 0.3s ease,
          box-shadow 0.3s ease;
      }

      .role-card::before {
        content: "";
        position: absolute;
        inset: 0;
        background: radial-gradient(
          350px circle at var(--mx, 50%) var(--my, 50%),
          rgba(234, 88, 12, 0.16),
          transparent 65%
        );
        opacity: 0;
        transition: opacity 0.3s ease;
        pointer-events: none;
        z-index: 2;
      }

      .role-card:hover::before {
        opacity: 1;
      }

      .role-card:hover {
        transform: translateY(-6px);
        border-color: rgba(56, 189, 248, 0.45);
        box-shadow: 0 18px 40px rgba(0, 0, 0, 0.4);
      }

      .role-card-img {
        height: 185px;
        width: 100%;
        object-fit: cover;
        display: block;
        transition: transform 0.5s ease;
      }

      .role-card:hover .role-card-img {
        transform: scale(1.06);
      }

      .role-card-body {
        padding: 1.5rem;
        position: relative;
        z-index: 3;
      }

      .role-tag {
        display: inline-block;
        font-size: 0.72rem;
        font-weight: 700;
        letter-spacing: 0.1em;
        text-transform: uppercase;
        padding: 0.25rem 0.65rem;
        border-radius: 4px;
        margin-bottom: 0.75rem;
      }

      .tag-red {
        background: rgba(220, 38, 38, 0.2);
        color: #fca5a5;
      }
      .tag-blue {
        background: rgba(37, 99, 235, 0.25);
        color: #93c5fd;
      }
      .tag-amber {
        background: rgba(234, 88, 12, 0.22);
        color: #fdba74;
      }

      .role-card-body h3 {
        font-size: 1.5rem;
        margin-bottom: 0.5rem;
      }

      .role-card-body p {
        color: var(--text-muted);
        font-size: 0.92rem;
        margin-bottom: 1.25rem;
      }

      /* Features Grid */
      .features-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 1.25rem;
      }

      .feature-card {
        background: rgba(13, 33, 73, 0.38);
        border: 1px solid var(--border-glass);
        border-radius: 10px;
        padding: 1.5rem;
        transition: transform 0.25s ease, border-color 0.25s ease;
      }

      .feature-card:hover {
        transform: translateY(-4px);
        border-color: var(--flame-500);
      }

      .feature-icon {
        width: 42px;
        height: 42px;
        border-radius: 8px;
        background: linear-gradient(
          135deg,
          rgba(220, 38, 38, 0.25),
          rgba(37, 99, 235, 0.25)
        );
        border: 1px solid rgba(255, 255, 255, 0.15);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.2rem;
        margin-bottom: 1rem;
      }

      .feature-card h3 {
        font-size: 1.25rem;
        margin-bottom: 0.4rem;
      }

      .feature-card p {
        color: var(--text-muted);
        font-size: 0.88rem;
      }

      /* Map Panel */
      .map-shell {
        background: var(--navy-900);
        border: 1px solid var(--border-glass);
        border-radius: 14px;
        padding: 1.25rem;
        box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
      }

      #map {
        height: 390px;
        border-radius: 10px;
        z-index: 1;
      }

      /* Footer */
      .site-footer {
        background: #030812;
        border-top: 1px solid var(--border-glass);
        padding: 2.5rem 5%;
        text-align: center;
        color: var(--text-muted);
        font-size: 0.88rem;
      }

      @media (max-width: 900px) {
        .hero-grid {
          grid-template-columns: 1fr;
          text-align: center;
        }
        .hero-subtitle {
          margin-left: auto;
          margin-right: auto;
        }
        .hero-cta-group {
          justify-content: center;
        }
        .nav-links {
          display: none;
        }
      }
    </style>
  </head>
  <body>
    <!-- Navigation Bar -->
    <nav class="main-nav">
      <div class="nav-inner">
        <a href="${pageContext.request.contextPath}/" class="brand-logo-wrap">
          <img
            src="${pageContext.request.contextPath}/assets/images/disasterManageLogo.png"
            onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/disasterManageLogo.jpeg';"
            alt="Disaster Management Emblem"
            class="brand-logo-img"
          />
          <div>
            <div
              style="
                font-family: 'Rajdhani', sans-serif;
                font-weight: 700;
                font-size: 1.2rem;
                letter-spacing: 0.05em;
              "
            >
              DISASTER RELIEF
            </div>
            <div
              style="
                font-size: 0.68rem;
                color: #fca5a5;
                letter-spacing: 0.14em;
                font-weight: 700;
              "
            >
              PREPARE &bull; RESPOND &bull; RECOVER
            </div>
          </div>
        </a>

        <div class="nav-links">
          <a href="#roles">Portals</a>
          <a href="#capabilities">Capabilities</a>
          <a href="#live-map">Live Relief Map</a>
        </div>

        <div style="display: flex; align-items: center; gap: 0.85rem">
          <span class="status-chip">
            <span class="pulse-dot"></span>
            DB: ${empty dbStatus ? 'Active' : dbStatus} &bull; Users: ${empty
            userCount ? 3 : userCount}
          </span>
          <a
            href="${pageContext.request.contextPath}/login"
            class="btn-relief"
            style="padding: 0.55rem 1.2rem; font-size: 0.88rem"
          >
            Sign In
          </a>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-grid">
        <div class="reveal">
          <div class="eyebrow-badge">
            &#9888; Unified Emergency Response Platform
          </div>
          <h1 class="hero-title">
            Rapid Relief.<br />
            <span>Verified Coordination.</span>
          </h1>
          <p class="hero-subtitle">
            Bridging affected communities, verified donors, and disaster
            management authorities through AI-assisted need classification,
            smart resource matching, and real-time geo-alerts.
          </p>
          <div class="hero-cta-group">
            <a
              href="${pageContext.request.contextPath}/login"
              class="btn-relief"
            >
              Access Relief Portal &rarr;
            </a>
            <a href="#live-map" class="btn-outline-glass"> Explore Live Map </a>
          </div>
        </div>

        <div class="hero-emblem-card reveal">
          <img
            src="${pageContext.request.contextPath}/assets/images/disasterManageLogo.png"
            onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/disasterManageLogo.jpeg';"
            alt="Disaster Management Shield Emblem"
          />
          <h3 style="font-size: 1.35rem; letter-spacing: 0.1em; color: #ffffff">
            PREPARE &bull; RESPOND &bull; RECOVER
          </h3>
          <p
            style="
              font-size: 0.85rem;
              color: var(--text-muted);
              margin-top: 0.35rem;
            "
          >
            Real-time tracking of required, pledged, allocated, and remaining
            supplies.
          </p>
          <div class="hero-stats-strip">
            <div class="stat-item">
              <strong>3</strong>
              <span>Unified Roles</span>
            </div>
            <div class="stat-item">
              <strong>AI + ML</strong>
              <span>Duplicate Filter</span>
            </div>
            <div class="stat-item">
              <strong>24/7</strong>
              <span>Geo-Alerts</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Three Actor Portals -->
    <section id="roles" class="section-wrap">
      <div class="section-header reveal">
        <h2>Coordinated Response Ecosystem</h2>
        <p>
          Purpose-built workflows for every participant during floods, cyclones,
          landslides, and emergencies.
        </p>
      </div>

      <div class="roles-grid">
        <!-- Affected Person -->
        <div class="role-card spotlight-card reveal">
          <img
            class="role-card-img"
            src="https://images.unsplash.com/photo-1593113598332-cd288d649433?auto=format&fit=crop&w=800&q=80"
            alt="Emergency relief supplies"
          />
          <div class="role-card-body">
            <span class="role-tag tag-red">Affected Citizen</span>
            <h3>Report Urgent Needs</h3>
            <p>
              Submit food, water, medicine, or shelter requests with automatic
              GPS coordinates and track fulfillment status in real time.
            </p>
            <a
              href="${pageContext.request.contextPath}/login"
              class="btn-outline-glass"
              style="width: 100%"
            >
              Request Assistance &rarr;
            </a>
          </div>
        </div>

        <!-- Donor -->
        <div class="role-card spotlight-card reveal">
          <img
            class="role-card-img"
            src="https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=800&q=80"
            alt="Community relief volunteers"
          />
          <div class="role-card-body">
            <span class="role-tag tag-blue">Verified Donor</span>
            <h3>Pledge &amp; Match Resources</h3>
            <p>
              Discover nearby department-verified requests ranked by urgency,
              commit full or partial pledges, and chat directly with recipients.
            </p>
            <a
              href="${pageContext.request.contextPath}/login"
              class="btn-outline-glass"
              style="width: 100%"
            >
              Pledge Supplies &rarr;
            </a>
          </div>
        </div>

        <!-- Department -->
        <div class="role-card spotlight-card reveal">
          <img
            class="role-card-img"
            src="https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80"
            alt="Relief analytics command center"
          />
          <div class="role-card-body">
            <span class="role-tag tag-amber">Relief Authority</span>
            <h3>Verify &amp; Command</h3>
            <p>
              Audit incoming requests with AI duplicate checks, reallocate
              surplus pledges, issue radius alerts, and export PDF/Excel
              reports.
            </p>
            <a
              href="${pageContext.request.contextPath}/login"
              class="btn-outline-glass"
              style="width: 100%"
            >
              Open Command Desk &rarr;
            </a>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Capabilities -->
    <section id="capabilities" class="section-wrap" style="padding-top: 1rem">
      <div class="section-header reveal">
        <h2>Core System Capabilities</h2>
        <p>
          Engineered to eliminate duplicated requests, delayed allocations, and
          fragmented communication.
        </p>
      </div>

      <div class="features-grid">
        <div class="feature-card reveal">
          <div class="feature-icon">&#129302;</div>
          <h3>AI Classification &amp; Dedup</h3>
          <p>
            Python scikit-learn &amp; Gemini API automatically categorize needs,
            flag duplicate entries, and score priority.
          </p>
        </div>
        <div class="feature-card reveal">
          <div class="feature-icon">&#127919;</div>
          <h3>Smart Donor Matching</h3>
          <p>
            Matches donor capacity against verified needs using proximity,
            urgency tier, category, and remaining quantity.
          </p>
        </div>
        <div class="feature-card reveal">
          <div class="feature-icon">&#128225;</div>
          <h3>Radius Geo-Alerts</h3>
          <p>
            Relief officers broadcast targeted emergency alerts to citizens and
            donors within a defined kilometer radius.
          </p>
        </div>
        <div class="feature-card reveal">
          <div class="feature-icon">&#128202;</div>
          <h3>Audit &amp; Multi-Format Reports</h3>
          <p>
            Track required vs. allocated stock with Chart.js dashboards and
            one-click PDF, Excel, and CSV exports.
          </p>
        </div>
      </div>
    </section>

    <!-- Interactive Relief Map -->
    <section id="live-map" class="section-wrap" style="padding-top: 1rem">
      <div class="section-header reveal">
        <h2>Live Disaster Relief Map</h2>
        <p>
          Real-time geospatial view of active command centers, verified relief
          zones, and resource distribution.
        </p>
      </div>

      <div class="map-shell reveal">
        <div id="map"></div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="site-footer">
      <img
        src="${pageContext.request.contextPath}/assets/images/disasterManageLogo.png"
        onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/disasterManageLogo.jpeg';"
        alt="Logo"
        style="
          width: 44px;
          height: 44px;
          border-radius: 50%;
          margin-bottom: 0.6rem;
        "
      />
      <p style="color: #f8fafc; font-weight: 600">
        Disaster Relief Coordination System
      </p>
      <p style="margin-top: 0.25rem">
        Prepare &bull; Respond &bull; Recover &mdash; Department of Computer
        Science &amp; Engineering
      </p>
    </footer>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/cursor-effects.js"></script>
  </body>
</html>
