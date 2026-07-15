# LLD System Design Interview Q&A

This document collects the current low-level design interview questions and detailed answers for the core LLD topics commonly asked in interviews.

Topics covered:
- LLD fundamentals
- Object-oriented design
- SOLID principles
- UML and object relationships
- Interfaces, abstract classes, and contracts
- Composition vs inheritance
- Encapsulation and immutability
- Design patterns
- Concurrency and thread safety
- Collections and data structures in design
- Validation, exceptions, and API contracts
- Testing and extensibility
- Common LLD interview problems

---

## 1) Core LLD Interview Questions

### Q1. What is Low-Level Design (LLD)?
**Answer:**
Low-Level Design focuses on how a system or module is implemented internally. It describes classes, interfaces, methods, fields, data structures, object interaction, validations, and code-level responsibilities.

In interviews, LLD is about:
- identifying entities and responsibilities
- defining class relationships
- choosing interfaces and abstractions
- applying design principles and patterns
- handling edge cases and extensibility

### Q2. What is the difference between HLD and LLD?
**Answer:**
- **HLD** explains the architecture at a service and component level.
- **LLD** explains how a module is implemented at the class and object level.

Example:
- HLD says: "We need an order service, payment service, and queue."
- LLD says: "OrderService depends on PaymentProcessor, OrderRepository, and InventoryManager interfaces."

### Q3. What is the goal of a good LLD answer in interviews?
**Answer:**
A strong LLD answer should show:
- clean separation of responsibilities
- extensibility for future requirements
- clear abstractions
- readable and testable design
- good handling of edge cases

### Q4. How do you start an LLD interview answer?
**Answer:**
Start by showing that you understand the problem before jumping into classes.

Use this sequence:
1. Clarify requirements and scope
   - ask what features are in scope for v1
   - ask which edge cases matter most
   - confirm any constraints like scale, persistence, or concurrency
2. Identify core entities
   - list the main nouns in the problem statement
   - examples: `Order`, `User`, `Ticket`, `Notification`, `ParkingSlot`
3. Define responsibilities for each entity
   - decide what each class should own
   - keep each class focused on one responsibility
4. Identify relationships between entities
   - determine association, composition, or dependency
   - decide which object creates, owns, or uses another object
5. Create interfaces for behavior that may vary
   - use interfaces for strategies, providers, repositories, and services that may have multiple implementations
6. Choose design patterns only where useful
   - for example: Strategy for varying behavior, Factory for object creation, Observer for event-driven updates
7. Discuss validations, concurrency, and extensibility
   - explain how you will enforce invariants
   - mention thread safety if shared state exists
   - show how the design supports future changes without major rewrites

### A simple interview template
You can say:
"First I’ll clarify the requirements and confirm the scope. Then I’ll identify the main entities and their responsibilities, define relationships between them, and introduce interfaces wherever behavior may vary. After that, I’ll discuss patterns, validations, and any concurrency or extensibility concerns."

### Example using a notification system
If the problem is a notification system, your opening could be:
- clarify channels: email, SMS, push, in-app
- identify entities: `NotificationRequest`, `Notification`, `Template`, `UserPreference`
- define responsibilities: rendering, sending, preference checks, retry handling
- introduce interfaces: `NotificationSender`, `TemplateEngine`, `Provider`
- mention future support for WhatsApp or fallback providers

### Common mistake to avoid
Do not start by writing classes immediately.
Interviews usually reward a short requirements discussion first, because it shows good problem decomposition and prevents designing the wrong solution.

### Q5. What do interviewers usually evaluate in an LLD round?
**Answer:**
They usually look for a combination of design thinking, code organization, and the ability to handle change.

More specifically, interviewers evaluate whether you can:
- identify the right entities and responsibilities
- model relationships correctly using composition, association, or inheritance where appropriate
- apply OOP principles cleanly
- use abstractions and interfaces instead of hardcoding behavior
- follow SOLID principles, especially SRP, OCP, and DIP
- keep the design extensible for future requirements
- avoid unnecessary complexity and overengineering
- think about edge cases, validation, and error handling
- discuss testability and maintainability
- explain tradeoffs clearly

