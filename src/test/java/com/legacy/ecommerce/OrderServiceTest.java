package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.OrderService;
import com.legacy.ecommerce.manager.ProductManager;
import com.legacy.ecommerce.manager.UserManager;
import com.legacy.ecommerce.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    private OrderService orderService;
    private UserManager userManager;
    private ProductManager productManager;

    private User testUser;
    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {
        LegacyDatabase.getInstance().reset();
        orderService = new OrderService();
        userManager = new UserManager();
        productManager = new ProductManager();

        testUser = userManager.registerUser("Dave", "dave@test.com", "Password123", "456 Oak Lane", "555-8888");
        laptop = productManager.addProduct("Pro Laptop", "Electronics", 1000.0, 10, 2.0, "sup@laptop.com");
        mouse = productManager.addProduct("USB Mouse", "Accessories", 20.0, 50, 0.2, "sup@mouse.com");
    }

    @Test
    @DisplayName("Should successfully checkout: deduct inventory, process payment, create order, invoice, and shipment")
    void testCheckout_Success() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(laptop, 1);
        cart.addItem(mouse, 2); // Subtotal = 1040.0

        Order order = orderService.checkout(
                testUser.getId(),
                cart,
                PaymentMethod.PAYPAL,
                "dave@paypal.com",
                testUser.getAddress(),
                "FEDEX",
                false
        );

        assertNotNull(order);
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(1040.0, order.getSubtotal(), 0.001);
        assertNotNull(order.getInvoiceNumber());
        assertNotNull(order.getTrackingNumber());

        // Stock must be deducted
        assertEquals(9, productManager.getProductById(laptop.getId()).getStockQuantity());
        assertEquals(48, productManager.getProductById(mouse.getId()).getStockQuantity());

        // Cart should be cleared
        assertTrue(cart.getItems().isEmpty());

        // Loyalty points awarded: 1 point per $10 spent
        assertTrue(testUser.getLoyaltyPoints() > 0);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when attempting checkout with empty cart")
    void testCheckout_EmptyCart() {
        ShoppingCart emptyCart = new ShoppingCart(testUser.getId());

        assertThrows(IllegalArgumentException.class, () ->
                orderService.checkout(
                        testUser.getId(),
                        emptyCart,
                        PaymentMethod.PAYPAL,
                        "dave@paypal.com",
                        testUser.getAddress(),
                        "FEDEX",
                        false
                ));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when item stock is insufficient")
    void testCheckout_InsufficientStock() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(laptop, 15); // Only 10 available!

        assertThrows(IllegalStateException.class, () ->
                orderService.checkout(
                        testUser.getId(),
                        cart,
                        PaymentMethod.PAYPAL,
                        "dave@paypal.com",
                        testUser.getAddress(),
                        "FEDEX",
                        false
                ));

        // Ensure stock was not deducted
        assertEquals(10, productManager.getProductById(laptop.getId()).getStockQuantity());
    }

    @Test
    @DisplayName("Should apply VIP tier discount during checkout")
    void testCheckout_VipTierDiscount() {
        // Upgrade Dave to GOLD (10% discount)
        userManager.addLoyaltyPoints(testUser.getId(), 1000);
        assertEquals(CustomerTier.GOLD, testUser.getTier());

        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(laptop, 1); // 1000.0

        Order order = orderService.checkout(
                testUser.getId(),
                cart,
                PaymentMethod.PAYPAL,
                "dave@paypal.com",
                testUser.getAddress(),
                "STANDARD_POST",
                false
        );

        // 10% discount on 1000 = 100.0 discount
        assertEquals(100.0, order.getDiscountAmount(), 0.001);
    }

    @Test
    @DisplayName("Should rollback deducted inventory if payment processing fails")
    void testCheckout_PaymentFailureRollback() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(laptop, 2);

        // Intentionally provide invalid credit card details to force payment failure
        String invalidCard = "invalid-card-number";

        assertThrows(IllegalStateException.class, () ->
                orderService.checkout(
                        testUser.getId(),
                        cart,
                        PaymentMethod.CREDIT_CARD,
                        invalidCard,
                        testUser.getAddress(),
                        "DHL",
                        false
                ));

        // Inventory must be rolled back to 10
        assertEquals(10, productManager.getProductById(laptop.getId()).getStockQuantity());
    }

    @Test
    @DisplayName("Should update order status to SHIPPED and trigger carrier shipment status")
    void testUpdateOrderStatus() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(mouse, 1);

        Order order = orderService.checkout(
                testUser.getId(),
                cart,
                PaymentMethod.PAYPAL,
                "dave@paypal.com",
                testUser.getAddress(),
                "DHL",
                false
        );

        orderService.updateOrderStatus(order.getId(), OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }
}
