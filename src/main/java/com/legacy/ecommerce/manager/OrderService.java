package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Legacy God Class: OrderService.
 * The central monolith of the legacy system.
 * Handles checkout orchestration, validation, inventory reservation & rollback,
 * tax computation, duplicated discount calculations, payment dispatch,
 * invoice creation, shipping dispatch, loyalty points assignment,
 * and customer notifications.
 */
public class OrderService {
    private final LegacyDatabase database = LegacyDatabase.getInstance();

    // Direct concrete instantiations (Tight coupling)
    private final UserManager userManager = new UserManager();
    private final ProductManager productManager = new ProductManager();
    private final PaymentProcessor paymentProcessor = new PaymentProcessor();
    private final ShippingManager shippingManager = new ShippingManager();
    private final NotificationService notificationService = new NotificationService();

    /**
     * Long monolithic checkout method with multiple responsibilities.
     */
    public Order checkout(String userId, ShoppingCart cart, PaymentMethod paymentMethod,
                          String paymentAccountDetails, String shippingAddress,
                          String carrier, boolean isExpress) {

        // 1. User Validation
        User user = userManager.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        if (!user.isActive()) {
            throw new IllegalStateException("User account is inactive: " + userId);
        }

        // 2. Cart Validation
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart.");
        }

        // 3. Inventory Stock Validation
        for (CartItem cartItem : cart.getItems()) {
            Product p = cartItem.getProduct();
            if (p == null) {
                throw new IllegalArgumentException("Cart contains an invalid product.");
            }
            if (!productManager.hasSufficientStock(p.getId(), cartItem.getQuantity())) {
                Product currentProd = productManager.getProductById(p.getId());
                int avail = (currentProd != null) ? currentProd.getStockQuantity() : 0;
                throw new IllegalStateException("Insufficient stock for product '" + p.getName()
                        + "'. Requested: " + cartItem.getQuantity() + ", Available: " + avail);
            }
        }

        // 4. Calculate Subtotal & Bulk Discounts (Duplicated from ShoppingCart!)
        double subtotal = 0.0;
        double bulkDiscount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            double lineTotal = item.getProduct().getPrice() * item.getQuantity();
            subtotal += lineTotal;

            // Duplicated bulk discount rule: >= 5 quantity gets 5% off
            if (item.getQuantity() >= 5) {
                bulkDiscount += lineTotal * 0.05;
            }

