package com.example.roadsideassistance;

public class BatteryRequest {
    private String requestId;
    private String userId;
    private String userEmail;
    private String vehicleType;
    private String fuelType;
    private String quantity;
    private String estimatedPrice;
    private String issue;
    private String location;
    private String status;
    private String assignedTechnician;
    private long createdAt;

    public BatteryRequest() {
        // Default constructor required for Firebase Realtime Database
    }

    public BatteryRequest(String requestId, String userId, String userEmail, String vehicleType, String fuelType, String quantity, String estimatedPrice, String issue, String location, String status, String assignedTechnician, long createdAt) {
        this.requestId = requestId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.vehicleType = vehicleType;
        this.fuelType = fuelType;
        this.quantity = quantity;
        this.estimatedPrice = estimatedPrice;
        this.issue = issue;
        this.location = location;
        this.status = status;
        this.assignedTechnician = assignedTechnician;
        this.createdAt = createdAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(String estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(String assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
