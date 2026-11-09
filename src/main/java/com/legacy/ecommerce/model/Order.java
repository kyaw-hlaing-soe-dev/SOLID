package com.legacy.ecommerce.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy Order entity.
 * Mixes state with cancellation validation, formatting, and audit string generation.
 */
public class Order {
    private String id;
    private String userId;
    private List<OrderItem> items = new ArrayList<>();
    private OrderStatus status;
    private double subtotal;
    private double discountAmount;
    private double taxAmount;
    private double shippingCost;
    private double totalAmount;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private String trackingNumber;
    private String invoiceNumber;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Order(String id, String userId, List<OrderItem> items) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Embedded business checks & formatting (SRP Violations) ---

    public boolean canBeCancelled() {
        return this.status != OrderStatus.DELIVERED && this.status != OrderStatus.CANCELLED;
    }

    public String formatReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("================ RECEIPT ================\n");
        sb.append("Order ID: ").append(id).append("\n");
        sb.append("Customer ID: ").append(userId).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Items:\n");
        for (OrderItem item : items) {
            sb.append(" - ").append(item.getProductName())
              .append(" x").append(item.getQuantity())
              .append(" @ $").append(String.format("%.2f", item.getUnitPrice()))
              .append(" = $").append(String.format("%.2f", item.getLineTotal())).append("\n");
        }
        sb.append("----------------------------------------\n");
        sb.append("Subtotal: $").append(String.format("%.2f", subtotal)).append("\n");
        sb.append("Discount: -$").append(String.format("%.2f", discountAmount)).append("\n");
        sb.append("Tax:      $").append(String.format("%.2f", taxAmount)).append("\n");
        sb.append("Shipping: $").append(String.format("%.2f", shippingCost)).append("\n");
        sb.append("Total:    $").append(String.format("%.2f", totalAmount)).append("\n");
        if (trackingNumber != null) {
            sb.append("Tracking: ").append(trackingNumber).append("\n");
        }
        sb.append("========================================\n");
        return sb.toString();
    }

    public String toAuditString() {
        return "[" + createdAt + "] ORDER " + id + " USER " + userId + " TOTAL " + totalAmount + " STATUS " + status;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
