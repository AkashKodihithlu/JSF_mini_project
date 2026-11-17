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
            body.put("message", "Pledge recorded and matched successfully.");
            body.put("allocationSummary", result);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/pledges/{pledgeId}/withdraw")
    public ResponseEntity<?> withdrawPledge(@PathVariable int pledgeId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return buildError(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }
        if (!"DONOR".equals(user.getRole())) {
            return buildError(HttpStatus.FORBIDDEN, "Only Donors can withdraw their pledges.");
        }

        try {
            pledgeAllocationService.withdrawPledge(pledgeId, user.getUserId());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 200);
            body.put("message", "Pledge #" + pledgeId + " withdrawn successfully.");
            return ResponseEntity.ok(body);
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
            body.put("message", "Allocation #" + allocationId + " marked as " + status.toUpperCase());
            return ResponseEntity.ok(body);
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