### What a strong answer looks like
A strong LLD answer is not just a list of classes. It should show:
- why each class exists
- what responsibility it owns
- how objects interact
- where behavior may change in the future
- how the design stays easy to test and extend

### Example
If the problem is a notification system, a strong candidate would not only say:
- `EmailSender`
- `SmsSender`
- `PushSender`

They would also explain:
- `NotificationService` orchestrates the workflow
- `NotificationSender` is an interface so new channels can be added later
- templates and preferences are separated into their own services
- retries and fallback are handled without changing the main flow

### What interviewers usually prefer
They usually prefer candidates who:
- ask clarifying questions before designing
- keep the first version simple
- separate concerns cleanly
- justify design choices instead of naming patterns mechanically
- show awareness of future change

### Common red flags
Interviewers may consider it a weak answer if you:
- jump into code without understanding requirements
- create a huge class with many responsibilities
- use inheritance everywhere
- ignore edge cases or invalid inputs
- cannot explain why you chose a particular design

---

## 2) Object-Oriented Design Basics

### Q1. What are the four pillars of OOP?
**Answer:**
- **Encapsulation:** hide internal state behind methods
- **Abstraction:** expose only essential behavior
- **Inheritance:** derive specialized behavior from a base type
- **Polymorphism:** treat multiple implementations through one contract

### Q2. What is encapsulation and why does it matter?
**Answer:**
Encapsulation means keeping an object's internal state private and allowing controlled access through methods. It protects invariants and prevents invalid state changes.

Example:
- instead of making `balance` public
- expose `deposit()` and `withdraw()` methods with validation

### Q3. What is abstraction in LLD?
**Answer:**
Abstraction means exposing the behavior that callers need without exposing internal implementation details.

Example:
- `PaymentProcessor` interface exposes `pay()`
- callers do not need to know whether implementation uses Stripe, PayPal, or a mock

### Q4. What is polymorphism and why is it useful?
**Answer:**
Polymorphism allows different classes to be used through the same interface or parent type.

It helps because:
- client code becomes simpler
- new implementations can be added without changing existing callers
- behavior can vary cleanly by implementation

### Q5. What is inheritance and when can it become risky?
**Answer:**
Inheritance allows a child class to reuse and extend behavior from a parent class. It becomes risky when the hierarchy is deep, unnatural, or forces subclasses to inherit behavior they do not need.

Prefer inheritance only when there is a true "is-a" relationship.

---

## 3) SOLID Principles

### Q1. What is SOLID?
**Answer:**
SOLID is a set of object-oriented design principles that improve maintainability and extensibility:
- **S**ingle Responsibility Principle
- **O**pen/Closed Principle
- **L**iskov Substitution Principle
- **I**nterface Segregation Principle
- **D**ependency Inversion Principle

### Q2. What is Single Responsibility Principle (SRP)?
**Answer:**
A class should have one reason to change. That means a class should own one clear responsibility.

Bad example:
- `InvoiceService` calculates invoice totals
- saves to DB
- sends email
- generates PDF

Better design:
- `InvoiceCalculator`
- `InvoiceRepository`
- `EmailNotifier`
- `PdfGenerator`

### Q3. What is Open/Closed Principle (OCP)?
**Answer:**
Software entities should be open for extension but closed for modification.

This means:
- add new behavior by creating new implementations
- avoid changing stable existing code every time requirements grow

Example:
- use `DiscountStrategy` interface for percentage, flat, or festival discount

### Q4. What is Liskov Substitution Principle (LSP)?
**Answer:**
Subtypes should be replaceable for their base types without breaking correctness.

If `Bird` has `fly()`, then a `Penguin` subclass creates a design problem because it cannot behave like a normal flying bird.

### Q5. What is Interface Segregation Principle (ISP)?
**Answer:**
Clients should not be forced to depend on methods they do not use.

Instead of one huge interface, create smaller focused interfaces.

Example:
- `Workable`
- `Eatable`
- `Payable`

This avoids fake implementations or unsupported methods.

### Q6. What is Dependency Inversion Principle (DIP)?
**Answer:**
High-level modules should not depend on low-level modules directly. Both should depend on abstractions.

