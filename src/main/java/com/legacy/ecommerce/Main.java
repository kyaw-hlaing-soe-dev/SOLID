package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.*;
import com.legacy.ecommerce.model.*;

/**
 * Application Entry Point / Demonstration Runner.
 * Runs through complete enterprise workflows: user registration, product management,
 * shopping cart, checkout, payment processing, shipping dispatch, reporting, and refund.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   STARTING ENTERPRISE E-COMMERCE SYSTEM (LEGACY MONOLITH)       ");
        System.out.println("=================================================================");

        // Initialize Services
        UserManager userManager = new UserManager();
        ProductManager productManager = new ProductManager();
        OrderService orderService = new OrderService();
        RefundService refundService = new RefundService();
        ReportManager reportManager = new ReportManager();
        ShippingManager shippingManager = new ShippingManager();

        // 1. Seed Product Catalog
        System.out.println("\n--- Step 1: Populating Product Catalog ---");
        Product laptop = productManager.addProduct("ThinkPad X1 Carbon", "Electronics", 1299.99, 15, 1.4, "supplier@lenovo.com");
        Product mouse = productManager.addProduct("Wireless Ergonomic Mouse", "Accessories", 49.99, 50, 0.2, "logistics@logitech.com");
        Product monitor = productManager.addProduct("4K UltraHD Monitor 27\"", "Electronics", 399.99, 8, 4.5, "b2b@dell.com");
        Product cleanCodeBook = productManager.addProduct("Clean Code Book", "Books", 35.00, 20, 0.8, "sales@pearson.com");

        System.out.println("Added " + productManager.getAllProducts().size() + " products to catalog.");

        // 2. Register Customers
        System.out.println("\n--- Step 2: Registering Customers ---");
        User alice = userManager.registerUser(
                "Alice Smith",
                "alice@enterprise.com",
                "Secret123",
                "742 Evergreen Terrace, Springfield",
                "+1-555-0199"
        );
        System.out.println("Registered user: " + alice.getName() + " [ID: " + alice.getId() + "] Tier: " + alice.getTier());

        User bob = userManager.registerUser(
                "Bob Johnson",
                "bob@enterprise.com",
                "Password99",
                "100 Main Street, Seattle",
                "+1-555-0188"
        );
        // Grant Bob some existing loyalty points to simulate VIP tier
        userManager.addLoyaltyPoints(bob.getId(), 1200); // Upgraded to GOLD!
        System.out.println("Registered user: " + bob.getName() + " [ID: " + bob.getId() + "] Tier: " + bob.getTier() + " Points: " + bob.getLoyaltyPoints());

        // 3. Shopping Cart & Checkout for Alice
        System.out.println("\n--- Step 3: Shopping Cart & Checkout (Alice) ---");
        ShoppingCart cartAlice = new ShoppingCart(alice.getId());
        cartAlice.addItem(laptop, 1);
        cartAlice.addItem(mouse, 2);
        cartAlice.applyCoupon("SUMMER10");

        System.out.println("Alice Cart Subtotal: $" + String.format("%.2f", cartAlice.calculateSubtotal()));
        System.out.println("Alice Applied Coupon: " + cartAlice.getCouponCode() + " (-$" + String.format("%.2f", cartAlice.getAppliedDiscountAmount()) + ")");

        // Checkout with Credit Card
        Order aliceOrder = orderService.checkout(
                alice.getId(),
                cartAlice,
                PaymentMethod.CREDIT_CARD,
                "4111111111111111:12/28:123",
                alice.getAddress(),
                "FEDEX",
                true
        );

        System.out.println("\n--- Checkout Successful! ---");
        System.out.println("Order ID: " + aliceOrder.getId());
        System.out.println("Order Status: " + aliceOrder.getStatus());
        System.out.println("Grand Total: $" + String.format("%.2f", aliceOrder.getTotalAmount()));
        System.out.println("Tracking Code: " + aliceOrder.getTrackingNumber());
        System.out.println("Alice New Loyalty Points: " + alice.getLoyaltyPoints());

        // 4. Shopping Cart & Checkout for Bob (VIP Gold)
        System.out.println("\n--- Step 4: Shopping Cart & Checkout (Bob - GOLD VIP) ---");
        ShoppingCart cartBob = new ShoppingCart(bob.getId());
        cartBob.addItem(monitor, 2);
        cartBob.addItem(cleanCodeBook, 6); // Bulk discount eligible! (>= 5)

        Order bobOrder = orderService.checkout(
                bob.getId(),
                cartBob,
                PaymentMethod.PAYPAL,
                "bob.paypal@enterprise.com",
                bob.getAddress(),
                "DHL",
                false
        );

        System.out.println("\n--- Bob's Order Placed ---");
        System.out.println("Order ID: " + bobOrder.getId());
        System.out.println("Subtotal: $" + String.format("%.2f", bobOrder.getSubtotal()));
        System.out.println("Total Discount (VIP + Bulk): $" + String.format("%.2f", bobOrder.getDiscountAmount()));
        System.out.println("Grand Total: $" + String.format("%.2f", bobOrder.getTotalAmount()));

        // Print Commercial Invoice for Bob
        Invoice bobInvoice = LegacyDatabase.getInstance().getInvoices().get(bobOrder.getInvoiceNumber());
        if (bobInvoice != null) {
            System.out.println("\n" + bobInvoice.generatePlainText());
        }

        // Print Shipping Label
        System.out.println(shippingManager.printShippingLabel(bobOrder.getTrackingNumber()));

        // 5. Update Status: Ship Bob's order
        System.out.println("\n--- Step 5: Shipping Bob's Order ---");
        orderService.updateOrderStatus(bobOrder.getId(), OrderStatus.SHIPPED);
        System.out.println("Bob's Order Status updated to: " + bobOrder.getStatus());

        // 6. Executive Sales Report
        System.out.println("\n--- Step 6: Executive Sales Report ---");
        System.out.println("Total Net Revenue: $" + String.format("%.2f", reportManager.calculateTotalRevenue()));
        System.out.println("Total Orders: " + reportManager.getTotalOrdersCount());
        System.out.println("Top Selling Products: " + reportManager.getTopSellingProducts(3));
        System.out.println("Revenue By Category: " + reportManager.getRevenueByCategory());

        // 7. Order Cancellation & Refund Demo
        System.out.println("\n--- Step 7: Order Cancellation & Refund ---");
        System.out.println("Attempting cancellation of Bob's order (Currently SHIPPED, subject to 20% restocking fee)...");
        double refund = refundService.cancelOrder(bobOrder.getId(), "Customer changed mind after dispatch");
        System.out.println("Refund Processed: $" + String.format("%.2f", refund));
        System.out.println("Bob's Order Status now: " + bobOrder.getStatus());
        System.out.println("Clean Code Book Restocked: " + productManager.getProductById(cleanCodeBook.getId()).getStockQuantity());

        System.out.println("\n=================================================================");
        System.out.println("             APPLICATION EXECUTION COMPLETED SUCCESSFULLY        ");
        System.out.println("=================================================================");
    }
}
