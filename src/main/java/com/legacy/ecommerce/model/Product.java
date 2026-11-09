package com.legacy.ecommerce.model;

/**
 * Legacy Product entity.
 * Intentionally mixes data model, presentation formatting, and stock threshold logic.
 */
public class Product {
    private String id;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;
    private int reorderThreshold;
    private double weightKg;
    private String supplierEmail;

    public Product() {
        this.reorderThreshold = 5;
    }

    public Product(String id, String name, String category, double price, int stockQuantity, double weightKg, String supplierEmail) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.reorderThreshold = 5;
        this.weightKg = weightKg;
        this.supplierEmail = supplierEmail;
    }

    // --- Embedded business rules & presentation logic (SRP Violations) ---

    public boolean isLowStock() {
        return this.stockQuantity <= this.reorderThreshold;
    }

    public boolean isEligibleForBulkDiscount(int purchaseQuantity) {
        // Hardcoded rule: bulk discount applies if purchasing 5 or more units
        return purchaseQuantity >= 5;
    }

    public String toHtmlCard() {
        return "<div class='product-card'>" +
                "<h3>" + name + "</h3>" +
                "<p>Category: " + category + "</p>" +
                "<p>Price: $" + String.format("%.2f", price) + "</p>" +
                "<p>Stock: " + stockQuantity + "</p>" +
                "</div>";
    }

    // --- Getters and Setters ---

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }
}
