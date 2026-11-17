package com.relief.model;

import java.sql.Timestamp;

public class Allocation {
    private int allocationId;
    private int pledgeId;
    private int needId;
    private int quantityAllocated;
    private String status; // ALLOCATED, IN_TRANSIT, DELIVERED, CONFIRMED, REASSIGNED, CANCELLED
    private String cancellationReason;
    private Timestamp allocatedAt;
    private Timestamp cancelledAt;
    private Timestamp confirmedAt;

    // Joined display fields for Horizontal Timeline across all 3 dashboards
    private int donorId;
    private String donorName;
    private String donorMobile;
    private int affectedPersonId;
    private String affectedPersonName;
    private String needCategory;
    private String needDescription;
    private String needUrgency;
    private String needStatus;

    public int getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(int allocationId) {
        this.allocationId = allocationId;
    }

    public int getPledgeId() {
        return pledgeId;
    }

    public void setPledgeId(int pledgeId) {
        this.pledgeId = pledgeId;
    }

    public int getNeedId() {
        return needId;
    }

    public void setNeedId(int needId) {
        this.needId = needId;
    }

    public int getQuantityAllocated() {
        return quantityAllocated;
    }

    public void setQuantityAllocated(int quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Timestamp getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(Timestamp allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Timestamp getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Timestamp confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorMobile() {
        return donorMobile;
    }

    public void setDonorMobile(String donorMobile) {
        this.donorMobile = donorMobile;
    }

    public int getAffectedPersonId() {
        return affectedPersonId;
    }

    public void setAffectedPersonId(int affectedPersonId) {
        this.affectedPersonId = affectedPersonId;
    }

    public String getAffectedPersonName() {
        return affectedPersonName;
    }

    public void setAffectedPersonName(String affectedPersonName) {
        this.affectedPersonName = affectedPersonName;
    }

    public String getNeedCategory() {
        return needCategory;
    }

    public void setNeedCategory(String needCategory) {
        this.needCategory = needCategory;
    }

    public String getNeedDescription() {
        return needDescription;
    }

    public void setNeedDescription(String needDescription) {
        this.needDescription = needDescription;
    }

    public String getNeedUrgency() {
        return needUrgency;
    }

    public void setNeedUrgency(String needUrgency) {
        this.needUrgency = needUrgency;
    }

    public String getNeedStatus() {
        return needStatus;
    }

    public void setNeedStatus(String needStatus) {
        this.needStatus = needStatus;
    }
}