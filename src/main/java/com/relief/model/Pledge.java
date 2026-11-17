package com.relief.model;

import java.sql.Timestamp;

public class Pledge {
    private int pledgeId;
    private int needId;
    private int donorId;
    private int quantityOffered;
    private int quantityAllocated;
    private String status; // PLEDGED, PARTIALLY_ALLOCATED, ALLOCATED, CANCELLED, WITHDRAWN
    private String cancellationReason;
    private Timestamp createdAt;
    private Timestamp cancelledAt;

    // Optional joined display fields for Donor / Department views
    private String donorName;
    private String needCategory;
    private String needDescription;
    private String needUrgency;
    private String needStatus;

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

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public int getQuantityOffered() {
        return quantityOffered;
    }

    public void setQuantityOffered(int quantityOffered) {
        this.quantityOffered = quantityOffered;
    }

    public int getQuantityAllocated() {
        return quantityAllocated;
    }

    public void setQuantityAllocated(int quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    public int getQuantitySurplus() {
        return Math.max(0, quantityOffered - quantityAllocated);
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
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