Example:
- `OrderService` should depend on `PaymentProcessor`
- not directly on `StripePaymentProcessor`

### Q7. Which SOLID principle is most commonly tested in LLD interviews?
**Answer:**
Usually SRP, OCP, and DIP are tested most often because they directly affect extensibility and clean object design.

---

## 4) UML and Object Relationships

### Q1. What is the difference between association, aggregation, and composition?
**Answer:**
- **Association:** one object knows or uses another
- **Aggregation:** weak whole-part relationship; child can exist independently
- **Composition:** strong whole-part relationship; child lifecycle depends on parent

Example:
- Teacher and Student -> association
- Team and Player -> aggregation
- House and Room -> composition

### Q2. What is dependency in UML?
**Answer:**
A dependency means one class temporarily uses another, usually in a method parameter, local variable, or return type.

It is weaker than ownership.

### Q3. What is a good rule to identify composition?
**Answer:**
Ask: "Can the child meaningfully exist without the parent?"

If no, composition is usually a better fit.

### Q4. Why do relationships matter in LLD?
**Answer:**
They affect:
- ownership and lifecycle
- memory and object creation responsibility
- coupling and testability
- extensibility of the model

---

## 5) Interfaces, Abstract Classes, and Contracts

### Q1. When should you use an interface?
**Answer:**
Use an interface when:
- multiple implementations are expected
- callers only need a behavior contract
- you want loose coupling and easier testing

### Q2. When should you use an abstract class?
**Answer:**
Use an abstract class when:
- related classes share common state or default behavior
- you want partial implementation reuse
- there is a strong conceptual base type

### Q3. Interface vs abstract class?
**Answer:**
- **Interface:** contract-first, best for interchangeable behaviors
- **Abstract class:** shared base behavior/state, best for controlled inheritance

In most modern LLD designs, prefer interface-driven design unless shared base logic is clearly useful.

### Q4. Why should service classes depend on interfaces?
**Answer:**
Because interfaces reduce coupling and make unit testing easier through mocks, stubs, or fake implementations.

### Q5. What is a contract in LLD?
**Answer:**
A contract defines expected behavior, inputs, outputs, and side effects of a class or method.

Good contracts clearly define:
- valid input range
- return guarantees
- exceptions
- mutability expectations
- thread-safety assumptions

---

## 6) Composition vs Inheritance

### Q1. Why is composition often preferred over inheritance?
**Answer:**
Composition is preferred because it is more flexible and creates less tight coupling than inheritance.

With composition:
- behavior can be plugged in at runtime
- classes stay smaller and more focused
- deep inheritance hierarchies are avoided

### Q2. Give an example where composition is better than inheritance.
**Answer:**
Instead of:
- `SportsCar extends Car`
- `ElectricSportsCar extends SportsCar`
- `LuxuryElectricSportsCar extends ElectricSportsCar`

Use:
- `Car` has an `Engine`
- `Car` has a `DriveModeStrategy`
- `Car` has a `PricingPolicy`

This avoids hierarchy explosion.

### Q3. When is inheritance still appropriate?
**Answer:**
Use inheritance when:
- there is a true hierarchical relationship
- derived types genuinely satisfy the base contract
- shared logic is stable and natural

### Q4. What is the main danger of deep inheritance hierarchies?
**Answer:**
They make behavior hard to trace, increase coupling, and make changes risky because a parent-class change can unintentionally break many subclasses.

---

## 7) Encapsulation, Immutability, and Object State

### Q1. Why is immutability useful?
**Answer:**
Immutable objects are easier to reason about because their state does not change after creation.

Benefits:
- thread safety
- simpler debugging
- fewer accidental side effects
- easier caching and sharing

### Q2. Which objects are good candidates for immutability?
**Answer:**
Good candidates include:
- value objects
- money, coordinates, identifiers
- configuration objects
- request DTOs when mutation is unnecessary

### Q3. How do you design an immutable class?
**Answer:**
Typical rules:
- make fields final / read-only
- initialize through constructor or builder
- do not expose setters
- make defensive copies for mutable fields

