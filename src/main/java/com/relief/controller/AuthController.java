package com.relief.controller;

import com.relief.model.User;
import com.relief.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // --- JSP Page Routes ---

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        if (session.getAttribute("verifiedMobile") == null) {
            return "redirect:/login";
        }
        return "auth/register";
    }

    @GetMapping("/dashboard")
    public String routeToRoleDashboard(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        String role = user.getRole();
        if ("AFFECTED_PERSON".equals(role)) {
            return "redirect:/affected/dashboard";
        } else if ("DONOR".equals(role)) {
            return "redirect:/donor/dashboard";
        } else if ("DEPARTMENT".equals(role)) {
            return "redirect:/department/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // --- Auth API Endpoints ---

    @PostMapping("/auth/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendOtp(@RequestParam String mobileNumber,
            HttpSession session) {
        try {
            authService.generateAndSendOtp(mobileNumber);
            session.setAttribute("pendingMobile", mobileNumber.trim());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "OTP sent to +91-" + mobileNumber.trim());
            body.put("mobileNumber", mobileNumber.trim());
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return buildError(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }

    @PostMapping("/auth/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestParam String otp,
            HttpSession session) {
        String pendingMobile = (String) session.getAttribute("pendingMobile");
        if (pendingMobile == null) {
            return buildError(HttpStatus.BAD_REQUEST, "Session expired or no OTP requested. Please request a new OTP.");
        }

        if (!authService.verifyOtp(pendingMobile, otp)) {
            return buildError(HttpStatus.UNAUTHORIZED, "Invalid or expired OTP.");
        }

        session.removeAttribute("pendingMobile");
        User existingUser = authService.findUserByMobile(pendingMobile);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);

        if (existingUser != null) {
            session.setAttribute("loggedInUser", existingUser);
            body.put("message", "Login successful");
            body.put("isNewUser", false);
            body.put("role", existingUser.getRole());
            body.put("nextUrl", "/dashboard");
        } else {
            session.setAttribute("verifiedMobile", pendingMobile);
            body.put("message", "OTP verified. Complete user registration.");
            body.put("isNewUser", true);
            body.put("nextUrl", "/register");
        }

        return ResponseEntity.ok(body);
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestParam String name,
            @RequestParam String role,
            @RequestParam(defaultValue = "13.2150") double latitude,
            @RequestParam(defaultValue = "74.9960") double longitude,
            HttpSession session) {
        String verifiedMobile = (String) session.getAttribute("verifiedMobile");
        if (verifiedMobile == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Mobile number not verified. Please verify OTP first.");
        }

        try {
            User newUser = authService.registerNewUser(verifiedMobile, name, role, latitude, longitude);
            session.removeAttribute("verifiedMobile");
            session.setAttribute("loggedInUser", newUser);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 201);
            body.put("message", "Registration successful");
            body.put("userId", newUser.getUserId());
            body.put("role", newUser.getRole());
            body.put("nextUrl", "/dashboard");
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            return buildError(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    private ResponseEntity<?> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status.value());
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}