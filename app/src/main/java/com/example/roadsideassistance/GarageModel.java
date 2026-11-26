package com.example.roadsideassistance;

public class GarageModel {
    private String id;
    private String name;
    private String address;
    private String distance;
    private String rating;
    private String phone;
    private String serviceType;

    public GarageModel() {
        // Default constructor
    }

    public GarageModel(String id, String name, String address, String distance, String rating, String phone, String serviceType) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.rating = rating;
        this.phone = phone;
        this.serviceType = serviceType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
