package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.Shipment;

import java.util.UUID;

/**
 * Legacy ShippingManager.
 * Mixes carrier fee calculations, express/international surcharges,
 * tracking number generation, database updates, and shipping label printing.
 */
public class ShippingManager {
    private final LegacyDatabase database = LegacyDatabase.getInstance();

    public double calculateShippingCost(String carrier, double totalWeightKg, boolean isExpress, String destinationCountry) {
        if (totalWeightKg < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }

        double cost = 0.0;
        String carrierUpper = (carrier != null) ? carrier.toUpperCase() : "STANDARD_POST";

        if (carrierUpper.equals("DHL")) {
            // DHL: Base $12.00 + $2.50/kg
            cost = 12.00 + (totalWeightKg * 2.50);
            if (isExpress) {
                cost += 15.00;
            }
            if (destinationCountry != null && !destinationCountry.equalsIgnoreCase("US") && !destinationCountry.equalsIgnoreCase("USA")) {
                cost += 35.00; // International customs fee
            }
        } else if (carrierUpper.equals("FEDEX")) {
            // FedEx: Base $10.00 + $2.20/kg
            cost = 10.00 + (totalWeightKg * 2.20);
            if (isExpress) {
                cost += 12.00;
            }
            if (destinationCountry != null && !destinationCountry.equalsIgnoreCase("US") && !destinationCountry.equalsIgnoreCase("USA")) {
                cost += 30.00;
            }
        } else {
            // STANDARD_POST: Base $5.00 + $1.50/kg
            cost = 5.00 + (totalWeightKg * 1.50);
            if (isExpress) {
                cost += 10.00;
            }
            if (destinationCountry != null && !destinationCountry.equalsIgnoreCase("US") && !destinationCountry.equalsIgnoreCase("USA")) {
                cost += 25.00;
            }
        }

        return cost;
    }

    public Shipment createShipment(String orderId, String carrier, double weightKg, boolean isExpress, String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Shipping address cannot be empty.");
        }

        double cost = calculateShippingCost(carrier, weightKg, isExpress, "USA");
        String prefix = (carrier != null) ? carrier.toUpperCase() : "STD";
        String trackingNumber = "TRK-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Shipment shipment = new Shipment(trackingNumber, orderId, carrier, address, weightKg, cost, isExpress);
        database.getShipments().put(trackingNumber, shipment);
        database.addAuditLog("SHIPMENT_CREATED: Tracking " + trackingNumber + " for Order " + orderId);

        return shipment;
    }

    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return database.getShipments().get(trackingNumber);
    }

    public Shipment getShipmentByOrderId(String orderId) {
        for (Shipment s : database.getShipments().values()) {
            if (s.getOrderId().equals(orderId)) {
                return s;
            }
        }
        return null;
    }

    public void updateShipmentStatus(String trackingNumber, String status) {
        Shipment shipment = getShipmentByTrackingNumber(trackingNumber);
        if (shipment != null) {
            shipment.setStatus(status);
            database.addAuditLog("SHIPMENT_STATUS_UPDATED: " + trackingNumber + " -> " + status);
        }
    }

    // --- Presentation / Shipping Label generation (SRP Violation) ---
    public String printShippingLabel(String trackingNumber) {
        Shipment shipment = getShipmentByTrackingNumber(trackingNumber);
        if (shipment == null) {
            return "Shipment not found: " + trackingNumber;
        }
        return "================= SHIPPING LABEL =================\n" +
                "CARRIER:  " + shipment.getCarrier() + "\n" +
                "TRACKING: " + shipment.getTrackingNumber() + "\n" +
                "SERVICE:  " + (shipment.isExpress() ? "PRIORITY EXPRESS" : "STANDARD GROUND") + "\n" +
                "WEIGHT:   " + shipment.getWeightKg() + " KG\n" +
                "SHIP TO:  " + shipment.getShippingAddress() + "\n" +
                "EST. DEL: " + shipment.getEstimatedDelivery() + "\n" +
                "==================================================";
    }
}
