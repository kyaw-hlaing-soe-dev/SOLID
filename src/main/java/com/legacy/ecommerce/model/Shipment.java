package com.legacy.ecommerce.model;

import java.time.LocalDateTime;

public class Shipment {
    private String trackingNumber;
    private String orderId;
    private String carrier; // "DHL", "FEDEX", "STANDARD_POST"
    private String shippingAddress;
    private double weightKg;
    private double shippingCost;
    private boolean express;
    private String status; // "PENDING", "DISPATCHED", "IN_TRANSIT", "DELIVERED"
    private LocalDateTime estimatedDelivery;

    public Shipment() {
        this.status = "PENDING";
        this.estimatedDelivery = LocalDateTime.now().plusDays(3);
    }

    public Shipment(String trackingNumber, String orderId, String carrier, String shippingAddress, double weightKg, double shippingCost, boolean express) {
        this.trackingNumber = trackingNumber;
        this.orderId = orderId;
        this.carrier = carrier;
        this.shippingAddress = shippingAddress;
        this.weightKg = weightKg;
        this.shippingCost = shippingCost;
        this.express = express;
        this.status = "DISPATCHED";
        this.estimatedDelivery = express ? LocalDateTime.now().plusDays(1) : LocalDateTime.now().plusDays(4);
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public boolean isExpress() {
        return express;
    }

    public void setExpress(boolean express) {
        this.express = express;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }
}
