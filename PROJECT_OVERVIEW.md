# OOP & SOLID Simulation Project

## Purpose

This project is a Java simulation designed to demonstrate practical proficiency with **object-oriented programming (OOP)** and the **SOLID design principles**. Rather than presenting each concept as an isolated example, the project evolves a small simulation domain containing vehicles, trackable assets, power systems, threats, weapons, repositories, and prioritized simulation events.

The goal is to show how these principles work together to produce code that is easier to extend, reason about, test, and maintain.

## What the Project Demonstrates

### Encapsulation

Core object state is kept inside the classes responsible for it. `Vehicle` owns state such as its ID, speed, health, position, and power policy instead of exposing those fields directly. `MissionController` similarly encapsulates its event queue and duplicate-event tracking state.

Validation is performed through controlled operations so objects cannot be placed into invalid states as easily.

### Abstraction

The project uses abstract classes and interfaces to expose behavior without requiring callers to understand implementation details.

Examples include:

- `Vehicle` defines common vehicle behavior while leaving movement to subclasses.
- `Trackable` represents anything capable of reporting a position.
- `ThreatScorable` represents anything that can provide a threat score.
- `Armed`, `FuelPowered`, and `Rechargeable` model independent capabilities.
- `PowerPolicy` abstracts how available power is validated.
- `EventRepository` abstracts how simulation events are persisted.

### Inheritance

Concrete vehicle types such as `Tank`, `Aircraft`, `Ship`, `Drone`, and `TransportTruck` inherit common vehicle state and behavior from `Vehicle` while implementing behavior specific to their type.

The design uses inheritance for genuine **is-a** relationships while using interfaces and composition for independent capabilities.

### Polymorphism

Collections operate on abstractions rather than concrete implementations. A `List<Vehicle>` can contain tanks, aircraft, ships, drones, and trucks, while a `List<Trackable>` can contain both vehicles and non-vehicle objects such as soldiers and missiles.

The same principle applies to simulation events: heterogeneous event implementations can be treated as `SimulationEvent` objects and processed through their common abstraction without `instanceof` chains.

## SOLID Principles

### Single Responsibility Principle (SRP)

Responsibilities are distributed across focused classes rather than concentrated in one large class. For example:

- Vehicles manage vehicle state and movement.
- `PowerPolicy` implementations manage power rules.
- `ThreatCalculator` calculates threat through the `ThreatScorable` contract.
- Repositories handle event persistence.
- `MissionController` coordinates event submission, duplicate rejection, prioritization, and retrieval.

This keeps unrelated responsibilities from becoming tightly coupled.

### Open/Closed Principle (OCP)

The system is designed to be extended without repeatedly modifying existing high-level logic.

New implementations can be introduced for interfaces such as `PowerPolicy`, `EventRepository`, `Trackable`, `ThreatScorable`, or `Armed`. New simulation event types can also participate through the common event abstraction without requiring the controller to contain type-specific conditional logic.

### Liskov Substitution Principle (LSP)

Code using a `Vehicle`, `Trackable`, `ThreatScorable`, or other abstraction can work with any valid implementation of that abstraction without needing to know the concrete type.

For example, the vehicle movement loop treats each object as a `Vehicle`, while runtime polymorphism selects the correct subclass movement behavior.

### Interface Segregation Principle (ISP)

Capabilities are separated into small interfaces rather than forcing every simulation object to implement one oversized interface.

A transport truck can be `FuelPowered` without being `Armed`. A drone can be `Rechargeable` and `Armed`. A soldier can be `Trackable`, `ThreatScorable`, and `Armed` without being a `Vehicle`.

This allows each class to implement only the contracts that make sense for it.

### Dependency Inversion Principle (DIP)

High-level classes depend on abstractions rather than concrete implementations.

`MissionController` depends on `EventRepository`, not specifically on an in-memory or file repository. The repository implementation is supplied through constructor injection. This makes the controller easier to extend and test because persistence can be replaced without modifying its core logic.

The same idea is demonstrated by injecting a `PowerPolicy` into `Vehicle` rather than hard-coding fuel or battery rules into the vehicle hierarchy.

## Composition and Strategy

Power handling demonstrates composition and the Strategy pattern. A vehicle receives a `PowerPolicy` rather than inheriting fuel or battery behavior directly.

This allows different power rules to be supplied independently of the vehicle hierarchy and prevents the inheritance tree from becoming responsible for unrelated implementation details.

## Collections and Event Processing

The project also demonstrates Java collection selection based on behavioral requirements.

`MissionController` uses a set of event IDs to reject duplicate events and a priority queue to process events according to priority. Events with equal priority use a sequence value as a tie-breaker, preserving arrival order among equal-priority events.

This demonstrates practical use of:

- `List`
- `Set`
- `ConcurrentHashMap.newKeySet()`
- `PriorityBlockingQueue`
- `Comparator`

## Concurrency and Thread Safety

The project progresses from ordinary collections to concurrent collections and synchronized compound operations.

`ConcurrentHashMap.newKeySet()` provides thread-safe duplicate-ID tracking, while `PriorityBlockingQueue` provides a thread-safe priority queue. Event submission protects the compound operation so concurrent callers cannot independently pass duplicate checks and corrupt the controller's logical state.

The project therefore distinguishes between **thread-safe individual data structures** and **thread-safe multi-step operations**.

## Dependency Injection and Testability

Dependencies are supplied to high-level classes rather than constructed internally. For example:

```java
EventRepository repository = new InMemoryEventRepository();
MissionController controller = new MissionController(repository);
```

A different repository can be substituted without changing `MissionController`:

```java
EventRepository repository = new FileEventRepository();
MissionController controller = new MissionController(repository);
```

This design makes in-memory implementations useful as lightweight test dependencies and reduces coupling between business logic and infrastructure.

## Final MissionController Challenge

The final controller brings the concepts together. It is designed to:

- accept heterogeneous simulation events;
- reject duplicate event IDs;
- process higher-priority events first;
- preserve arrival order among events with equal priority;
- work with `Trackable` objects through an interface;
- preserve power validation through injected `PowerPolicy` implementations;
- depend on the `EventRepository` abstraction;
- avoid `instanceof` chains by using polymorphism;
- keep queue and duplicate-tracking state encapsulated; and
- remain testable with in-memory dependencies.

## Skills Demonstrated

By completing this project, the implementation demonstrates working knowledge of Java OOP and design beyond simple syntax, including inheritance, encapsulation, abstraction, runtime polymorphism, composition, interfaces, constructor injection, SOLID principles, dependency inversion, collection selection, priority ordering, duplicate detection, concurrency, thread safety, and extensible domain design.

The project is intentionally small enough to understand as a whole while containing enough interacting components to demonstrate why these design principles matter in a real codebase.
