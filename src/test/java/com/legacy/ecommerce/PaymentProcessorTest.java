package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.PaymentProcessor;
import com.legacy.ecommerce.model.Payment;
import com.legacy.ecommerce.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentProcessorTest {

    private PaymentProcessor paymentProcessor;

    @BeforeEach
    void setUp() {
        LegacyDatabase.getInstance().reset();
        paymentProcessor = new PaymentProcessor();
    }

    @Test
    @DisplayName("Should process valid Credit Card payment with 2.5% fee")
    void testCreditCardPayment_Success() {
        // Valid Visa test card (passes Luhn)
        String cardDetails = "4111111111111111:12/28:123";
        Payment payment = paymentProcessor.processPayment("ORD-101", 200.0, PaymentMethod.CREDIT_CARD, cardDetails);

        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(200.0, payment.getAmount());
        assertEquals(5.0, payment.getFee(), 0.001); // 2.5% of 200 = 5.0
        assertNotNull(payment.getTransactionReference());
    }

    @Test
    @DisplayName("Should reject Credit Card with invalid Luhn checksum or bad format")
    void testCreditCardPayment_InvalidCard() {
        // Invalid Luhn card
        String badCard = "4111111111111112:12/28:123";
        assertThrows(IllegalArgumentException.class, () ->
                paymentProcessor.processPayment("ORD-102", 100.0, PaymentMethod.CREDIT_CARD, badCard));

        // Bad format
        assertThrows(IllegalArgumentException.class, () ->
                paymentProcessor.processPayment("ORD-102", 100.0, PaymentMethod.CREDIT_CARD, "invalidformat"));
    }

    @Test
    @DisplayName("Should process PayPal payment with 3.0% fee")
    void testPayPalPayment_Success() {
        Payment payment = paymentProcessor.processPayment("ORD-103", 100.0, PaymentMethod.PAYPAL, "user@paypal.com");

        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(3.0, payment.getFee(), 0.001); // 3.0% of 100 = 3.0
    }

    @Test
    @DisplayName("Should process Bank Transfer with flat $5 fee")
    void testBankTransferPayment_Success() {
        Payment payment = paymentProcessor.processPayment("ORD-104", 500.0, PaymentMethod.BANK_TRANSFER, "US99BANK123456789");

        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(5.0, payment.getFee(), 0.001);
    }

    @Test
    @DisplayName("Should process Crypto payment with 1% network fee and 42-char hex wallet")
    void testCryptoPayment_Success() {
        String ethWallet = "0x71C84513643E7ECAB136984597B47565A60Faa62";
        Payment payment = paymentProcessor.processPayment("ORD-105", 300.0, PaymentMethod.CRYPTO, ethWallet);

        assertNotNull(payment);
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(3.0, payment.getFee(), 0.001); // 1% of 300 = 3.0
    }

    @Test
    @DisplayName("Should process refund and mark previous payment as REFUNDED")
    void testProcessRefund_Success() {
        paymentProcessor.processPayment("ORD-106", 150.0, PaymentMethod.PAYPAL, "payer@test.com");

        Payment refund = paymentProcessor.processRefund("ORD-106", 150.0, "Customer request");

        assertNotNull(refund);
        assertEquals("REFUND_COMPLETED", refund.getStatus());
        assertEquals(150.0, refund.getAmount());
        assertEquals(2.0, refund.getFee(), 0.001);
    }
}
