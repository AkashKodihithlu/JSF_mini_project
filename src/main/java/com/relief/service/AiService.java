package com.relief.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relief.model.Need;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final Set<String> VALID_CATEGORIES = Set.of(
            "Food & Rations",
            "Clean Drinking Water",
            "Medical & First Aid",
            "Shelter & Clothing",
            "Rescue & Evacuation",
            "General Relief Supplies");

    private static final Set<String> VALID_URGENCIES = Set.of("CRITICAL", "HIGH", "MEDIUM", "LOW");

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Uses real Google Gemini API to classify a relief request, compute urgency,
     * detect semantic duplicates against existing disaster needs, and explain why.
     */
    public Map<String, Object> analyzeReliefNeed(String description,
            int quantityRequired,
            Integer affectedPeopleCount,
            Integer criticalPeopleCount,
            List<Need> existingNeeds) {
        String cleanDesc = description == null ? "" : description.trim();
        String activeKey = resolveApiKey();

        if (!activeKey.isEmpty() && !cleanDesc.isEmpty()) {
            try {
                Map<String, Object> geminiResult = callGeminiForNeedAnalysis(
                        activeKey, cleanDesc, quantityRequired, affectedPeopleCount, criticalPeopleCount,
                        existingNeeds);
                if (geminiResult != null) {
                    return geminiResult;
                }
            } catch (Exception ex) {
                log.warn("Gemini API call failed, switching to dynamic NLP fallback: {}", ex.getMessage());
            }
        }

        return runDynamicNlpFallback(cleanDesc, quantityRequired, affectedPeopleCount, criticalPeopleCount,
                existingNeeds);
    }

    /**
     * Uses real Gemini API to evaluate the operational impact when a donor cancels
     * an allocation and recommend immediate reallocation actions to officers.
     */
    public String analyzeCancellationImpact(String category,
            int cancelledQty,
            int remainingQty,
            String urgency,
            String cancellationReason) {
        String activeKey = resolveApiKey();
        if (!activeKey.isEmpty()) {
            try {
                String prompt = String.format(
                        "You are an emergency disaster relief logistics AI. A donor just cancelled %d units of '%s' (Urgency: %s). "
                                +
                                "Remaining unfulfilled quantity is now %d units. Donor's cancellation reason: \"%s\". "
                                +
                                "Provide a concise 1-to-2 sentence actionable logistics recommendation for the Relief Department Officer. "
                                +
                                "Return JSON: {\"recommendation\": \"...\"}",
                        cancelledQty, category, urgency, remainingQty, cancellationReason);
                JsonNode root = invokeGeminiJson(activeKey, prompt);
                if (root != null && root.hasNonNull("recommendation")) {
                    return root.get("recommendation").asText();
                }
            } catch (Exception ex) {
                log.warn("Gemini cancellation analysis failed: {}", ex.getMessage());
            }
        }

        return String.format(
                "%d units of %s (%s urgency) returned to open queue (total remaining: %d). Check Surplus Reallocation pool or notify nearby verified donors.",
                cancelledQty, category, urgency, remainingQty);
    }

    /**
     * Generates a concise AI dispatch priority insight for a donor viewing a
     * verified need.
     */
    public String generateDonorLogisticsBrief(String category,
            String description,
            int remainingQty,
            String urgency,
            double distanceKm) {
        String activeKey = resolveApiKey();
        if (!activeKey.isEmpty()) {
            try {
                String prompt = String.format(
                        "You are a disaster relief supply assistant. Summarize in 1 short sentence (max 22 words) why fulfilling "
                                +
                                "%d units of '%s' (%s urgency, %.1f km away, details: \"%s\") is impactful right now. "
                                +
                                "Return JSON: {\"brief\": \"...\"}",
                        remainingQty, category, urgency, distanceKm, description);
                JsonNode root = invokeGeminiJson(activeKey, prompt);
                if (root != null && root.hasNonNull("brief")) {
                    return root.get("brief").asText();
                }
            } catch (Exception ex) {
                log.debug("Gemini donor brief fallback: {}", ex.getMessage());
            }
        }
        return String.format("%s priority %s request %.1f km away (%d units still needed).",
                urgency, category, distanceKm, remainingQty);
    }

    private Map<String, Object> callGeminiForNeedAnalysis(String apiKey,
            String description,
            int quantityRequired,
            Integer affectedPeopleCount,
            Integer criticalPeopleCount,
            List<Need> existingNeeds) throws Exception {
        String existingSummary = "None";
        if (existingNeeds != null && !existingNeeds.isEmpty()) {
            existingSummary = existingNeeds.stream()
                    .filter(n -> !"REJECTED".equalsIgnoreCase(n.getStatus()))
                    .limit(15)
                    .map(n -> String.format("[Need #%d | %s | Qty:%d | Status:%s | Desc: %s]",
                            n.getNeedId(), n.getCategory(), n.getQuantityRequired(), n.getStatus(), n.getDescription()))
                    .collect(Collectors.joining("\n"));
        }

        String prompt = "You are an AI triage and deduplication engine for a Disaster Relief Coordination System.\n" +
                "Analyze this incoming citizen relief request:\n" +
                "- Description: \"" + description + "\"\n" +
                "- Quantity Required: " + quantityRequired + "\n" +
                "- Affected People Count: " + (affectedPeopleCount != null ? affectedPeopleCount : "Not specified")
                + "\n" +
                "- Critical People Count: " + (criticalPeopleCount != null ? criticalPeopleCount : "Not specified")
                + "\n\n" +
                "Existing active relief requests for this disaster:\n" + existingSummary + "\n\n" +
                "Rules:\n" +
                "1. 'suggestedCategory' MUST be exact one of: \"Food & Rations\", \"Clean Drinking Water\", \"Medical & First Aid\", \"Shelter & Clothing\", \"Rescue & Evacuation\", \"General Relief Supplies\".\n"
                +
                "2. 'suggestedUrgency' MUST be exact one of: \"CRITICAL\", \"HIGH\", \"MEDIUM\", \"LOW\".\n" +
                "3. Compare semantically against existing requests to detect duplicate reports from the same shelter/ward/incident.\n"
                +
                "4. Return strictly valid JSON with keys:\n" +
                "{\n" +
                "  \"suggestedCategory\": \"...\",\n" +
                "  \"suggestedUrgency\": \"...\",\n" +
                "  \"isDuplicateSuspected\": true/false,\n" +
                "  \"similarityScore\": 0.0 to 1.0,\n" +
                "  \"matchedNeedId\": number or null,\n" +
                "  \"aiReasoning\": \"1-sentence explanation of category, urgency, and duplicate check\"\n" +
                "}";

        JsonNode parsed = invokeGeminiJson(apiKey, prompt);
        if (parsed == null) {
            return null;
        }

        String rawCat = parsed.path("suggestedCategory").asText("General Relief Supplies");
        String category = VALID_CATEGORIES.contains(rawCat) ? rawCat : normalizeCategory(rawCat);

        String rawUrg = parsed.path("suggestedUrgency").asText("MEDIUM").toUpperCase(Locale.ROOT);
        String urgency = VALID_URGENCIES.contains(rawUrg) ? rawUrg : "MEDIUM";

        double simScore = Math.max(0.0, Math.min(1.0, parsed.path("similarityScore").asDouble(0.0)));
        boolean isDup = parsed.path("isDuplicateSuspected").asBoolean(simScore >= 0.65);
        Integer matchedId = parsed.hasNonNull("matchedNeedId") && parsed.get("matchedNeedId").isInt()
                ? parsed.get("matchedNeedId").asInt()
                : null;
        String reasoning = parsed.path("aiReasoning").asText("Classified via Gemini AI triage.");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("suggestedCategory", category);
        result.put("suggestedUrgency", urgency);
        result.put("isDuplicateSuspected", isDup);
        result.put("similarityScore", Math.round(simScore * 100.0) / 100.0);
        result.put("matchedNeedId", matchedId);
        result.put("aiReasoning", reasoning);
        result.put("aiSource", "GEMINI_LIVE");
        return result;
    }

    private JsonNode invokeGeminiJson(String apiKey, String promptText) throws Exception {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiModel + ":generateContent?key=" + apiKey;

        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", promptText)))),
                "generationConfig", Map.of(
                        "temperature", 0.15,
                        "responseMimeType", "application/json"));

        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.warn("Gemini HTTP {}: {}", response.statusCode(), response.body());
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            return null;
        }

        String rawJson = textNode.asText().trim();
        if (rawJson.startsWith("```")) {
            rawJson = rawJson.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }
        return objectMapper.readTree(rawJson);
    }

    private Map<String, Object> runDynamicNlpFallback(String description,
            int quantityRequired,
            Integer affectedPeopleCount,
            Integer criticalPeopleCount,
            List<Need> existingNeeds) {
        String lower = description.toLowerCase(Locale.ROOT);
        String category = normalizeCategory(lower);

        String urgency = "MEDIUM";
        if ((criticalPeopleCount != null && criticalPeopleCount > 0)
                || lower.contains("critical") || lower.contains("insulin") || lower.contains("trapped")
                || lower.contains("bleeding") || lower.contains("infant") || lower.contains("oxygen")
                || lower.contains("rescue") || lower.contains("drowning")) {
            urgency = "CRITICAL";
        } else if ((affectedPeopleCount != null && affectedPeopleCount >= 25)
                || quantityRequired >= 50 || lower.contains("urgent") || lower.contains("elderly")
                || lower.contains("fever") || lower.contains("shelter")) {
            urgency = "HIGH";
        } else if (quantityRequired <= 5 && lower.length() < 25) {
            urgency = "LOW";
        }

        double maxSim = 0.0;
        Integer matchedId = null;
        Set<String> incomingTokens = tokenize(lower);

        if (existingNeeds != null && !incomingTokens.isEmpty()) {
            for (Need existing : existingNeeds) {
                if ("REJECTED".equalsIgnoreCase(existing.getStatus()) || existing.getDescription() == null) {
                    continue;
                }
                Set<String> existingTokens = tokenize(existing.getDescription().toLowerCase(Locale.ROOT));
                if (existingTokens.isEmpty())
                    continue;

                Set<String> intersection = new HashSet<>(incomingTokens);
                intersection.retainAll(existingTokens);

                Set<String> union = new HashSet<>(incomingTokens);
                union.addAll(existingTokens);

                double sim = (double) intersection.size() / union.size();
                if (sim > maxSim) {
                    maxSim = sim;
                    matchedId = existing.getNeedId();
                }
            }
        }

        boolean isDup = maxSim >= 0.55;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("suggestedCategory", category);
        result.put("suggestedUrgency", urgency);
        result.put("isDuplicateSuspected", isDup);
        result.put("similarityScore", Math.round(maxSim * 100.0) / 100.0);
        result.put("matchedNeedId", isDup ? matchedId : null);
        result.put("aiReasoning", isDup
                ? "High lexical overlap with existing Need #" + matchedId
                        + " (offline NLP fallback; set GEMINI_API_KEY for semantic AI)."
                : "Classified via keyword/Jaccard NLP engine (set GEMINI_API_KEY for live Gemini AI reasoning).");
        result.put("aiSource", "NLP_FALLBACK");
        return result;
    }

    private String normalizeCategory(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("water") || lower.contains("drinking") || lower.contains("hydrate")) {
            return "Clean Drinking Water";
        }
        if (lower.contains("med") || lower.contains("insulin") || lower.contains("first aid")
                || lower.contains("doctor") || lower.contains("hospital") || lower.contains("oxygen")) {
            return "Medical & First Aid";
        }
        if (lower.contains("food") || lower.contains("ration") || lower.contains("rice")
                || lower.contains("milk") || lower.contains("meal") || lower.contains("bread")) {
            return "Food & Rations";
        }
        if (lower.contains("rescue") || lower.contains("boat") || lower.contains("evacuat")
                || lower.contains("trapped") || lower.contains("flood")) {
            return "Rescue & Evacuation";
        }
        if (lower.contains("shelter") || lower.contains("blanket") || lower.contains("cloth")
                || lower.contains("tent") || lower.contains("tarpaulin")) {
            return "Shelter & Clothing";
        }
        return "General Relief Supplies";
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.replaceAll("[^a-z0-9\\s]", " ").split("\\s+"))
                .filter(w -> w.length() > 2)
                .collect(Collectors.toSet());
    }

    private String resolveApiKey() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return geminiApiKey.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        return envKey != null ? envKey.trim() : "";
    }
}