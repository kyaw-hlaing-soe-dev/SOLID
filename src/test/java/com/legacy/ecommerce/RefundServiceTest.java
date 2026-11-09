package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.OrderService;
import com.legacy.ecommerce.manager.ProductManager;
import com.legacy.ecommerce.manager.RefundService;
import com.legacy.ecommerce.manager.UserManager;
import com.legacy.ecommerce.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RefundServiceTest {

    private OrderService orderService;
    private RefundService refundService;
    private UserManager userManager;
    private ProductManager productManager;

    private User testUser;
    private Product product;

    @BeforeEach
    void setUp() {
        LegacyDatabase.getInstance().reset();
        orderService = new OrderService();
        refundService = new RefundService();
        userManager = new UserManager();
        productManager = new ProductManager();

        testUser = userManager.registerUser("Sarah", "sarah@test.com", "SecretPass1", "12 Birch Rd", "555-7777");
        product = productManager.addProduct("Camera Lens", "Photography", 500.0, 10, 1.0, "sup@lens.com");
    }

    @Test
    @DisplayName("Should cancel PAID order with 5% processing cancellation fee deduction")
    void testCancelOrder_PaidStatus_5PercentFee() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(product, 1);

        Order order = orderService.checkout(
                testUser.getId(),
                cart,
                PaymentMethod.PAYPAL,
                "sarah@paypal.com",
                testUser.getAddress(),
                "STANDARD_POST",
                false
        );

        double totalPaid = order.getTotalAmount();
        double expectedRefund = totalPaid * 0.95; // 5% fee deducted

        double actualRefund = refundService.cancelOrder(order.getId(), "Accidental purchase");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(expectedRefund, actualRefund, 0.01);

        // Product inventory must be restored back to 10
        assertEquals(10, productManager.getProductById(product.getId()).getStockQuantity());
    }

    @Test
    @DisplayName("Should cancel SHIPPED order with 20% restocking fee and non-refundable shipping")
    void testCancelOrder_ShippedStatus_20PercentRestockingFee() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(product, 1);

        Order order = orderService.checkout(
                testUser.getId(),
                cart,
                PaymentMethod.PAYPAL,
                "sarah@paypal.com",
                testUser.getAddress(),
                "DHL",
                false
        );

        orderService.updateOrderStatus(order.getId(), OrderStatus.SHIPPED);

        double productValue = order.getSubtotal() - order.getDiscountAmount();
        double restockingFee = productValue * 0.20;
        double expectedRefund = order.getTotalAmount() - order.getShippingCost() - restockingFee;

        double actualRefund = refundService.cancelOrder(order.getId(), "Refused package");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(expectedRefund, actualRefund, 0.01);
    }

    @Test
    @DisplayName("Should reject cancellation if order has already been DELIVERED")
    void testCancelOrder_Delivered_ThrowsException() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(product, 1);

        Order order = orderService.checkout(
                testUser.getId(),
                cart,
                PaymentMethod.PAYPAL,
                "sarah@paypal.com",
                testUser.getAddress(),
                "STANDARD_POST",
                false
        );

        orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);

        assertThrows(IllegalStateException.class, () ->
                refundService.cancelOrder(order.getId(), "No longer wanted"));
    }

    @Test
    @DisplayName("Should reverse loyalty points when order is cancelled")
    void testCancelOrder_ReversesLoyaltyPoints() {
        ShoppingCart cart = new ShoppingCart(testUser.getId());
        cart.addItem(product, 1);

        Order order = orderService.checkout(
                testUser.getId(),
                cart,
                PaymentMethod.PAYPAL,
                "sarah@paypal.com",
                testUser.getAddress(),
                "STANDARD_POST",
                false
        );

        int pointsAfterOrder = testUser.getLoyaltyPoints();
        assertTrue(pointsAfterOrder > 0);

        refundService.cancelOrder(order.getId(), "Customer requested cancel");

        assertEquals(0, testUser.getLoyaltyPoints());
    }
}
