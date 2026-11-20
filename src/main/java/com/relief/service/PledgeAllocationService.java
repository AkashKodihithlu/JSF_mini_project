package com.relief.service;

import com.relief.dao.AllocationDAO;
import com.relief.dao.NeedDAO;
import com.relief.dao.NeedParticipantDAO;
import com.relief.dao.PledgeDAO;
import com.relief.model.Allocation;
import com.relief.model.Need;
import com.relief.model.Pledge;
import com.relief.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class PledgeAllocationService {

        private static final Set<String> VALID_DELIVERY_STATUSES = Set.of(
                        "ALLOCATED", "IN_TRANSIT", "DELIVERED", "CONFIRMED", "REASSIGNED", "CANCELLED");

        private final PledgeDAO pledgeDAO;
        private final AllocationDAO allocationDAO;
        private final NeedDAO needDAO;
        private final NeedParticipantDAO needParticipantDAO;
        private final AiService aiService;

        // 4-arg constructor preserved for backward compatibility with test suites
        public PledgeAllocationService(
                        PledgeDAO pledgeDAO,
                        AllocationDAO allocationDAO,
                        NeedDAO needDAO,
                        NeedParticipantDAO needParticipantDAO) {
                this(pledgeDAO, allocationDAO, needDAO, needParticipantDAO, null);
        }

        @Autowired
        public PledgeAllocationService(
                        PledgeDAO pledgeDAO,
                        AllocationDAO allocationDAO,
                        NeedDAO needDAO,
                        NeedParticipantDAO needParticipantDAO,
                        AiService aiService) {
                this.pledgeDAO = pledgeDAO;
                this.allocationDAO = allocationDAO;
                this.needDAO = needDAO;
                this.needParticipantDAO = needParticipantDAO;
                this.aiService = aiService;
        }

        public static class MatchedNeed {
                private final Need need;
                private final double distanceKm;
                private final double matchScore;
                private final String aiLogisticsBrief;

                public MatchedNeed(Need need, double distanceKm, double matchScore) {
                        this(need, distanceKm, matchScore, null);
                }

                public MatchedNeed(Need need, double distanceKm, double matchScore, String aiLogisticsBrief) {
                        this.need = need;
                        this.distanceKm = distanceKm;
                        this.matchScore = matchScore;
                        this.aiLogisticsBrief = aiLogisticsBrief;
                }

                public Need getNeed() {
                        return need;
                }

                public double getDistanceKm() {
                        return distanceKm;
                }

                public double getMatchScore() {
                        return matchScore;
                }

                public String getAiLogisticsBrief() {
                        return aiLogisticsBrief;
                }
        }

        /**
         * Ranks verified/partially-fulfilled needs for a donor based on urgency,
         * GPS proximity, and live AI logistics briefs.
         */
        public List<MatchedNeed> getMatchedNeedsForDonor(User donor) {
                List<Need> availableNeeds = needDAO.findAvailableForDonors();
                List<MatchedNeed> ranked = new ArrayList<>();

                for (Need need : availableNeeds) {
                        double distanceKm = calculateHaversineKm(
                                        donor.getLatitude(), donor.getLongitude(),
                                        need.getLatitude(), need.getLongitude());

                        double roundedDist = Math.round(distanceKm * 10.0) / 10.0;
                        String urg = (need.getUrgency() == null)
                                        ? "MEDIUM"
                                        : need.getUrgency().trim().toUpperCase(Locale.ROOT);

                        int urgencyScore = switch (urg) {
                                case "CRITICAL" -> 100;
                                case "HIGH" -> 70;
                                case "MEDIUM" -> 40;
                                default -> 20;
                        };

                        double proximityBonus = Math.max(0.0, 50.0 - roundedDist);
                        double matchScore = Math.round((urgencyScore + proximityBonus) * 10.0) / 10.0;

                        String brief = (aiService != null)
                                        ? aiService.generateDonorLogisticsBrief(
                                                        need.getCategory(), need.getDescription(),
                                                        need.getQuantityRemaining(), urg, roundedDist)
                                        : null;

                        ranked.add(new MatchedNeed(need, roundedDist, matchScore, brief));
                }

                ranked.sort(Comparator.comparingDouble(MatchedNeed::getMatchScore).reversed());
                return ranked;
        }

        /**
         * Donor commits a pledge toward a verified need and initiates the connection
         * timeline.
         */
        @Transactional
        public Map<String, Object> createPledgeAndAllocate(int needId, int donorId, int quantityOffered) {
                if (quantityOffered <= 0) {
                        throw new IllegalArgumentException("Pledge quantity must be greater than zero.");
                }

                Need need = needDAO.findById(needId);
                if (need == null) {
                        throw new IllegalArgumentException("Relief need #" + needId + " does not exist.");
                }

                if (!"VERIFIED".equalsIgnoreCase(need.getStatus())
                                && !"PARTIALLY_FULFILLED".equalsIgnoreCase(need.getStatus())) {
                        throw new IllegalStateException("Pledges can only be made to verified needs.");
                }

                int immediateAllocation = Math.min(quantityOffered, need.getQuantityRemaining());

                String pledgeStatus;
                if (immediateAllocation == 0) {
                        pledgeStatus = "PLEDGED";
                } else if (immediateAllocation < quantityOffered) {
                        pledgeStatus = "PARTIALLY_ALLOCATED";
                } else {
                        pledgeStatus = "ALLOCATED";
                }

                Pledge pledge = new Pledge();
                pledge.setNeedId(needId);
                pledge.setDonorId(donorId);
                pledge.setQuantityOffered(quantityOffered);
                pledge.setQuantityAllocated(immediateAllocation);
                pledge.setStatus(pledgeStatus);

                pledgeDAO.save(pledge);

                List<Pledge> donorPledges = pledgeDAO.findByDonorId(donorId);
                int savedPledgeId = donorPledges.isEmpty() ? 0 : donorPledges.get(0).getPledgeId();

                if (immediateAllocation > 0 && savedPledgeId > 0) {
                        Allocation allocation = new Allocation();
                        allocation.setPledgeId(savedPledgeId);
                        allocation.setNeedId(needId);
                        allocation.setQuantityAllocated(immediateAllocation);
                        allocation.setStatus("ALLOCATED");
                        allocationDAO.save(allocation);
                }

                int updatedPledged = need.getQuantityPledged() + quantityOffered;
                int updatedAllocated = need.getQuantityAllocated() + immediateAllocation;
                int updatedRemaining = Math.max(0, need.getQuantityRequired() - updatedAllocated);
                String updatedNeedStatus = (updatedRemaining == 0) ? "FULFILLED" : "PARTIALLY_FULFILLED";

                needDAO.updateQuantitiesAndStatus(
                                needId, updatedPledged, updatedAllocated, updatedRemaining, updatedNeedStatus);

                needParticipantDAO.addOrActivateParticipant(needId, donorId);

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("pledgeId", savedPledgeId);
                result.put("quantityOffered", quantityOffered);
                result.put("allocatedImmediately", immediateAllocation);
                result.put("surplusAvailable", quantityOffered - immediateAllocation);
                result.put("needStatus", updatedNeedStatus);
                result.put("needRemaining", updatedRemaining);
                return result;
        }

        /**
         * Donor cancels an active allocation with a mandatory explanation reason.
         * Rolls back need quantities and invokes Gemini AI for reallocation guidance.
         */
        @Transactional
        public Map<String, Object> cancelAllocationWithReason(int allocationId, int donorId, String reason) {
                if (reason == null || reason.trim().length() < 5) {
                        throw new IllegalArgumentException(
                                        "Please provide a clear cancellation reason (at least 5 characters).");
                }

                Allocation allocation = allocationDAO.findById(allocationId);
                if (allocation == null) {
                        throw new IllegalArgumentException("Allocation #" + allocationId + " not found.");
                }

                Pledge pledge = pledgeDAO.findById(allocation.getPledgeId());
                if (pledge == null || pledge.getDonorId() != donorId) {
                        throw new SecurityException("You are only authorized to cancel your own allocations.");
                }

                String currentStatus = allocation.getStatus() == null ? ""
                                : allocation.getStatus().toUpperCase(Locale.ROOT);
                if ("CANCELLED".equals(currentStatus)) {
                        throw new IllegalStateException("This allocation is already cancelled.");
                }
                if ("CONFIRMED".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
                        throw new IllegalStateException(
                                        "Cannot cancel an allocation that has already been delivered or confirmed.");
                }

                String cleanReason = reason.trim();
                allocationDAO.cancelWithReason(allocationId, cleanReason);

                int cancelledQty = allocation.getQuantityAllocated();
                int remainingPledgeAlloc = Math.max(0, pledge.getQuantityAllocated() - cancelledQty);
                int remainingPledgeOffered = Math.max(0, pledge.getQuantityOffered() - cancelledQty);

                if (remainingPledgeOffered == 0) {
                        pledgeDAO.cancelWithReason(pledge.getPledgeId(), cleanReason);
                } else {
                        String newPledgeStatus = remainingPledgeAlloc == 0 ? "PLEDGED" : "PARTIALLY_ALLOCATED";
                        pledgeDAO.updateAllocationAndStatus(pledge.getPledgeId(), remainingPledgeAlloc,
                                        newPledgeStatus);
                }

                Need need = needDAO.findById(allocation.getNeedId());
                int updatedRemaining = 0;
                String aiImpactNote = null;

                if (need != null) {
                        int newPledged = Math.max(0, need.getQuantityPledged() - cancelledQty);
                        int newAllocated = Math.max(0, need.getQuantityAllocated() - cancelledQty);
                        updatedRemaining = Math.min(need.getQuantityRequired(),
                                        Math.max(0, need.getQuantityRequired() - newAllocated));
                        String newStatus = (newAllocated == 0)
                                        ? "VERIFIED"
                                        : (updatedRemaining == 0 ? "FULFILLED" : "PARTIALLY_FULFILLED");

                        needDAO.updateQuantitiesAndStatus(
                                        need.getNeedId(), newPledged, newAllocated, updatedRemaining, newStatus);

                        if (aiService != null) {
                                aiImpactNote = aiService.analyzeCancellationImpact(
                                                need.getCategory(), cancelledQty, updatedRemaining, need.getUrgency(),
                                                cleanReason);
                        }
                }

                syncParticipantStatusAfterCancel(allocation.getNeedId(), donorId);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("allocationId", allocationId);
                response.put("status", "CANCELLED");
                response.put("cancellationReason", cleanReason);
                response.put("restoredNeedUnits", cancelledQty);
                response.put("needRemaining", updatedRemaining);
                if (aiImpactNote != null) {
                        response.put("aiRecommendation", aiImpactNote);
                }
                return response;
        }

        /**
         * Donor cancels an entire pledge with a mandatory reason.
         */
        @Transactional
        public Map<String, Object> cancelPledgeWithReason(int pledgeId, int donorId, String reason) {
                if (reason == null || reason.trim().length() < 5) {
                        throw new IllegalArgumentException(
                                        "Please provide a clear cancellation reason (at least 5 characters).");
                }

                Pledge pledge = pledgeDAO.findById(pledgeId);
                if (pledge == null) {
                        throw new IllegalArgumentException("Pledge #" + pledgeId + " not found.");
                }
                if (pledge.getDonorId() != donorId) {
                        throw new SecurityException("You can only cancel your own pledges.");
                }
                if ("CANCELLED".equalsIgnoreCase(pledge.getStatus())) {
                        throw new IllegalStateException("Pledge #" + pledgeId + " is already cancelled.");
                }

                String cleanReason = reason.trim();
                List<Allocation> linkedAllocations = allocationDAO.findByPledgeId(pledgeId);
                for (Allocation alloc : linkedAllocations) {
                        String st = alloc.getStatus() == null ? "" : alloc.getStatus().toUpperCase(Locale.ROOT);
                        if ("DELIVERED".equals(st) || "CONFIRMED".equals(st)) {
                                throw new IllegalStateException("Cannot cancel pledge because Allocation #"
                                                + alloc.getAllocationId() + " has already reached the recipient.");
                        }
                        if (!"CANCELLED".equals(st)) {
                                allocationDAO.cancelWithReason(alloc.getAllocationId(), cleanReason);
                        }
                }

                pledgeDAO.cancelWithReason(pledgeId, cleanReason);

                Need need = needDAO.findById(pledge.getNeedId());
                int updatedRemaining = 0;
                String aiImpactNote = null;

                if (need != null) {
                        int newPledged = Math.max(0, need.getQuantityPledged() - pledge.getQuantityOffered());
                        int newAllocated = Math.max(0, need.getQuantityAllocated() - pledge.getQuantityAllocated());
                        updatedRemaining = Math.min(need.getQuantityRequired(),
                                        Math.max(0, need.getQuantityRequired() - newAllocated));
                        String newStatus = (newAllocated == 0)
                                        ? "VERIFIED"
                                        : (updatedRemaining == 0 ? "FULFILLED" : "PARTIALLY_FULFILLED");

                        needDAO.updateQuantitiesAndStatus(
                                        need.getNeedId(), newPledged, newAllocated, updatedRemaining, newStatus);

                        if (aiService != null) {
                                aiImpactNote = aiService.analyzeCancellationImpact(
                                                need.getCategory(), pledge.getQuantityOffered(), updatedRemaining,
                                                need.getUrgency(), cleanReason);
                        }
                }

                syncParticipantStatusAfterCancel(pledge.getNeedId(), donorId);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("pledgeId", pledgeId);
                response.put("status", "CANCELLED");
                response.put("cancellationReason", cleanReason);
                response.put("needRemaining", updatedRemaining);
                if (aiImpactNote != null) {
                        response.put("aiRecommendation", aiImpactNote);
                }
                return response;
        }

        /**
         * Backward-compatible withdraw method used by automated verification tests.
         */
        @Transactional
        public void withdrawPledge(int pledgeId, int donorId) {
                cancelPledgeWithReason(pledgeId, donorId, "Withdrawn by donor.");
        }

        /**
         * Affected citizen confirms they have physically received the allocated relief
         * supplies,
         * completing the horizontal timeline and verifying the need is reached.
         */
        @Transactional
        public Map<String, Object> confirmAllocationReceived(int allocationId, int affectedUserId) {
                Allocation allocation = allocationDAO.findById(allocationId);
                if (allocation == null) {
                        throw new IllegalArgumentException("Allocation #" + allocationId + " not found.");
                }

                Need need = needDAO.findById(allocation.getNeedId());
                if (need == null) {
                        throw new IllegalArgumentException("Associated relief need not found.");
                }

                if (need.getCreatedBy() != affectedUserId) {
                        throw new SecurityException("Only the citizen who submitted Need #" + need.getNeedId()
                                        + " can confirm receipt.");
                }

                String currentStatus = allocation.getStatus() == null ? ""
                                : allocation.getStatus().toUpperCase(Locale.ROOT);
                if ("CANCELLED".equals(currentStatus) || "REASSIGNED".equals(currentStatus)) {
                        throw new IllegalStateException(
                                        "Cannot confirm receipt for a cancelled or reassigned allocation.");
                }
                if ("CONFIRMED".equals(currentStatus)) {
                        throw new IllegalStateException(
                                        "Receipt has already been confirmed for Allocation #" + allocationId + ".");
                }

                allocationDAO.confirmReceipt(allocationId);

                // Verify whether all active allocations for this need are now CONFIRMED and
                // remaining == 0
                List<Allocation> needAllocations = allocationDAO.findByNeedId(need.getNeedId());
                boolean allActiveConfirmed = needAllocations.stream()
                                .filter(a -> !"CANCELLED".equalsIgnoreCase(a.getStatus())
                                                && !"REASSIGNED".equalsIgnoreCase(a.getStatus()))
                                .allMatch(a -> a.getAllocationId() == allocationId
                                                || "CONFIRMED".equalsIgnoreCase(a.getStatus()));

                String finalNeedStatus = need.getStatus();
                if (allActiveConfirmed && need.getQuantityRemaining() == 0) {
                        finalNeedStatus = "FULFILLED";
                        needDAO.updateQuantitiesAndStatus(
                                        need.getNeedId(),
                                        need.getQuantityPledged(),
                                        need.getQuantityAllocated(),
                                        0,
                                        "FULFILLED");
                }

                Map<String, Object> res = new LinkedHashMap<>();
                res.put("allocationId", allocationId);
                res.put("status", "CONFIRMED");
                res.put("needId", need.getNeedId());
                res.put("needStatus", finalNeedStatus);
                res.put("needReached", "FULFILLED".equalsIgnoreCase(finalNeedStatus));
                return res;
        }

        /**
         * Reallocates unallocated donor pledge surplus to another verified need.
         */
        @Transactional
        public void reallocateSurplus(int pledgeId, int targetNeedId, int quantityToAllocate) {
                if (quantityToAllocate <= 0) {
                        throw new IllegalArgumentException("Allocation quantity must be greater than zero.");
                }

                Pledge pledge = pledgeDAO.findById(pledgeId);
                if (pledge == null) {
                        throw new IllegalArgumentException("Pledge #" + pledgeId + " not found.");
                }

                int surplus = pledge.getQuantitySurplus();
                if (quantityToAllocate > surplus) {
                        throw new IllegalArgumentException(
                                        "Requested quantity (" + quantityToAllocate + ") exceeds available surplus ("
                                                        + surplus + ").");
                }

                Need targetNeed = needDAO.findById(targetNeedId);
                if (targetNeed == null) {
                        throw new IllegalArgumentException("Target need #" + targetNeedId + " not found.");
                }

                if (targetNeed.getQuantityRemaining() <= 0) {
                        throw new IllegalStateException("Target need #" + targetNeedId + " is already fulfilled.");
                }

                int actualTransfer = Math.min(quantityToAllocate, targetNeed.getQuantityRemaining());

                Allocation allocation = new Allocation();
                allocation.setPledgeId(pledgeId);
                allocation.setNeedId(targetNeedId);
                allocation.setQuantityAllocated(actualTransfer);
                allocation.setStatus("ALLOCATED");
                allocationDAO.save(allocation);

                int newPledgeAllocated = pledge.getQuantityAllocated() + actualTransfer;
                String newPledgeStatus = (newPledgeAllocated >= pledge.getQuantityOffered())
                                ? "ALLOCATED"
                                : "PARTIALLY_ALLOCATED";
                pledgeDAO.updateAllocationAndStatus(pledgeId, newPledgeAllocated, newPledgeStatus);

                int newNeedPledged = targetNeed.getQuantityPledged() + actualTransfer;
                int newNeedAllocated = targetNeed.getQuantityAllocated() + actualTransfer;
                int newNeedRemaining = Math.max(0, targetNeed.getQuantityRequired() - newNeedAllocated);
                String newNeedStatus = (newNeedRemaining == 0) ? "FULFILLED" : "PARTIALLY_FULFILLED";

                needDAO.updateQuantitiesAndStatus(
                                targetNeedId, newNeedPledged, newNeedAllocated, newNeedRemaining, newNeedStatus);

                needParticipantDAO.addOrActivateParticipant(targetNeedId, pledge.getDonorId());
        }

        public void updateAllocationDeliveryStatus(int allocationId, String status) {
                if (status == null || !VALID_DELIVERY_STATUSES.contains(status.trim().toUpperCase(Locale.ROOT))) {
                        throw new IllegalArgumentException("Invalid delivery status.");
                }
                Allocation existing = allocationDAO.findById(allocationId);
                if (existing == null) {
                        throw new IllegalArgumentException("Allocation #" + allocationId + " not found.");
                }
                if ("CANCELLED".equalsIgnoreCase(existing.getStatus())) {
                        throw new IllegalStateException("Cannot update a cancelled allocation.");
                }
                allocationDAO.updateStatus(allocationId, status);
        }

        private void syncParticipantStatusAfterCancel(int needId, int donorId) {
                List<Pledge> allPledges = pledgeDAO.findByNeedId(needId);
                boolean hasOtherActive = allPledges.stream()
                                .anyMatch(p -> p.getDonorId() == donorId
                                                && !"CANCELLED".equalsIgnoreCase(p.getStatus()));
                if (!hasOtherActive) {
                        needParticipantDAO.withdrawParticipant(needId, donorId);
                }
        }

        public List<Pledge> getPledgesByDonor(int donorId) {
                return pledgeDAO.findByDonorId(donorId);
        }

        public List<Pledge> getSurplusPledges() {
                return pledgeDAO.findSurplusPledges();
        }

        public List<Pledge> getAllPledges() {
                return pledgeDAO.findAll();
        }

        public List<Allocation> getAllAllocations() {
                return allocationDAO.findAll();
        }

        public List<Allocation> getAllocationsByDonor(int donorId) {
                return allocationDAO.findByDonorId(donorId);
        }

        public List<Allocation> getAllocationsByAffectedUser(int affectedUserId) {
                return allocationDAO.findByAffectedUserId(affectedUserId);
        }

        public List<Allocation> getAllocationsForNeed(int needId) {
                return allocationDAO.findByNeedId(needId);
        }

        public static double calculateHaversineKm(double lat1, double lon1, double lat2, double lon2) {
                final double R = 6371.0;
                double dLat = Math.toRadians(lat2 - lat1);
                double dLon = Math.toRadians(lon2 - lon1);
                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                + Math.cos(Math.toRadians(lat1))
                                                * Math.cos(Math.toRadians(lat2))
                                                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                return R * c;
        }
}