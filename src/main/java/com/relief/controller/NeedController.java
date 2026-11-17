package com.relief.controller;

import com.relief.model.Disaster;
import com.relief.model.Need;
import com.relief.model.User;
import com.relief.service.NeedService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NeedController {

    private final NeedService needService;

    public NeedController(NeedService needService) {
        this.needService = needService;
    }

    // --- Disasters Lookup ---

    @GetMapping("/disasters")
    public ResponseEntity<?> getActiveDisasters() {
        List<Disaster> disasters = needService.getActiveDisasters();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("count", disasters.size());
        body.put("disasters", disasters);
        return ResponseEntity.ok(body);
    }

    // --- AI Preview (Category Classification, Priority & Duplicate Check) ---

    @PostMapping("/needs/ai-analyze")
    public ResponseEntity<?> analyzeNeedWithAi(@RequestParam String description,
            @RequestParam(defaultValue = "1") int quantityRequired,
            @RequestParam(defaultValue = "1") int disasterId,
            HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (description == null || description.trim().length() < 5) {
            return buildError(HttpStatus.BAD_REQUEST, "Enter at least 5 characters for AI analysis.");
        }

        String cleanDesc = description.trim();
        String suggestedCategory = needService.classifyCategory(cleanDesc);
        String suggestedUrgency = needService.recommendPriority(cleanDesc, quantityRequired);

        List<String> existingDescriptions = new ArrayList<>();
        for (Need n : needService.getAllNeeds()) {
            if (n.getDisasterId() == disasterId && !"REJECTED".equalsIgnoreCase(n.getStatus())) {
                existingDescriptions.add(n.getDescription());
            }
        }

        Map<String, Object> dupCheck = needService.checkDuplicateNeed(cleanDesc, existingDescriptions);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("suggestedCategory", suggestedCategory);
        body.put("suggestedUrgency", suggestedUrgency);
        body.put("isDuplicateSuspected", dupCheck.get("isDuplicate"));
        body.put("similarityScore", dupCheck.get("similarity"));
        return ResponseEntity.ok(body);
    }

    // --- Submit Relief Need (Affected Person or Department Officer on behalf of
    // citizen) ---

    @PostMapping("/needs")
    public ResponseEntity<?> submitNeed(@RequestParam(defaultValue = "1") int disasterId,
            @RequestParam(defaultValue = "AUTO") String category,
            @RequestParam String description,
            @RequestParam int quantityRequired,
            @RequestParam(defaultValue = "AUTO") String urgency,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer affectedPeopleCount,
            @RequestParam(required = false) Integer criticalPeopleCount,
            @RequestParam(required = false) String locationName,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in to report a relief need.");
        }

        if (!"AFFECTED_PERSON".equals(user.getRole()) && !"DEPARTMENT".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Affected Persons or Department Officers can report needs.");
        }

        double lat = (latitude != null) ? latitude : user.getLatitude();
        double lng = (longitude != null) ? longitude : user.getLongitude();

        try {
            Map<String, Object> result = needService.submitNeed(
                    disasterId,
                    user.getUserId(),
                    category,
                    description,
                    quantityRequired,
                    urgency,
                    lat,
                    lng,
                    affectedPeopleCount,
                    criticalPeopleCount,
                    locationName);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 201);
            body.put("message", "Relief need submitted for department verification.");
            body.put("analysis", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // --- Department Verification (Approve / Reject / Flag Duplicate) ---

    @PostMapping("/needs/{needId}/verify")
    public ResponseEntity<?> verifyNeed(@PathVariable int needId,
            @RequestParam String decision,
            @RequestParam(required = false) String urgency,
            @RequestParam(defaultValue = "") String remarks,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DEPARTMENT".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Department Officers can verify relief needs.");
        }

        try {
            needService.verifyNeed(needId, user.getUserId(), decision, urgency, remarks);
            Need updated = needService.getNeedById(needId);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "Need #" + needId + " updated to " + updated.getStatus());
            body.put("need", updated);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // --- Need Feeds for Dashboards & Live Map ---

    @GetMapping("/needs")
    public ResponseEntity<?> getAllNeeds(@RequestParam(required = false) String status,
            HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        List<Need> needs = (status != null && !status.isBlank())
                ? needService.getPendingNeeds()
                : needService.getAllNeeds();

        if (status != null && !status.isBlank() && !"PENDING_VERIFICATION".equalsIgnoreCase(status.trim())) {
            List<Need> filtered = new ArrayList<>();
            for (Need n : needService.getAllNeeds()) {
                if (status.trim().equalsIgnoreCase(n.getStatus())) {
                    filtered.add(n);
                }
            }
            needs = filtered;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("count", needs.size());
        body.put("needs", needs);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/needs/my")
    public ResponseEntity<?> getMyNeeds(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        List<Need> myNeeds = needService.getNeedsByUser(user.getUserId());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("count", myNeeds.size());
        body.put("needs", myNeeds);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/needs/available")
    public ResponseEntity<?> getAvailableNeedsForDonors() {
        List<Need> available = needService.getAvailableNeedsForDonors();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("count", available.size());
        body.put("needs", available);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/needs/{needId}/verifications")
    public ResponseEntity<?> getVerificationAudit(@PathVariable int needId,
            HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        Need need = needService.getNeedById(needId);
        if (need == null) {
            return buildError(HttpStatus.NOT_FOUND, "Need #" + needId + " not found.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("needId", needId);
        body.put("history", needService.getVerificationHistory(needId));
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<?> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status.value());
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}