### Q4. What is a value object?
**Answer:**
A value object represents a concept defined by its value rather than identity.

Examples:
- `Money(amount, currency)`
- `Address`
- `DateRange`

Two value objects with the same content are considered equal.

### Q5. Entity vs value object?
**Answer:**
- **Entity:** has identity and lifecycle, such as `User` or `Order`
- **Value Object:** defined by attributes only, such as `Money` or `Location`

---

## 8) Common Design Patterns

### Q1. What is a design pattern?
**Answer:**
A design pattern is a reusable solution template for a recurring design problem. It is not a copy-paste implementation; it is a design idea applied to context.

### Q2. Which design patterns are most commonly asked in LLD interviews?
**Answer:**
Frequently asked patterns:
- Singleton
- Factory / Abstract Factory
- Builder
- Strategy
- Observer
- Adapter
- Decorator
- Facade
- Command
- State

### Q3. What is the Factory Pattern?
**Answer:**
Factory encapsulates object creation logic so callers do not need to know the concrete class.

Use when:
- object creation varies by input
- creation logic is non-trivial
- you want to avoid `new` logic spread everywhere

### Q4. What is the Builder Pattern?
**Answer:**
Builder helps construct complex objects step by step, especially when there are many optional fields.

It avoids telescoping constructors and improves readability.

### Q5. What is the Strategy Pattern?
**Answer:**
Strategy defines a family of algorithms behind a common interface so they can be swapped without changing the caller.

Examples:
- pricing strategy
- payment strategy
- routing strategy
- sorting or ranking strategy

### Q6. What is the Observer Pattern?
**Answer:**
Observer lets one subject notify many listeners when state changes.

Examples:
- event listeners
- notification subscribers
- UI updates

### Q7. What is the Decorator Pattern?
**Answer:**
Decorator wraps an object to add behavior without modifying the original class.

Examples:
- adding logging
- adding caching
- adding authorization checks

### Q8. What is the Adapter Pattern?
**Answer:**
Adapter converts one interface into another that the client expects.

Useful when integrating legacy or third-party systems.

### Q9. What is the Facade Pattern?
**Answer:**
Facade provides a simple interface over a complex subsystem.

Example:
- `CheckoutFacade` may coordinate cart, payment, inventory, and order creation

### Q10. What is the Singleton Pattern and what is the common caution?
**Answer:**
Singleton ensures only one instance of a class exists and provides global access to it.

Common caution:
- overuse can create hidden global state
- it can hurt testing and make dependencies implicit

### Q11. When should you avoid forcing patterns into a design?
**Answer:**
Always avoid pattern-first design. Use a pattern only when it solves a real problem such as duplication, unstable creation logic, or behavior variation.

---

## 9) Collections and Data Structures in LLD

### Q1. Why do data structure choices matter in LLD?
**Answer:**
Because class design is not only about relationships; it also depends on lookup speed, ordering, uniqueness, memory usage, and concurrency behavior.

### Q2. When would you use a `Map` in design?
**Answer:**
Use a `Map` when:
- lookup by key is frequent
- object identity is driven by an ID or name
- deduplication is required

Example:
- `Map<String, UserSession>` for sessions by token

### Q3. When would you use a `List` vs `Set`?
**Answer:**
- **List:** ordered collection, duplicates allowed
- **Set:** uniqueness required, ordering may or may not matter depending on implementation

### Q4. What is a common LLD mistake around collections?
**Answer:**
Using a `List` for everything even when fast lookup or uniqueness is required. That creates extra loops and hidden inefficiency.

### Q5. How do you choose data structures in interviews?
**Answer:**
Explain them in terms of:
- access pattern
- update frequency
- ordering requirement
- uniqueness requirement
- concurrency requirement

---

## 10) Concurrency and Thread Safety

### Q1. Why does thread safety matter in LLD?
**Answer:**
Because many real systems are multi-threaded. If shared state is updated without coordination, race conditions, lost updates, and inconsistent reads can occur.

### Q2. What is a race condition?
**Answer:**
A race condition happens when program correctness depends on timing between concurrent operations.

