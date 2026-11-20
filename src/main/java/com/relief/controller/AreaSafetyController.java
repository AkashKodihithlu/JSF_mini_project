package com.relief.controller;

import com.relief.model.AreaSafetyAssessment;
import com.relief.model.User;
import com.relief.service.AreaSafetyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/safety-assessments")
public class AreaSafetyController {

    private final AreaSafetyService areaSafetyService;

    public AreaSafetyController(AreaSafetyService areaSafetyService) {
        this.areaSafetyService = areaSafetyService;
    }

    /**
     * Submit a new area safety assessment.
     * Restricted to authenticated DEPARTMENT officers.
     */
    @PostMapping
    public ResponseEntity<?> createAssessment(@RequestParam int disasterId,
                                             @RequestParam String areaName,
                                             @RequestParam double latitude,
                                             @RequestParam double longitude,
                                             @RequestParam double radiusKm,
                                             @RequestParam String safetyStatus,
                                             @RequestParam(required = false) Double floodLevelMeters,
                                             @RequestParam(required = false) String assessmentNotes,
                                             HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DEPARTMENT".equalsIgnoreCase(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Department Officers can create or update area safety assessments.");
        }

        try {
            AreaSafetyAssessment created = areaSafetyService.recordSafetyAssessment(
                    disasterId,
                    user,
                    areaName,
                    latitude,
                    longitude,
                    radiusKm,
                    safetyStatus,
                    floodLevelMeters,
                    assessmentNotes
            );

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 201);
            body.put("message", "Area safety assessment recorded successfully.");
            body.put("assessment", created);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            return buildError(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Get the latest safety assessment for each monitored area in a disaster event.
     */
    @GetMapping
    public ResponseEntity<?> getLatestAssessments(@RequestParam(defaultValue = "1") int disasterId,
                                                  HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        List<AreaSafetyAssessment> assessments = areaSafetyService.getLatestAssessmentsForDisaster(disasterId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("disasterId", disasterId);
        body.put("count", assessments.size());
        body.put("assessments", assessments);
        return ResponseEntity.ok(body);
    }

    /**
     * Get the chronological safety assessment history for a specific area,
     * demonstrating that historical assessments are preserved and not overwritten.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getAssessmentHistory(@RequestParam(defaultValue = "1") int disasterId,
                                                 @RequestParam String areaName,
                                                 @RequestParam(required = false) Double latitude,
                                                 @RequestParam(required = false) Double longitude,
                                                 HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (areaName == null || areaName.trim().isEmpty()) {
            return buildError(HttpStatus.BAD_REQUEST, "Area name parameter is required.");
        }

        List<AreaSafetyAssessment> history = areaSafetyService.getAssessmentHistoryForLocation(disasterId, areaName, latitude, longitude);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("disasterId", disasterId);
        body.put("areaName", areaName.trim());
        body.put("count", history.size());
        body.put("history", history);
        return ResponseEntity.ok(body);
    }

    public ResponseEntity<?> getAssessmentHistory(int disasterId, String areaName, HttpSession session) {
        return getAssessmentHistory(disasterId, areaName, null, null, session);
    }

    /**
     * Get aggregate geospatial and safety metrics for a disaster event.
     * Computes unique affected people using spatial clustering to prevent double counting.
     */
    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics(@RequestParam(defaultValue = "1") int disasterId,
                                        HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        Map<String, Object> metrics = areaSafetyService.getGeospatialMetrics(disasterId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.putAll(metrics);
        return ResponseEntity.ok(body);
    }

    /**
     * Evaluates safety for a specific geographic coordinate based on active assessment perimeters.
     * Returns UNKNOWN if the coordinate is not inside any monitored area assessment.
     */
    @GetMapping("/evaluate")
    public ResponseEntity<?> evaluatePoint(@RequestParam double latitude,
                                          @RequestParam double longitude,
                                          @RequestParam(defaultValue = "1") int disasterId,
                                          HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        Map<String, Object> evaluation = areaSafetyService.evaluatePointSafety(latitude, longitude, disasterId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("disasterId", disasterId);
        body.put("latitude", latitude);
        body.put("longitude", longitude);
        body.putAll(evaluation);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<?> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status.value());
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
