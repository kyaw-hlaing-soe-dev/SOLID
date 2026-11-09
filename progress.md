# Legacy Java Refactoring Challenge — Progress & Tracking

## Project Overview
* **Project Name**: `legacy-ecommerce-system` (Enterprise E-Commerce Monolith)
* **Domain**: Enterprise E-Commerce Management System
* **Architecture**: Monolithic Core Java Application with Intentional Legacy Smells
* **Branch**: `refactor/srp` (Working branch), `main` (Original legacy baseline)
* **Status**: Legacy Baseline Established (45/45 Unit Tests Passing)

---

## 1. Technology Stack
* **Java Version**: Java 17+ (Target: Release 17, Verified on OpenJDK 25)
* **Build System**: Apache Maven 3.9+ (Maven Wrapper `./mvnw` provided)
* **Testing Library**: JUnit 5 (JUnit Jupiter `5.10.2`)
* **Persistence**: In-Memory Mock Database Singleton (`LegacyDatabase`)
* **Framework Policy**: Pure Core Java SE (Zero third-party frameworks)

---

## 2. Deliverables Checklist

| Item | Requirement | Status | Notes |
| :--- | :--- | :---: | :--- |
| 1 | Complete Working Legacy Codebase | ✅ Completed | 22 Java classes, compiling with 0 warnings |
| 2 | End-to-End Business Workflows | ✅ Completed | Fully runnable in `Main.java` |
| 3 | Baseline Unit Test Suite | ✅ Completed | 8 Test classes, 45 tests, 100% passing |
| 4 | Git Repository & Branching | ✅ Completed | `main` baseline committed; `refactor/srp` created |
| 5 | Build & Execution Scripts | ✅ Completed | `./mvnw` wrapper scripts configured |
| 6 | Documentation | ✅ Completed | `README.md` and `progress.md` created |

---

## 3. Initial Baseline Test Suite Protection

All 45 baseline unit tests pass cleanly, providing a regression safety net:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.legacy.ecommerce.ReportManagerTest         (4 tests)  - PASSED
[INFO] Running com.legacy.ecommerce.UserManagerTest           (7 tests)  - PASSED
[INFO] Running com.legacy.ecommerce.ProductAndInventoryTest   (6 tests)  - PASSED
[INFO] Running com.legacy.ecommerce.OrderServiceTest          (6 tests)  - PASSED
[INFO] Running com.legacy.ecommerce.PaymentProcessorTest      (6 tests)  - PASSED
[INFO] Running com.legacy.ecommerce.InvoiceAndShippingTest    (5 tests)  - PASSED
[INFO] Running com.legacy.ecommerce.ShoppingCartTest          (6 tests)  - PASSED
[INFO] Running com.legacy.ecommerce.RefundServiceTest         (4 tests)  - PASSED
[INFO] 
[INFO] Results:
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 4. Codebase Inventory (22 Classes)

### Domain Models (`com.legacy.ecommerce.model`)
1. `User.java`: Customer data, password validation, loyalty logic, CSV export.
2. `CustomerTier.java`: Enum (`REGULAR`, `SILVER`, `GOLD`, `PLATINUM`).
3. `Product.java`: Catalog item, low stock check, bulk discount rule, HTML rendering.
4. `CartItem.java`: Product reference and quantity.
5. `ShoppingCart.java`: Item collection, bulk discounts, coupon parsing, tax estimation.
6. `Order.java`: Order aggregate, status checks, receipt generation, audit string formatting.
7. `OrderItem.java`: Ordered item snapshot with unit price and line total.
8. `OrderStatus.java`: Enum (`PENDING`, `PAID`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
9. `Payment.java`: Payment transaction state.
10. `PaymentMethod.java`: Enum (`CREDIT_CARD`, `PAYPAL`, `BANK_TRANSFER`, `CRYPTO`).
11. `Shipment.java`: Shipping dispatch details and carrier status.
12. `Invoice.java`: Invoicing entity with text and HTML document generators.

### Data Storage (`com.legacy.ecommerce.data`)
13. `LegacyDatabase.java`: In-memory thread-safe storage singleton with mutable tables.

### Managers & Monolith Services (`com.legacy.ecommerce.manager`)
14. `UserManager.java`: User CRUD, authentication, loyalty progression, direct emails, CSV disk writing.
15. `ProductManager.java`: Catalog CRUD, inventory deduction, low-stock supplier alerts, HTML catalog.
16. `OrderService.java`: Central God Class orchestrating checkout, inventory reservation, rollback, duplicate discount rules, payments, invoicing, shipping, loyalty points, and emails.
17. `PaymentProcessor.java`: Luhn algorithm validation, PayPal, bank transfers, crypto exchange conversion, fee deductions, and refunds.
18. `ShippingManager.java`: Carrier fee computation, tracking number generation, shipping label printing.
19. `NotificationService.java`: SMTP simulator, SMS gateway simulator, template generation.
20. `RefundService.java`: Order cancellation rules, restocking penalties, inventory restoration, points deduction, refund payment dispatch.
21. `ReportManager.java`: Net revenue calculation, top-selling products, category sales breakdown, CSV export, HTML summaries.

### Application Entry Point (`com.legacy.ecommerce`)
22. `Main.java`: Runs full end-to-end e-commerce scenarios.

---

## 5. Refactoring Learning Roadmap

```
Phase 1: SRP (Single Responsibility Principle)  <-- [CURRENT FOCUS]
├── Identify God Classes and multiple reasons to change
├── Separate business calculations, data storage, notifications, and formatting
└── Verify with regression tests (45/45 passing)

Phase 2: OCP (Open/Closed Principle)             <-- [UPCOMING]
├── Replace complex switch statements (e.g., PaymentMethod, Carrier)
└── Enable adding new discount types and carriers without modifying existing code

Phase 3: LSP (Liskov Substitution Principle)     <-- [UPCOMING]
├── Ensure subtype interchangeable correctness in domain models

Phase 4: ISP (Interface Segregation Principle)    <-- [UPCOMING]
├── Split fat interfaces into focused, client-specific contracts

Phase 5: DIP (Dependency Inversion Principle)    <-- [UPCOMING]
└── Introduce abstractions, invert control, and eliminate hardcoded 'new' instantiations
```

---

## 6. How to Run & Verify

### Execute Demo Workflows
```bash
./mvnw clean compile exec:java
# or: mvn clean compile exec:java
```

### Run Regression Test Suite
```bash
./mvnw test
# or: mvn test
```

---

## 7. Current Status & Next Action Item

* **Active Phase**: **Phase 1 — Single Responsibility Principle (SRP)**
* **Active Branch**: `refactor/srp`
* **Junior Developer Next Task**:
  1. Inspect the classes in `com.legacy.ecommerce.manager` and `com.legacy.ecommerce.model`.
  2. Pick **one class** that demonstrates the most severe SRP violation / God Class behavior.
  3. Detail the separate responsibilities it holds and your proposed strategy for splitting them.
  4. Submit your proposal to your Senior Mentor for architectural review before making code edits.
