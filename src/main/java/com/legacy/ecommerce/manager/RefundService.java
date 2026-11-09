package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.Order;
import com.legacy.ecommerce.model.OrderItem;
import com.legacy.ecommerce.model.OrderStatus;
import com.legacy.ecommerce.model.User;

/**
 * Legacy RefundService.
 * Handles order cancellations, fee penalty logic, stock restoration,
 * loyalty points reversal, refund execution via PaymentProcessor,
 * and cancellation notification emails.
 */
public class RefundService {
    private final LegacyDatabase database = LegacyDatabase.getInstance();

    // Direct dependencies (Tight coupling)
    private final ProductManager productManager = new ProductManager();
    private final PaymentProcessor paymentProcessor = new PaymentProcessor();
    private final UserManager userManager = new UserManager();
    private final NotificationService notificationService = new NotificationService();

    public double cancelOrder(String orderId, String reason) {
        Order order = database.getOrders().get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that has already been delivered. Please initiate return request.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order #" + orderId + " is already cancelled.");
        }

        // Calculate refund amount based on order status policy
        double refundAmount;
        double totalPaid = order.getTotalAmount();

        if (order.getStatus() == OrderStatus.SHIPPED) {
            // Policy: 20% restocking fee + shipping cost is non-refundable
            double productValue = order.getSubtotal() - order.getDiscountAmount();
            double restockingFee = productValue * 0.20;
            refundAmount = totalPaid - order.getShippingCost() - restockingFee;
        } else if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PROCESSING) {
            // Policy: 5% processing cancellation fee
            double fee = totalPaid * 0.05;
            refundAmount = totalPaid - fee;
        } else {
            // PENDING or unpaid: full refund
            refundAmount = totalPaid;
        }

        refundAmount = Math.max(0.0, refundAmount);

        // 1. Restore Inventory
        for (OrderItem item : order.getItems()) {
            productManager.restoreStock(item.getProductId(), item.getQuantity());
        }

        // 2. Reversal of Loyalty Points
        int pointsToRemove = (int) (order.getTotalAmount() / 10.0);
        if (pointsToRemove > 0) {
            userManager.deductLoyaltyPoints(order.getUserId(), pointsToRemove);
        }

        // 3. Process Refund via PaymentProcessor (if payment was made)
        if (order.getStatus() != OrderStatus.PENDING && refundAmount > 0) {
            try {
                paymentProcessor.processRefund(orderId, refundAmount, reason);
            } catch (Exception e) {
                System.err.println("Warning: Payment refund processing encountered error: " + e.getMessage());
            }
        }

        // 4. Update Order Status
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        database.addAuditLog("ORDER_CANCELLED: " + orderId + " Reason: " + reason + " Refund: $" + refundAmount);

        // 5. Send Notification
        User user = userManager.getUserById(order.getUserId());
        if (user != null) {
            try {
                notificationService.sendOrderCancellationAlert(order, user, refundAmount);
            } catch (Exception e) {
                System.err.println("Warning: Failed to send cancellation email: " + e.getMessage());
            }
        }

        return refundAmount;
    }
}
