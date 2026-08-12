# Simulation Control System — OOP + SOLID Progressive Project

Work through the stages in order. The domain is a simplified military simulation system with Tanks, Aircraft, Ships, Drones, Soldiers, Missiles, events, tracking, and power validation.

## Stage 1 — Encapsulation
Create `Vehicle` with private `id`, `speed`, and `health`.
Add constructor validation and methods:
- `accelerate(int amount)`
- `stop()`
- `takeDamage(int amount)`
- `getStatus()`

Be ready to explain why fields are private and what invariants are protected.

## Stage 2 — Inheritance + Abstraction
Change `Vehicle` to an abstract class.
Add:
```java
public final void move() {
    validateOperational();
    performMovement();
}
protected abstract void performMovement();
```
Create `Tank`, `Aircraft`, and `Ship`.

Explain shared behavior vs varying behavior, abstraction, inheritance, and why `move()` may be `final`.

## Stage 3 — Polymorphism
Create `List<Vehicle>` containing several subtype objects and call `move()` in one loop.
Do not use `instanceof`.

Explain runtime polymorphism and dynamic dispatch.

## Stage 4 — Interfaces / Capabilities
Create:
```java
interface Trackable {
    Position getPosition();
}
```
Make Tank, Aircraft, Ship, Soldier, and Missile implement it.
Create one method that accepts `List<Trackable>` and reports positions.

Explain why this is an interface rather than a new base class.

## Stage 5 — Composition
Create:
```java
interface PowerPolicy {
    void validateAvailablePower();
}
```
Implement `FuelPowerPolicy` and `BatteryPowerPolicy`.
Inject `PowerPolicy` into `Vehicle`.
Reject null with `Objects.requireNonNull`.

Explain composition, "has-a", dependency injection, and programming to an interface.

## Stage 6 — SOLID: Single Responsibility
Refactor a bad `SimulationManager` that processes events, logs, saves to a database, sends notifications, and renders UI.
Split into focused classes/interfaces such as:
- `EventProcessor`
- `EventRepository`
- `NotificationService`
- `SimulationLogger`

## Stage 7 — SOLID: Open/Closed
Replace an `instanceof` chain for threat scoring with:
```java
interface ThreatScorable {
    double threatScore();
}
```
New asset types should not require modification of the calculator.

## Stage 8 — SOLID: Liskov Substitution
Start with a bad design where every `Vehicle` has `refuel()`, but `ElectricDrone.refuel()` throws `UnsupportedOperationException`.
Refactor into capability interfaces such as:
- `FuelPowered`
- `Rechargeable`

Explain why subtypes must honor the behavioral expectations of their base type.

## Stage 9 — SOLID: Interface Segregation
Refactor this bad interface:
```java
interface MilitaryAsset {
    void move();
    void fly();
    void sail();
    void fireWeapon();
    void reportPosition();
}
```
into focused interfaces:
- `Movable`
- `Flyable`
- `Sailable`
- `Armed`
- `Trackable`

## Stage 10 — SOLID: Dependency Inversion
Refactor:
```java
class MissionController {
    private final FileEventRepository repository = new FileEventRepository();
}
```
so it depends on:
```java
interface EventRepository
```
and receives the repository in its constructor.

Implement both `FileEventRepository` and `InMemoryEventRepository`.

## Stage 11 — Collections / Event Design
Create `SimulationEvent` with:
- `eventId`
- `priority`
- `sequence`
- `description`

Use:
- `PriorityQueue` for event priority
- `HashSet<Integer>` for duplicate IDs
- sequence number as a secondary comparator for FIFO among equal priorities

Explain `equals()`/`hashCode()` if you switch to `HashSet<SimulationEvent>`.

## Stage 12 — Thread Safety
Assume multiple threads submit events.
Replace structures where appropriate with:
- `ConcurrentHashMap`
- `ConcurrentHashMap.newKeySet()`
- `PriorityBlockingQueue`

Explain race conditions, atomic operations, `putIfAbsent()`, and `computeIfAbsent()`.

## Final Challenge
Build a `MissionController` that:
- accepts heterogeneous simulation events
- rejects duplicate event IDs
- processes higher priority events first
- preserves arrival order among equal priorities
- accepts `Trackable` objects
- delegates power validation through `PowerPolicy`
- depends on `EventRepository`
- contains no `instanceof` chains
- keeps state encapsulated
- is testable with in-memory dependencies

After finishing, be able to answer:
1. Encapsulation vs abstraction?
2. Interface vs abstract class?
3. Composition vs inheritance?
4. Compile-time vs runtime polymorphism?
5. Why program to an interface?
6. What is dependency injection?
7. What does each SOLID letter mean in practical terms?
8. Give an LSP violation example.
9. Why are giant interfaces bad?
10. Why does constructor injection improve testability?
