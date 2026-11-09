package com.legacy.ecommerce;

import com.legacy.ecommerce.model.Product;
import com.legacy.ecommerce.model.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingCartTest {

    private ShoppingCart cart;
    private Product p1;
    private Product p2;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart("test-user");
        p1 = new Product("P1", "Wireless Mouse", "Accessories", 50.0, 100, 0.2, "sup@mouse.com");
        p2 = new Product("P2", "Keyboard", "Accessories", 100.0, 50, 1.0, "sup@key.com");
    }

    @Test
    @DisplayName("Should add items and correctly calculate subtotal")
    void testAddAndRemoveItems() {
        cart.addItem(p1, 2); // 2 * 50 = 100
        cart.addItem(p2, 1); // 1 * 100 = 100

        assertEquals(2, cart.getItems().size());
        assertEquals(200.0, cart.calculateSubtotal(), 0.001);

        cart.removeItem("P1");
        assertEquals(1, cart.getItems().size());
        assertEquals(100.0, cart.calculateSubtotal(), 0.001);
    }

    @Test
    @DisplayName("Should apply 5% bulk discount when product quantity is 5 or more")
    void testBulkDiscountCalculation() {
        cart.addItem(p1, 5); // 5 * 50 = 250. 5% bulk discount = 12.50
        cart.addItem(p2, 2); // 2 * 100 = 200. No bulk discount.

        assertEquals(450.0, cart.calculateSubtotal(), 0.001);
        assertEquals(12.50, cart.calculateBulkDiscount(), 0.001);
    }

    @Test
    @DisplayName("Should apply SUMMER10 coupon for 10% discount")
    void testCouponSummer10() {
        cart.addItem(p1, 2); // 100.0
        boolean applied = cart.applyCoupon("SUMMER10");

        assertTrue(applied);
        assertEquals("SUMMER10", cart.getCouponCode());
        assertEquals(10.0, cart.getAppliedDiscountAmount(), 0.001);
    }

    @Test
    @DisplayName("Should apply SAVE20 coupon for 20% discount capped at $50")
    void testCouponSave20CappedAt50() {
        cart.addItem(p2, 4); // 4 * 100 = 400. 20% of 400 is 80, but capped at 50!
        boolean applied = cart.applyCoupon("SAVE20");

        assertTrue(applied);
        assertEquals(50.0, cart.getAppliedDiscountAmount(), 0.001);
    }

    @Test
    @DisplayName("Should reject FLAT50 coupon if subtotal is below $200 threshold")
    void testCouponFlat50Threshold() {
        cart.addItem(p1, 2); // 100.0 < 200.0
        assertFalse(cart.applyCoupon("FLAT50"));

        cart.addItem(p2, 2); // Subtotal becomes 300.0 >= 200.0
        assertTrue(cart.applyCoupon("FLAT50"));
        assertEquals(50.0, cart.getAppliedDiscountAmount(), 0.001);
    }

    @Test
    @DisplayName("Should calculate estimated total including bulk discount, coupon, and 8% tax")
    void testCalculateEstimatedTotal() {
        cart.addItem(p2, 3); // 3 * 100 = 300
        cart.applyCoupon("SUMMER10"); // 10% of 300 = 30. Taxable = 270.
        // Tax = 270 * 0.08 = 21.60. Total = 270 + 21.60 = 291.60

        assertEquals(291.60, cart.calculateEstimatedTotal(), 0.01);
    }
}
