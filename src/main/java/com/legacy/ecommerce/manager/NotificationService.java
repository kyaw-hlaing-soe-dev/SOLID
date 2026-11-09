package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.Order;
import com.legacy.ecommerce.model.Shipment;
import com.legacy.ecommerce.model.User;

import java.util.regex.Pattern;

/**
 * Legacy NotificationService.
 * Mixes email sending, SMS sending, push notifications, regex validation,
 * and hardcoded HTML/Text email template creation.
 */
public class NotificationService {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private final LegacyDatabase database = LegacyDatabase.getInstance();

    public boolean sendEmail(String toEmail, String subject, String body) {
        if (toEmail == null || !EMAIL_REGEX.matcher(toEmail).matches()) {
            System.err.println("[EMAIL ERROR] Invalid email address: " + toEmail);
            return false;
        }
        String logEntry = "EMAIL to " + toEmail + " | Subject: " + subject + " | Body: " + body;
        database.addNotificationLog(logEntry);
        System.out.println("[SMTP SIMULATOR] " + logEntry);
        return true;
    }

    public boolean sendSms(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().length() < 7) {
            System.err.println("[SMS ERROR] Invalid phone number: " + phoneNumber);
            return false;
        }
        // SMS length truncation rule
        String finalMessage = message;
        if (message != null && message.length() > 160) {
            finalMessage = message.substring(0, 157) + "...";
        }
        String logEntry = "SMS to " + phoneNumber + " | Content: " + finalMessage;
        database.addNotificationLog(logEntry);
        System.out.println("[SMS GATEWAY] " + logEntry);
        return true;
    }

    // --- Hardcoded Template Generators directly in Notification Service (SRP Violation) ---

    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to Enterprise E-Commerce, " + user.getName() + "!";
        String body = "Dear " + user.getName() + ",\n\n"
                + "Thank you for creating an account with us.\n"
                + "Your customer tier is: " + user.getTier() + ".\n"
                + "Earn loyalty points with every purchase to unlock exclusive discounts!\n\n"
                + "Best regards,\nCustomer Care Team";
        sendEmail(user.getEmail(), subject, body);
    }

    public void sendOrderConfirmation(Order order, User user) {
        String subject = "Order Confirmation #" + order.getId();
        String body = "Dear " + user.getName() + ",\n\n"
                + "We have received your order #" + order.getId() + ".\n"
                + "Order Total: $" + String.format("%.2f", order.getTotalAmount()) + "\n"
                + "Status: " + order.getStatus() + "\n"
                + "We will notify you once your order is shipped.\n\n"
                + "Invoice Number: " + order.getInvoiceNumber() + "\n\n"
                + "Thank you for shopping with us!";
        sendEmail(user.getEmail(), subject, body);
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            sendSms(user.getPhoneNumber(), "Order #" + order.getId() + " confirmed. Total: $" + String.format("%.2f", order.getTotalAmount()));
        }
    }

    public void sendShippingAlert(Order order, Shipment shipment, User user) {
        String subject = "Your Order #" + order.getId() + " Has Shipped!";
        String body = "Hello " + user.getName() + ",\n\n"
                + "Your order #" + order.getId() + " has been dispatched via " + shipment.getCarrier() + ".\n"
                + "Tracking Number: " + shipment.getTrackingNumber() + "\n"
                + "Estimated Delivery: " + shipment.getEstimatedDelivery() + "\n\n"
                + "Track your package anytime.";
        sendEmail(user.getEmail(), subject, body);
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            sendSms(user.getPhoneNumber(), "Order #" + order.getId() + " shipped! Tracking: " + shipment.getTrackingNumber());
        }
    }

    public void sendOrderCancellationAlert(Order order, User user, double refundAmount) {
        String subject = "Order Cancellation #" + order.getId();
        String body = "Dear " + user.getName() + ",\n\n"
                + "Your order #" + order.getId() + " has been cancelled.\n"
                + "Reason: " + order.getCancellationReason() + "\n"
                + "Refund Amount Issued: $" + String.format("%.2f", refundAmount) + "\n\n"
                + "The funds will return to your account within 3-5 business days.";
        sendEmail(user.getEmail(), subject, body);
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            sendSms(user.getPhoneNumber(), "Order #" + order.getId() + " cancelled. Refund: $" + String.format("%.2f", refundAmount));
        }
    }
}
