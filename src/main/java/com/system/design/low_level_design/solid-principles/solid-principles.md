# SOLID Principles in Low Level Design

This document explains the SOLID principles in a topic-wise way with detailed examples, common mistakes, and interview-friendly explanations.

SOLID is a set of five object-oriented design principles:
- **S**ingle Responsibility Principle
- **O**pen/Closed Principle
- **L**iskov Substitution Principle
- **I**nterface Segregation Principle
- **D**ependency Inversion Principle

These principles help design code that is:
- easier to maintain
- easier to extend
- easier to test
- less coupled
- more resilient to change

---

## 1) Why SOLID Matters

### What problem does SOLID solve?
Without SOLID, classes often become:
- too large
- tightly coupled
- hard to test
- hard to extend
- full of repeated logic

SOLID helps avoid the common "god class" problem and makes code easier to grow safely.

### Example of a non-SOLID design problem
Imagine an `OrderService` that:
- calculates price
- applies discounts
- saves the order
- sends email
- sends SMS
- logs analytics
- handles payment

That class has too many responsibilities and will become fragile when requirements change.

---

## 2) Single Responsibility Principle (SRP)

### Definition
A class should have only one reason to change.

In practice, this means a class should own one clear responsibility.

### Why SRP matters
If a class handles too many concerns, a change in one concern may break another. Small focused classes are easier to:
- understand
- test
- reuse
- modify

### Bad example
`InvoiceService` does everything:
- calculates invoice total
- formats invoice PDF
- saves invoice in DB
- emails the invoice to the customer

This violates SRP because multiple reasons to change exist:
- pricing logic changes
- PDF format changes
- persistence changes
- email template changes

### Better design
Split into separate classes:
- `InvoiceCalculator`
- `InvoiceFormatter`
- `InvoiceRepository`
- `InvoiceEmailService`

### Example in code structure
- `InvoiceCalculator.calculateTotal(items)`
- `InvoiceRepository.save(invoice)`
- `InvoiceNotifier.send(invoice)`

### Interview explanation
Say: "I would separate calculation, persistence, and communication into different classes so each class has one responsibility and changes stay localized."

### Common SRP mistakes
- creating a utility class with unrelated methods
- mixing business logic and I/O
- making one service manage all workflow steps internally

### SRP check question
Ask: "If I change one business rule, would this class need to change for unrelated reasons too?"

---

## 3) Open/Closed Principle (OCP)

### Definition
Software entities should be open for extension but closed for modification.

This means you should be able to add new behavior without rewriting stable existing logic.

### Why OCP matters
OCP reduces regression risk because existing tested code remains unchanged while new behavior is added through extension.

### Bad example
Suppose you calculate discounts like this:

- if customer type is regular, apply 10%
- if customer type is premium, apply 20%
- if holiday sale, apply 30%

If new discount types keep getting added, you keep modifying the same `calculateDiscount()` method.

### Better design using Strategy Pattern
Create a `DiscountStrategy` interface:
- `PercentageDiscountStrategy`
- `FlatDiscountStrategy`
- `FestivalDiscountStrategy`

The caller depends on `DiscountStrategy`, not concrete classes.

### Example scenario
If tomorrow you add:
- employee discount
- loyalty discount
- coupon discount

You create new strategy classes instead of editing a giant `if-else` block.

### Interview explanation
Say: "I would avoid condition-heavy logic and use polymorphism or strategies so new rules can be added without changing existing code."

### Common OCP violations
- large switch statements on type
- huge if-else chains for behavior selection
- modifying stable code for every new variant

### OCP check question
Ask: "Can I add a new type of behavior by creating a new class instead of modifying an existing one?"

---

## 4) Liskov Substitution Principle (LSP)

### Definition
Subtypes should be substitutable for their base types without breaking correctness.

If a function expects a base class, any derived class should work in its place without surprising behavior.

### Why LSP matters
LSP ensures inheritance is safe. If a subclass cannot honor the base contract, then inheritance is a poor design choice.

### Bad example: Bird problem
Suppose base class `Bird` has method `fly()`.

Then `Sparrow` can fly, but `Penguin` cannot.

If you force `Penguin` to inherit `Bird`, calling `fly()` may throw an error or behave incorrectly.

That breaks substitutability.

### Better design
Separate behaviors:
- `Bird` for shared bird properties
- `Flyable` interface for flying capability

