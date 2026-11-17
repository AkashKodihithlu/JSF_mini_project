package com.relief;

import com.relief.controller.AreaSafetyController;
import com.relief.dao.AreaSafetyAssessmentDAO;
import com.relief.dao.DisasterDAO;
import com.relief.dao.NeedDAO;
import com.relief.model.AreaSafetyAssessment;
import com.relief.model.Disaster;
import com.relief.model.Need;
import com.relief.model.User;
import com.relief.service.AreaSafetyService;
import com.relief.service.NeedService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AreaSafetyVerification {

    private static HttpSession createMockSession(User user) {
        Map<String, Object> sessionData = new HashMap<>();
        if (user != null) {
            sessionData.put("loggedInUser", user);
        }
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, methodArgs) -> {
                    String methodName = method.getName();
                    if ("getAttribute".equals(methodName)) {
                        return sessionData.get(methodArgs[0]);
                    } else if ("setAttribute".equals(methodName)) {
                        sessionData.put((String) methodArgs[0], methodArgs[1]);
                        return null;
                    } else if ("removeAttribute".equals(methodName)) {
                        return sessionData.remove(methodArgs[0]);
                    }
                    return null;
                }
        );
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError("FAIL: " + message + " -> Expected: <" + expected + ">, but was: <" + actual + ">");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAIL: " + message);
        }
    }

    private static User createUser(int id, String name, String mobile, String role) {
        User u = new User();
        u.setUserId(id);
        u.setName(name);
        u.setMobileNumber(mobile);
        u.setRole(role);
        return u;
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("=== RELIEFGRID PHASE 1 GEOSPATIAL & SAFETY VERIFICATION SUITE ===");
        System.out.println("===============================================================");

        // Setup in-memory mock DAOs for isolated, deterministic unit & service testing
        List<Need> mockNeeds = new ArrayList<>();
        List<AreaSafetyAssessment> mockAssessments = new ArrayList<>();
        AtomicInteger assessmentIdGen = new AtomicInteger(100);

        // Disaster DAO mock
        Disaster activeDisaster = new Disaster();
        activeDisaster.setDisasterId(1);
        activeDisaster.setName("Cyclone Nivar");
        activeDisaster.setStatus("ACTIVE");

        DisasterDAO mockDisasterDAO = new DisasterDAO(null) {
            @Override
            public Disaster findById(int id) {
                return id == 1 ? activeDisaster : null;
            }
            @Override
            public List<Disaster> findActiveDisasters() {
                return List.of(activeDisaster);
            }
        };

        // Need DAO mock
        NeedDAO mockNeedDAO = new NeedDAO(null) {
            @Override
            public int save(Need need) {
                int id = mockNeeds.size() + 1;
                need.setNeedId(id);
                mockNeeds.add(need);
                return id;
            }
            @Override
            public Need findById(int id) {
                return mockNeeds.stream().filter(n -> n.getNeedId() == id).findFirst().orElse(null);
            }
            @Override
            public List<Need> findAll() {
                return new ArrayList<>(mockNeeds);
            }
            @Override
            public List<String> findDescriptionsByDisaster(int disasterId) {
                return Collections.emptyList();
            }
        };

        // AreaSafetyAssessment DAO mock
        AreaSafetyAssessmentDAO mockSafetyDAO = new AreaSafetyAssessmentDAO(null) {
            @Override
            public int save(AreaSafetyAssessment a) {
                int id = assessmentIdGen.incrementAndGet();
                a.setAssessmentId(id);
                a.setAssessedAt(new Timestamp(System.currentTimeMillis()));
                mockAssessments.add(a);
                return id;
            }
            @Override
            public AreaSafetyAssessment findById(int id) {
                return mockAssessments.stream().filter(a -> a.getAssessmentId() == id).findFirst().orElse(null);
            }
            @Override
            public List<AreaSafetyAssessment> findLatestByDisaster(int disasterId) {
                Map<String, AreaSafetyAssessment> latestPerArea = new HashMap<>();
                for (AreaSafetyAssessment a : mockAssessments) {
                    if (a.getDisasterId() == disasterId) {
                        AreaSafetyAssessment existing = latestPerArea.get(a.getAreaName());
                        if (existing == null || a.getAssessmentId() > existing.getAssessmentId()) {
                            latestPerArea.put(a.getAreaName(), a);
                        }
                    }
                }
                return new ArrayList<>(latestPerArea.values());
            }
            @Override
            public List<AreaSafetyAssessment> findHistoryByDisasterAndArea(int disasterId, String areaName) {
                return mockAssessments.stream()
                        .filter(a -> a.getDisasterId() == disasterId && a.getAreaName().equalsIgnoreCase(areaName))
                        .sorted((a1, a2) -> Integer.compare(a2.getAssessmentId(), a1.getAssessmentId()))
                        .collect(Collectors.toList());
            }
            @Override
            public List<AreaSafetyAssessment> findAllByDisaster(int disasterId) {
                return mockAssessments.stream()
                        .filter(a -> a.getDisasterId() == disasterId)
                        .sorted((a1, a2) -> Integer.compare(a2.getAssessmentId(), a1.getAssessmentId()))
                        .collect(Collectors.toList());
            }
        };

        // Users
        User deptOfficer = createUser(1, "Officer Kumar", "9876543210", "DEPARTMENT");
        User donorUser = createUser(2, "Meera Donor", "9876543211", "DONOR");
        User affectedUser = createUser(3, "Ravi Citizen", "9876543212", "AFFECTED_PERSON");

        // Services & Controllers
        NeedService needService = new NeedService(mockNeedDAO, null, mockDisasterDAO);
        AreaSafetyService safetyService = new AreaSafetyService(mockSafetyDAO, mockDisasterDAO, null);
        AreaSafetyController safetyController = new AreaSafetyController(safetyService);

        // =============================================================
        // TEST 1: Population Data - Valid Counts and Location
        // =============================================================
        System.out.println("\n--- [TEST 1] Population Data Validation: Valid Counts ---");
        needService.submitNeed(1, 3, "Food", "Emergency food packets for flood victims", 100, "CRITICAL", 13.0827, 80.2707, 50, 10, "Royapuram Shelter");
        Need need1 = mockNeedDAO.findById(1);
        assertEquals(Integer.valueOf(50), need1.getAffectedPeopleCount(), "Test 1: affectedPeopleCount correctly saved");
        assertEquals(Integer.valueOf(10), need1.getCriticalPeopleCount(), "Test 1: criticalPeopleCount correctly saved");
        assertEquals("Royapuram Shelter", need1.getLocationName(), "Test 1: locationName correctly saved");
        System.out.println("[PASS] Test 1: Valid population counts and location name saved and retrieved accurately.");

        // =============================================================
        // TEST 2: Population Data - Invalid Critical Counts & Negative Counts
        // =============================================================
        System.out.println("\n--- [TEST 2] Population Data Validation: Invalid & Negative Counts ---");
        // 2a: critical > affected
        boolean caughtCriticalEx = false;
        try {
            needService.submitNeed(1, 3, "Water", "Clean drinking water containers", 50, "HIGH", 13.0827, 80.2707, 20, 25, "Marina Beach");
        } catch (IllegalArgumentException e) {
            caughtCriticalEx = true;
            assertTrue(e.getMessage().contains("cannot exceed"), "Test 2a: Exception explains critical > affected");
        }
        assertTrue(caughtCriticalEx, "Test 2a: criticalPeopleCount > affectedPeopleCount must throw IllegalArgumentException");

        // 2b: negative affected count
        boolean caughtNegAffected = false;
        try {
            needService.submitNeed(1, 3, "Medical", "Emergency medical first-aid kits", 10, "HIGH", 13.0827, 80.2707, -5, 0, "Adyar");
        } catch (IllegalArgumentException e) {
            caughtNegAffected = true;
        }
        assertTrue(caughtNegAffected, "Test 2b: negative affectedPeopleCount must throw IllegalArgumentException");

        // 2c: negative critical count
        boolean caughtNegCritical = false;
        try {
            needService.submitNeed(1, 3, "Medical", "Emergency medical first-aid kits", 10, "HIGH", 13.0827, 80.2707, 10, -2, "Adyar");
        } catch (IllegalArgumentException e) {
            caughtNegCritical = true;
        }
        assertTrue(caughtNegCritical, "Test 2c: negative criticalPeopleCount must throw IllegalArgumentException");
        System.out.println("[PASS] Test 2: Invalid population counts properly rejected with validation errors.");

        // =============================================================
        // TEST 3: Existing Records Intact & Missing Population Info (Null preserved)
        // =============================================================
        System.out.println("\n--- [TEST 3] Missing Population Data & Existing Records Preservation ---");
        // Needs created without population information must keep affectedPeopleCount and criticalPeopleCount as null
        needService.submitNeed(1, 3, "Shelter", "Tarpaulin sheets for temporary camp", 20, "MEDIUM", 13.0850, 80.2750, null, null, null);
        Need need2 = mockNeedDAO.findById(2);
        assertEquals(null, need2.getAffectedPeopleCount(), "Test 3: affectedPeopleCount remains null when not reported");
        assertEquals(null, need2.getCriticalPeopleCount(), "Test 3: criticalPeopleCount remains null when not reported");
        assertEquals(null, need2.getLocationName(), "Test 3: locationName remains null when not reported");
        // Ensure quantity_required is NOT misinterpreted as population
        assertEquals(20, need2.getQuantityRequired(), "Test 3: quantityRequired remains separate resource count");
        System.out.println("[PASS] Test 3: Missing population remains null (Unknown). Resource quantity is NOT used as population.");

        // =============================================================
        // TEST 4: Area Safety Assessments - Coordinate Validation
        // =============================================================
        System.out.println("\n--- [TEST 4] Coordinate Bounds Validation ---");
        // Latitude > 90
        boolean caughtLatHigh = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone 1", 95.0, 80.0, 5.0, "SAFE", 0.0, "Test");
        } catch (IllegalArgumentException e) {
            caughtLatHigh = true;
        }
        assertTrue(caughtLatHigh, "Test 4a: Latitude > 90 must be rejected");

        // Latitude < -90
        boolean caughtLatLow = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone 1", -95.0, 80.0, 5.0, "SAFE", 0.0, "Test");
        } catch (IllegalArgumentException e) {
            caughtLatLow = true;
        }
        assertTrue(caughtLatLow, "Test 4b: Latitude < -90 must be rejected");

        // Longitude > 180
        boolean caughtLngHigh = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone 1", 13.0, 185.0, 5.0, "SAFE", 0.0, "Test");
        } catch (IllegalArgumentException e) {
            caughtLngHigh = true;
        }
        assertTrue(caughtLngHigh, "Test 4c: Longitude > 180 must be rejected");

        // Longitude < -180
        boolean caughtLngLow = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone 1", 13.0, -185.0, 5.0, "SAFE", 0.0, "Test");
        } catch (IllegalArgumentException e) {
            caughtLngLow = true;
        }
        assertTrue(caughtLngLow, "Test 4d: Longitude < -180 must be rejected");

        // Radius out of bounds (< 0.1 or > 100)
        boolean caughtRadiusZero = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone 1", 13.0, 80.0, 0.0, "SAFE", 0.0, "Test");
        } catch (IllegalArgumentException e) {
            caughtRadiusZero = true;
        }
        assertTrue(caughtRadiusZero, "Test 4e: Radius 0.0 km must be rejected");

        boolean caughtRadiusHuge = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone 1", 13.0, 80.0, 150.0, "SAFE", 0.0, "Test");
        } catch (IllegalArgumentException e) {
            caughtRadiusHuge = true;
        }
        assertTrue(caughtRadiusHuge, "Test 4f: Radius > 100 km must be rejected");
        System.out.println("[PASS] Test 4: Coordinate boundaries and radius limits strictly enforced.");

        // =============================================================
        // TEST 5: Flood Level Measurements - Valid, Nullable & Boundary Checks
        // =============================================================
        System.out.println("\n--- [TEST 5] Flood Level Measurements Validation ---");
        // Valid flood level 2.5m
        AreaSafetyAssessment a1 = safetyService.recordSafetyAssessment(1, deptOfficer, "Saidapet Bridge", 13.0200, 80.2200, 3.0, "MONITORING", 2.5, "Rising water level");
        assertEquals(Double.valueOf(2.5), a1.getFloodLevelMeters(), "Test 5a: Flood level 2.5m accepted");

        // Nullable flood level
        AreaSafetyAssessment a2 = safetyService.recordSafetyAssessment(1, deptOfficer, "Nungambakkam High Ground", 13.0600, 80.2400, 2.0, "SAFE", null, "No standing water");
        assertEquals(null, a2.getFloodLevelMeters(), "Test 5b: Null flood level accepted without fabrication");

        // Negative flood level rejected
        boolean caughtNegFlood = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone X", 13.0, 80.0, 2.0, "MONITORING", -1.5, "Negative water");
        } catch (IllegalArgumentException e) {
            caughtNegFlood = true;
        }
        assertTrue(caughtNegFlood, "Test 5c: Negative flood level rejected");

        // Excessively high flood level (> 30m) rejected
        boolean caughtHighFlood = false;
        try {
            safetyService.recordSafetyAssessment(1, deptOfficer, "Zone X", 13.0, 80.0, 2.0, "UNSAFE", 35.0, "Unrealistic depth");
        } catch (IllegalArgumentException e) {
            caughtHighFlood = true;
        }
        assertTrue(caughtHighFlood, "Test 5d: Flood level > 30m rejected");
        System.out.println("[PASS] Test 5: Flood measurement ranges verified. Nullable values preserved without fabricating data.");

        // =============================================================
        // TEST 6: Strict Department Authorization Enforcement
        // =============================================================
        System.out.println("\n--- [TEST 6] Role-Based Access Control: DEPARTMENT Only ---");
        // 6a: DONOR user rejected
        boolean caughtDonorAuth = false;
        try {
            safetyService.recordSafetyAssessment(1, donorUser, "Adyar Basin", 13.0067, 80.2550, 4.0, "SAFE", 0.0, "Donor says safe");
        } catch (SecurityException e) {
            caughtDonorAuth = true;
            assertTrue(e.getMessage().contains("Only DEPARTMENT users"), "Test 6a: Correct security message");
        }
        assertTrue(caughtDonorAuth, "Test 6a: DONOR user must be rejected with SecurityException");

        // 6b: AFFECTED_PERSON rejected
        boolean caughtAffectedAuth = false;
        try {
            safetyService.recordSafetyAssessment(1, affectedUser, "Velachery", 12.9800, 80.2200, 3.0, "UNSAFE", 3.0, "Water entering house");
        } catch (SecurityException e) {
            caughtAffectedAuth = true;
        }
        assertTrue(caughtAffectedAuth, "Test 6b: AFFECTED_PERSON must be rejected with SecurityException");

        // 6c: Unauthenticated user rejected
        boolean caughtNullAuth = false;
        try {
            safetyService.recordSafetyAssessment(1, null, "Velachery", 12.9800, 80.2200, 3.0, "UNSAFE", 3.0, "Water entering house");
        } catch (SecurityException e) {
            caughtNullAuth = true;
        }
        assertTrue(caughtNullAuth, "Test 6c: Unauthenticated user must be rejected with SecurityException");
        System.out.println("[PASS] Test 6: Non-department users and unauthenticated users strictly blocked.");

        // =============================================================
        // TEST 7: Historical Assessment Preservation (No Overwriting)
        // =============================================================
        System.out.println("\n--- [TEST 7] Historical Assessment Preservation ---");
        // Submit 3 successive assessments for "Velachery Lake Sector"
        safetyService.recordSafetyAssessment(1, deptOfficer, "Velachery Lake Sector", 12.9800, 80.2200, 3.5, "MONITORING", 0.8, "Water level at 0.8m, monitoring sluice gates.");
        safetyService.recordSafetyAssessment(1, deptOfficer, "Velachery Lake Sector", 12.9800, 80.2200, 3.5, "UNSAFE", 2.4, "Breach detected! Severe waterlogging, evacuation ordered.");
        safetyService.recordSafetyAssessment(1, deptOfficer, "Velachery Lake Sector", 12.9800, 80.2200, 3.5, "SAFE", 0.3, "Water receded to safe limits. Roads cleared.");

        // Verify history contains all 3 assessments
        List<AreaSafetyAssessment> history = safetyService.getAssessmentHistoryForArea(1, "Velachery Lake Sector");
        assertEquals(3, history.size(), "Test 7a: History retains all 3 assessment records");
        assertEquals("SAFE", history.get(0).getSafetyStatus(), "Test 7b: Most recent assessment is first in history");
        assertEquals("UNSAFE", history.get(1).getSafetyStatus(), "Test 7c: Intermediate assessment preserved");
        assertEquals("MONITORING", history.get(2).getSafetyStatus(), "Test 7d: Initial assessment preserved");

        // Verify findLatestByDisaster returns ONLY the most recent status ("SAFE")
        List<AreaSafetyAssessment> latestList = safetyService.getLatestAssessmentsForDisaster(1);
        AreaSafetyAssessment latestVelachery = latestList.stream()
                .filter(a -> a.getAreaName().equalsIgnoreCase("Velachery Lake Sector"))
                .findFirst()
                .orElse(null);
        assertTrue(latestVelachery != null, "Test 7e: Latest assessment found for Velachery");
        assertEquals("SAFE", latestVelachery.getSafetyStatus(), "Test 7f: Latest status is SAFE");
        assertEquals(Double.valueOf(0.3), latestVelachery.getFloodLevelMeters(), "Test 7g: Latest flood level is 0.3m");
        System.out.println("[PASS] Test 7: Historical assessments preserved in full chronological audit trail.");

        // =============================================================
        // TEST 8: Coordinate Point Safety Evaluation (SAFE, UNSAFE, MONITORING, UNKNOWN)
        // =============================================================
        System.out.println("\n--- [TEST 8] Point Evaluation Logic & UNKNOWN Default ---");
        // Clear assessments and setup controlled zones:
        mockAssessments.clear();
        // Safe Zone: Guindy Park (13.0067, 80.2206), radius 2.0 km
        safetyService.recordSafetyAssessment(1, deptOfficer, "Guindy Safe Zone", 13.0067, 80.2206, 2.0, "SAFE", 0.0, "High ground, shelter operational.");
        // Unsafe Zone: Mudichur (12.9100, 80.0700), radius 4.0 km
        safetyService.recordSafetyAssessment(1, deptOfficer, "Mudichur Flood Plain", 12.9100, 80.0700, 4.0, "UNSAFE", 2.2, "Deep flooding on major roads.");

        // 8a: Point inside Safe Zone (13.0080, 80.2210 - ~150 meters from center)
        Map<String, Object> evalSafe = safetyService.evaluatePointSafety(13.0080, 80.2210, 1);
        assertEquals("SAFE", evalSafe.get("safetyStatus"), "Test 8a: Point inside safe zone evaluated as SAFE");
        assertEquals(true, evalSafe.get("isAssessed"), "Test 8a: isAssessed is true");

        // 8b: Point inside Unsafe Zone (12.9150, 80.0750 - ~750 meters from center)
        Map<String, Object> evalUnsafe = safetyService.evaluatePointSafety(12.9150, 80.0750, 1);
        assertEquals("UNSAFE", evalUnsafe.get("safetyStatus"), "Test 8b: Point inside unsafe zone evaluated as UNSAFE");
        assertEquals(Double.valueOf(2.2), evalUnsafe.get("floodLevelMeters"), "Test 8b: Flood level returned");

        // 8c: Point outside all assessed zones (e.g. Tiruvallur: 13.1432, 79.9074 - > 20 km away)
        Map<String, Object> evalOutside = safetyService.evaluatePointSafety(13.1432, 79.9074, 1);
        assertEquals("UNKNOWN", evalOutside.get("safetyStatus"), "Test 8c: Unassessed point MUST return UNKNOWN");
        assertEquals(false, evalOutside.get("isAssessed"), "Test 8c: isAssessed is false");
        assertEquals(null, evalOutside.get("areaName"), "Test 8c: areaName is null for unassessed location");

        // 8d: Disaster without any assessments must return UNKNOWN (never automatically marked SAFE)
        Map<String, Object> evalNoDisaster = safetyService.evaluatePointSafety(13.0067, 80.2206, 999);
        assertEquals("UNKNOWN", evalNoDisaster.get("safetyStatus"), "Test 8d: Absence of alerts/assessments yields UNKNOWN");
        assertEquals(false, evalNoDisaster.get("isAssessed"), "Test 8d: isAssessed is false");
        System.out.println("[PASS] Test 8: Point evaluation correctly yields SAFE, UNSAFE, and strictly UNKNOWN for unassessed areas.");

        // =============================================================
        // TEST 9: AreaSafetyController HTTP Integration Verification
        // =============================================================
        System.out.println("\n--- [TEST 9] Controller HTTP Endpoint & Session Role Verification ---");
        HttpSession deptSession = createMockSession(deptOfficer);
        HttpSession donorSession = createMockSession(donorUser);
        HttpSession anonSession = createMockSession(null);

        // 9a: POST /api/safety-assessments with DEPARTMENT session -> 201 Created
        ResponseEntity<?> postResp = safetyController.createAssessment(
                1, "Pallikaranai Marsh", 12.9370, 80.2150, 3.0, "MONITORING", 1.1, "Water levels rising near bypass", deptSession);
        assertEquals(HttpStatus.CREATED, postResp.getStatusCode(), "Test 9a: Department post returns 201 Created");

        // 9b: POST /api/safety-assessments with DONOR session -> 403 Forbidden
        ResponseEntity<?> donorPost = safetyController.createAssessment(
                1, "Adyar", 13.00, 80.25, 2.0, "SAFE", 0.0, "Safe", donorSession);
        assertEquals(HttpStatus.FORBIDDEN, donorPost.getStatusCode(), "Test 9b: Donor post returns 403 Forbidden");

        // 9c: POST /api/safety-assessments unauthenticated -> 401 Unauthorized
        ResponseEntity<?> anonPost = safetyController.createAssessment(
                1, "Adyar", 13.00, 80.25, 2.0, "SAFE", 0.0, "Safe", anonSession);
        assertEquals(HttpStatus.UNAUTHORIZED, anonPost.getStatusCode(), "Test 9c: Unauthenticated post returns 401 Unauthorized");

        // 9d: GET /api/safety-assessments -> 200 OK with assessments
        ResponseEntity<?> getResp = safetyController.getLatestAssessments(1, deptSession);
        assertEquals(HttpStatus.OK, getResp.getStatusCode(), "Test 9d: GET assessments returns 200 OK");

        // 9e: GET /api/safety-assessments/history -> 200 OK
        ResponseEntity<?> getHist = safetyController.getAssessmentHistory(1, "Pallikaranai Marsh", deptSession);
        assertEquals(HttpStatus.OK, getHist.getStatusCode(), "Test 9e: GET history returns 200 OK");

        // 9f: GET /api/safety-assessments/evaluate -> 200 OK with evaluation map
        ResponseEntity<?> evalResp = safetyController.evaluatePoint(12.9375, 80.2155, 1, deptSession);
        assertEquals(HttpStatus.OK, evalResp.getStatusCode(), "Test 9f: GET evaluate returns 200 OK");
        @SuppressWarnings("unchecked")
        Map<String, Object> evalBody = (Map<String, Object>) evalResp.getBody();
        assertEquals("MONITORING", evalBody.get("safetyStatus"), "Test 9f: Point inside Pallikaranai returns MONITORING");

        System.out.println("[PASS] Test 9: AreaSafetyController endpoints, permissions, and responses verified.");

        System.out.println("\n===============================================================");
        System.out.println("=== ALL 9 VERIFICATION CHECKS COMPLETED SUCCESSFULLY (100%) ===");
        System.out.println("===============================================================");
    }
}