Example:
- two threads decrement the same inventory count simultaneously
- both read value 1
- both write back 0
- one sale is lost or oversold

### Q3. What are common ways to make code thread-safe?
**Answer:**
- immutability
- synchronization / locks
- atomic variables
- concurrent data structures
- minimizing shared mutable state

### Q4. When should you avoid coarse-grained locking?
**Answer:**
Avoid very large synchronized blocks when they reduce throughput or create contention. Lock only the minimum state required.

### Q5. What is the difference between thread-safe and immutable?
**Answer:**
- **Immutable:** object state never changes
- **Thread-safe:** object can be safely used by multiple threads

Immutable objects are usually thread-safe, but mutable objects can also be thread-safe with proper coordination.

### Q6. What is a deadlock?
**Answer:**
A deadlock occurs when two or more threads wait forever on each other’s locks.

Typical prevention strategies:
- consistent lock ordering
- minimizing nested locks
- using timeouts where appropriate

### Q7. Which LLD interview problems commonly involve concurrency?
**Answer:**
Examples:
- elevator system
- parking lot slot allocation
- rate limiter
- logger
- scheduler
- booking system

---

## 11) Validation, Exceptions, and Defensive Design

### Q1. Why is validation important in LLD?
**Answer:**
Validation protects domain invariants and prevents illegal states from entering the system.

Example:
- negative account balance where not allowed
- booking with invalid time range
- payment with unsupported currency

### Q2. What is an invariant?
**Answer:**
An invariant is a rule that must always remain true for an object.

Examples:
- account balance cannot be below minimum allowed value
- reservation end time must be after start time
- order total must equal item total plus tax minus discount

### Q3. Where should validation live?
**Answer:**
Validation should live close to the domain object or service responsible for enforcing that rule.

Good practice:
- structural validation at input boundary
- business validation in domain/service layer

### Q4. Should methods return null or throw exceptions?
**Answer:**
It depends on the contract:
- use exceptions for exceptional or invalid scenarios
- use optional/empty results for legitimate absence

The key is consistency and clear method contracts.

### Q5. What makes an exception strategy clean?
**Answer:**
- meaningful exception types
- no swallowing exceptions silently
- preserve root cause
- translate low-level errors into domain-friendly errors when needed

---

## 12) Equality, Identity, and Object Lifecycle

### Q1. What is the difference between identity and equality?
**Answer:**
- **Identity:** whether two references represent the same entity instance or same conceptual entity
- **Equality:** whether two objects have equivalent values

For entities, identity often depends on an ID.
For value objects, equality depends on all relevant fields.

### Q2. Why do `equals()` and `hashCode()` matter in Java-based LLD?
**Answer:**
They matter because collections like `HashMap` and `HashSet` depend on consistent hashing and equality behavior.

If implemented incorrectly:
- duplicates may appear unexpectedly
- lookups may fail
- set/map behavior becomes unreliable

### Q3. When should you base equality on business fields instead of technical ID?
**Answer:**
Usually for value objects. For entities, business-field equality can be risky if fields mutate or if persistence identity is the actual source of truth.

### Q4. Why is mutable state dangerous in hash-based collections?
**Answer:**
If a field used in `hashCode()` changes after insertion, the object may no longer be found in the expected bucket.

---

## 13) Testing and Extensibility in LLD

### Q1. Why should LLD designs be testable?
**Answer:**
Testable designs are easier to verify, refactor, and maintain. If a design is hard to test, it is often too tightly coupled.

### Q2. What design choices improve testability?
**Answer:**
- dependency injection
- interfaces for external dependencies
- pure functions where possible
- small focused classes
- clear separation between business logic and I/O

### Q3. What is dependency injection in simple terms?
**Answer:**
Dependency injection means a class receives its dependencies from outside instead of creating them internally.

This improves:
- testability
- flexibility
- inversion of control

### Q4. How do you design for extensibility without overengineering?
**Answer:**
Design around likely change points, not hypothetical future fantasies.

Good rule:
- abstract what is expected to vary
- keep everything else simple

### Q5. What is overengineering in LLD?
**Answer:**
Overengineering means introducing too many layers, abstractions, or patterns without real need.

