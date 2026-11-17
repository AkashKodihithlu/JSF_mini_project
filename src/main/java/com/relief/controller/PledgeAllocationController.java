package com.relief.controller;

import com.relief.model.User;
import com.relief.service.PledgeAllocationService;
import com.relief.service.PledgeAllocationService.MatchedNeed;
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
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PledgeAllocationController {

    private final PledgeAllocationService pledgeAllocationService;

    public PledgeAllocationController(PledgeAllocationService pledgeAllocationService) {
        this.pledgeAllocationService = pledgeAllocationService;
    }

    @GetMapping("/matching/needs")
    public ResponseEntity<?> getMatchedNeeds(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        List<MatchedNeed> matches = pledgeAllocationService.getMatchedNeedsForDonor(user);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("count", matches.size());
        body.put("matches", matches);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/pledges")
    public ResponseEntity<?> submitPledge(@RequestParam int needId,
            @RequestParam int quantityOffered,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DONOR".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Donors can submit resource pledges.");
        }

        try {
            Map<String, Object> result = pledgeAllocationService.createPledgeAndAllocate(
                    needId, user.getUserId(), quantityOffered);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 201);
            body.put("message", "Pledge recorded and connected with affected citizen.");
            body.put("allocationSummary", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Donor cancels an entire pledge with a mandatory description reason.
     */
    @PostMapping("/pledges/{pledgeId}/cancel")
    public ResponseEntity<?> cancelPledgeWithReason(@PathVariable int pledgeId,
            @RequestParam String reason,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DONOR".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Donors can cancel their pledges.");
        }

        try {
            Map<String, Object> result = pledgeAllocationService.cancelPledgeWithReason(
                    pledgeId, user.getUserId(), reason);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "Pledge #" + pledgeId + " cancelled and quantities restored.");
            body.put("cancellationSummary", result);
            return ResponseEntity.ok(body);
        } catch (SecurityException e) {
            return buildError(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Backward-compatible withdraw endpoint (supports optional reason parameter).
     */
    @PostMapping("/pledges/{pledgeId}/withdraw")
    public ResponseEntity<?> withdrawPledge(@PathVariable int pledgeId,
            @RequestParam(defaultValue = "Withdrawn by donor.") String reason,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DONOR".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Donors can withdraw their pledges.");
        }

        try {
            Map<String, Object> result = pledgeAllocationService.cancelPledgeWithReason(
                    pledgeId, user.getUserId(), reason);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "Pledge #" + pledgeId + " withdrawn successfully.");
            body.put("cancellationSummary", result);
            return ResponseEntity.ok(body);
        } catch (SecurityException e) {
            return buildError(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/pledges/my")
    public ResponseEntity<?> getMyPledges(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("pledges", pledgeAllocationService.getPledgesByDonor(user.getUserId()));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/pledges/surplus")
    public ResponseEntity<?> getSurplusPledges(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("surplusPledges", pledgeAllocationService.getSurplusPledges());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/allocations/reallocate")
    public ResponseEntity<?> reallocateSurplus(@RequestParam int pledgeId,
            @RequestParam int targetNeedId,
            @RequestParam int quantity,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DEPARTMENT".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Department Officers can reallocate surplus stock.");
        }

        try {
            pledgeAllocationService.reallocateSurplus(pledgeId, targetNeedId, quantity);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "Surplus quantity reallocated to Need #" + targetNeedId);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Advances horizontal timeline stages (ALLOCATED -> IN_TRANSIT -> DELIVERED).
     */
    @PostMapping("/allocations/{allocationId}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable int allocationId,
            @RequestParam String status,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DEPARTMENT".equals(user.getRole()) && !"DONOR".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Unauthorized to update delivery status.");
        }

        try {
            pledgeAllocationService.updateAllocationDeliveryStatus(allocationId, status);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message",
                    "Timeline updated: Allocation #" + allocationId + " is now " + status.toUpperCase(Locale.ROOT));
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Donor cancels an active allocation connection with a mandatory description
     * reason.
     */
    @PostMapping("/allocations/{allocationId}/cancel")
    public ResponseEntity<?> cancelAllocation(@PathVariable int allocationId,
            @RequestParam String reason,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DONOR".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Donors can cancel an active allocation.");
        }

        try {
            Map<String, Object> summary = pledgeAllocationService.cancelAllocationWithReason(
                    allocationId, user.getUserId(), reason);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "Allocation #" + allocationId + " cancelled. Need quantities have been reopened.");
            body.put("cancellationSummary", summary);
            return ResponseEntity.ok(body);
        } catch (SecurityException e) {
            return buildError(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Affected person confirms receipt of supplies, completing the timeline and
     * verifying the need is reached.
     */
    @PostMapping("/allocations/{allocationId}/confirm-received")
    public ResponseEntity<?> confirmReceived(@PathVariable int allocationId,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"AFFECTED_PERSON".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only the affected citizen can confirm receipt of supplies.");
        }

        try {
            Map<String, Object> summary = pledgeAllocationService.confirmAllocationReceived(
                    allocationId, user.getUserId());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "Receipt confirmed! Relief delivery timeline marked complete.");
            body.put("confirmationSummary", summary);
            return ResponseEntity.ok(body);
        } catch (SecurityException e) {
            return buildError(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/allocations/my")
    public ResponseEntity<?> getMyAllocations(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        Object allocations = switch (user.getRole()) {
            case "DONOR" -> pledgeAllocationService.getAllocationsByDonor(user.getUserId());
            case "AFFECTED_PERSON" -> pledgeAllocationService.getAllocationsByAffectedUser(user.getUserId());
            default -> pledgeAllocationService.getAllAllocations();
        };

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 200);
        body.put("allocations", allocations);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<?> buildError(HttpStatus status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status.value());
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
}