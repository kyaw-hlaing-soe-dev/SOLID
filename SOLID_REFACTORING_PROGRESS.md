# SOLID Principles Refactoring Roadmap & Progress Journal

> **Developer**: Junior Java Developer  
> **Mentor**: Senior Java Software Engineer  
> **Repository**: `legacy-ecommerce-system`  
> **Base Branch**: `main` (Original legacy implementation)  
> **Active Branch**: `refactor/srp`  
> **Regression Test Suite**: 45 / 45 Tests Passing (`BUILD SUCCESS`)  

---

## 🧭 Mentorship & Refactoring Protocol

1. **One Principle at a Time**: Never mix future SOLID principles into the current phase (e.g., do not introduce dynamic polymorphism or strategy patterns for OCP while focusing on SRP).
2. **Behavior Preservation (Zero Regressions)**: Run `./mvnw test` before and after every extraction. All 45 tests must stay green.
3. **Review Before Implementation**: Identify smells $\rightarrow$ Propose design & class responsibilities $\rightarrow$ Receive Senior Mentor feedback $\rightarrow$ Implement & Verify.
4. **Git Discipline**: Every refactoring task gets dedicated commits on its feature branch. Never touch `main`.

---

## 📊 SOLID Refactoring High-Level Status

| Phase | Principle | Focus Area | Status | Target Classes | Regression Tests |
| :---: | :--- | :--- | :---: | :--- | :---: |
| **Phase 1** | **SRP** (Single Responsibility) | God classes, mixed concerns, extraction | 🟡 **IN PROGRESS** | `OrderService`, `UserManager`, `ProductManager`, `Invoice`, etc. | 45 / 45 Passing |
| **Phase 2** | **OCP** (Open/Closed) | Switch statements, carrier & payment extensions | ⚪ UPCOMING | `PaymentProcessor`, `ShippingManager`, Discount logic | - |
| **Phase 3** | **LSP** (Liskov Substitution) | Subtype substitutability, contract integrity | ⚪ UPCOMING | Payment hierarchy, user tiers, order types | - |
| **Phase 4** | **ISP** (Interface Segregation) | Lean, client-specific role interfaces | ⚪ UPCOMING | Notification contracts, storage repositories | - |
| **Phase 5** | **DIP** (Dependency Inversion) | Inversion of control, abstraction over concretion | ⚪ UPCOMING | Direct `new` instantiations across all managers | - |

---

## 🔍 Codebase Smell Heatmap (Baseline Legacy Audit)

This matrix maps our legacy classes to the primary design violations to be addressed progressively across the 5 phases:

| Class Name | SRP (Phase 1) | OCP (Phase 2) | LSP (Phase 3) | ISP (Phase 4) | DIP (Phase 5) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `OrderService` | 🔴 Severe (God Class) | 🟡 Moderate | ⚪ Low | 🟡 Moderate | 🔴 Direct `new` deps |
| `UserManager` | 🔴 Severe (Mixed concerns) | ⚪ Low | ⚪ Low | 🟡 Moderate | 🟡 Direct `new` deps |
| `ProductManager` | 🔴 Severe (Catalog + Inventory + Alert) | ⚪ Low | ⚪ Low | ⚪ Low | 🟡 Direct `new` deps |
| `PaymentProcessor` | 🟡 Moderate (Crypto/Luhn/Gateway) | 🔴 Severe (`switch` cases) | 🟡 Moderate | 🟡 Fat contracts | ⚪ In-memory singleton |
| `ShippingManager` | 🟡 Moderate (Rates + Label + State) | 🔴 Severe (`if-else` carriers) | ⚪ Low | ⚪ Low | ⚪ In-memory singleton |
| `RefundService` | 🟡 Moderate (Fees + Inventory + Points) | 🟡 Status branches | ⚪ Low | ⚪ Low | 🔴 Direct `new` deps |
| `ReportManager` | 🔴 High (Metrics + CSV + Email) | ⚪ Low | ⚪ Low | ⚪ Low | 🟡 Direct `new` deps |
| `Invoice` | 🟡 Formatting inside model | ⚪ Low | ⚪ Low | ⚪ Low | ⚪ None |
| `User` | 🟡 Password/Tier logic inside model | ⚪ Low | ⚪ Low | ⚪ Low | ⚪ None |
| `Product` | 🟡 HTML rendering inside model | ⚪ Low | ⚪ Low | ⚪ Low | ⚪ None |
| `ShoppingCart` | 🟡 Duplicated discount/coupon rules | 🟡 Hardcoded codes | ⚪ Low | ⚪ Low | ⚪ None |

---

## 🎯 Phase 1: Single Responsibility Principle (SRP) — Detailed Tracking

> **SRP Rule**: *"A class should have one, and only one, reason to change."*

### Phase 1 Objectives
- [ ] Identify all God Classes and mixed-responsibility classes.
- [ ] Separate business calculations from persistence, validation, external messaging, and view/rendering.
- [ ] Remove duplicated business logic between classes (e.g., discount calculations).
- [ ] Keep all public APIs compatible or update callers so all 45 existing unit tests pass without modifying test assertions.