Then:
- `Sparrow implements Flyable`
- `Penguin` does not implement `Flyable`

### Another example: Rectangle and Square
If `Square` extends `Rectangle`, code that sets width and height independently may break square invariants.

This is a classic LSP issue because a `Square` does not behave like a normal `Rectangle` in every context.

### Better design choices
- use separate value objects
- avoid forcing inheritance where behavior differs
- prefer composition or shared interfaces

### Interview explanation
Say: "I would not use inheritance unless the child can truly honor the parent contract in every context. If not, I would refactor using interfaces or composition."

### Common LSP violations
- subclasses that throw `UnsupportedOperationException`
- subclasses that weaken preconditions or strengthen postconditions unexpectedly
- child objects that break parent assumptions

### LSP check question
Ask: "If I replace the base type with this subtype, will all callers still behave correctly?"

---

## 5) Interface Segregation Principle (ISP)

### Definition
Clients should not be forced to depend on methods they do not use.

This means prefer small, focused interfaces over one large general-purpose interface.

### Why ISP matters
Large interfaces create unnecessary coupling and lead to dummy implementations or unsupported methods.

### Bad example
Suppose we define one interface for a worker:

- `work()`
- `eat()`
- `sleep()`
- `attendMeeting()`
- `code()`
- `design()`

Not every class needs all of these methods.

### Better design
Split into focused interfaces:
- `Workable`
- `Eatable`
- `Sleepable`
- `Meetable`

Or in software systems:
- `ReadableRepository`
- `WritableRepository`
- `NotificationSender`
- `TemplateRenderer`

### Example in a device system
A `Printer` should not be forced to implement `scan()` if it only prints.

So create:
- `Printable`
- `Scannable`
- `Faxable`

### Interview explanation
Say: "I would split broad interfaces into smaller contracts so each client only depends on the methods it actually uses."

### Common ISP violations
- large "fat" interfaces
- empty methods in implementations
- implementations throwing unsupported-operation exceptions

### ISP check question
Ask: "Does every implementer really need every method in this interface?"

---

## 6) Dependency Inversion Principle (DIP)

### Definition
High-level modules should not depend on low-level modules directly. Both should depend on abstractions.

Abstractions should not depend on details; details should depend on abstractions.

### Why DIP matters
DIP makes systems flexible and testable. You can change the implementation without changing business logic.

### Bad example
`OrderService` directly creates:
- `StripePaymentProcessor`
- `SendGridEmailService`
- `MySqlOrderRepository`

This tightly couples business logic to concrete classes.

### Better design
`OrderService` depends on interfaces:
- `PaymentProcessor`
- `EmailSender`
- `OrderRepository`

Concrete implementations are injected from outside.

### Example
```text
OrderService -> PaymentProcessor interface
StripePaymentProcessor -> implements PaymentProcessor
```

### Dependency Injection relation
DIP is often implemented using dependency injection:
- constructor injection
- setter injection
- framework injection

### Interview explanation
Say: "I would inject abstractions into high-level services so provider changes or test doubles do not require modifying the core business logic."

### Common DIP violations
- direct `new` of concrete dependencies inside services
- hardcoding vendor APIs inside domain services
- business logic tightly coupled to infrastructure details

### DIP check question
Ask: "Can I replace this concrete dependency with a mock or another implementation without changing the service code?"

---

## 7) How the Five Principles Work Together

### Example: Notification system
Suppose you build a notification service.

- **SRP:** separate template rendering, user preference checks, delivery, and persistence
- **OCP:** add a new channel like WhatsApp by creating a new sender
- **LSP:** any sender implementation should work wherever `NotificationSender` is expected
- **ISP:** keep `EmailSender`, `SmsSender`, `PushSender` interfaces focused
- **DIP:** `NotificationService` depends on abstractions, not vendor-specific APIs

### Why this combination is powerful
The design becomes:
- easy to extend
- easy to test
- resilient to change
- cleaner to reason about

---

## 8) Practical Example: Order Processing System

### Bad monolithic service
`OrderService` does:
- price calculation
- discount logic
- payment
- invoice generation
- email notifications
- analytics

This violates SRP and DIP and often OCP too.

### Better design
- `PriceCalculator`
- `DiscountStrategy`
- `PaymentProcessor`
- `InvoiceGenerator`
- `NotificationSender`
- `OrderRepository`

