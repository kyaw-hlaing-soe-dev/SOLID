package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.Payment;
import com.legacy.ecommerce.model.PaymentMethod;

import java.util.UUID;

/**
 * Legacy PaymentProcessor.
 * Handles credit card validations (Luhn check), PayPal email checks, bank routing,
 * crypto address checks and exchange rate conversions, transaction fees, and refunds.
 */
public class PaymentProcessor {
    private final LegacyDatabase database = LegacyDatabase.getInstance();

    public Payment processPayment(String orderId, double amount, PaymentMethod method, String accountDetails) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        double fee = 0.0;
        boolean isValid = false;

        // Big switch statement with complex business rules and validations
        switch (method) {
            case CREDIT_CARD:
                // Expected format: "cardNumber:expiry:cvv"
                if (accountDetails == null || !accountDetails.contains(":")) {
                    throw new IllegalArgumentException("Invalid credit card details format. Expected 'number:mm/yy:cvv'");
                }
                String[] cardParts = accountDetails.split(":");
                if (cardParts.length != 3) {
                    throw new IllegalArgumentException("Invalid credit card details format.");
                }
                String cardNumber = cardParts[0].replaceAll("\\s+", "");
                String cvv = cardParts[2].trim();

                if (!validateLuhn(cardNumber) || cvv.length() < 3 || cvv.length() > 4) {
                    throw new IllegalArgumentException("Credit card failed validation or Luhn check.");
                }
                // Credit card fee: 2.5%
                fee = amount * 0.025;
                isValid = true;
                break;

            case PAYPAL:
                // Expected format: email address
                if (accountDetails == null || !accountDetails.contains("@")) {
                    throw new IllegalArgumentException("Invalid PayPal email address.");
                }
                // PayPal fee: 3.0%
                fee = amount * 0.03;
                isValid = true;
                break;

            case BANK_TRANSFER:
                // Expected format: IBAN / Account number (at least 8 chars)
                if (accountDetails == null || accountDetails.trim().length() < 8) {
                    throw new IllegalArgumentException("Invalid bank transfer account number.");
                }
                // Flat $5.00 fee
                fee = 5.00;
                isValid = true;
                break;

            case CRYPTO:
                // Expected format: Ethereum wallet address (0x...)
                if (accountDetails == null || !accountDetails.startsWith("0x") || accountDetails.length() != 42) {
                    throw new IllegalArgumentException("Invalid Ethereum wallet address format.");
                }
                // 1% crypto network fee
                fee = amount * 0.01;
                // Crypto exchange rate simulation (e.g. 1 ETH = $3000 USD)
                double ethAmount = amount / 3000.0;
                System.out.println("[CRYPTO GATEWAY] Processed " + String.format("%.6f", ethAmount) + " ETH from " + accountDetails);
                isValid = true;
                break;

            default:
                throw new UnsupportedOperationException("Unsupported payment method: " + method);
        }

        if (!isValid) {
            throw new IllegalStateException("Payment verification failed.");
        }

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Payment payment = new Payment(paymentId, orderId, method, amount, fee, txnRef, "SUCCESS");

        database.getPayments().put(paymentId, payment);
        database.addAuditLog("PAYMENT_SUCCESS: Order " + orderId + ", Amount: $" + amount + ", Fee: $" + fee + " via " + method);

        return payment;
    }

    public Payment processRefund(String orderId, double refundAmount, String reason) {
        if (refundAmount <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive.");
        }

        // Find existing payment for the order
        Payment existingPayment = null;
        for (Payment p : database.getPayments().values()) {
            if (p.getOrderId().equals(orderId) && "SUCCESS".equals(p.getStatus())) {
                existingPayment = p;
                break;
            }
        }

        if (existingPayment == null) {
            throw new IllegalStateException("No successful payment found to refund for order: " + orderId);
        }

        if (refundAmount > existingPayment.getAmount()) {
            throw new IllegalArgumentException("Refund amount exceeds original payment amount.");
        }

        existingPayment.setStatus("REFUNDED");

        String refundId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String refundTxn = "RTXN-" + System.currentTimeMillis();
        // Flat $2.00 refund gateway processing fee
        double refundFee = 2.00;
        Payment refundRecord = new Payment(refundId, orderId, existingPayment.getMethod(), refundAmount, refundFee, refundTxn, "REFUND_COMPLETED");

        database.getPayments().put(refundId, refundRecord);
        database.addAuditLog("REFUND_ISSUED: Order " + orderId + ", Refund Amount: $" + refundAmount + " Reason: " + reason);

        return refundRecord;
    }

    // Luhn Algorithm check for credit cards (SRP Violation: cryptographic/checksum algorithm mixed inside processor)
    private boolean validateLuhn(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            char c = cardNumber.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int n = c - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
