

import java.util.*;

public class Main {
    public static void main(String[] args) {
        
        List<Vehicle> vehicles = new ArrayList<>();
        List<Trackable> trackables = new ArrayList<>();
        
        Vehicle tank1 = new Tank("bravo", 0, 76, new Position(0.0, 0.0), new FuelPowerPolicy(100));
        Vehicle tank2 = new Tank("echo", 2, 36, new Position(0.0, 1.0), new FuelPowerPolicy(100));
        Vehicle aircraft1 = new Aircraft("alpha", 50, 100, new Position(1.0, 0.0), new FuelPowerPolicy(90));
        Vehicle aircraft2 = new Aircraft("delta", 95, 34, new Position(1.0, 1.0), new FuelPowerPolicy(100));
        Vehicle ship1 = new Ship("omega", 89, 32, new Position(0.0, 2.0), new FuelPowerPolicy(100));
        Vehicle ship2 = new Ship("charlie", 10, 38, new Position(2.0, 0.0), new FuelPowerPolicy(100));
        Trackable soldier1 = new Soldier(new Position(2.0, 2.0));
        Trackable missile1 = new Missile(new Position(0.0, 3.0));
        Vehicle drone1 = new Drone("golf", 90, 68, new Position(3.0, 0.0), new BatteryPowerPolicy(100));
        
        vehicles.add(tank1);
        vehicles.add(tank2);
        vehicles.add(aircraft1);
        vehicles.add(aircraft2);
        vehicles.add(ship1);
        vehicles.add(ship2);
        vehicles.add(drone1);
        
        trackables.add(soldier1);
        trackables.add(missile1);
        trackables.add(tank1);
        trackables.add(tank2);
        trackables.add(aircraft1);
        trackables.add(aircraft2);
        trackables.add(ship1);
        trackables.add(ship2);
        trackables.add(drone1);
        
        System.out.println("Vehicle movements:");
        for (Vehicle vehicle : vehicles){
            vehicle.move();
        }
        
        System.out.println("Tracked positions:");
        reportPosition(trackables);
    }
    
    public static void reportPosition(List<Trackable> trackables) {
        for (Trackable trackable : trackables) {
            System.out.println(trackable.getPosition());
        }
    }
}

abstract class Vehicle implements Trackable {
    Vehicle(String id, int speed, int health, Position position, PowerPolicy powerPolicy){
        
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        
        if (speed < 0) {
            throw new IllegalArgumentException("speed cannot be negative");
        }
        
        if (health < 0 || health > 100) {
            throw new IllegalArgumentException("health must be between 0 and 100");
        }
        
        if (position == null) {
            throw new IllegalArgumentException("position cannot be null");
        }
        
        this.id = id;
        this.speed = speed;
        this.health = health;
        this.position = position;
        this.powerPolicy = Objects.requireNonNull(powerPolicy);
    }
    
    private final String id;
    private int speed;
    private int health;
    private Position position;
    private final PowerPolicy powerPolicy;
    
    public void accelerate(int amount) {
        if (amount < 0){
            throw new IllegalArgumentException("acceleration cannot be negative");
        }
        speed += amount;
    }
    
    public void stop() {
        speed  = 0;
    }
    
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        health = Math.max(0, health - amount);
    }
        
    public String getStatus() {
        return "Status: speed " + speed + " health: " + health;
    }
    
    public final void move() {
        validateOperational();
        powerPolicy.validateAvailablePower();
        performMovement();
    }
    
    protected abstract void performMovement();
    
    public Position getPosition() {
        return position;
    }
    
    private void validateOperational() {
        if(health <= 0){
            throw new IllegalStateException("health is 0");
        }
    }
}

class Drone extends Vehicle {
    
    Drone(String id, int speed, int health, Position position, PowerPolicy policy) {
        super(id, speed, health, position, policy);
    } 
    
    @Override
    protected void performMovement() {
        System.out.println("Fly");
    }
}
class Tank extends Vehicle {
    
    Tank(String id, int speed, int health, Position position, PowerPolicy policy){
        super(id, speed, health, position, policy);
    }
    
    @Override
    protected void performMovement() {
        System.out.println("Drive");
    }
}
class Aircraft extends Vehicle {
        
    Aircraft(String id, int speed, int health, Position position, PowerPolicy policy){
        super(id, speed, health, position, policy);
    }
    
    @Override
    protected void performMovement() {
        System.out.println("Fly");
    }
}

class Ship extends Vehicle {
    
    Ship(String id, int speed, int health, Position position, PowerPolicy policy){
        super(id, speed, health, position, policy);
    }
    
    @Override
    protected void performMovement() {
        System.out.println("Sail");
    }
}

class Soldier implements Trackable {
    
    private Position position;
    
    Soldier(Position position){
        this.position = position;
    }
    
    @Override
    public Position getPosition() {
        return position;
    }
}

class Missile implements Trackable {
    
    private Position position;
    
    Missile(Position position){
        this.position = position;
    }
    
    @Override
    public Position getPosition() {
        return position;
    }
}

interface Trackable {
    Position getPosition();
}

record Position(double x, double y) {}

class FuelPowerPolicy implements PowerPolicy {

    private int fuel;

    FuelPowerPolicy(int fuel) {
        this.fuel = fuel;
    }

    @Override
    public void validateAvailablePower() {
        if (fuel <= 0) {
            throw new IllegalStateException("Not enough fuel");
        }
    }
}

class BatteryPowerPolicy implements PowerPolicy {

    private int battery;

    BatteryPowerPolicy(int battery) {
        this.battery = battery;
    }

    @Override
    public void validateAvailablePower() {
        if (battery <= 5) {
            throw new IllegalStateException("Battery too low");
        }
    }
}

interface PowerPolicy {
    void validateAvailablePower();
}


class SimulationManager {
    
    private final EventProcessor processor;
    private final SimulationLogger logger;
    private final EventRepository repository;
    
    SimulationManager(
        EventProcessor processor,
        SimulationLogger logger,
        EventRepository repository
    ) {
        this.processor = processor;
        this.logger = logger;
        this.repository = repository;
    }
    
    public void handleEvent(SimulationEvent event) {
        processor.process(event);
        repository.save(event);
        logger.log("Event handled");
    }
}

class DefaultEventProcessor implements EventProcessor {
    
    @Override 
    public void process(SimulationEvent event) {
        System.out.println("Processed event: ");
    }
}

class ConsoleLogger implements SimulationLogger {
    
    @Override
    public void log(String message) {
        System.out.println(message);
    }
}

class InMemoryEventRepository implements EventRepository {

    @Override
    public void save(SimulationEvent event) {
        System.out.println("Saving event: ");
    }
}

interface EventProcessor {
    void process(SimulationEvent event);
}

interface NotificationService {
    void notify(String message);
}

interface SimulationLogger {
    void log(String message);
}

interface EventRepository {
    void save(SimulationEvent event);
}

class SimulationEvent {
    // TODO Stage 11
}