### Task Breakdown & Candidate Classes

#### Task 1.1: `UserManager` Refactoring
* **Current Violations**: User CRUD, password strength validation, base64 hashing, loyalty point logic, welcome email dispatch, CSV formatting, file disk writing.
* **Proposed Extractions**:
  - `UserValidator`: Pure validation rules (email regex, password complexity).
  - `PasswordHasher`: Credential security/encoding logic.
  - `UserCsvExporter`: Data presentation and CSV serialization.
* **Status**: ⚪ Pending Junior Proposal

#### Task 1.2: `ProductManager` Refactoring
* **Current Violations**: Catalog metadata CRUD, inventory stock operations, low-stock threshold supplier alert emails, HTML catalog rendering.
* **Proposed Extractions**:
  - `InventoryService`: Dedicated stock tracking and deductions.
  - `ProductCatalogService`: Product search, metadata, and categories.
  - `CatalogHtmlView`: HTML presentation rendering.
* **Status**: ⚪ Pending Junior Proposal

#### Task 1.3: `OrderService` (The Ultimate God Class) Refactoring
* **Current Violations**: Checkout workflow coordination, inventory reservation, rollback logic, duplicated discount rules, tax computation, payment dispatch, invoice creation, shipment creation, loyalty updates, email/SMS dispatch.
* **Proposed Extractions**:
  - `PricingCalculator` / `DiscountService`: Centralized tax and discount computation (eliminates duplication with `ShoppingCart`).
  - `OrderFulfillmentCoordinator`: High-level workflow orchestration delegating to dedicated domain services.
* **Status**: ⚪ Pending Junior Proposal

#### Task 1.4: `PaymentProcessor` Cleanup (SRP Scope)
* **Current Violations**: Luhn algorithm validation, crypto wallet address checking, currency exchange rate math, payment gateway simulation, refund tracking.
* **Proposed Extractions**:
  - `CreditCardValidator`: Luhn algorithm and CVV validation.
  - `CryptoCurrencyConverter`: Currency rate math.
* **Status**: ⚪ Pending Junior Proposal

#### Task 1.5: `ReportManager` Refactoring
* **Current Violations**: Metric aggregations, CSV generation, HTML executive report formatting, email dispatch to admin.
* **Proposed Extractions**:
  - `SalesAnalyticsService`: Pure business metrics and aggregation math.
  - `ReportCsvFormatter`: CSV formatting.
  - `ReportHtmlFormatter`: HTML template formatting.
* **Status**: ⚪ Pending Junior Proposal

#### Task 1.6: Domain Models Cleanup (`User`, `Product`, `Invoice`, `Order`)
* **Current Violations**:
  - `User`: contains CSV generation and password validation.
  - `Product`: contains HTML card rendering.
  - `Invoice`: contains text and HTML document generation engines.
  - `Order`: contains receipt text generation.
* **Proposed Extractions**: Move presentation and validation logic out of domain entities into dedicated formatters/validators.
* **Status**: ⚪ Pending Junior Proposal

---

## 🔮 Roadmap Preview (Future Phases — Do Not Apply Yet)

### Phase 2: Open/Closed Principle (OCP)
* Replace carrier `if-else` ladders with `ShippingStrategy` (DHL, FedEx, StandardPost).
* Replace payment `switch` statements with `PaymentHandler` implementations.
* Replace hardcoded coupon codes with composable `DiscountPolicy` rules.

### Phase 3: Liskov Substitution Principle (LSP)
* Create verified inheritance contracts where any payment method or shipment type can be substituted transparently without unexpected side effects or runtime type-check assertions.

### Phase 4: Interface Segregation Principle (ISP)
* Decompose fat service contracts into fine-grained client interfaces (e.g., `StockReader`, `StockDeductor`, `OrderNotifier`, `Auditable`).

### Phase 5: Dependency Inversion Principle (DIP)
* Decouple all high-level business services from concrete classes (`new` calls) and singletons (`LegacyDatabase.getInstance()`).
* Introduce repository interfaces (`UserRepository`, `OrderRepository`, `ProductRepository`) and dependency injection patterns.

---

## 📝 Refactoring Activity & Verification Log

| Date | Branch | Class Refactored | Responsibilities Extracted | Tests Result | Commit Hash |
| :---: | :---: | :--- | :--- | :---: | :---: |
| 2026-11-09 | `main` | Baseline Codebase | Initial 22 classes legacy monolith | 45 / 45 Passed | `a655cf1` |
| 2026-11-09 | `refactor/srp` | Docs & Setup | Added `progress.md` and wrapper | 45 / 45 Passed | `be449fd` |
| 2026-11-17 | `refactor/srp` | Roadmap Documentation | Added `SOLID_REFACTORING_PROGRESS.md` | 45 / 45 Passed | Pending |

---

## 🚀 Execution Quick Reference

```bash
# Set Java 25 / 17 environment
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home"

# Run full regression suite (must remain 100% green at all times)
./mvnw test

# Run application demo workflow
./mvnw clean compile exec:java
```
