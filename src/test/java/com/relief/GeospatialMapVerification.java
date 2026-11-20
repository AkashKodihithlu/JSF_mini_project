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

public class GeospatialMapVerification {

    private static HttpSession createMockSession(User user) {
        Map<String, Object> sessionData = new HashMap<>();
        if (user != null) {
            sessionData.put("loggedInUser", user);
        }
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class },
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
                });
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

    private static Need createNeed(int id, int disasterId, String category, String status, double lat, double lng,
            Integer affected, Integer critical, String locName) {
        Need n = new Need();
        n.setNeedId(id);
        n.setDisasterId(disasterId);
        n.setCategory(category);
        n.setStatus(status);
        n.setLatitude(lat);
        n.setLongitude(lng);
        n.setQuantityRequired(100);
        n.setAffectedPeopleCount(affected);
        n.setCriticalPeopleCount(critical);
        n.setLocationName(locName);
        return n;
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("=== RELIEFGRID PHASE 2 GEOSPATIAL MAP & METRICS VERIFICATION ===");
        System.out.println("===============================================================");

        List<Need> mockNeeds = new ArrayList<>();
        List<AreaSafetyAssessment> mockAssessments = new ArrayList<>();
        AtomicInteger assessmentIdGen = new AtomicInteger(100);

        Disaster activeDisaster = new Disaster();
        activeDisaster.setDisasterId(1);
        activeDisaster.setName("Coastal Monsoon Flood 2026");
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

        NeedDAO mockNeedDAO = new NeedDAO(null) {
            @Override
            public List<Need> findAll() {
                return new ArrayList<>(mockNeeds);
            }

            @Override
            public Need findById(int id) {
                return mockNeeds.stream().filter(n -> n.getNeedId() == id).findFirst().orElse(null);
            }
        };

        // Safety DAO with composite geographic grouping (area_name + rounded
        // coordinates)
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
                // Group by composite key: area_name + "_" + round(lat, 3) + "_" + round(lng, 3)
                Map<String, AreaSafetyAssessment> latestPerComposite = new HashMap<>();
                for (AreaSafetyAssessment a : mockAssessments) {
                    if (a.getDisasterId() == disasterId) {
                        String key = a.getAreaName().trim().toLowerCase() + "_" +
                                Math.round(a.getLatitude() * 1000.0) / 1000.0 + "_" +
                                Math.round(a.getLongitude() * 1000.0) / 1000.0;
                        AreaSafetyAssessment existing = latestPerComposite.get(key);
                        if (existing == null || a.getAssessmentId() > existing.getAssessmentId()) {
                            latestPerComposite.put(key, a);
                        }
                    }
                }
                return new ArrayList<>(latestPerComposite.values());
            }

            @Override
            public List<AreaSafetyAssessment> findHistoryByDisasterAndArea(int disasterId, String areaName) {
                return mockAssessments.stream()
                        .filter(a -> a.getDisasterId() == disasterId && a.getAreaName().equalsIgnoreCase(areaName))
                        .sorted((a1, a2) -> Integer.compare(a2.getAssessmentId(), a1.getAssessmentId()))
                        .collect(Collectors.toList());
            }

            @Override
            public List<AreaSafetyAssessment> findHistoryByDisasterAndLocation(int disasterId, String areaName,
                    Double latitude, Double longitude) {
                if (latitude == null || longitude == null) {
                    return findHistoryByDisasterAndArea(disasterId, areaName);
                }
                return mockAssessments.stream()
                        .filter(a -> a.getDisasterId() == disasterId &&
                                a.getAreaName().equalsIgnoreCase(areaName) &&
                                Math.round(a.getLatitude() * 1000.0) == Math.round(latitude * 1000.0) &&
                                Math.round(a.getLongitude() * 1000.0) == Math.round(longitude * 1000.0))
                        .sorted((a1, a2) -> Integer.compare(a2.getAssessmentId(), a1.getAssessmentId()))
                        .collect(Collectors.toList());
            }
        };

        User deptOfficer = createUser(1, "Command Officer", "9876543210", "DEPARTMENT");
        User donorUser = createUser(2, "Kind Donor", "9876543211", "DONOR");

        AreaSafetyService safetyService = new AreaSafetyService(mockSafetyDAO, mockDisasterDAO, null, mockNeedDAO);
        AreaSafetyController safetyController = new AreaSafetyController(safetyService);

        // =============================================================
        // CHECK 1: Safety Evaluation Priority: UNSAFE > MONITORING > SAFE
        // =============================================================
        System.out.println("\n--- [CHECK 1] Overlapping Assessment Circles & Severity Priority ---");
        // Assessment A: SAFE shelter (center 13.0800, 80.2700, radius 2.0 km)
        safetyService.recordSafetyAssessment(1, deptOfficer, "Central Shelter", 13.0800, 80.2700, 2.0, "SAFE", 0.0,
                "Shelter safe");
        // Assessment B: UNSAFE active flood basin (center 13.0900, 80.2800, radius 3.5
        // km)
        safetyService.recordSafetyAssessment(1, deptOfficer, "Harbor Flood Basin", 13.0900, 80.2800, 3.5, "UNSAFE", 2.6,
                "Severe inundation");
        // Assessment C: MONITORING perimeter (center 13.0750, 80.2650, radius 2.5 km)
        safetyService.recordSafetyAssessment(1, deptOfficer, "Canal Sluice Area", 13.0750, 80.2650, 2.5, "MONITORING",
                1.2, "Water rising");

        // Point P is at (13.0820, 80.2720):
        // Distance to Shelter (SAFE): ~0.3 km <= 2.0 km -> INSIDE SAFE
        // Distance to Basin (UNSAFE): ~1.2 km <= 3.5 km -> INSIDE UNSAFE
        // Distance to Canal (MONITORING): ~1.1 km <= 2.5 km -> INSIDE MONITORING
        // Point is inside all 3 zones. Distance to SAFE (0.3km) is closest to center.
        // MUST evaluate to UNSAFE because UNSAFE > MONITORING > SAFE!
        Map<String, Object> evalOverlapping = safetyService.evaluatePointSafety(13.0820, 80.2720, 1);
        assertEquals("UNSAFE", evalOverlapping.get("safetyStatus"),
                "Check 1a: Overlapping SAFE and UNSAFE must evaluate to UNSAFE");
        assertEquals(true, evalOverlapping.get("isAssessed"), "Check 1b: isAssessed must be true");
        assertTrue((int) evalOverlapping.get("coveringCount") >= 2, "Check 1c: All covering assessments recorded");

        // Assessment D: SAFE sector south (center 12.9800, 80.2200, radius 2.0 km)
        safetyService.recordSafetyAssessment(1, deptOfficer, "South Park Shelter", 12.9800, 80.2200, 2.0, "SAFE", 0.0,
                "Shelter safe");
        // Assessment E: MONITORING sector south (center 12.9850, 80.2250, radius 2.5
        // km, > 10km away from UNSAFE Harbor Basin)
        safetyService.recordSafetyAssessment(1, deptOfficer, "South Lake Perimeter", 12.9850, 80.2250, 2.5,
                "MONITORING", 0.9, "Water rising");

        // Point Q is inside South Park Shelter (SAFE) and South Lake Perimeter
        // (MONITORING) only:
        // Center: 12.9820, 80.2220 (~14 km away from Harbor Basin)
        Map<String, Object> evalMonSafe = safetyService.evaluatePointSafety(12.9820, 80.2220, 1);
        assertEquals("MONITORING", evalMonSafe.get("safetyStatus"),
                "Check 1d: Overlapping MONITORING and SAFE must evaluate to MONITORING");

        // Point R is far away from all assessments (Tiruvallur: 13.1432, 79.9074)
        Map<String, Object> evalOutside = safetyService.evaluatePointSafety(13.1432, 79.9074, 1);
        assertEquals("UNKNOWN", evalOutside.get("safetyStatus"),
                "Check 1e: Point outside all circles must evaluate to UNKNOWN");
        assertEquals(false, evalOutside.get("isAssessed"), "Check 1f: isAssessed must be false for unassessed point");
        System.out.println("[PASS] Check 1: Strict severity priority UNSAFE > MONITORING > SAFE and UNKNOWN verified.");

        // =============================================================
        // CHECK 2: Same Area Name at Different Coordinates
        // =============================================================
        System.out.println("\n--- [CHECK 2] Same Area Name at Distinct Geographical Locations ---");
        // Clear previous assessments
        mockAssessments.clear();
        // Location 1: "Gandhi Nagar" in Adyar (Lat 13.0067, Lng 80.2550) - UNSAFE
        safetyService.recordSafetyAssessment(1, deptOfficer, "Gandhi Nagar", 13.0067, 80.2550, 3.0, "UNSAFE", 2.1,
                "Adyar sector");
        // Location 2: "Gandhi Nagar" in Avadi (Lat 13.1150, Lng 80.1000, 20 km away) -
        // SAFE
        safetyService.recordSafetyAssessment(1, deptOfficer, "Gandhi Nagar", 13.1150, 80.1000, 2.5, "SAFE", 0.0,
                "Avadi sector");

        List<AreaSafetyAssessment> latestList = safetyService.getLatestAssessmentsForDisaster(1);
        assertEquals(2, latestList.size(),
                "Check 2a: Both distinct geographical locations must be returned despite sharing area_name");

        AreaSafetyAssessment adyar = latestList.stream().filter(a -> Math.round(a.getLatitude() * 10.0) == 130)
                .findFirst().orElse(null);
        AreaSafetyAssessment avadi = latestList.stream().filter(a -> Math.round(a.getLatitude() * 10.0) == 131)
                .findFirst().orElse(null);
        assertTrue(adyar != null, "Check 2b: Adyar Gandhi Nagar present");
        assertTrue(avadi != null, "Check 2c: Avadi Gandhi Nagar present");
        assertEquals("UNSAFE", adyar.getSafetyStatus(), "Check 2d: Adyar is UNSAFE");
        assertEquals("SAFE", avadi.getSafetyStatus(), "Check 2e: Avadi is SAFE");

        // Verify location-specific history query
        List<AreaSafetyAssessment> adyarHist = safetyService.getAssessmentHistoryForLocation(1, "Gandhi Nagar", 13.0067,
                80.2550);
        assertEquals(1, adyarHist.size(), "Check 2f: Location-specific history isolated to Adyar coordinate");
        System.out.println(
                "[PASS] Check 2: Distinct locations with identical area names preserve individual status and audit history.");

        // =============================================================
        // CHECK 3: Population Aggregation & Co-Location Clustering
        // =============================================================
        System.out.println("\n--- [CHECK 3] Population Aggregation Without Double-Counting ---");
        mockNeeds.clear();
        // Shelter A: 3 co-located relief requests at Royapuram Shelter (13.0827,
        // 80.2707)
        // Request 1: Food, affected=120, critical=10
        mockNeeds.add(createNeed(1, 1, "Food Packets", "VERIFIED", 13.0827, 80.2707, 120, 10, "Royapuram Shelter"));
        // Request 2: Clean Water, affected=120, critical=15
        mockNeeds.add(createNeed(2, 1, "Drinking Water", "VERIFIED", 13.0827, 80.2707, 120, 15, "Royapuram Shelter"));
        // Request 3: Blankets, affected=110, critical=5
        mockNeeds.add(
                createNeed(3, 1, "Blankets", "PARTIALLY_FULFILLED", 13.0828, 80.2708, 110, 5, "Royapuram Shelter"));

        // Shelter B: 1 relief request at Marina Beach (13.0500, 80.2800) - ~4 km away
        // Request 4: Medical Kits, affected=60, critical=8
        mockNeeds.add(createNeed(4, 1, "Medical Kits", "VERIFIED", 13.0500, 80.2800, 60, 8, "Marina High Ground"));

        // Request 5: Tarpaulins with NO population reported (null)
        mockNeeds.add(createNeed(5, 1, "Tarpaulins", "VERIFIED", 13.0400, 80.2500, null, null, null));

        Map<String, Object> metrics = safetyService.calculateGeospatialMetrics(1, mockNeeds);
        assertEquals(5, metrics.get("totalActiveNeeds"), "Check 3a: Total active needs is 5");

        // Critical people sum: 10 + 15 + 5 + 8 = 38 (reported critical count across
        // specific requests)
        assertEquals(38, metrics.get("reportedCriticalPeople"), "Check 3b: Reported critical people sum is 38");

        // Estimated unique affected people:
        // Cluster 1 (Royapuram Shelter): needs #1, #2, #3 co-located. Max affected is
        // max(120, 120, 110) = 120. (NOT 350!)
        // Cluster 2 (Marina): need #4 affected is 60.
        // Cluster 3 (Tarpaulins): null affected count.
        // Total Estimated Unique Affected = 120 + 60 = 180!
        assertEquals(180, metrics.get("estimatedUniqueAffectedPeople"),
                "Check 3c: Estimated unique affected people is 180 (no triple-counting)");
        assertEquals(3, metrics.get("uniqueClustersCount"), "Check 3d: 3 spatial clusters identified");
        System.out.println(
                "[PASS] Check 3: Co-located needs clustered with max(affected_people_count); double-counting prevented.");

        // =============================================================
        // CHECK 4: NULL Population Values Preserved
        // =============================================================
        System.out.println("\n--- [CHECK 4] NULL Population Values Preserved As Unknown ---");
        List<Need> nullOnlyNeeds = List.of(
                createNeed(10, 1, "Food", "VERIFIED", 13.00, 80.00, null, null, null),
                createNeed(11, 1, "Water", "VERIFIED", 13.00, 80.00, null, null, null));
        Map<String, Object> nullMetrics = safetyService.calculateGeospatialMetrics(1, nullOnlyNeeds);
        assertEquals(null, nullMetrics.get("estimatedUniqueAffectedPeople"),
                "Check 4a: Population remains null (unknown) when no count is reported");
        assertEquals(null, nullMetrics.get("reportedCriticalPeople"),
                "Check 4b: Critical count remains null when no count is reported");
        System.out.println(
                "[PASS] Check 4: Missing population strictly preserved as null/unknown without defaulting to zero.");

        // =============================================================
        // CHECK 5: Department Role Authorization for Safety Assessments
        // =============================================================
        System.out.println("\n--- [CHECK 5] Strict Authorization Enforcement ---");
        HttpSession deptSession = createMockSession(deptOfficer);
        HttpSession donorSession = createMockSession(donorUser);
        HttpSession anonSession = createMockSession(null);

        ResponseEntity<?> createResp = safetyController.createAssessment(
                1, "New Sector", 13.05, 80.20, 2.0, "MONITORING", 1.5, "Rising", deptSession);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode(),
                "Check 5a: Department session creates assessment (201)");

        ResponseEntity<?> donorResp = safetyController.createAssessment(
                1, "New Sector", 13.05, 80.20, 2.0, "MONITORING", 1.5, "Rising", donorSession);
        assertEquals(HttpStatus.FORBIDDEN, donorResp.getStatusCode(),
                "Check 5b: Donor session rejected with 403 Forbidden");

        ResponseEntity<?> anonResp = safetyController.createAssessment(
                1, "New Sector", 13.05, 80.20, 2.0, "MONITORING", 1.5, "Rising", anonSession);
        assertEquals(HttpStatus.UNAUTHORIZED, anonResp.getStatusCode(),
                "Check 5c: Anonymous session rejected with 401 Unauthorized");
        System.out.println("[PASS] Check 5: Only DEPARTMENT users authorized; 401/403 enforced.");

        // =============================================================
        // CHECK 6: Metrics API Endpoint Response
        // =============================================================
        System.out.println("\n--- [CHECK 6] Metrics REST Endpoint Response ---");
        ResponseEntity<?> metricsResp = safetyController.getMetrics(1, deptSession);
        assertEquals(HttpStatus.OK, metricsResp.getStatusCode(),
                "Check 6a: GET /api/safety-assessments/metrics returns 200 OK");
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) metricsResp.getBody();
        assertTrue(body.containsKey("totalActiveNeeds"), "Check 6b: Contains totalActiveNeeds");
        assertTrue(body.containsKey("estimatedUniqueAffectedPeople"),
                "Check 6c: Contains estimatedUniqueAffectedPeople");
        assertTrue(body.containsKey("unsafeAreasCount"), "Check 6d: Contains unsafeAreasCount");
        System.out.println("[PASS] Check 6: Metrics REST endpoint returns complete geospatial summary.");

        System.out.println("\n===============================================================");
        System.out.println("=== ALL 6 PHASE 2 GEOSPATIAL CHECKS COMPLETED SUCCESSFULLY (100%) ===");
        System.out.println("===============================================================");
    }
}
