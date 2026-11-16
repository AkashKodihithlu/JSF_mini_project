package com.relief.model;

public class Need {
    private int needId;
    private int disasterId;
    private int createdBy;
    private String category;
    private String description;
    private int quantityRequired;
    private int quantityPledged;
    private int quantityAllocated;
    private int quantityRemaining;
    private String urgency;
    private String status;
    private double latitude;
    private double longitude;

    public int getNeedId() { return needId; }
    public void setNeedId(int needId) { this.needId = needId; }

    public int getDisasterId() { return disasterId; }
    public void setDisasterId(int disasterId) { this.disasterId = disasterId; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(int quantityRequired) { this.quantityRequired = quantityRequired; }

    public int getQuantityPledged() { return quantityPledged; }
    public void setQuantityPledged(int quantityPledged) { this.quantityPledged = quantityPledged; }

    public int getQuantityAllocated() { return quantityAllocated; }
    public void setQuantityAllocated(int quantityAllocated) { this.quantityAllocated = quantityAllocated; }

    public int getQuantityRemaining() { return quantityRemaining; }
    public void setQuantityRemaining(int quantityRemaining) { this.quantityRemaining = quantityRemaining; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