            orderItems.add(new OrderItem(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getPrice(),
                    item.getQuantity()
            ));
        }

        // 5. Calculate Customer Tier Discount (VIP rules)
        double tierDiscountRate = user.getTierDiscountRate();
        double tierDiscountAmount = (subtotal - bulkDiscount) * tierDiscountRate;

        // 6. Calculate Coupon Discount (Duplicated from ShoppingCart coupon logic!)
        double couponDiscount = 0.0;
        String coupon = cart.getCouponCode();
        if (coupon != null && !coupon.trim().isEmpty()) {
            String upperCoupon = coupon.trim().toUpperCase();
            if (upperCoupon.equals("SUMMER10")) {
                couponDiscount = subtotal * 0.10;
            } else if (upperCoupon.equals("SAVE20")) {
                couponDiscount = Math.min(subtotal * 0.20, 50.0);
            } else if (upperCoupon.equals("FLAT50")) {
                if (subtotal >= 200.0) {
                    couponDiscount = 50.0;
                }
            }
        }

        double totalDiscount = bulkDiscount + tierDiscountAmount + couponDiscount;
        if (totalDiscount > subtotal) {
            totalDiscount = subtotal; // Cannot discount more than subtotal
        }

        // 7. Calculate Tax (Standard 8% tax on taxable subtotal)
        double taxableAmount = Math.max(0.0, subtotal - totalDiscount);
        double taxAmount = taxableAmount * 0.08;

        // 8. Calculate Shipping Cost via ShippingManager
        double totalWeightKg = cart.getTotalWeightKg();
        double shippingCost = shippingManager.calculateShippingCost(carrier, totalWeightKg, isExpress, "USA");

        // Special legacy free shipping rule: if subtotal > 150, free shipping for standard post
        if (subtotal >= 150.0 && (carrier == null || carrier.equalsIgnoreCase("STANDARD_POST")) && !isExpress) {
            shippingCost = 0.0;
        }

        // 9. Compute Grand Total
        double grandTotal = taxableAmount + taxAmount + shippingCost;

        // 10. Generate Order ID
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 11. Deduct Inventory (State change with rollback capability)
        List<CartItem> deductedItems = new ArrayList<>();
        try {
            for (CartItem item : cart.getItems()) {
                productManager.deductStock(item.getProduct().getId(), item.getQuantity());
                deductedItems.add(item);
            }
        } catch (Exception e) {
            // Rollback already deducted items
            for (CartItem rolledBack : deductedItems) {
                productManager.restoreStock(rolledBack.getProduct().getId(), rolledBack.getQuantity());
            }
            throw new IllegalStateException("Failed to deduct inventory: " + e.getMessage(), e);
        }

        // 12. Process Payment via PaymentProcessor
        Payment payment;
        try {
            payment = paymentProcessor.processPayment(orderId, grandTotal, paymentMethod, paymentAccountDetails);
        } catch (Exception e) {
            // Rollback inventory deduction if payment fails!
            for (CartItem rolledBack : deductedItems) {
                productManager.restoreStock(rolledBack.getProduct().getId(), rolledBack.getQuantity());
            }
            database.addAuditLog("CHECKOUT_FAILED: Payment declined for Order " + orderId + " - " + e.getMessage());
            throw new IllegalStateException("Payment failed: " + e.getMessage(), e);
        }

        // 13. Create Shipment via ShippingManager
        Shipment shipment = shippingManager.createShipment(orderId, carrier, totalWeightKg, isExpress, shippingAddress);

        // 14. Create Invoice
        String invoiceNumber = "INV-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Invoice invoice = new Invoice(invoiceNumber, orderId, user.getName(), user.getEmail(), shippingAddress);
        invoice.setLineItems(orderItems);
        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(totalDiscount);
        invoice.setTaxAmount(taxAmount);
        invoice.setShippingCost(shippingCost);
        invoice.setGrandTotal(grandTotal);
        database.getInvoices().put(invoiceNumber, invoice);

        // 15. Create Order Record
        Order order = new Order(orderId, userId, orderItems);
        order.setStatus(OrderStatus.PAID);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(totalDiscount);
        order.setTaxAmount(taxAmount);
        order.setShippingCost(shippingCost);
        order.setTotalAmount(grandTotal);
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress);
        order.setTrackingNumber(shipment.getTrackingNumber());
        order.setInvoiceNumber(invoiceNumber);

        database.getOrders().put(orderId, order);
        database.addAuditLog("ORDER_CREATED: " + orderId + ", Total: $" + grandTotal + ", Status: " + order.getStatus());

        // 16. Award Loyalty Points (1 point per $10 spent)
        int earnedPoints = (int) (grandTotal / 10.0);
        if (earnedPoints > 0) {
            userManager.addLoyaltyPoints(userId, earnedPoints);
        }

        // 17. Send Notifications
        try {
            notificationService.sendOrderConfirmation(order, user);
        } catch (Exception e) {
            System.err.println("Warning: Could not send order confirmation notification: " + e.getMessage());
        }

        // 18. Clear the shopping cart
        cart.clear();

        return order;
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        database.addAuditLog("ORDER_STATUS_CHANGED: " + orderId + " (" + oldStatus + " -> " + newStatus + ")");

        User user = userManager.getUserById(order.getUserId());
        Shipment shipment = shippingManager.getShipmentByOrderId(orderId);

        if (newStatus == OrderStatus.SHIPPED && shipment != null && user != null) {
            shippingManager.updateShipmentStatus(shipment.getTrackingNumber(), "DISPATCHED");
            notificationService.sendShippingAlert(order, shipment, user);
        } else if (newStatus == OrderStatus.DELIVERED && shipment != null) {
            shippingManager.updateShipmentStatus(shipment.getTrackingNumber(), "DELIVERED");
        }
    }

    public Order getOrderById(String orderId) {
        return database.getOrders().get(orderId);
    }

    public List<Order> getOrdersByUserId(String userId) {
        List<Order> result = new ArrayList<>();
        for (Order o : database.getOrders().values()) {
            if (o.getUserId().equals(userId)) {
                result.add(o);
            }
        }
        return result;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(database.getOrders().values());
    }
}
