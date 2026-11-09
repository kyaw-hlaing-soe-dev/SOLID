package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.ProductManager;
import com.legacy.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductAndInventoryTest {

    private ProductManager productManager;

    @BeforeEach
    void setUp() {
        LegacyDatabase.getInstance().reset();
        productManager = new ProductManager();
    }

    @Test
    @DisplayName("Should successfully add product with positive initial stock and price")
    void testAddProduct_Success() {
        Product product = productManager.addProduct("Gaming Laptop", "Electronics", 1500.0, 10, 2.5, "supplier@gaming.com");

        assertNotNull(product);
        assertNotNull(product.getId());
        assertEquals("Gaming Laptop", product.getName());
        assertEquals(1500.0, product.getPrice());
        assertEquals(10, product.getStockQuantity());
        assertNotNull(productManager.getProductById(product.getId()));
    }

    @Test
    @DisplayName("Should reject product creation with invalid parameters")
    void testAddProduct_InvalidParameters() {
        assertThrows(IllegalArgumentException.class, () ->
                productManager.addProduct("", "Electronics", 100.0, 5, 1.0, "sup@test.com"));

        assertThrows(IllegalArgumentException.class, () ->
                productManager.addProduct("Monitor", "Electronics", -50.0, 5, 1.0, "sup@test.com"));

        assertThrows(IllegalArgumentException.class, () ->
                productManager.addProduct("Keyboard", "Electronics", 50.0, -1, 1.0, "sup@test.com"));
    }

    @Test
    @DisplayName("Should filter and search products by keyword, category, and price range")
    void testSearchProducts() {
        productManager.addProduct("MacBook Pro 16", "Laptops", 2499.0, 5, 2.1, "apple@supply.com");
        productManager.addProduct("Dell XPS 15", "Laptops", 1899.0, 8, 1.9, "dell@supply.com");
        productManager.addProduct("iPad Pro", "Tablets", 999.0, 12, 0.6, "apple@supply.com");

        // By Category
        List<Product> laptops = productManager.searchProducts(null, "Laptops", null, null);
        assertEquals(2, laptops.size());

        // By Keyword
        List<Product> appleItems = productManager.searchProducts("Pro", null, null, null);
        assertEquals(2, appleItems.size());

        // By Price range
        List<Product> midRange = productManager.searchProducts(null, null, 1000.0, 2000.0);
        assertEquals(1, midRange.size());
        assertEquals("Dell XPS 15", midRange.get(0).getName());
    }

    @Test
    @DisplayName("Should correctly deduct inventory when sufficient stock is available")
    void testDeductStock_Success() {
        Product product = productManager.addProduct("Wireless Headphones", "Audio", 199.99, 20, 0.3, "audio@brand.com");

        productManager.deductStock(product.getId(), 5);
        assertEquals(15, product.getStockQuantity());

        productManager.deductStock(product.getId(), 10);
        assertEquals(5, product.getStockQuantity());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when deducting more than available stock")
    void testDeductStock_InsufficientStock() {
        Product product = productManager.addProduct("USB Hub", "Accessories", 25.0, 3, 0.1, "hub@brand.com");

        assertThrows(IllegalStateException.class, () ->
                productManager.deductStock(product.getId(), 5));
        // Stock should remain unchanged
        assertEquals(3, product.getStockQuantity());
    }

    @Test
    @DisplayName("Should restore stock correctly during returns or cancellations")
    void testRestoreStock() {
        Product product = productManager.addProduct("Office Chair", "Furniture", 250.0, 10, 15.0, "chair@brand.com");

        productManager.deductStock(product.getId(), 4);
        assertEquals(6, product.getStockQuantity());

        productManager.restoreStock(product.getId(), 4);
        assertEquals(10, product.getStockQuantity());
    }

    @Test
    @DisplayName("Should flag low stock when quantity falls below reorder threshold")
    void testLowStockAlert() {
        Product product = productManager.addProduct("Mechanical Keyboard", "Accessories", 120.0, 7, 1.1, "supplier@keys.com");
        product.setReorderThreshold(5);

        assertFalse(product.isLowStock());

        productManager.deductStock(product.getId(), 3); // 4 left <= 5
        assertTrue(product.isLowStock());
    }
}
