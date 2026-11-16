package com.relief.controller;

import com.relief.model.Alert;
import com.relief.model.Message;
import com.relief.model.User;
import com.relief.service.CommunicationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommunicationController {

    private final CommunicationService communicationService;

    public CommunicationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping("/messages/{needId}")
    public ResponseEntity<?> getMessages(@PathVariable int needId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        if (!communicationService.isUserAuthorizedForNeed(user, needId)) {
            return buildError(HttpStatus.FORBIDDEN, "You are not authorized to view messages for this relief need.");
        }

        List<Message> messages = communicationService.getMessagesForNeedAndUser(needId, user);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("needId", needId);
        body.put("messages", messages);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/messages")
    public ResponseEntity<?> postMessage(@RequestParam int needId,
            @RequestParam(required = false) Integer receiverId,
            @RequestParam(required = false) String content,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        if (content == null || content.trim().isEmpty()) {
            return buildError(HttpStatus.BAD_REQUEST, "Message cannot be empty.");
        }

        if (!communicationService.isUserAuthorizedForNeed(user, needId)) {
            return buildError(HttpStatus.FORBIDDEN, "You are not authorized to send messages on this relief need.");
        }

        try {
            communicationService.sendNeedMessage(needId, user, receiverId, content.trim());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 201);
            body.put("message", "Message sent.");
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        List<Alert> alerts = (user != null)
                ? communicationService.getRelevantAlertsForUser(user)
                : communicationService.getAllAlerts();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("count", alerts.size());
        body.put("alerts", alerts);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/alerts")
    public ResponseEntity<?> createAlert(@RequestParam(defaultValue = "1") int disasterId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "15.0") double radiusKm,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DEPARTMENT".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Department Officers can broadcast geo-alerts.");
        }

        double lat = (latitude != null) ? latitude : user.getLatitude();
        double lng = (longitude != null) ? longitude : user.getLongitude();

        try {
            communicationService.broadcastAlert(
                    disasterId, user.getUserId(), title, message, lat, lng, radiusKm);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 201);
            body.put("message", "Emergency geo-alert broadcasted within " + radiusKm + " km.");
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<?> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status.value());
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}
