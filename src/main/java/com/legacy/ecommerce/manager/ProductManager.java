package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Legacy Product & Inventory Manager.
 * Combines catalog CRUD, stock level changes, low-stock alerts, supplier reorders,
 * catalog search/filter, and HTML rendering.
 */
public class ProductManager {
    private final LegacyDatabase database = LegacyDatabase.getInstance();
    private final NotificationService notificationService = new NotificationService();

    public Product addProduct(String name, String category, double price, int initialStock, double weightKg, String supplierEmail) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Product price cannot be negative.");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        }

        String productId = "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Product product = new Product(productId, name.trim(), category, price, initialStock, weightKg, supplierEmail);
        database.getProducts().put(productId, product);
        database.addAuditLog("PRODUCT_ADDED: " + productId + " (" + name + ")");
        return product;
    }

    public Product getProductById(String productId) {
        return database.getProducts().get(productId);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(database.getProducts().values());
    }

    public List<Product> searchProducts(String keyword, String category, Double minPrice, Double maxPrice) {
        List<Product> results = new ArrayList<>();
        for (Product product : database.getProducts().values()) {
            boolean matchesKeyword = keyword == null || keyword.trim().isEmpty() ||
                    product.getName().toLowerCase().contains(keyword.toLowerCase());
            boolean matchesCategory = category == null || category.trim().isEmpty() ||
                    product.getCategory().equalsIgnoreCase(category);
            boolean matchesMin = minPrice == null || product.getPrice() >= minPrice;
            boolean matchesMax = maxPrice == null || product.getPrice() <= maxPrice;

            if (matchesKeyword && matchesCategory && matchesMin && matchesMax) {
                results.add(product);
            }
        }
        return results;
    }

    // --- Inventory Management Logic mixed in ---

    public synchronized boolean hasSufficientStock(String productId, int quantity) {
        Product product = getProductById(productId);
        return product != null && product.getStockQuantity() >= quantity;
    }

    public synchronized void deductStock(String productId, int quantity) {
        Product product = getProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName()
                    + " (Available: " + product.getStockQuantity() + ", Requested: " + quantity + ")");
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        database.addAuditLog("STOCK_DEDUCTED: " + productId + " -" + quantity + " (Remaining: " + product.getStockQuantity() + ")");

        // Check low stock alert and trigger supplier notification
        if (product.isLowStock()) {
            triggerLowStockAlert(product);
        }
    }

    public synchronized void restoreStock(String productId, int quantity) {
        Product product = getProductById(productId);
        if (product != null && quantity > 0) {
            product.setStockQuantity(product.getStockQuantity() + quantity);
            database.addAuditLog("STOCK_RESTORED: " + productId + " +" + quantity + " (New Stock: " + product.getStockQuantity() + ")");
        }
    }

    private void triggerLowStockAlert(Product product) {
        String alertMessage = "LOW STOCK WARNING: Product '" + product.getName() + "' (ID: " + product.getId() + ")"
                + " has reached " + product.getStockQuantity() + " units (Reorder threshold: " + product.getReorderThreshold() + ").";
        System.err.println(alertMessage);
        database.addAuditLog(alertMessage);

        if (product.getSupplierEmail() != null && !product.getSupplierEmail().isEmpty()) {
            notificationService.sendEmail(
                    product.getSupplierEmail(),
                    "Purchase Order Needed: " + product.getName(),
                    "Attention Supplier,\nStock for " + product.getName() + " is critical (" + product.getStockQuantity() + " left).\nPlease process restock."
            );
        }
    }

    // --- Web HTML Presentation mixed into Manager (SRP Violation) ---

    public String generateCatalogHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='catalog-container'>\n");
        for (Product product : database.getProducts().values()) {
            sb.append(product.toHtmlCard()).append("\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }
}