Examples:
- five interfaces for one stable class
- factory chains for trivial objects
- event-driven architecture inside a simple in-memory module

---

## 14) Common LLD Interview Problems and How to Approach Them

### Q1. What are common LLD interview problem types?
**Answer:**
Common problems include:
- parking lot
- elevator system
- vending machine
- tic-tac-toe
- snake and ladder
- book-my-show / movie booking
- splitwise
- library management system
- chess
- ATM
- logger / rate limiter
- notification system

### Q2. What is the first thing to do in an LLD problem like parking lot or book-my-show?
**Answer:**
Identify:
- primary entities
- operations/use cases
- constraints
- ownership boundaries

Then convert those into classes and interfaces.

### Q3. What are interviewers looking for in these problem-based LLD rounds?
**Answer:**
They want to see whether you can:
- model the domain correctly
- separate concerns
- handle edge cases
- support future changes
- avoid unnecessary complexity

### Q4. Should you start coding immediately in an LLD interview?
**Answer:**
No. First explain the model, entities, and relationships. Then define interfaces and major methods. Start coding only after the design direction is clear.

### Q5. What are common follow-up changes interviewers add?
**Answer:**
Common changes:
- support multiple pricing strategies
- add new vehicle or payment types
- support concurrency
- add cancellation/refund
- add filtering or search
- add notifications or audit trail

These are usually testing OCP, DIP, and separation of concerns.

---

## 15) Current and Frequently Asked LLD Follow-Up Questions

### Q1. How do you decide whether a behavior should be in an entity or a service?
**Answer:**
Put behavior in the entity when it directly protects or changes that entity's core invariants. Put it in a service when the logic coordinates multiple entities, repositories, or external systems.

### Q2. How do you keep a class from becoming a god object?
**Answer:**
Watch for signs such as too many methods, too many dependencies, or mixed responsibilities. Split by behavior and responsibility, and extract policies or helper collaborators.

### Q3. When should you introduce a repository abstraction?
**Answer:**
When domain logic should not depend directly on persistence details. Repository abstractions isolate storage concerns and improve testability.

### Q4. How do you design for change in an interview without overcomplicating the first version?
**Answer:**
Solve today's core requirements first, but abstract the most likely variability points such as pricing, payment, notification, or storage strategy.

### Q5. How do you handle state transitions cleanly in LLD?
**Answer:**
For simple workflows, enums plus guarded methods are enough. For complex workflows with behavior changing by state, the State Pattern can keep transitions and actions cleaner.

### Q6. How do you model optional features in an extensible way?
**Answer:**
Prefer composition, decorators, configuration objects, or feature strategies instead of adding many boolean flags across constructors and methods.

### Q7. How do you discuss tradeoffs in an LLD round?
**Answer:**
Explain:
- why you chose a class split
- what alternative you considered
- what flexibility you gained
- what complexity you intentionally avoided

### Q8. What is a strong one-line answer for good LLD?
**Answer:**
"Good LLD keeps responsibilities small, abstractions clear, state valid, and change localized."

---

## 16) Quick LLD Interview Cheat Sheet

- Start from **requirements and entities**, not from patterns
- Use **SRP** to keep classes focused
- Use **OCP** for new behavior via extension
- Use **DIP** so services depend on abstractions
- Prefer **composition over inheritance** unless the hierarchy is truly natural
- Use **interfaces** for swappable behaviors
- Use **abstract classes** only when shared base logic is valuable
- Protect **invariants** with validation close to the domain
- Prefer **immutable value objects** when possible
- Be explicit about **collections and lookup patterns**
- Mention **thread safety** when shared mutable state exists
- Use patterns such as **Strategy**, **Factory**, **Builder**, **Observer**, **Decorator**, and **Adapter** only when they solve a real need
- Optimize for **readability, extensibility, and testability**

---

## 17) Closing Guidance

In LLD interviews, the strongest answers do not just list classes. They explain:
- why each class exists,
- what responsibility it owns,
- how classes collaborate,
- where change is expected,
- and how the design stays maintainable when new requirements arrive.

That is the standard expected for a production-grade low-level design discussion.

