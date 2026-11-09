package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.OrderService;
import com.legacy.ecommerce.manager.ProductManager;
import com.legacy.ecommerce.manager.RefundService;
import com.legacy.ecommerce.manager.ReportManager;
import com.legacy.ecommerce.manager.UserManager;
import com.legacy.ecommerce.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReportManagerTest {

    private ReportManager reportManager;
    private OrderService orderService;
    private RefundService refundService;
    private UserManager userManager;
    private ProductManager productManager;

    @BeforeEach
    void setUp() {
        LegacyDatabase.getInstance().reset();
        reportManager = new ReportManager();
        orderService = new OrderService();
        refundService = new RefundService();
        userManager = new UserManager();
        productManager = new ProductManager();
    }

    @Test
    @DisplayName("Should aggregate revenue accurately, excluding cancelled orders")
    void testCalculateTotalRevenue() {
        User u1 = userManager.registerUser("User One", "one@rep.com", "Pass123", "Addr 1", "555-1111");
        Product p1 = productManager.addProduct("Gadget A", "Tech", 100.0, 50, 1.0, "sup@tech.com");

        ShoppingCart cart1 = new ShoppingCart(u1.getId());
        cart1.addItem(p1, 2); // 200.0
        Order order1 = orderService.checkout(u1.getId(), cart1, PaymentMethod.PAYPAL, "one@paypal.com", "Addr 1", "STANDARD_POST", false);

        ShoppingCart cart2 = new ShoppingCart(u1.getId());
        cart2.addItem(p1, 1); // 100.0
        Order order2 = orderService.checkout(u1.getId(), cart2, PaymentMethod.PAYPAL, "one@paypal.com", "Addr 1", "STANDARD_POST", false);

        assertEquals(2, reportManager.getTotalOrdersCount());
        double initialRevenue = reportManager.calculateTotalRevenue();
        assertEquals(order1.getTotalAmount() + order2.getTotalAmount(), initialRevenue, 0.01);

        // Cancel order 2
        refundService.cancelOrder(order2.getId(), "Test cancel");
        assertEquals(1, reportManager.getCancelledOrdersCount());

        // Cancelled order should now be excluded from total revenue
        double updatedRevenue = reportManager.calculateTotalRevenue();
        assertEquals(order1.getTotalAmount(), updatedRevenue, 0.01);
    }

    @Test
    @DisplayName("Should calculate top selling products by ordered quantity")
    void testGetTopSellingProducts() {
        User u = userManager.registerUser("Buyer", "buyer@rep.com", "Pass123", "Addr", "555-2222");
        Product pA = productManager.addProduct("Product A", "Cat1", 10.0, 100, 0.5, "sup@a.com");
        Product pB = productManager.addProduct("Product B", "Cat2", 20.0, 100, 0.5, "sup@b.com");

        ShoppingCart cart = new ShoppingCart(u.getId());
        cart.addItem(pA, 10);
        cart.addItem(pB, 3);
        orderService.checkout(u.getId(), cart, PaymentMethod.PAYPAL, "b@pay.com", "Addr", "STANDARD_POST", false);

        Map<String, Integer> top = reportManager.getTopSellingProducts(5);
        assertEquals(10, top.get("Product A"));
        assertEquals(3, top.get("Product B"));
    }

    @Test
    @DisplayName("Should group revenue breakdown by product category")
    void testGetRevenueByCategory() {
        User u = userManager.registerUser("Buyer", "buyer2@rep.com", "Pass123", "Addr", "555-3333");
        Product phone = productManager.addProduct("Smart Phone", "Electronics", 800.0, 10, 0.3, "sup@phone.com");
        Product book = productManager.addProduct("Java Guide", "Books", 50.0, 20, 0.9, "sup@book.com");

        ShoppingCart cart = new ShoppingCart(u.getId());
        cart.addItem(phone, 1);
        cart.addItem(book, 2);
        orderService.checkout(u.getId(), cart, PaymentMethod.PAYPAL, "b2@pay.com", "Addr", "STANDARD_POST", false);

        Map<String, Double> categoryRevenue = reportManager.getRevenueByCategory();
        assertEquals(800.0, categoryRevenue.get("Electronics"), 0.01);
        assertEquals(100.0, categoryRevenue.get("Books"), 0.01);
    }

    @Test
    @DisplayName("Should export sales report to CSV string with appropriate columns")
    void testExportSalesReportToCsv() {
        User u = userManager.registerUser("Buyer", "buyer3@rep.com", "Pass123", "Addr", "555-4444");
        Product item = productManager.addProduct("Item X", "Misc", 50.0, 10, 0.5, "sup@misc.com");

        ShoppingCart cart = new ShoppingCart(u.getId());
        cart.addItem(item, 1);
        orderService.checkout(u.getId(), cart, PaymentMethod.PAYPAL, "b3@pay.com", "Addr", "STANDARD_POST", false);

        String csv = reportManager.exportSalesReportToCsv();
        assertTrue(csv.contains("Order ID,User ID,Status,Subtotal,Discount,Tax,Shipping,Total,Created At"));
        assertTrue(csv.contains("PAID"));
    }
}
