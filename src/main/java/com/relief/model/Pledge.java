package com.relief.model;

import java.sql.Timestamp;

public class Pledge {
    private int pledgeId;
    private int needId;
    private int donorId;
    private int quantityOffered;
    private int quantityAllocated;
    private String status; // PLEDGED, PARTIALLY_ALLOCATED, ALLOCATED, CANCELLED
    private Timestamp createdAt;

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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}