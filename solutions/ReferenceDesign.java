// Reference design — compare only after attempting the stages.

import java.util.*;
import java.util.concurrent.*;

record Position(double x, double y) {}

interface Trackable {
    Position getPosition();
}

interface PowerPolicy {
    void validateAvailablePower();
}

final class FuelPowerPolicy implements PowerPolicy {
    private final double fuel;
    FuelPowerPolicy(double fuel) { this.fuel = fuel; }

    public void validateAvailablePower() {
        if (fuel <= 0) throw new IllegalStateException("Insufficient fuel");
    }
}

final class BatteryPowerPolicy implements PowerPolicy {
    private final double battery;
    BatteryPowerPolicy(double battery) { this.battery = battery; }

    public void validateAvailablePower() {
        if (battery <= 5) throw new IllegalStateException("Battery too low");
    }
}

abstract class Vehicle implements Trackable {
    private final String id;
    private int speed;
    private int health = 100;
    private final PowerPolicy powerPolicy;
    private Position position = new Position(0, 0);

    protected Vehicle(String id, PowerPolicy powerPolicy) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        this.id = id;
        this.powerPolicy = Objects.requireNonNull(powerPolicy, "powerPolicy must not be null");
    }

    public final void move() {
        if (health <= 0) throw new IllegalStateException("Vehicle disabled");
        powerPolicy.validateAvailablePower();
        performMovement();
    }

    protected abstract void performMovement();

    public void accelerate(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        speed += amount;
    }

    public void stop() { speed = 0; }

    public void takeDamage(int amount) {
        if (amount < 0) throw new IllegalArgumentException();
        health = Math.max(0, health - amount);
    }

    public String getStatus() {
        return id + " speed=" + speed + " health=" + health;
    }

    public Position getPosition() { return position; }
}

final class Tank extends Vehicle {
    Tank(String id, PowerPolicy p) { super(id, p); }
    protected void performMovement() { System.out.println("Tank moves on tracks"); }
}

final class Aircraft extends Vehicle {
    Aircraft(String id, PowerPolicy p) { super(id, p); }
    protected void performMovement() { System.out.println("Aircraft flies"); }
}

record SimulationEvent(int eventId, int priority, long sequence, String description) {}

interface EventRepository {
    void save(SimulationEvent event);
}

final class InMemoryEventRepository implements EventRepository {
    private final Map<Integer, SimulationEvent> events = new ConcurrentHashMap<>();
    public void save(SimulationEvent event) { events.put(event.eventId(), event); }
}

final class MissionController {
    private final EventRepository repository;
    private final Set<Integer> seenIds = ConcurrentHashMap.newKeySet();

    private final BlockingQueue<SimulationEvent> queue =
        new PriorityBlockingQueue<>(
            11,
            Comparator.comparingInt(SimulationEvent::priority)
                      .thenComparingLong(SimulationEvent::sequence)
        );

    MissionController(EventRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public boolean submit(SimulationEvent event) {
        if (!seenIds.add(event.eventId())) return false;
        repository.save(event);
        queue.offer(event);
        return true;
    }

    public SimulationEvent takeNext() throws InterruptedException {
        return queue.take();
    }
}
