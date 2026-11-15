<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Sign In | Disaster Relief Coordination System</title>
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
            rgba(8, 20, 43, 0.72) 0%,
            rgba(13, 33, 73, 0.45) 48%,
            rgba(5, 13, 29, 0.78) 100%
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
        grid-template-columns: 1.1fr 0.9fr;
        gap: 4rem;
        align-items: center;
        padding: 2rem 0;
      }

      /* Left Column (Matching Reference Layout) */
      .welcome-pane h1 {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-size: clamp(2.8rem, 5vw, 4.35rem);
        font-weight: 800;
        line-height: 1.06;
        color: #ffffff;
        letter-spacing: -0.02em;
        margin-bottom: 1.25rem;
        text-shadow: 0 4px 24px rgba(0, 0, 0, 0.35);
      }

      .welcome-pane p {
        max-width: 440px;
        font-size: 0.96rem;
        color: rgba(248, 250, 252, 0.9);
        line-height: 1.65;
        margin-bottom: 1.75rem;
      }

      .motto-pills {
        display: flex;
        gap: 0.6rem;
        flex-wrap: wrap;
        margin-bottom: 1.75rem;
      }

      .motto-pill {
        font-family: "Rajdhani", sans-serif;
        font-size: 0.85rem;
        font-weight: 700;
        letter-spacing: 0.12em;
        padding: 0.3rem 0.8rem;
        border-radius: 999px;
        background: rgba(8, 20, 43, 0.65);
        border: 1px solid rgba(255, 255, 255, 0.22);
        color: #f8fafc;
      }

      .social-row {
        display: flex;
        align-items: center;
        gap: 1.1rem;
      }

      .social-icon {
        width: 34px;
        height: 34px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.14);
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        text-decoration: none;
        transition: transform 0.25s ease, background-color 0.25s ease;
      }

      .social-icon:hover {
        transform: translateY(-3px) scale(1.08);
        background: var(--flame-600);
      }

      /* Right Column Form (Matching Reference Crisp White Inputs) */
      .form-pane {
        max-width: 400px;
        width: 100%;
        justify-self: center;
      }

      .form-pane h2 {
        font-family: "Plus Jakarta Sans", sans-serif;
        font-size: 2rem;
        font-weight: 700;
        color: #ffffff;
        text-align: center;
        margin-bottom: 1.75rem;
        letter-spacing: -0.01em;
      }

      .field-block {
        margin-bottom: 1.15rem;
      }

      .field-block label {
        display: block;
        font-size: 0.85rem;
        font-weight: 500;
        color: #f1f5f9;
        margin-bottom: 0.4rem;
      }

      .white-input-wrap {
        display: flex;
        align-items: center;
        background: #ffffff;
        border-radius: 3px;
        overflow: hidden;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.22);
        transition: box-shadow 0.22s ease, transform 0.22s ease;
      }

      .white-input-wrap:focus-within {
        box-shadow: 0 0 0 3px rgba(234, 88, 12, 0.55);
        transform: translateY(-1px);
      }

      .prefix-tag {
        padding: 0.72rem 0.85rem;
        background: #f1f5f9;
        color: #0f172a;
        font-weight: 700;
        font-size: 0.95rem;
        border-right: 1px solid #cbd5e1;
      }

      .white-input {
        width: 100%;
        border: none;
        background: #ffffff;
        color: #0f172a;
        padding: 0.74rem 0.9rem;
        font-size: 0.98rem;
        font-weight: 500;
      }

      .white-input:focus {
        outline: none;
      }

      .otp-code-input {
        letter-spacing: 0.35rem;
        font-weight: 700;
        font-size: 1.1rem;
      }

      .remember-row {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin: 0.75rem 0 1.25rem;
        font-size: 0.84rem;
        color: #f1f5f9;
      }

      .remember-row input[type="checkbox"] {
        accent-color: var(--flame-600);
        width: 15px;
        height: 15px;
        cursor: pointer;
      }

      .btn-ref-submit {
        padding: 0.72rem 1.75rem;
        border-radius: 3px;
        font-size: 0.95rem;
      }

      .sub-links {
        margin-top: 1rem;
        display: flex;
        gap: 1.25rem;
        flex-wrap: wrap;
      }

      .text-action-btn {
        background: none;
        border: none;
        color: #f8fafc;
        font-size: 0.84rem;
        cursor: pointer;
        padding: 0;
        text-decoration: none;
        opacity: 0.9;
        transition: opacity 0.2s ease, color 0.2s ease;
      }

      .text-action-btn:hover {
        opacity: 1;
        color: #fdba74;
        text-decoration: underline;
      }

      .legal-caption {
        margin-top: 1.75rem;
        text-align: center;
        font-size: 0.78rem;
        color: rgba(241, 245, 249, 0.85);
        line-height: 1.5;
      }

      .legal-caption a {
        color: #ffffff;
        text-decoration: underline;
      }

      .status-banner {
        padding: 0.7rem 0.9rem;
        border-radius: 4px;
        font-size: 0.85rem;
        margin-bottom: 1rem;
        display: none;
      }

      .status-error {
        background: rgba(153, 27, 27, 0.9);
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
          gap: 2.25rem;
        }
        .welcome-pane {
          text-align: center;
        }
        .welcome-pane p {
          margin-left: auto;
          margin-right: auto;
        }
        .motto-pills,
        .social-row {
          justify-content: center;
        }
      }
    </style>
  </head>
  <body>
    <div class="split-auth-page">
      <!-- Top Brand Header -->
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
          href="${pageContext.request.contextPath}/"
          class="btn-outline-glass"
          style="padding: 0.5rem 1rem; font-size: 0.85rem"
        >
          &larr; Back to Home
        </a>
      </header>

      <!-- Split Hero Content -->
      <main class="split-container">
        <!-- Left Welcome Column -->
        <section class="welcome-pane reveal">
          <h1>Welcome<br />Back</h1>
          <p>
            Centralized emergency response connecting affected citizens,
            verified donors, and relief authorities with real-time resource
            matching and geo-alerts.
          </p>
          <div class="motto-pills">
            <span class="motto-pill">PREPARE</span>
            <span class="motto-pill">RESPOND</span>
            <span class="motto-pill">RECOVER</span>
          </div>
          <div class="social-row">
            <a
              href="${pageContext.request.contextPath}/"
              class="social-icon"
              title="Live Relief Map"
            >
              <svg
                width="16"
                height="16"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 010-5 2.5 2.5 0 010 5z"
                />
              </svg>
            </a>
            <a
              href="${pageContext.request.contextPath}/"
              class="social-icon"
              title="Emergency Broadcasts"
            >
              <svg
                width="16"
                height="16"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  d="M12 22c1.1 0 2-.9 2-2h-4a2 2 0 002 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"
                />
              </svg>
            </a>
            <a
              href="${pageContext.request.contextPath}/"
              class="social-icon"
              title="Verified Shield"
            >
              <svg
                width="16"
                height="16"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-1 6h2v4h4v2h-4v4h-2v-4H7v-2h4V7z"
                />
              </svg>
            </a>
          </div>
        </section>

        <!-- Right Sign-In Form Column -->
        <section class="form-pane reveal">
          <h2>Sign in</h2>

          <div id="alertBox" class="status-banner"></div>

          <!-- Step 1: Mobile Number -->
          <form id="sendOtpForm">
            <div class="field-block">
              <label for="mobileNumber">Mobile Number</label>
              <div class="white-input-wrap">
                <span class="prefix-tag">+91</span>
                <input
                  type="tel"
                  id="mobileNumber"
                  name="mobileNumber"
                  class="white-input"
                  maxlength="10"
                  pattern="[6-9][0-9]{9}"
                  placeholder="Enter 10-digit mobile"
                  required
                />
              </div>
            </div>

            <div class="remember-row">
              <input type="checkbox" id="rememberSession" checked />
              <label for="rememberSession" style="margin: 0; cursor: pointer"
                >Keep session active</label
              >
            </div>

            <button
              type="submit"
              id="sendOtpBtn"
              class="btn-relief btn-ref-submit"
            >
              Send OTP now
            </button>

            <div class="sub-links">
              <button
                type="button"
                class="text-action-btn"
                onclick="document.getElementById('mobileNumber').focus()"
              >
                New user? Verify mobile to register &rarr;
              </button>
            </div>
          </form>

          <!-- Step 2: OTP Verification -->
          <form id="verifyOtpForm" style="display: none">
            <div class="field-block">
              <label for="otp">One-Time Password (6 Digits)</label>
              <div class="white-input-wrap">
                <input
                  type="text"
                  id="otp"
                  name="otp"
                  class="white-input otp-code-input"
                  maxlength="6"
                  inputmode="numeric"
                  placeholder="••••••"
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              id="verifyOtpBtn"
              class="btn-relief btn-ref-submit"
            >
              Verify &amp; Sign in
            </button>

            <div class="sub-links">
              <button
                type="button"
                id="changeNumberBtn"
                class="text-action-btn"
              >
                Change mobile number
              </button>
              <button type="button" id="resendOtpBtn" class="text-action-btn">
                Resend OTP
              </button>
            </div>
          </form>

          <p class="legal-caption">
            By signing in you agree to the Disaster Relief<br />
            <a href="${pageContext.request.contextPath}/">Terms of Service</a> |
            <a href="${pageContext.request.contextPath}/">Privacy Policy</a>
          </p>
        </section>
      </main>

      <div></div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/cursor-effects.js"></script>
    <script>
      const contextPath = "${pageContext.request.contextPath}";
      const alertBox = document.getElementById("alertBox");
      const sendOtpForm = document.getElementById("sendOtpForm");
      const verifyOtpForm = document.getElementById("verifyOtpForm");
      const mobileInput = document.getElementById("mobileNumber");
      const otpInput = document.getElementById("otp");

      function showAlert(message, isError) {
        alertBox.textContent = message;
        alertBox.className =
          "status-banner " + (isError ? "status-error" : "status-info");
        alertBox.style.display = "block";
      }

      async function requestOtp(buttonElement) {
        buttonElement.disabled = true;
        const originalText = buttonElement.textContent;
        buttonElement.textContent = "Dispatching OTP...";

        try {
          const res = await fetch(contextPath + "/auth/send-otp", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ mobileNumber: mobileInput.value }),
          });
          const data = await res.json();

          if (res.ok) {
            showAlert(
              "OTP dispatched for +91-" +
                data.mobileNumber +
                ". Valid for 5 minutes.",
              false
            );
            sendOtpForm.style.display = "none";
            verifyOtpForm.style.display = "block";
            otpInput.value = "";
            otpInput.focus();
          } else {
            showAlert(data.error || "Failed to send OTP.", true);
          }
        } catch (err) {
          showAlert("Network error while sending OTP.", true);
        } finally {
          buttonElement.disabled = false;
          buttonElement.textContent = originalText;
        }
      }

      sendOtpForm.addEventListener("submit", (e) => {
        e.preventDefault();
        requestOtp(document.getElementById("sendOtpBtn"));
      });

      document.getElementById("resendOtpBtn").addEventListener("click", () => {
        requestOtp(document.getElementById("resendOtpBtn"));
      });

      verifyOtpForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = document.getElementById("verifyOtpBtn");
        btn.disabled = true;
        btn.textContent = "Verifying...";

        try {
          const res = await fetch(contextPath + "/auth/verify-otp", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ otp: otpInput.value }),
          });
          const data = await res.json();

          if (res.ok) {
            window.location.href = contextPath + data.nextUrl;
          } else {
            showAlert(data.error || "OTP verification failed.", true);
          }
        } catch (err) {
          showAlert("Network error while verifying OTP.", true);
        } finally {
          btn.disabled = false;
          btn.textContent = "Verify & Sign in";
        }
      });

      document
        .getElementById("changeNumberBtn")
        .addEventListener("click", () => {
          verifyOtpForm.style.display = "none";
          sendOtpForm.style.display = "block";
          alertBox.style.display = "none";
        });
    </script>
  </body>
</html>
