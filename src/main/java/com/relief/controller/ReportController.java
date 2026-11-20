package com.relief.controller;

import com.relief.model.Need;
import com.relief.model.User;
import com.relief.service.NeedService;
import com.relief.service.PdfReportService;
import com.relief.service.PledgeAllocationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final NeedService needService;
    private final PledgeAllocationService pledgeAllocationService;
    private final PdfReportService pdfReportService;

    public ReportController(NeedService needService,
            PledgeAllocationService pledgeAllocationService,
            PdfReportService pdfReportService) {
        this.needService = needService;
        this.pledgeAllocationService = pledgeAllocationService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAnalyticsSummary(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "error", "Please sign in first."));
        }

        List<Need> allNeeds = needService.getAllNeeds();
        int totalRequired = 0;
        int totalPledged = 0;
        int totalAllocated = 0;
        int totalRemaining = 0;
        int pendingCount = 0;
        int verifiedCount = 0;
        int fulfilledCount = 0;

        Map<String, Integer> categoryTotals = new LinkedHashMap<>();

        for (Need n : allNeeds) {
            totalRequired += n.getQuantityRequired();
            totalPledged += n.getQuantityPledged();
            totalAllocated += n.getQuantityAllocated();
            totalRemaining += n.getQuantityRemaining();

            if ("PENDING_VERIFICATION".equalsIgnoreCase(n.getStatus()))
                pendingCount++;
            else if ("FULFILLED".equalsIgnoreCase(n.getStatus()))
                fulfilledCount++;
            else if ("VERIFIED".equalsIgnoreCase(n.getStatus())
                    || "PARTIALLY_FULFILLED".equalsIgnoreCase(n.getStatus()))
                verifiedCount++;

            categoryTotals.merge(n.getCategory(), n.getQuantityRequired(), Integer::sum);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("status", 200);
        summary.put("totalNeeds", allNeeds.size());
        summary.put("pendingVerificationCount", pendingCount);
        summary.put("activeVerifiedCount", verifiedCount);
        summary.put("fulfilledCount", fulfilledCount);
        summary.put("totalQuantityRequired", totalRequired);
        summary.put("totalQuantityPledged", totalPledged);
        summary.put("totalQuantityAllocated", totalAllocated);
        summary.put("totalQuantityRemaining", totalRemaining);
        summary.put("totalPledges", pledgeAllocationService.getAllPledges().size());
        summary.put("totalAllocations", pledgeAllocationService.getAllAllocations().size());
        summary.put("categoryBreakdown", categoryTotals);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportNeedsCsv(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StringBuilder csv = new StringBuilder();
        csv.append(
                "Need ID,Disaster ID,Category,Description,Required,Pledged,Allocated,Remaining,Urgency,Status,Latitude,Longitude\n");

        for (Need n : needService.getAllNeeds()) {
            csv.append(n.getNeedId()).append(',')
                    .append(n.getDisasterId()).append(',')
                    .append(escapeCsv(n.getCategory())).append(',')
                    .append(escapeCsv(n.getDescription())).append(',')
                    .append(n.getQuantityRequired()).append(',')
                    .append(n.getQuantityPledged()).append(',')
                    .append(n.getQuantityAllocated()).append(',')
                    .append(n.getQuantityRemaining()).append(',')
                    .append(escapeCsv(n.getUrgency())).append(',')
                    .append(escapeCsv(n.getStatus())).append(',')
                    .append(n.getLatitude()).append(',')
                    .append(n.getLongitude()).append('\n');
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"disaster_relief_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportNeedsPdf(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"DEPARTMENT".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            byte[] pdfBytes = pdfReportService.generateDepartmentReport();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"disaster_relief_coordination_report.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}