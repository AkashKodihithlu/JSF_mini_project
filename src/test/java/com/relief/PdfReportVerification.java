package com.relief;

import com.lowagie.text.pdf.PdfReader;
import com.relief.controller.ReportController;
import com.relief.model.Allocation;
import com.relief.model.Disaster;
import com.relief.model.Need;
import com.relief.model.Pledge;
import com.relief.model.User;
import com.relief.service.PdfReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfReportVerification {

    private static HttpSession createMockSession(Map<String, Object> attributes) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, methodArgs) -> {
                    String methodName = method.getName();
                    if ("getAttribute".equals(methodName)) {
                        return attributes.get(methodArgs[0]);
                    } else if ("setAttribute".equals(methodName)) {
                        attributes.put((String) methodArgs[0], methodArgs[1]);
                        return null;
                    } else if ("removeAttribute".equals(methodName)) {
                        return attributes.remove(methodArgs[0]);
                    }
                    return null;
                }
        );
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Assertion Failed: " + message + " [expected=" + expected + ", actual=" + actual + "]");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Running PDF Report Verification Suite ===");

        PdfReportService service = new PdfReportService(null, null);

        // -------------------------------------------------------------
        // Test 1: Empty dataset PDF Generation
        // -------------------------------------------------------------
        byte[] emptyPdf = service.generatePdfReport(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertTrue(emptyPdf != null && emptyPdf.length > 0, "Empty dataset PDF output must not be empty");

        try (PdfReader reader = new PdfReader(emptyPdf)) {
            assertEquals(1, reader.getNumberOfPages(), "Empty dataset PDF must contain exactly 1 page");
            System.out.println("[PASS] Test 1: Empty dataset produces a valid 1-page PDF (" + emptyPdf.length + " bytes).");
        } catch (Exception e) {
            throw new RuntimeException("Test 1 Failed: Generated PDF cannot be parsed by PdfReader", e);
        }

        // -------------------------------------------------------------
        // Test 2: Populated dataset PDF Generation
        // -------------------------------------------------------------
        List<Disaster> disasters = new ArrayList<>();
        Disaster d1 = new Disaster();
        d1.setDisasterId(1);
        d1.setName("Coastal Monsoon Flood 2026");
        d1.setType("Flood");
        d1.setStatus("ACTIVE");
        disasters.add(d1);

        Disaster d2 = new Disaster();
        d2.setDisasterId(2);
        d2.setName("Western Ghats Landslide");
        d2.setType("Landslide");
        d2.setStatus("CONTAINED");
        disasters.add(d2);

        List<Need> needs = new ArrayList<>();
        Need n1 = new Need();
        n1.setNeedId(101);
        n1.setDisasterId(1);
        n1.setCategory("Clean Drinking Water");
        n1.setDescription("500 liters potable water bottles required for Sector 4 relief camp.");
        n1.setQuantityRequired(500);
        n1.setQuantityAllocated(300);
        n1.setQuantityRemaining(200);
        n1.setUrgency("CRITICAL");
        n1.setStatus("PARTIALLY_FULFILLED");
        needs.add(n1);

        Need n2 = new Need();
        n2.setNeedId(102);
        n2.setDisasterId(1);
        n2.setCategory("Medical & First Aid");
        n2.setDescription("Emergency insulin, first aid kits, antiseptic bandages.");
        n2.setQuantityRequired(80);
        n2.setQuantityAllocated(80);
        n2.setQuantityRemaining(0);
        n2.setUrgency("HIGH");
        n2.setStatus("FULFILLED");
        needs.add(n2);

        Need n3 = new Need();
        n3.setNeedId(103);
        n3.setDisasterId(2);
        n3.setCategory("Shelter & Clothing");
        n3.setDescription("Waterproof tarpaulins and dry blankets for evacuated families.");
        n3.setQuantityRequired(150);
        n3.setQuantityAllocated(0);
        n3.setQuantityRemaining(150);
        n3.setUrgency("MEDIUM");
        n3.setStatus("PENDING_VERIFICATION");
        needs.add(n3);

        Need n4 = new Need();
        n4.setNeedId(104);
        n4.setDisasterId(999); // Unmatched disaster ID to test fallback
        n4.setCategory("Food & Rations");
        n4.setDescription("Packed ration kits with rice, pulses, and baby formula.");
        n4.setQuantityRequired(200);
        n4.setQuantityAllocated(0);
        n4.setQuantityRemaining(200);
        n4.setUrgency("LOW");
        n4.setStatus("VERIFIED");
        needs.add(n4);

        Need n5 = new Need();
        n5.setNeedId(105);
        n5.setDisasterId(1);
        n5.setCategory(null); // Null category fallback
        n5.setDescription(null); // Null description fallback
        n5.setQuantityRequired(25);
        n5.setQuantityAllocated(0);
        n5.setQuantityRemaining(25);
        n5.setUrgency(null); // Null urgency fallback
        n5.setStatus("REJECTED");
        needs.add(n5);

        List<Pledge> pledges = new ArrayList<>();
        Pledge p1 = new Pledge();
        p1.setPledgeId(201);
        p1.setNeedId(101);
        p1.setQuantityOffered(300);
        p1.setQuantityAllocated(300);
        p1.setStatus("ALLOCATED");
        pledges.add(p1);

        List<Allocation> allocations = new ArrayList<>();
        Allocation a1 = new Allocation();
        a1.setAllocationId(301);
        a1.setPledgeId(201);
        a1.setNeedId(101);
        a1.setQuantityAllocated(300);
        a1.setStatus("IN_TRANSIT");
        allocations.add(a1);

        byte[] populatedPdf = service.generatePdfReport(disasters, needs, pledges, allocations);
        assertTrue(populatedPdf != null && populatedPdf.length > 0, "Populated dataset PDF must not be empty");

        try (PdfReader reader = new PdfReader(populatedPdf)) {
            assertTrue(reader.getNumberOfPages() >= 1, "Populated dataset PDF must contain at least 1 page");
            System.out.println("[PASS] Test 2: Populated dataset produces a valid PDF (" + reader.getNumberOfPages() + " page(s), " + populatedPdf.length + " bytes).");
        } catch (Exception e) {
            throw new RuntimeException("Test 2 Failed: Populated PDF cannot be parsed by PdfReader", e);
        }

        // Save sample PDF for visual validation
        File sampleFile = new File("target/test_disaster_relief_report.pdf");
        if (sampleFile.getParentFile() != null && !sampleFile.getParentFile().exists()) {
            sampleFile.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(sampleFile)) {
            fos.write(populatedPdf);
            System.out.println("       Sample PDF saved to: " + sampleFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Could not save sample file: " + e.getMessage(), e);
        }
        assertTrue(sampleFile.exists() && sampleFile.length() > 0,
                "Sample PDF file target/test_disaster_relief_report.pdf must exist and have non-zero size");

        // -------------------------------------------------------------
        // Test 3: Large Multi-page Dataset (50 items)
        // -------------------------------------------------------------
        List<Need> manyNeeds = new ArrayList<>(needs);
        for (int i = 106; i <= 155; i++) {
            Need mn = new Need();
            mn.setNeedId(i);
            mn.setDisasterId((i % 2) + 1);
            mn.setCategory("Relief Item #" + i);
            mn.setDescription("Detailed field description for relief request #" + i + " dispatched from sector " + (i % 5));
            mn.setQuantityRequired(50 + i);
            mn.setQuantityAllocated(20);
            mn.setQuantityRemaining(30 + i);
            mn.setUrgency((i % 4 == 0) ? "CRITICAL" : (i % 3 == 0) ? "HIGH" : "MEDIUM");
            mn.setStatus((i % 2 == 0) ? "VERIFIED" : "PENDING_VERIFICATION");
            manyNeeds.add(mn);
        }

        byte[] multiPagePdf = service.generatePdfReport(disasters, manyNeeds, pledges, allocations);
        try (PdfReader reader = new PdfReader(multiPagePdf)) {
            int pages = reader.getNumberOfPages();
            assertTrue(pages > 1, "Large dataset must span multiple pages (found: " + pages + ")");
            System.out.println("[PASS] Test 3: Multi-page dataset correctly paginates across " + pages + " pages (" + multiPagePdf.length + " bytes).");
        } catch (Exception e) {
            throw new RuntimeException("Test 3 Failed: Multi-page PDF test error", e);
        }

        // -------------------------------------------------------------
        // Controller Setup
        // -------------------------------------------------------------
        PdfReportService mockPdfReportService = new PdfReportService(null, null) {
            @Override
            public byte[] generateDepartmentReport() {
                return populatedPdf;
            }
        };

        ReportController controller = new ReportController(null, null, mockPdfReportService);

        // -------------------------------------------------------------
        // Test 4: Endpoint without authentication -> 401 Unauthorized
        // -------------------------------------------------------------
        HttpSession unauthSession = createMockSession(new HashMap<>());
        ResponseEntity<byte[]> unauthPdfResp = controller.exportNeedsPdf(unauthSession);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthPdfResp.getStatusCode(),
                "Unauthenticated request to /export/pdf must return 401 Unauthorized");
        System.out.println("[PASS] Test 4: Unauthenticated request returns 401 Unauthorized.");

        // -------------------------------------------------------------
        // Test 5: Endpoint with DONOR role -> 403 Forbidden
        // -------------------------------------------------------------
        Map<String, Object> donorSessionMap = new HashMap<>();
        User donorUser = new User();
        donorUser.setUserId(2);
        donorUser.setName("Donor John");
        donorUser.setRole("DONOR");
        donorSessionMap.put("loggedInUser", donorUser);
        HttpSession donorSession = createMockSession(donorSessionMap);

        ResponseEntity<byte[]> donorPdfResp = controller.exportNeedsPdf(donorSession);
        assertEquals(HttpStatus.FORBIDDEN, donorPdfResp.getStatusCode(),
                "DONOR user request to /export/pdf must return 403 Forbidden");
        System.out.println("[PASS] Test 5: Non-DEPARTMENT user (DONOR) returns 403 Forbidden.");

        // -------------------------------------------------------------
        // Test 6: Endpoint with AFFECTED_PERSON role -> 403 Forbidden
        // -------------------------------------------------------------
        Map<String, Object> affectedSessionMap = new HashMap<>();
        User affectedUser = new User();
        affectedUser.setUserId(3);
        affectedUser.setName("Citizen Jane");
        affectedUser.setRole("AFFECTED_PERSON");
        affectedSessionMap.put("loggedInUser", affectedUser);
        HttpSession affectedSession = createMockSession(affectedSessionMap);

        ResponseEntity<byte[]> affectedPdfResp = controller.exportNeedsPdf(affectedSession);
        assertEquals(HttpStatus.FORBIDDEN, affectedPdfResp.getStatusCode(),
                "AFFECTED_PERSON user request to /export/pdf must return 403 Forbidden");
        System.out.println("[PASS] Test 6: Non-DEPARTMENT user (AFFECTED_PERSON) returns 403 Forbidden.");

        // -------------------------------------------------------------
        // Test 7: Endpoint with DEPARTMENT role -> 200 OK + PDF binary
        // -------------------------------------------------------------
        Map<String, Object> deptSessionMap = new HashMap<>();
        User departmentOfficer = new User();
        departmentOfficer.setUserId(1);
        departmentOfficer.setName("Officer Sharma");
        departmentOfficer.setRole("DEPARTMENT");
        deptSessionMap.put("loggedInUser", departmentOfficer);
        HttpSession authSession = createMockSession(deptSessionMap);

        ResponseEntity<byte[]> authPdfResp = controller.exportNeedsPdf(authSession);
        assertEquals(HttpStatus.OK, authPdfResp.getStatusCode(),
                "DEPARTMENT officer request to /export/pdf must return 200 OK");
        assertEquals(MediaType.APPLICATION_PDF, authPdfResp.getHeaders().getContentType(),
                "Response Content-Type must be application/pdf");

        String contentDisp = authPdfResp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(contentDisp != null && contentDisp.contains("attachment") && contentDisp.contains("disaster_relief_coordination_report.pdf"),
                "Content-Disposition header must specify attachment with correct filename");
        assertEquals(populatedPdf.length, authPdfResp.getBody() != null ? authPdfResp.getBody().length : 0,
                "PDF response body byte count must match generated report");
        System.out.println("[PASS] Test 7: DEPARTMENT officer download returns 200 OK with Content-Disposition: " + contentDisp);

        System.out.println("=== All 7 Tests Passed Successfully (100% Verified) ===");
    }
}
