package com.relief.service;

import com.relief.dao.AreaSafetyAssessmentDAO;
import com.relief.dao.DisasterDAO;
import com.relief.dao.NeedDAO;
import com.relief.dao.UserDAO;
import com.relief.model.AreaSafetyAssessment;
import com.relief.model.Need;
import com.relief.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AreaSafetyService {

    public static final double MAX_ASSESSMENT_RADIUS_KM = 100.0;
    public static final double MIN_ASSESSMENT_RADIUS_KM = 0.1;
    public static final double MAX_FLOOD_LEVEL_METERS = 30.0;
    public static final double MIN_FLOOD_LEVEL_METERS = 0.0;

    private static final Set<String> VALID_SAFETY_STATUSES = Set.of("SAFE", "UNSAFE", "MONITORING");

    private final AreaSafetyAssessmentDAO safetyDAO;
    private final DisasterDAO disasterDAO;
    private final UserDAO userDAO;
    private final NeedDAO needDAO;

    public AreaSafetyService(AreaSafetyAssessmentDAO safetyDAO, DisasterDAO disasterDAO, UserDAO userDAO) {
        this(safetyDAO, disasterDAO, userDAO, null);
    }

    @Autowired
    public AreaSafetyService(AreaSafetyAssessmentDAO safetyDAO, DisasterDAO disasterDAO, UserDAO userDAO, NeedDAO needDAO) {
        this.safetyDAO = safetyDAO;
        this.disasterDAO = disasterDAO;
        this.userDAO = userDAO;
        this.needDAO = needDAO;
    }

    /**
     * Records a new area safety assessment.
     * Enforces strict DEPARTMENT role authorization and field validation.
     * Preserves assessment history by creating an immutable audit record.
     */
    @Transactional
    public AreaSafetyAssessment recordSafetyAssessment(int disasterId,
                                                       User departmentUser,
                                                       String areaName,
                                                       double latitude,
                                                       double longitude,
                                                       double radiusKm,
                                                       String safetyStatus,
                                                       Double floodLevelMeters,
                                                       String assessmentNotes) {
        if (departmentUser == null) {
            throw new SecurityException("Authentication required to submit safety assessments.");
        }
        if (!"DEPARTMENT".equalsIgnoreCase(departmentUser.getRole())) {
            throw new SecurityException("Only DEPARTMENT users may create or update area safety assessments.");
        }
        if (disasterDAO != null && disasterDAO.findById(disasterId) == null) {
            throw new IllegalArgumentException("Disaster event #" + disasterId + " does not exist.");
        }
        if (areaName == null || areaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Area name is required.");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0 degrees.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0 degrees.");
        }
        if (radiusKm < MIN_ASSESSMENT_RADIUS_KM || radiusKm > MAX_ASSESSMENT_RADIUS_KM) {
            throw new IllegalArgumentException("Radius must be between " + MIN_ASSESSMENT_RADIUS_KM + " km and " + MAX_ASSESSMENT_RADIUS_KM + " km.");
        }
        if (safetyStatus == null || !VALID_SAFETY_STATUSES.contains(safetyStatus.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid safety status. Must be SAFE, UNSAFE, or MONITORING.");
        }
        if (floodLevelMeters != null) {
            if (floodLevelMeters < MIN_FLOOD_LEVEL_METERS || floodLevelMeters > MAX_FLOOD_LEVEL_METERS) {
                throw new IllegalArgumentException("Flood level must be between " + MIN_FLOOD_LEVEL_METERS + " and " + MAX_FLOOD_LEVEL_METERS + " metres.");
            }
        }

        AreaSafetyAssessment assessment = new AreaSafetyAssessment();
        assessment.setDisasterId(disasterId);
        assessment.setDepartmentId(departmentUser.getUserId());
        assessment.setAreaName(areaName.trim());
        assessment.setLatitude(latitude);
        assessment.setLongitude(longitude);
        assessment.setRadiusKm(radiusKm);
        assessment.setSafetyStatus(safetyStatus.trim().toUpperCase(Locale.ROOT));
        assessment.setFloodLevelMeters(floodLevelMeters);
        assessment.setAssessmentNotes(assessmentNotes != null ? assessmentNotes.trim() : null);

        int generatedId = safetyDAO.save(assessment);
        assessment.setAssessmentId(generatedId);
        return assessment;
    }

    /**
     * Retrieves the latest assessment for each monitored area in a disaster.
     * Uses composite geographical grouping (area_name + rounded coordinates).
     */
    public List<AreaSafetyAssessment> getLatestAssessmentsForDisaster(int disasterId) {
        return safetyDAO.findLatestByDisaster(disasterId);
    }

    /**
     * Retrieves complete chronological assessment history for an area.
     */
    public List<AreaSafetyAssessment> getAssessmentHistoryForArea(int disasterId, String areaName) {
        if (areaName == null || areaName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return safetyDAO.findHistoryByDisasterAndArea(disasterId, areaName.trim());
    }

    /**
     * Retrieves complete chronological assessment history for an area and specific coordinates.
     */
    public List<AreaSafetyAssessment> getAssessmentHistoryForLocation(int disasterId, String areaName, Double latitude, Double longitude) {
        if (areaName == null || areaName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return safetyDAO.findHistoryByDisasterAndLocation(disasterId, areaName.trim(), latitude, longitude);
    }

    private static class AssessmentMatch {
        final AreaSafetyAssessment assessment;
        final double distanceKm;

        AssessmentMatch(AreaSafetyAssessment assessment, double distanceKm) {
            this.assessment = assessment;
            this.distanceKm = distanceKm;
        }
    }

    /**
     * Evaluates safety status for a specific geographic coordinate by finding
     * active assessment perimeters that cover the point.
     *
     * Strict Severity Priority Rule:
     * UNSAFE > MONITORING > SAFE
     * - Never returns SAFE if the point is inside an active UNSAFE assessment.
     * - Returns UNKNOWN when no assessment covers the point.
     * - Returns the primary selected assessment and all covering assessments.
     * - Selection is fully deterministic.
     */
    public Map<String, Object> evaluatePointSafety(double latitude, double longitude, int disasterId) {
        List<AreaSafetyAssessment> assessments = safetyDAO.findLatestByDisaster(disasterId);
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> coveringList = new ArrayList<>();
        List<AssessmentMatch> matches = new ArrayList<>();

        for (AreaSafetyAssessment a : assessments) {
            double distanceKm = PledgeAllocationService.calculateHaversineKm(
                    latitude, longitude, a.getLatitude(), a.getLongitude());

            if (distanceKm <= a.getRadiusKm()) {
                double roundedDist = Math.round(distanceKm * 10.0) / 10.0;
                matches.add(new AssessmentMatch(a, distanceKm));

                Map<String, Object> cov = new LinkedHashMap<>();
                cov.put("assessmentId", a.getAssessmentId());
                cov.put("areaName", a.getAreaName());
                cov.put("safetyStatus", a.getSafetyStatus());
                cov.put("distanceKm", roundedDist);
                cov.put("radiusKm", a.getRadiusKm());
                cov.put("floodLevelMeters", a.getFloodLevelMeters());
                cov.put("assessedAt", a.getAssessedAt());
                coveringList.add(cov);
            }
        }

        if (matches.isEmpty()) {
            // Strictly UNKNOWN when no authorized assessment covers the area
            result.put("safetyStatus", "UNKNOWN");
            result.put("assessmentId", null);
            result.put("areaName", null);
            result.put("distanceKm", null);
            result.put("floodLevelMeters", null);
            result.put("assessedAt", null);
            result.put("isAssessed", false);
            result.put("coveringCount", 0);
            result.put("coveringAssessments", Collections.emptyList());
            return result;
        }

        // Apply strict safety priority: UNSAFE > MONITORING > SAFE
        // Deterministic tie-breaking among same-priority: closest distanceKm ascending, then assessmentId descending
        matches.sort((m1, m2) -> {
            int p1 = getStatusPriorityRank(m1.assessment.getSafetyStatus());
            int p2 = getStatusPriorityRank(m2.assessment.getSafetyStatus());
            if (p1 != p2) {
                return Integer.compare(p2, p1); // Higher rank first
            }
            int distComp = Double.compare(m1.distanceKm, m2.distanceKm);
            if (distComp != 0) {
                return distComp;
            }
            return Integer.compare(m2.assessment.getAssessmentId(), m1.assessment.getAssessmentId());
        });

        AssessmentMatch selected = matches.get(0);
        AreaSafetyAssessment a = selected.assessment;

        result.put("safetyStatus", a.getSafetyStatus());
        result.put("assessmentId", a.getAssessmentId());
        result.put("areaName", a.getAreaName());
        result.put("distanceKm", Math.round(selected.distanceKm * 10.0) / 10.0);
        result.put("floodLevelMeters", a.getFloodLevelMeters());
        result.put("assessedAt", a.getAssessedAt());
        result.put("isAssessed", true);
        result.put("coveringCount", matches.size());
        result.put("coveringAssessments", coveringList);

        return result;
    }

    private static int getStatusPriorityRank(String status) {
        if (status == null) return 0;
        switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "UNSAFE": return 3;
            case "MONITORING": return 2;
            case "SAFE": return 1;
            default: return 0;
        }
    }

    /**
     * Calculates geospatial metrics and estimated unique affected population across geographic clusters.
     * Prevents double-counting by clustering co-located needs (within ~100 meters / 0.1km threshold)
     * and taking the MAXIMUM affected count per cluster rather than summing.
     */
    public Map<String, Object> calculateGeospatialMetrics(int disasterId, List<Need> needs) {
        Map<String, Object> metrics = new LinkedHashMap<>();

        List<Need> activeNeeds = new ArrayList<>();
        int criticalPeopleSum = 0;
        int needsWithReportedCritical = 0;
        int needsWithReportedAffected = 0;

        for (Need n : needs) {
            if (disasterId > 0 && n.getDisasterId() != disasterId) {
                continue;
            }
            if ("REJECTED".equalsIgnoreCase(n.getStatus())) {
                continue;
            }
            activeNeeds.add(n);

            if (n.getCriticalPeopleCount() != null && n.getCriticalPeopleCount() > 0) {
                criticalPeopleSum += n.getCriticalPeopleCount();
                needsWithReportedCritical++;
            }
            if (n.getAffectedPeopleCount() != null) {
                needsWithReportedAffected++;
            }
        }

        // Spatial clustering (100 meters threshold) to avoid multi-counting co-located needs
        List<List<Need>> clusters = new ArrayList<>();
        double CLUSTER_THRESHOLD_KM = 0.1;

        for (Need need : activeNeeds) {
            boolean added = false;
            for (List<Need> cluster : clusters) {
                Need rep = cluster.get(0);
                double d = PledgeAllocationService.calculateHaversineKm(
                        need.getLatitude(), need.getLongitude(),
                        rep.getLatitude(), rep.getLongitude()
                );
                if (d <= CLUSTER_THRESHOLD_KM) {
                    cluster.add(need);
                    added = true;
                    break;
                }
            }
            if (!added) {
                List<Need> newCluster = new ArrayList<>();
                newCluster.add(need);
                clusters.add(newCluster);
            }
        }

        int estimatedUniqueAffectedSum = 0;
        int clustersWithReportedAffected = 0;

        for (List<Need> cluster : clusters) {
            Integer clusterMax = null;
            for (Need n : cluster) {
                if (n.getAffectedPeopleCount() != null) {
                    if (clusterMax == null || n.getAffectedPeopleCount() > clusterMax) {
                        clusterMax = n.getAffectedPeopleCount();
                    }
                }
            }
            if (clusterMax != null) {
                estimatedUniqueAffectedSum += clusterMax;
                clustersWithReportedAffected++;
            }
        }

        // Count areas by latest safety assessment
        List<AreaSafetyAssessment> latestAssessments = safetyDAO.findLatestByDisaster(disasterId);
        int unsafeCount = 0;
        int monitoringCount = 0;
        int safeCount = 0;

        for (AreaSafetyAssessment a : latestAssessments) {
            String status = a.getSafetyStatus() != null ? a.getSafetyStatus().toUpperCase(Locale.ROOT) : "";
            switch (status) {
                case "UNSAFE": unsafeCount++; break;
                case "MONITORING": monitoringCount++; break;
                case "SAFE": safeCount++; break;
            }
        }

        // Count needs located in areas with unknown safety status
        int unknownSafetyNeedsCount = 0;
        for (Need n : activeNeeds) {
            Map<String, Object> eval = evaluatePointSafety(n.getLatitude(), n.getLongitude(), disasterId);
            if ("UNKNOWN".equals(eval.get("safetyStatus"))) {
                unknownSafetyNeedsCount++;
            }
        }

        metrics.put("disasterId", disasterId);
        metrics.put("totalActiveNeeds", activeNeeds.size());
        metrics.put("reportedCriticalPeople", needsWithReportedCritical > 0 ? criticalPeopleSum : null);
        metrics.put("estimatedUniqueAffectedPeople", clustersWithReportedAffected > 0 ? estimatedUniqueAffectedSum : null);
        metrics.put("uniqueClustersCount", clusters.size());
        metrics.put("clustersWithReportedPopulation", clustersWithReportedAffected);
        metrics.put("unsafeAreasCount", unsafeCount);
        metrics.put("monitoringAreasCount", monitoringCount);
        metrics.put("safeAreasCount", safeCount);
        metrics.put("unknownSafetyNeedsCount", unknownSafetyNeedsCount);
        metrics.put("clusteringRule", "Spatial clustering threshold 100m; max(affected_people_count) per cluster to eliminate co-located duplicate counts");

        return metrics;
    }

    public Map<String, Object> getGeospatialMetrics(int disasterId) {
        List<Need> allNeeds = needDAO != null ? needDAO.findAll() : Collections.emptyList();
        return calculateGeospatialMetrics(disasterId, allNeeds);
    }
}
