package com.legacy.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy ShoppingCart.
 * Mixes cart collection holding with discount calculations, coupon parsing, and tax estimations.
 */
public class ShoppingCart {
    private String userId;
    private List<CartItem> items = new ArrayList<>();
    private String couponCode;
    private double appliedDiscountAmount;

    public ShoppingCart() {
    }

    public ShoppingCart(String userId) {
        this.userId = userId;
    }

    public void addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(product, quantity));
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void updateQuantity(String productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(productId);
            return;
        }
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(newQuantity);
                return;
            }
        }
    }

    public void clear() {
        items.clear();
        couponCode = null;
        appliedDiscountAmount = 0.0;
    }

    public double calculateSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : items) {
            subtotal += item.getLineTotal();
        }
        return subtotal;
    }

    public double calculateBulkDiscount() {
        double bulkDiscount = 0.0;
        for (CartItem item : items) {
            // Duplicated business rule: >= 5 quantity gets 5% off
            if (item.getQuantity() >= 5) {
                bulkDiscount += item.getLineTotal() * 0.05;
            }
        }
        return bulkDiscount;
    }

    public boolean applyCoupon(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        double subtotal = calculateSubtotal();
        String upperCode = code.trim().toUpperCase();

        // Hardcoded coupon rules
        if (upperCode.equals("SUMMER10")) {
            this.couponCode = upperCode;
            this.appliedDiscountAmount = subtotal * 0.10;
            return true;
        } else if (upperCode.equals("SAVE20")) {
            this.couponCode = upperCode;
            double discount = subtotal * 0.20;
            // Cap at $50
            this.appliedDiscountAmount = Math.min(discount, 50.0);
            return true;
        } else if (upperCode.equals("FLAT50")) {
            if (subtotal >= 200.0) {
                this.couponCode = upperCode;
                this.appliedDiscountAmount = 50.0;
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public double calculateEstimatedTax(double taxRate) {
        double taxableAmount = calculateSubtotal() - calculateBulkDiscount() - appliedDiscountAmount;
        if (taxableAmount < 0) {
            taxableAmount = 0;
        }
        return taxableAmount * taxRate;
    }

    public double calculateEstimatedTotal() {
        double subtotal = calculateSubtotal();
        double bulkDiscount = calculateBulkDiscount();
        double estimatedTax = calculateEstimatedTax(0.08); // standard 8% tax
        double total = subtotal - bulkDiscount - appliedDiscountAmount + estimatedTax;
        return Math.max(0.0, total);
    }

    public double getTotalWeightKg() {
        double totalWeight = 0.0;
        for (CartItem item : items) {
            if (item.getProduct() != null) {
                totalWeight += item.getProduct().getWeightKg() * item.getQuantity();
            }
        }
        return totalWeight;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public double getAppliedDiscountAmount() {
        return appliedDiscountAmount;
    }
}