### Flow
1. `OrderService` validates order
2. `PriceCalculator` computes total
3. `DiscountStrategy` applies discount
4. `PaymentProcessor` processes payment
5. `OrderRepository` stores order
6. `NotificationSender` sends confirmation

### SOLID mapping
- SRP: each class has one concern
- OCP: new discount or payment type can be added safely
- LSP: `StripePaymentProcessor` and `PaypalPaymentProcessor` both satisfy `PaymentProcessor`
- ISP: separate interfaces for payment, refund, and capture if needed
- DIP: `OrderService` depends on abstractions

---

## 9) Practical Example: Notification System

### Problem
You need to support Email, SMS, and Push.

### Naive approach
One class with many `if-else` conditions:
- if email, call email API
- if SMS, call SMS API
- if push, call push API

### SOLID-based design
- `NotificationSender` interface
- `EmailNotificationSender`
- `SmsNotificationSender`
- `PushNotificationSender`
- `NotificationService` orchestrator

### Benefits
- add `WhatsAppSender` later without changing core logic
- easier mocking for tests
- simpler code review and debugging

---

## 10) Common Mistakes When Applying SOLID

### Mistake 1: Using inheritance everywhere
Inheritance is not the default answer. Prefer composition where behavior varies.

### Mistake 2: Creating too many tiny abstractions too early
SOLID should improve clarity, not create unnecessary complexity.

### Mistake 3: Giant interfaces
If an interface has too many unrelated methods, it likely violates ISP.

### Mistake 4: Hardcoding dependencies
Direct instantiation of concrete classes inside business services violates DIP.

### Mistake 5: Using patterns without a reason
Patterns should solve a real variability or duplication problem.

---

## 11) Interview-Friendly Summary of Each Principle

### SRP
One class, one responsibility.

### OCP
Extend behavior without changing stable code.

### LSP
Subtypes must behave like their base types.

### ISP
Keep interfaces small and focused.

### DIP
Depend on abstractions, not details.

---

## 12) Quick Mapping Table

| Principle | Main Idea | Common Fix |
|---|---|---|
| SRP | One class should do one thing | Split large class into smaller classes |
| OCP | Add new behavior without modifying old code | Use strategy/polymorphism |
| LSP | Subtypes must be safely replaceable | Avoid invalid inheritance |
| ISP | Don’t force unused methods | Split fat interfaces |
| DIP | Depend on abstractions | Use interfaces and injection |

---

## 13) Java-Oriented Examples

### SRP example
- `ReportGenerator`
- `ReportSaver`
- `ReportEmailSender`

### OCP example
- `DiscountStrategy`
- `SeasonalDiscount`
- `ReferralDiscount`

### LSP example
- `Shape` interface
- `Circle`, `Rectangle` implement `Shape`
- avoid making `Square` break expectations where `Rectangle` behavior differs

### ISP example
- `Readable`
- `Writable`
- `Closable`

### DIP example
- `CheckoutService` depends on `PaymentGateway`
- `StripeGateway` and `PaypalGateway` implement `PaymentGateway`

---

## 14) Common Interview Questions and Answers

### Q1. Which SOLID principle is easiest to violate?
**Answer:**
SRP is often the easiest to violate because one class gradually accumulates too many responsibilities.

### Q2. Which SOLID principle is most important in extensible systems?
**Answer:**
OCP and DIP are especially important because they make systems easier to extend without modifying core logic.

### Q3. Is SOLID only for large systems?
**Answer:**
No. SOLID is useful even in small systems because it helps the code remain simple as it grows.

### Q4. Can SOLID be overused?
**Answer:**
Yes. Creating too many abstractions too early can make the code harder to read. Use SOLID pragmatically.

### Q5. What is a good one-line answer for SOLID?
**Answer:**
"SOLID is a set of five principles that make object-oriented designs easier to maintain, extend, test, and reason about."

---

## 15) Final Interview Cheat Sheet

- **SRP:** one class should have one responsibility
- **OCP:** add new behavior by extension, not modification
- **LSP:** child classes must honor parent contracts
- **ISP:** split big interfaces into focused interfaces
- **DIP:** depend on abstractions, not concrete classes

### Strong interview phrasing
"I would separate responsibilities, inject abstractions, and use strategy-based polymorphism so the system remains open for extension but closed for modification."

---

## 16) Closing Note

SOLID is not about adding complexity. It is about making change safer.

When used well, SOLID helps you write software that is:
- clean
- flexible
- testable
- maintainable
- interview-ready

