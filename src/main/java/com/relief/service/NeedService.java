package com.relief.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relief.dao.DisasterDAO;
import com.relief.dao.NeedDAO;
import com.relief.dao.NeedVerificationDAO;
import com.relief.model.Disaster;
import com.relief.model.Need;
import com.relief.model.NeedVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class NeedService {

    private static final Set<String> VALID_URGENCIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
    private static final Set<String> VALID_DECISIONS = Set.of("APPROVED", "REJECTED", "FLAGGED_DUPLICATE");

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    private final NeedDAO needDAO;
    private final NeedVerificationDAO verificationDAO;
    private final DisasterDAO disasterDAO;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    // 3-arg constructor preserved for backward compatibility
    public NeedService(NeedDAO needDAO,
            NeedVerificationDAO verificationDAO,
            DisasterDAO disasterDAO) {
        this(needDAO, verificationDAO, disasterDAO, null);
    }

    @Autowired
    public NeedService(NeedDAO needDAO,
            NeedVerificationDAO verificationDAO,
            DisasterDAO disasterDAO,
            AiService aiService) {
        this.needDAO = needDAO;
        this.verificationDAO = verificationDAO;
        this.disasterDAO = disasterDAO;
        this.aiService = aiService;
    }

    /**
     * Runs live Gemini AI preview analysis for category, urgency, and semantic
     * duplicate detection.
     */
    public Map<String, Object> analyzeNeedPreview(int disasterId,
            String description,
            int quantityRequired,
            Integer affectedPeopleCount,
            Integer criticalPeopleCount) {
        String cleanDesc = description == null ? "" : description.trim();
        List<Need> existingForDisaster = new ArrayList<>();
        for (Need n : needDAO.findAll()) {
            if (n.getDisasterId() == disasterId && !"REJECTED".equalsIgnoreCase(n.getStatus())) {
                existingForDisaster.add(n);
            }
        }

        if (aiService != null) {
            return aiService.analyzeReliefNeed(
                    cleanDesc, quantityRequired, affectedPeopleCount, criticalPeopleCount, existingForDisaster);
        }

        String suggestedCategory = classifyCategory(cleanDesc);
        String suggestedUrgency = recommendPriority(cleanDesc, quantityRequired);
        List<String> existingDescriptions = existingForDisaster.stream().map(Need::getDescription).toList();
        Map<String, Object> dupCheck = checkDuplicateNeed(cleanDesc, existingDescriptions);

        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("suggestedCategory", suggestedCategory);
        fallback.put("suggestedUrgency", suggestedUrgency);
        fallback.put("isDuplicateSuspected", dupCheck.get("isDuplicate"));
        fallback.put("similarityScore", dupCheck.get("similarity"));
        fallback.put("aiReasoning", "Classified via local NLP rules.");
        fallback.put("aiSource", "LOCAL_FALLBACK");
        return fallback;
    }

    /**
     * Submits a new relief need, auto-classifies category/urgency using live Gemini
     * AI if requested,
     * checks for duplicate requests, and persists to MySQL.
     */
    public Map<String, Object> submitNeed(int disasterId,
            int createdByUserId,
            String category,
            String description,
            int quantityRequired,
            String urgency,
            double latitude,
            double longitude) {
        return submitNeed(disasterId, createdByUserId, category, description, quantityRequired,
                urgency, latitude, longitude, null, null, null);
    }

    public Map<String, Object> submitNeed(int disasterId,
            int createdByUserId,
            String category,
            String description,
            int quantityRequired,
            String urgency,
            double latitude,
            double longitude,
            Integer affectedPeopleCount,
            Integer criticalPeopleCount,
            String locationName) {
        if (disasterDAO.findById(disasterId) == null) {
            throw new IllegalArgumentException("Selected disaster event does not exist.");
        }
        if (description == null || description.trim().length() < 5) {
            throw new IllegalArgumentException("Please provide a clear description (at least 5 characters).");
        }
        if (quantityRequired <= 0) {
            throw new IllegalArgumentException("Quantity required must be greater than zero.");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0 degrees.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0 degrees.");
        }
        if (affectedPeopleCount != null && affectedPeopleCount < 0) {
            throw new IllegalArgumentException("Affected people count cannot be negative.");
        }
        if (criticalPeopleCount != null && criticalPeopleCount < 0) {
            throw new IllegalArgumentException("Critical people count cannot be negative.");
        }
        if (affectedPeopleCount != null && criticalPeopleCount != null && criticalPeopleCount > affectedPeopleCount) {
            throw new IllegalArgumentException("Critical people count cannot exceed total affected people count.");
        }

        String cleanDesc = description.trim();
        Map<String, Object> aiAnalysis = analyzeNeedPreview(
                disasterId, cleanDesc, quantityRequired, affectedPeopleCount, criticalPeopleCount);

        String resolvedCategory = (category == null || category.isBlank() || "AUTO".equalsIgnoreCase(category.trim()))
                ? String.valueOf(aiAnalysis.getOrDefault("suggestedCategory", classifyCategory(cleanDesc)))
                : category.trim();

        String resolvedUrgency = (urgency == null || urgency.isBlank() || "AUTO".equalsIgnoreCase(urgency.trim()))
                ? String.valueOf(
                        aiAnalysis.getOrDefault("suggestedUrgency", recommendPriority(cleanDesc, quantityRequired)))
                : urgency.trim().toUpperCase(Locale.ROOT);

        if (!VALID_URGENCIES.contains(resolvedUrgency)) {
            resolvedUrgency = "MEDIUM";
        }

        Need need = new Need();
        need.setDisasterId(disasterId);
        need.setCreatedBy(createdByUserId);
        need.setCategory(resolvedCategory);
        need.setDescription(cleanDesc);
        need.setQuantityRequired(quantityRequired);
        need.setUrgency(resolvedUrgency);
        need.setStatus("PENDING_VERIFICATION");
        need.setLatitude(latitude);
        need.setLongitude(longitude);
        need.setAffectedPeopleCount(affectedPeopleCount);
        need.setCriticalPeopleCount(criticalPeopleCount);
        need.setLocationName(locationName != null && !locationName.isBlank() ? locationName.trim() : null);

        needDAO.save(need);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "PENDING_VERIFICATION");
        response.put("classifiedCategory", resolvedCategory);
        response.put("recommendedUrgency", resolvedUrgency);
        response.put("isDuplicateSuspected", aiAnalysis.getOrDefault("isDuplicateSuspected", false));
        response.put("similarityScore", aiAnalysis.getOrDefault("similarityScore", 0.0));
        response.put("aiReasoning", aiAnalysis.get("aiReasoning"));
        response.put("aiSource", aiAnalysis.get("aiSource"));
        response.put("affectedPeopleCount", affectedPeopleCount);
        response.put("criticalPeopleCount", criticalPeopleCount);
        response.put("locationName", need.getLocationName());
        return response;
    }

    /**
     * Department officer verifies, rejects, or flags a need as duplicate,
     * optionally adjusting its urgency level and recording audit remarks.
     */
    @Transactional
    public void verifyNeed(int needId,
            int departmentUserId,
            String decision,
            String updatedUrgency,
            String remarks) {
        Need existing = needDAO.findById(needId);
        if (existing == null) {
            throw new IllegalArgumentException("Relief need ID #" + needId + " not found.");
        }

        String normalizedDecision = (decision == null) ? "" : decision.trim().toUpperCase(Locale.ROOT);
        if (!VALID_DECISIONS.contains(normalizedDecision)) {
            throw new IllegalArgumentException(
                    "Invalid verification decision. Must be APPROVED, REJECTED, or FLAGGED_DUPLICATE.");
        }

        String finalUrgency = (updatedUrgency != null
                && VALID_URGENCIES.contains(updatedUrgency.trim().toUpperCase(Locale.ROOT)))
                        ? updatedUrgency.trim().toUpperCase(Locale.ROOT)
                        : existing.getUrgency();

        String newNeedStatus = "APPROVED".equals(normalizedDecision) ? "VERIFIED" : "REJECTED";

        needDAO.updateStatusAndUrgency(needId, newNeedStatus, finalUrgency);

        NeedVerification audit = new NeedVerification();
        audit.setNeedId(needId);
        audit.setDepartmentId(departmentUserId);
        audit.setStatus(normalizedDecision);
        audit.setRemarks(remarks);
        verificationDAO.save(audit);
    }

    /**
     * Calls Python AI microservice (/check-duplicate) with a local Jaccard
     * similarity fallback.
     */
    public Map<String, Object> checkDuplicateNeed(String description, List<String> existingDescriptions) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (existingDescriptions == null || existingDescriptions.isEmpty()) {
            result.put("isDuplicate", false);
            result.put("similarity", 0.0);
            return result;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "description", description,
                    "existing", existingDescriptions);
            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiServiceUrl + "/check-duplicate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode node = objectMapper.readTree(response.body());
                result.put("isDuplicate", node.path("is_duplicate").asBoolean(false));
                result.put("similarity", node.path("max_similarity").asDouble(0.0));
                return result;
            }
        } catch (Exception ignored) {
            // Fallback to local token similarity
        }

        double maxSim = 0.0;
        Set<String> targetTokens = tokenize(description);
        for (String existing : existingDescriptions) {
            double sim = jaccardSimilarity(targetTokens, tokenize(existing));
            if (sim > maxSim) {
                maxSim = sim;
            }
        }

        result.put("isDuplicate", maxSim >= 0.70);
        result.put("similarity", Math.round(maxSim * 10000.0) / 10000.0);
        return result;
    }

    public String classifyCategory(String description) {
        String text = description.toLowerCase(Locale.ROOT);
        if (containsAny(text, "medicine", "insulin", "first aid", "bandage", "doctor", "hospital", "fever", "oxygen",
                "tablet")) {
            return "Medical & First Aid";
        }
        if (containsAny(text, "water", "drinking", "purifier", "hydration", "tanker", "bottle")) {
            return "Clean Drinking Water";
        }
        if (containsAny(text, "food", "rice", "ration", "milk", "baby food", "meal", "bread", "grocery")) {
            return "Food & Rations";
        }
        if (containsAny(text, "tent", "tarpaulin", "shelter", "blanket", "mattress", "clothes", "roof")) {
            return "Shelter & Clothing";
        }
        if (containsAny(text, "boat", "rescue", "evacuate", "trapped", "ambulance", "generator", "torch")) {
            return "Rescue & Evacuation";
        }
        return "General Relief Supplies";
    }

    public String recommendPriority(String description, int quantity) {
        String text = description.toLowerCase(Locale.ROOT);
        if (containsAny(text, "trapped", "critical", "urgent", "infant", "pregnant", "oxygen", "insulin", "bleeding",
                "elderly")) {
            return "CRITICAL";
        }
        if (containsAny(text, "flooded", "no water", "no food", "children", "stranded") || quantity >= 100) {
            return "HIGH";
        }
        if (quantity >= 25) {
            return "MEDIUM";
        }
        return "LOW";
    }

    // --- Query Helpers for Controllers & Dashboards ---

    public List<Disaster> getAllDisasters() {
        return disasterDAO.findAll();
    }

    public List<Disaster> getActiveDisasters() {
        return disasterDAO.findActiveDisasters();
    }

    public List<Need> getNeedsByUser(int userId) {
        return needDAO.findByCreatedBy(userId);
    }

    public List<Need> getPendingNeeds() {
        return needDAO.findByStatus("PENDING_VERIFICATION");
    }

    public List<Need> getAllNeeds() {
        return needDAO.findAll();
    }

    public List<Need> getAvailableNeedsForDonors() {
        return needDAO.findAvailableForDonors();
    }

    public Need getNeedById(int needId) {
        return needDAO.findById(needId);
    }

    public List<NeedVerification> getVerificationHistory(int needId) {
        return verificationDAO.findByNeedId(needId);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null)
            return tokens;
        String[] parts = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        tokens.addAll(Arrays.asList(parts));
        tokens.remove("");
        return tokens;
    }

    private double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty())
            return 0.0;
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }
}