<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Complete Registration | Disaster Relief Coordination System</title>
    <link
      rel="stylesheet"
      href="${pageContext.request.contextPath}/assets/css/style.css"
    />
    <style>
      .split-auth-page {
        min-height: 100vh;
        width: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        background: linear-gradient(
            105deg,
            rgba(8, 20, 43, 0.76) 0%,
            rgba(13, 33, 73, 0.52) 48%,
            rgba(5, 13, 29, 0.82) 100%
          ),
          url("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=1920&q=80")
            center center / cover no-repeat fixed;
        padding: 1.75rem 5%;
      }

      .top-brand-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        max-width: 1240px;
        margin: 0 auto;
      }

      .brand-text-stack {
        display: flex;
        flex-direction: column;
      }

      .brand-title {
        font-size: 1.25rem;
        font-weight: 700;
        letter-spacing: 0.06em;
        text-transform: uppercase;
        color: #ffffff;
        line-height: 1.1;
      }

      .brand-tagline {
        font-size: 0.7rem;
        font-weight: 700;
        letter-spacing: 0.16em;
        color: #fca5a5;
        text-transform: uppercase;
      }

      .split-container {
        width: 100%;
        max-width: 1180px;
        margin: auto;
        display: grid;
        grid-template-columns: 1.05fr 0.95fr;
        gap: 4rem;
        align-items: center;
        padding: 2rem 0;
      }

      .welcome-pane h1 {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-size: clamp(2.5rem, 4.5vw, 3.85rem);
        font-weight: 800;
        line-height: 1.08;
        color: #ffffff;
        letter-spacing: -0.02em;
        margin-bottom: 1.1rem;
      }

      .welcome-pane p {
        max-width: 440px;
        font-size: 0.95rem;
        color: rgba(248, 250, 252, 0.9);
        line-height: 1.65;
        margin-bottom: 1.5rem;
      }

      .verified-pill {
        display: inline-flex;
        align-items: center;
        gap: 0.45rem;
        background: rgba(22, 101, 52, 0.85);
        border: 1px solid #86efac;
        color: #dcfce7;
        padding: 0.4rem 0.9rem;
        border-radius: 999px;
        font-size: 0.84rem;
        font-weight: 600;
      }

      .form-pane {
        max-width: 420px;
        width: 100%;
        justify-self: center;
      }

      .form-pane h2 {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-size: 1.9rem;
        font-weight: 700;
        color: #ffffff;
        text-align: center;
        margin-bottom: 1.4rem;
      }

      .field-block {
        margin-bottom: 1rem;
      }

      .field-block label {
        display: block;
        font-size: 0.84rem;
        font-weight: 500;
        color: #f1f5f9;
        margin-bottom: 0.35rem;
      }

      .white-input,
      .white-select {
        width: 100%;
        border: none;
        border-radius: 3px;
        background: #ffffff;
        color: #0f172a;
        padding: 0.72rem 0.85rem;
        font-size: 0.95rem;
        font-weight: 500;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.22);
        transition: box-shadow 0.22s ease;
      }

      .white-input:focus,
      .white-select:focus {
        outline: none;
        box-shadow: 0 0 0 3px rgba(234, 88, 12, 0.55);
      }

      .coord-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0.65rem;
        margin-top: 0.5rem;
      }

      .gps-btn {
        width: 100%;
        padding: 0.55rem 0.85rem;
        border-radius: 3px;
        border: 1px solid rgba(255, 255, 255, 0.35);
        background: rgba(13, 33, 73, 0.75);
        color: #f8fafc;
        font-size: 0.82rem;
        font-weight: 600;
        cursor: pointer;
        transition: background 0.2s ease, border-color 0.2s ease;
      }

      .gps-btn:hover {
        background: rgba(37, 99, 235, 0.55);
        border-color: #38bdf8;
      }

      .status-banner {
        padding: 0.7rem 0.9rem;
        border-radius: 4px;
        font-size: 0.84rem;
        margin-bottom: 1rem;
        display: none;
      }

      .status-error {
        background: rgba(153, 27, 27, 0.92);
        border: 1px solid #fca5a5;
        color: #ffffff;
      }

      .status-info {
        background: rgba(13, 33, 73, 0.92);
        border: 1px solid #38bdf8;
        color: #e0f2fe;
      }

      @media (max-width: 860px) {
        .split-container {
          grid-template-columns: 1fr;
          gap: 2rem;
        }
        .welcome-pane {
          text-align: center;
        }
        .welcome-pane p {
          margin-left: auto;
          margin-right: auto;
        }
      }
    </style>
  </head>
  <body>
    <div class="split-auth-page">
      <header class="top-brand-bar">
        <a href="${pageContext.request.contextPath}/" class="brand-logo-wrap">
          <img
            src="${pageContext.request.contextPath}/assets/images/disasterManageLogo.png"
            onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/disasterManageLogo.jpeg';"
            alt="Disaster Management Logo"
            class="brand-logo-img"
          />
          <div class="brand-text-stack">
            <span class="brand-title">Disaster Relief</span>
            <span class="brand-tagline"
              >Prepare &bull; Respond &bull; Recover</span
            >
          </div>
        </a>
        <a
          href="${pageContext.request.contextPath}/logout"
          class="btn-outline-glass"
          style="padding: 0.5rem 1rem; font-size: 0.85rem"
        >
          Cancel
        </a>
      </header>

      <main class="split-container">
        <!-- Left Info Column -->
        <section class="welcome-pane reveal">
          <h1>Join the<br />Relief Network</h1>
          <p>
            Complete your profile to report urgent needs, pledge supplies to
            verified families, or coordinate district-wide relief operations.
          </p>
          <div class="verified-pill">
            &#10003; Verified Mobile: +91-${sessionScope.verifiedMobile}
          </div>
        </section>

        <!-- Right Registration Form -->
        <section class="form-pane reveal">
          <h2>Create Account</h2>

          <div id="alertBox" class="status-banner"></div>

          <form id="registerForm">
            <div class="field-block">
              <label for="name">Full Name / Organization Name</label>
              <input
                type="text"
                id="name"
                name="name"
                class="white-input"
                placeholder="Enter your full name"
                required
              />
            </div>

            <div class="field-block">
              <label for="role">Select Your Role</label>
              <select id="role" name="role" class="white-select" required>
                <option value="AFFECTED_PERSON">
                  Affected Person (Request Relief Resources)
                </option>
                <option value="DONOR">
                  Donor (Pledge &amp; Supply Relief Resources)
                </option>
                <option value="DEPARTMENT">
                  Department / Relief Officer (Verify &amp; Coordinate)
                </option>
              </select>
            </div>

            <div class="field-block">
              <label
                >Location Coordinates (For Nearby Matching &amp; Alerts)</label
              >
              <button type="button" id="detectLocationBtn" class="gps-btn">
                &#128205; Auto-Detect My Current GPS Location
              </button>
              <div class="coord-grid">
                <input
                  type="number"
                  step="any"
                  id="latitude"
                  name="latitude"
                  class="white-input"
                  value="13.2150"
                  placeholder="Latitude"
                  required
                />
                <input
                  type="number"
                  step="any"
                  id="longitude"
                  name="longitude"
                  class="white-input"
                  value="74.9960"
                  placeholder="Longitude"
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              id="registerBtn"
              class="btn-relief"
              style="width: 100%; margin-top: 0.5rem"
            >
              Complete Registration &rarr;
            </button>
          </form>
        </section>
      </main>

      <div></div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/cursor-effects.js"></script>
    <script>
      const contextPath = "${pageContext.request.contextPath}";
      const alertBox = document.getElementById("alertBox");
      const registerForm = document.getElementById("registerForm");
      const latInput = document.getElementById("latitude");
      const lngInput = document.getElementById("longitude");

      function showAlert(message, isError) {
        alertBox.textContent = message;
        alertBox.className =
          "status-banner " + (isError ? "status-error" : "status-info");
        alertBox.style.display = "block";
      }

      document
        .getElementById("detectLocationBtn")
        .addEventListener("click", () => {
          if (!navigator.geolocation) {
            showAlert("Geolocation is not supported by your browser.", true);
            return;
          }
          showAlert("Fetching your GPS coordinates...", false);
          navigator.geolocation.getCurrentPosition(
            (position) => {
              latInput.value = position.coords.latitude.toFixed(6);
              lngInput.value = position.coords.longitude.toFixed(6);
              showAlert("Location updated from GPS.", false);
            },
            () => {
              showAlert(
                "Could not access GPS. Using default coordinates.",
                true
              );
            }
          );
        });

      registerForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = document.getElementById("registerBtn");
        btn.disabled = true;
        btn.textContent = "Creating Account...";

        const formData = new URLSearchParams({
          name: document.getElementById("name").value,
          role: document.getElementById("role").value,
          latitude: latInput.value,
          longitude: lngInput.value,
        });

        try {
          const res = await fetch(contextPath + "/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: formData,
          });
          const data = await res.json();

          if (res.ok) {
            window.location.href = contextPath + data.nextUrl;
          } else {
            showAlert(data.error || "Registration failed.", true);
          }
        } catch (err) {
          showAlert("Network error while completing registration.", true);
        } finally {
          btn.disabled = false;
          btn.textContent = "Complete Registration \u2192";
        }
      });
    </script>
  </body>
</html>
