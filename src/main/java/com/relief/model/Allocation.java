package com.relief.model;

import java.sql.Timestamp;

public class Allocation {
    private int allocationId;
    private int pledgeId;
    private int needId;
    private int quantityAllocated;
    private String status; // ALLOCATED, IN_TRANSIT, DELIVERED, REASSIGNED
    private Timestamp allocatedAt;

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

    public Timestamp getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(Timestamp allocatedAt) {
        this.allocatedAt = allocatedAt;
    }
}