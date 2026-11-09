package com.legacy.ecommerce;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.manager.ShippingManager;
import com.legacy.ecommerce.model.Invoice;
import com.legacy.ecommerce.model.OrderItem;
import com.legacy.ecommerce.model.Shipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InvoiceAndShippingTest {

    private ShippingManager shippingManager;

    @BeforeEach
    void setUp() {
        LegacyDatabase.getInstance().reset();
        shippingManager = new ShippingManager();
    }

    @Test
    @DisplayName("Should generate plain text invoice with line items, tax, and totals")
    void testInvoicePlainTextGeneration() {
        Invoice invoice = new Invoice("INV-2026-001", "ORD-999", "Mark Spencer", "mark@test.com", "77 Broadway, NY");
        invoice.setLineItems(List.of(
                new OrderItem("P1", "Mechanical Keyboard", 100.0, 1),
                new OrderItem("P2", "Gaming Mousepad", 25.0, 2)
        ));
        invoice.setSubtotal(150.0);
        invoice.setDiscountAmount(15.0);
        invoice.setTaxAmount(10.80);
        invoice.setShippingCost(12.00);
        invoice.setGrandTotal(157.80);

        String text = invoice.generatePlainText();

        assertTrue(text.contains("COMMERCIAL INVOICE"));
        assertTrue(text.contains("INV-2026-001"));
        assertTrue(text.contains("Mark Spencer"));
        assertTrue(text.contains("Mechanical Keyboard"));
        assertTrue(text.contains("$157.80"));
    }

    @Test
    @DisplayName("Should calculate Standard Post shipping: $5 base + $1.50/kg, +$10 express")
    void testShippingCost_StandardPost() {
        // 2 kg standard ground: 5.0 + (2 * 1.50) = 8.0
        double cost = shippingManager.calculateShippingCost("STANDARD_POST", 2.0, false, "USA");
        assertEquals(8.0, cost, 0.001);

        // 2 kg express: 8.0 + 10.0 = 18.0
        double expressCost = shippingManager.calculateShippingCost("STANDARD_POST", 2.0, true, "USA");
        assertEquals(18.0, expressCost, 0.001);
    }

    @Test
    @DisplayName("Should calculate DHL shipping: $12 base + $2.50/kg, +$15 express, +$35 international")
    void testShippingCost_DHL() {
        // 4 kg domestic express: 12.0 + (4 * 2.50) + 15.0 = 37.0
        double domesticExpress = shippingManager.calculateShippingCost("DHL", 4.0, true, "USA");
        assertEquals(37.0, domesticExpress, 0.001);

        // 4 kg international ground: 12.0 + 10.0 + 35.0 = 57.0
        double intlGround = shippingManager.calculateShippingCost("DHL", 4.0, false, "CANADA");
        assertEquals(57.0, intlGround, 0.001);
    }

    @Test
    @DisplayName("Should calculate FedEx shipping: $10 base + $2.20/kg, +$12 express")
    void testShippingCost_FedEx() {
        // 3 kg ground: 10.0 + (3 * 2.20) = 16.60
        double cost = shippingManager.calculateShippingCost("FEDEX", 3.0, false, "USA");
        assertEquals(16.60, cost, 0.001);
    }

    @Test
    @DisplayName("Should create shipment and print formatted shipping label")
    void testCreateShipmentAndPrintLabel() {
        Shipment shipment = shippingManager.createShipment("ORD-888", "DHL", 3.5, true, "55 Pine St, Boston");

        assertNotNull(shipment);
        assertNotNull(shipment.getTrackingNumber());
        assertTrue(shipment.getTrackingNumber().startsWith("TRK-DHL-"));

        String label = shippingManager.printShippingLabel(shipment.getTrackingNumber());
        assertTrue(label.contains("SHIPPING LABEL"));
        assertTrue(label.contains("PRIORITY EXPRESS"));
        assertTrue(label.contains(shipment.getTrackingNumber()));
        assertTrue(label.contains("55 Pine St, Boston"));
    }
}
