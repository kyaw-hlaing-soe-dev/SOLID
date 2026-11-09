# Legacy Enterprise E-Commerce Management System

## 1. Project Overview
This repository contains a monolithic enterprise e-commerce management system. It provides end-to-end functionality including user management, product cataloging, inventory stock tracking, shopping carts, checkout, payment processing, shipping dispatch, invoicing, cancellation/refunds, and executive sales analytics.

The application is fully operational and covered by a comprehensive regression test suite.

## 2. Technology Stack
- **Language**: Java 17+ (tested on OpenJDK 25)
- **Build Tool**: Apache Maven (Maven Wrapper `./mvnw` included)
- **Testing Framework**: JUnit 5 (Jupiter 5.10.2)
- **Storage**: In-Memory Mock Database Singleton (`LegacyDatabase`)

## 3. Project Structure
```
SOLID/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/legacy/ecommerce/
│   │           ├── Main.java
│   │           ├── model/
│   │           │   ├── CustomerTier.java
│   │           │   ├── OrderStatus.java
│   │           │   ├── PaymentMethod.java
│   │           │   ├── User.java
│   │           │   ├── Product.java
│   │           │   ├── CartItem.java
│   │           │   ├── ShoppingCart.java
│   │           │   ├── OrderItem.java
│   │           │   ├── Order.java
│   │           │   ├── Payment.java
│   │           │   ├── Shipment.java
│   │           │   └── Invoice.java
│   │           ├── data/
│   │           │   └── LegacyDatabase.java
│   │           └── manager/
│   │               ├── UserManager.java
│   │               ├── ProductManager.java
│   │               ├── PaymentProcessor.java
│   │               ├── ShippingManager.java
│   │               ├── NotificationService.java
│   │               ├── OrderService.java
│   │               ├── RefundService.java
│   │               └── ReportManager.java
│   └── test/
│       └── java/
│           └── com/legacy/ecommerce/
│               ├── UserManagerTest.java
│               ├── ProductAndInventoryTest.java
│               ├── ShoppingCartTest.java
│               ├── PaymentProcessorTest.java
│               ├── OrderServiceTest.java
│               ├── RefundServiceTest.java
│               ├── InvoiceAndShippingTest.java
│               └── ReportManagerTest.java
└── README.md
```

## 4. How to Build and Run

### Run the Application Demo
```bash
./mvnw clean compile exec:java
# or using system maven:
mvn clean compile exec:java
```

### Run All Unit Tests
```bash
./mvnw test
# or using system maven:
mvn test
```

## 5. Core Business Workflows
1. **User Registration & Loyalty Progression**: Validates input, hashes passwords, assigns default tier (`REGULAR`), and automatically upgrades tiers (`SILVER`, `GOLD`, `PLATINUM`) as loyalty points accumulate.
2. **Product Catalog & Inventory**: Manages stock levels, deducts stock upon order placement, restores stock upon cancellation, and flags low-stock warnings with supplier restock notifications.
3. **Cart & Pricing Discounts**: Calculates item subtotals, bulk item quantity discounts, tiered customer discounts, and promotional coupons (`SUMMER10`, `SAVE20`, `FLAT50`).
4. **Checkout & Multi-Channel Payment**: Orchestrates stock reservation, payment validation (Credit Card Luhn check, PayPal, Bank Transfer, Crypto ETH), rollback handling upon payment failures, invoice creation, and shipping label generation.
5. **Shipping Management**: Computes carrier rates (`STANDARD_POST`, `DHL`, `FEDEX`) based on parcel weight, destination, and express priority.
6. **Cancellation & Refunds**: Enforces order lifecycle policies (e.g. 5% processing fee on paid orders, 20% restocking fee on dispatched orders, delivered orders non-cancellable).
7. **Executive Reporting**: Aggregates total net sales revenue, order volumes, top-selling items, category breakdowns, and exports formatted CSV data.
