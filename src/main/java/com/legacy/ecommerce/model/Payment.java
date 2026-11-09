package com.legacy.ecommerce.model;

import java.time.LocalDateTime;

public class Payment {
    private String id;
    private String orderId;
    private PaymentMethod method;
    private double amount;
    private double fee;
    private String transactionReference;
    private String status; // "SUCCESS", "FAILED", "REFUNDED"
    private LocalDateTime timestamp;

    public Payment() {
        this.timestamp = LocalDateTime.now();
    }

    public Payment(String id, String orderId, PaymentMethod method, double amount, double fee, String transactionReference, String status) {
        this.id = id;
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.fee = fee;
        this.transactionReference = transactionReference;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
