package com.legacy.ecommerce.data;

import com.legacy.ecommerce.model.*;

import java.util.*;

/**
 * Legacy In-Memory Database Singleton.
 * Holds all application data tables in simple Maps, exposing direct mutable collections.
 */
public class LegacyDatabase {
    private static LegacyDatabase instance;

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, Order> orders = new HashMap<>();
    private final Map<String, Payment> payments = new HashMap<>();
    private final Map<String, Shipment> shipments = new HashMap<>();
    private final Map<String, Invoice> invoices = new HashMap<>();
    private final List<String> notificationLogs = new ArrayList<>();
    private final List<String> auditLogs = new ArrayList<>();

    private LegacyDatabase() {
    }

    public static synchronized LegacyDatabase getInstance() {
        if (instance == null) {
            instance = new LegacyDatabase();
        }
        return instance;
    }

    public synchronized void reset() {
        users.clear();
        products.clear();
        orders.clear();
        payments.clear();
        shipments.clear();
        invoices.clear();
        notificationLogs.clear();
        auditLogs.clear();
    }

    // Direct access to tables (Tight coupling & encapsulation violation)
    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public Map<String, Order> getOrders() {
        return orders;
    }

    public Map<String, Payment> getPayments() {
        return payments;
    }

    public Map<String, Shipment> getShipments() {
        return shipments;
    }

    public Map<String, Invoice> getInvoices() {
        return invoices;
    }

    public List<String> getNotificationLogs() {
        return notificationLogs;
    }

    public List<String> getAuditLogs() {
        return auditLogs;
    }

    public void addAuditLog(String log) {
        auditLogs.add(new Date() + " - " + log);
    }

    public void addNotificationLog(String log) {
        notificationLogs.add(new Date() + " - " + log);
    }
}
