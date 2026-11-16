package com.relief.service;

import com.relief.dao.UserDAO;
import com.relief.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes
    private static final Set<String> VALID_ROLES = Set.of("AFFECTED_PERSON", "DONOR", "DEPARTMENT");
    private static final String FAST2SMS_URL = "https://www.fast2sms.com/dev/bulkV2";

    // Real Telegram Toggle
    @Value("${telegram.enabled:false}")
    private boolean telegramEnabled;

    @Value("${telegram.bot.token:}")
    private String telegramBotToken;

    @Value("${telegram.chat.id:}")
    private String telegramChatId;

    // Real SMS Toggle
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.api.key:}")
    private String smsApiKey;

    private final UserDAO userDAO;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private record OtpRecord(String otp, Instant expiresAt) {
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Dispatches OTP based on active mode:
     * 1. telegram.enabled=true -> Real OTP to phone via Telegram Bot
     * 2. sms.enabled=true -> Real OTP to phone via Fast2SMS
     * 3. Both false -> Developer Mode (prints OTP in terminal console)
     */
    public void generateAndSendOtp(String mobileNumber) {
        String normalized = normalizeMobile(mobileNumber);
        if (!normalized.matches("^[6-9]\\d{9}$")) {
            throw new IllegalArgumentException("Please enter a valid 10-digit Indian mobile number.");
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStore.put(normalized, new OtpRecord(otp, Instant.now().plusSeconds(OTP_VALIDITY_SECONDS)));

        if (telegramEnabled) {
            sendOtpViaTelegram(normalized, otp);
        } else if (smsEnabled) {
            sendSmsViaFast2Sms(normalized, otp);
        } else {
            System.out.println("==========================================");
            System.out.println("[OTP DEV MODE] Mobile: " + normalized + " | OTP: " + otp);
            System.out.println("==========================================");
        }
    }

    private void sendOtpViaTelegram(String mobileNumber, String otp) {
        if (telegramBotToken == null || telegramBotToken.isBlank()
                || telegramChatId == null || telegramChatId.isBlank()) {
            throw new IllegalStateException(
                    "Telegram OTP is enabled, but telegram.bot.token or telegram.chat.id is missing.");
        }

        String apiUrl = "https://api.telegram.org/bot" + telegramBotToken.trim() + "/sendMessage";
        String messageText = "Disaster Relief System\n"
                + "------------------------\n"
                + "Mobile: +91-" + mobileNumber + "\n"
                + "Your OTP is: " + otp + "\n"
                + "Valid for 5 minutes. Do not share this code.\n\n"
                + "- By Abhinanda, Akash, Aqeel and Adithya";

        String jsonPayload = "{"
                + "\"chat_id\":\"" + telegramChatId.trim() + "\","
                + "\"text\":\"" + escapeJson(messageText) + "\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || !response.body().contains("\"ok\":true")) {
                System.err.println("[TELEGRAM ERROR] Status: " + response.statusCode() + " | Body: " + response.body());
                throw new RuntimeException(
                        "Failed to send OTP via Telegram. Check bot token, chat ID, and ensure you sent /start to the bot.");
            }

            System.out.println("[TELEGRAM OTP SENT] Dispatched OTP for +91-" + mobileNumber);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to Telegram API: " + e.getMessage(), e);
        }
    }

    private void sendSmsViaFast2Sms(String mobileNumber, String otp) {
        if (smsApiKey == null || smsApiKey.isBlank()) {
            throw new IllegalStateException(
                    "SMS is enabled, but sms.api.key is missing in application-local.properties.");
        }

        String message = "Your Disaster Relief verification OTP is " + otp + ". Valid for 5 minutes.";
        String jsonPayload = "{"
                + "\"route\":\"q\","
                + "\"message\":\"" + message + "\","
                + "\"language\":\"english\","
                + "\"flash\":0,"
                + "\"numbers\":\"" + mobileNumber + "\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FAST2SMS_URL))
                    .header("authorization", smsApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || !response.body().contains("\"return\":true")) {
                System.err.println("[SMS ERROR] Status: " + response.statusCode() + " | Body: " + response.body());
                throw new RuntimeException("Failed to send SMS via gateway. Check API key or wallet balance.");
            }

            System.out.println("[SMS SENT] Real OTP dispatched to +91-" + mobileNumber);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to SMS gateway: " + e.getMessage(), e);
        }
    }

    public boolean verifyOtp(String mobileNumber, String submittedOtp) {
        String normalized = normalizeMobile(mobileNumber);
        if (submittedOtp == null || submittedOtp.isBlank()) {
            return false;
        }

        OtpRecord record = otpStore.get(normalized);
        if (record == null) {
            return false;
        }

        if (Instant.now().isAfter(record.expiresAt())) {
            otpStore.remove(normalized);
            return false;
        }

        if (record.otp().equals(submittedOtp.trim())) {
            otpStore.remove(normalized);
            return true;
        }

        return false;
    }

    public User findUserByMobile(String mobileNumber) {
        return userDAO.findByMobile(normalizeMobile(mobileNumber));
    }

    public User registerNewUser(String mobileNumber, String name, String role, double latitude, double longitude) {
        String normalized = normalizeMobile(mobileNumber);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (role == null || !VALID_ROLES.contains(role.trim().toUpperCase())) {
            throw new IllegalArgumentException("Invalid role selected.");
        }
        if (userDAO.findByMobile(normalized) != null) {
            throw new IllegalStateException("Mobile number is already registered.");
        }

        User user = new User();
        user.setMobileNumber(normalized);
        user.setName(name.trim());
        user.setRole(role.trim().toUpperCase());
        user.setLatitude(latitude);
        user.setLongitude(longitude);

        userDAO.save(user);
        return userDAO.findByMobile(normalized);
    }

    private String normalizeMobile(String mobileNumber) {
        return mobileNumber == null ? "" : mobileNumber.trim();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}