
package com.relief.service;

import com.relief.dao.AllocationDAO;
import com.relief.dao.NeedDAO;
import com.relief.dao.NeedParticipantDAO;
import com.relief.dao.PledgeDAO;

import com.relief.model.Allocation;
import com.relief.model.Need;
import com.relief.model.Pledge;
import com.relief.model.User;

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

    private static final Set<String> VALID_DELIVERY_STATUSES = Set.of("ALLOCATED", "IN_TRANSIT", "DELIVERED",
            "REASSIGNED");

    private final PledgeDAO pledgeDAO;
    private final AllocationDAO allocationDAO;
    private final NeedDAO needDAO;
    private final NeedParticipantDAO needParticipantDAO;

    // Single constructor: Spring automatically injects all four dependencies.
    public PledgeAllocationService(
            PledgeDAO pledgeDAO,
            AllocationDAO allocationDAO,
            NeedDAO needDAO,
            NeedParticipantDAO needParticipantDAO) {

        this.pledgeDAO = pledgeDAO;
        this.allocationDAO = allocationDAO;
        this.needDAO = needDAO;
        this.needParticipantDAO = needParticipantDAO;
    }

    public static class MatchedNeed {

        private final Need need;
        private final double distanceKm;
        private final double matchScore;

        public MatchedNeed(Need need, double distanceKm, double matchScore) {
            this.need = need;
            this.distanceKm = distanceKm;
            this.matchScore = matchScore;
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
    }

    /**
     * Ranks verified/partially-fulfilled needs for a donor based on urgency
     * and GPS proximity.
     */
    public List<MatchedNeed> getMatchedNeedsForDonor(User donor) {

        List<Need> availableNeeds = needDAO.findAvailableForDonors();
        List<MatchedNeed> ranked = new ArrayList<>();

        for (Need need : availableNeeds) {

            double distanceKm = calculateHaversineKm(
                    donor.getLatitude(), donor.getLongitude(),
                    need.getLatitude(), need.getLongitude());

            double roundedDist = Math.round(distanceKm * 10.0) / 10.0;

            int urgencyScore = switch (need.getUrgency().toUpperCase(Locale.ROOT)) {
                case "CRITICAL" -> 100;
                case "HIGH" -> 70;
                case "MEDIUM" -> 40;
                default -> 20;
            };

            double proximityBonus = Math.max(0.0, 50.0 - roundedDist);
            double matchScore = Math.round(
                    (urgencyScore + proximityBonus) * 10.0) / 10.0;

            ranked.add(new MatchedNeed(need, roundedDist, matchScore));
        }

        ranked.sort(
                Comparator.comparingDouble(MatchedNeed::getMatchScore).reversed());

        return ranked;
    }

    /**
     * Donor commits a pledge toward a verified need.
     * Automatically allocates up to the remaining required quantity.
     */
    @Transactional
    public Map<String, Object> createPledgeAndAllocate(
            int needId, int donorId, int quantityOffered) {

        if (quantityOffered <= 0) {
            throw new IllegalArgumentException(
                    "Pledge quantity must be greater than zero.");
        }

        Need need = needDAO.findById(needId);

        if (need == null) {
            throw new IllegalArgumentException(
                    "Relief need #" + needId + " does not exist.");
        }

        if (!"VERIFIED".equalsIgnoreCase(need.getStatus())
                && !"PARTIALLY_FULFILLED".equalsIgnoreCase(need.getStatus())) {
            throw new IllegalStateException(
                    "Pledges can only be made to verified needs.");
        }

        int immediateAllocation = Math.min(
                quantityOffered, need.getQuantityRemaining());

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

        int savedPledgeId = donorPledges.isEmpty()
                ? 0
                : donorPledges.get(0).getPledgeId();

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

        int updatedRemaining = Math.max(
                0, need.getQuantityRequired() - updatedAllocated);

        String updatedNeedStatus = (updatedRemaining == 0)
                ? "FULFILLED"
                : "PARTIALLY_FULFILLED";

        needDAO.updateQuantitiesAndStatus(
                needId,
                updatedPledged,
                updatedAllocated,
                updatedRemaining,
                updatedNeedStatus);

        // Register or reactivate donor in need_participants.
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
     * Donor withdraws an existing pledge.
     */
    @Transactional
    public void withdrawPledge(int pledgeId, int donorId) {

        Pledge pledge = pledgeDAO.findById(pledgeId);

        if (pledge == null) {
            throw new IllegalArgumentException(
                    "Pledge #" + pledgeId + " not found.");
        }

        if (pledge.getDonorId() != donorId) {
            throw new IllegalArgumentException(
                    "You can only withdraw your own pledges.");
        }

        if ("CANCELLED".equalsIgnoreCase(pledge.getStatus())) {
            throw new IllegalStateException(
                    "Pledge #" + pledgeId + " is already cancelled.");
        }

        pledgeDAO.updateStatus(pledgeId, "CANCELLED");

        Need need = needDAO.findById(pledge.getNeedId());

        if (need != null) {

            int newPledged = Math.max(
                    0, need.getQuantityPledged() - pledge.getQuantityOffered());

            int newAllocated = Math.max(
                    0, need.getQuantityAllocated() - pledge.getQuantityAllocated());

            int newRemaining = Math.min(
                    need.getQuantityRequired(),
                    Math.max(0, need.getQuantityRequired() - newAllocated));

            String newStatus = (newAllocated == 0)
                    ? "VERIFIED"
                    : (newRemaining == 0 ? "FULFILLED" : "PARTIALLY_FULFILLED");

            needDAO.updateQuantitiesAndStatus(
                    need.getNeedId(),
                    newPledged,
                    newAllocated,
                    newRemaining,
                    newStatus);
        }

        // Check if donor has other active pledges for this need.
        List<Pledge> allPledges = pledgeDAO.findByNeedId(pledge.getNeedId());

        boolean hasOtherActive = allPledges.stream()
                .anyMatch(p -> p.getDonorId() == donorId
                        && p.getPledgeId() != pledgeId
                        && !"CANCELLED".equalsIgnoreCase(p.getStatus()));

        if (!hasOtherActive) {
            needParticipantDAO.withdrawParticipant(
                    pledge.getNeedId(), donorId);
        }
    }

    /**
     * Department Officer reallocates unallocated surplus.
     */
    @Transactional
    public void reallocateSurplus(
            int pledgeId, int targetNeedId, int quantityToAllocate) {

        if (quantityToAllocate <= 0) {
            throw new IllegalArgumentException(
                    "Allocation quantity must be greater than zero.");
        }

        Pledge pledge = pledgeDAO.findById(pledgeId);

        if (pledge == null) {
            throw new IllegalArgumentException(
                    "Pledge #" + pledgeId + " not found.");
        }

        int surplus = pledge.getQuantitySurplus();

        if (quantityToAllocate > surplus) {
            throw new IllegalArgumentException(
                    "Requested quantity (" + quantityToAllocate
                            + ") exceeds available surplus (" + surplus + ").");
        }

        Need targetNeed = needDAO.findById(targetNeedId);

        if (targetNeed == null) {
            throw new IllegalArgumentException(
                    "Target need #" + targetNeedId + " not found.");
        }

        if (targetNeed.getQuantityRemaining() <= 0) {
            throw new IllegalStateException(
                    "Target need #" + targetNeedId + " is already fulfilled.");
        }

        int actualTransfer = Math.min(
                quantityToAllocate, targetNeed.getQuantityRemaining());

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

        pledgeDAO.updateAllocationAndStatus(
                pledgeId, newPledgeAllocated, newPledgeStatus);

        int newNeedPledged = targetNeed.getQuantityPledged() + actualTransfer;

        int newNeedAllocated = targetNeed.getQuantityAllocated() + actualTransfer;

        int newNeedRemaining = Math.max(
                0, targetNeed.getQuantityRequired() - newNeedAllocated);

        String newNeedStatus = (newNeedRemaining == 0)
                ? "FULFILLED"
                : "PARTIALLY_FULFILLED";

        needDAO.updateQuantitiesAndStatus(
                targetNeedId,
                newNeedPledged,
                newNeedAllocated,
                newNeedRemaining,
                newNeedStatus);
    }

    public void updateAllocationDeliveryStatus(
            int allocationId, String status) {

        if (status == null
                || !VALID_DELIVERY_STATUSES.contains(
                        status.trim().toUpperCase(Locale.ROOT))) {

            throw new IllegalArgumentException("Invalid delivery status.");
        }

        if (allocationDAO.findById(allocationId) == null) {
            throw new IllegalArgumentException(
                    "Allocation #" + allocationId + " not found.");
        }

        allocationDAO.updateStatus(allocationId, status);
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

    public List<Allocation> getAllocationsForNeed(int needId) {
        return allocationDAO.findByNeedId(needId);
    }

    public static double calculateHaversineKm(
            double lat1, double lon1, double lat2, double lon2) {

        final double R = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
