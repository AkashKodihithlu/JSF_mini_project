package com.relief.model;

import java.sql.Timestamp;

public class AreaSafetyAssessment {

    private int assessmentId;
    private int disasterId;
    private int departmentId;
    private String areaName;
    private double latitude;
    private double longitude;
    private double radiusKm;
    private String safetyStatus; // SAFE, UNSAFE, MONITORING
    private Double floodLevelMeters; // Nullable
    private String assessmentNotes;
    private Timestamp assessedAt;
    private String departmentOfficerName;

    public AreaSafetyAssessment() {
    }

    public AreaSafetyAssessment(int assessmentId, int disasterId, int departmentId, String areaName,
                                double latitude, double longitude, double radiusKm, String safetyStatus,
                                Double floodLevelMeters, String assessmentNotes, Timestamp assessedAt) {
        this.assessmentId = assessmentId;
        this.disasterId = disasterId;
        this.departmentId = departmentId;
        this.areaName = areaName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
        this.safetyStatus = safetyStatus;
        this.floodLevelMeters = floodLevelMeters;
        this.assessmentNotes = assessmentNotes;
        this.assessedAt = assessedAt;
    }

    public int getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
    }

    public int getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(int disasterId) {
        this.disasterId = disasterId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(double radiusKm) {
        this.radiusKm = radiusKm;
    }

    public String getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(String safetyStatus) {
        this.safetyStatus = safetyStatus;
    }

    public Double getFloodLevelMeters() {
        return floodLevelMeters;
    }

    public void setFloodLevelMeters(Double floodLevelMeters) {
        this.floodLevelMeters = floodLevelMeters;
    }

    public String getAssessmentNotes() {
        return assessmentNotes;
    }

    public void setAssessmentNotes(String assessmentNotes) {
        this.assessmentNotes = assessmentNotes;
    }

    public Timestamp getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(Timestamp assessedAt) {
        this.assessedAt = assessedAt;
    }

    public String getDepartmentOfficerName() {
        return departmentOfficerName;
    }

    public void setDepartmentOfficerName(String departmentOfficerName) {
        this.departmentOfficerName = departmentOfficerName;
    }
}
