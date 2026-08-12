

import java.util.*;

public class Main {
    public static void main(String[] args) {
        
        EventProcessor processor = new DefaultEventProcessor();
        SimulationLogger logger = new ConsoleLogger();
        EventRepository memoryRepository = new InMemoryEventRepository();
        EventRepository fileRepository = new FileEventRepository();
        
        //create two simulation managers, one for file events and one for memory events
        //Dependency inversion principle: SimulationManager depends on abstractions (EventProcessor, SimulationLogger, EventRepository) rather than concrete implementations
        SimulationManager fileManager = new SimulationManager(processor, logger, fileRepository);
        SimulationManager memoryManager = new SimulationManager(processor, logger, memoryRepository);
        ThreatCalculator calculator = new ThreatCalculator();
        
        List<Vehicle> vehicles = new ArrayList<>();
        List<Trackable> trackables = new ArrayList<>();
        List<ThreatScorable> threats = new ArrayList<>();
        List<Armed> armedAssets = new ArrayList<>();

        //create a priority queue for simulation events, ordered by priority and then by sequence
        PriorityQueue<SimulationEvent> eventQueue = new PriorityQueue<>(
            Comparator
                .comparingInt(SimulationEvent::getPriority)
                .thenComparingLong(SimulationEvent::getSequence)
            );

        //add a set for duplicate detection of event IDs
        Set<Integer> seenEventIds = new HashSet<>();
        List<SimulationEvent> events = new ArrayList<>();
        
        Vehicle tank1 = new Tank("bravo", 0, 76, new Position(0.0, 0.0), new FuelPowerPolicy(100));
        Vehicle tank2 = new Tank("echo", 2, 36, new Position(0.0, 1.0), new FuelPowerPolicy(100));
        Vehicle aircraft1 = new Aircraft("alpha", 50, 100, new Position(1.0, 0.0), new FuelPowerPolicy(90));
        Vehicle aircraft2 = new Aircraft("delta", 95, 34, new Position(1.0, 1.0), new FuelPowerPolicy(100));
        Vehicle ship1 = new Ship("omega", 89, 32, new Position(0.0, 2.0), new FuelPowerPolicy(100));
        Vehicle ship2 = new Ship("charlie", 10, 38, new Position(2.0, 0.0), new FuelPowerPolicy(100));
        Vehicle drone1 = new Drone("golf", 90, 68, new Position(3.0, 0.0), new BatteryPowerPolicy(100));
        Vehicle truck1 = new TransportTruck("foxtrot", 40, 100, new Position(0.0, 3.0), new FuelPowerPolicy(80));
        Soldier soldier1 = new Soldier(new Position(2.0, 2.0));
        Missile missile1 = new Missile(new Position(0.0, 3.0));
        Tank tank3 = new Tank("india", 2, 36, new Position(0.0, 4.0), new FuelPowerPolicy(100));
        Drone drone2 = new Drone("hotel", 90, 68, new Position(3.0, 3.0), new BatteryPowerPolicy(100));
        Ship ship3 = new Ship("Kilo", 88, 25, new Position(4.0, 0.0), new FuelPowerPolicy(75));
        Aircraft aircraft3 = new Aircraft("Mike", 100, 89, new Position(4.0, 4.0), new FuelPowerPolicy(100));
        
        //create simulation events with unique IDs and add them to the list
        SimulationEvent e1 = new SimulationEvent(1, 1, 0, "Enemy detected");
        SimulationEvent e2 = new SimulationEvent(2, 3, 1, "Weather update");
        SimulationEvent e3 = new SimulationEvent(3, 1, 2, "Missile launch");
        SimulationEvent e4 = new SimulationEvent(1, 2, 3, "Duplicate enemy detection");

        events.add(e1);
        events.add(e2);
        events.add(e3);
        events.add(e4);
        
        vehicles.add(tank1);
        vehicles.add(tank2);
        vehicles.add(aircraft1);
        vehicles.add(aircraft2);
        vehicles.add(ship1);
        vehicles.add(ship2);
        vehicles.add(drone1);
        vehicles.add(truck1);
        
        trackables.add(soldier1);
        trackables.add(missile1);
        trackables.add(tank1);
        trackables.add(tank2);
        trackables.add(aircraft1);
        trackables.add(aircraft2);
        trackables.add(ship1);
        trackables.add(ship2);
        trackables.add(drone1);
        trackables.add(truck1);
        
        threats.add(tank1);
        threats.add(tank2);
        threats.add(aircraft1);
        threats.add(aircraft2);
        threats.add(ship1);
        threats.add(ship2);
        threats.add(drone1);
        threats.add(soldier1);
        threats.add(missile1);
        threats.add(truck1);
        
        armedAssets.add(soldier1);
        armedAssets.add(missile1);
        armedAssets.add(tank3);
        armedAssets.add(drone2);
        armedAssets.add(ship3);
        armedAssets.add(aircraft3);
        
        refuelAsset(tank3);
        rechargeAsset(drone2);
        
        System.out.println("\nVehicle movements:");
        for (Vehicle vehicle : vehicles){
            vehicle.move();
        }
        
        System.out.println("\nTracked positions:");
        reportPosition(trackables);
        
        System.out.println("\nThreat scores:");
        for (ThreatScorable threat : threats) {
            System.out.println(calculator.calculateThreat(threat));
        }
        
        System.out.println("\nFire Weapons:");
        for (Armed asset : armedAssets) {
            engageTarget(asset);
        }

        System.out.println("\nEvent processing:");
        for (SimulationEvent event : events) {
            submitEvent(event, seenEventIds, eventQueue);
        }

        while (!eventQueue.isEmpty()) {
            SimulationEvent event = eventQueue.poll();
            System.out.println("Processing event: " + event.getPriority() + " - " + event.getDescription());
        }
    }
    
    public static void reportPosition(List<Trackable> trackables) {
        for (Trackable trackable : trackables) {
            System.out.println(trackable.getPosition());
        }
    }
    
    public static void refuelAsset(FuelPowered asset) {
        asset.refuel();
    }

    public static void rechargeAsset(Rechargeable asset) {
        asset.recharge();
    }
    
    public static void engageTarget(Armed asset) {
        asset.fireWeapon();
    }

    public static void submitEvent(SimulationEvent event, Set<Integer> seenEventIds, PriorityQueue<SimulationEvent> eventQueue) {
        // Check for duplicate event IDs before adding to the queue
        if (seenEventIds.add(event.getEventId())) {
            eventQueue.offer(event);
        } else {
            System.out.println("Duplicate event ID ignored: " + event.getEventId());
        }
    }
}

abstract class Vehicle implements Trackable, ThreatScorable {

    Vehicle(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy powerPolicy
    ) {
        // Validate inputs
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }

        if (speed < 0) {
            throw new IllegalArgumentException("speed cannot be negative");
        }

        if (health < 0 || health > 100) {
            throw new IllegalArgumentException(
                "health must be between 0 and 100"
            );
        }

        if (position == null) {
            throw new IllegalArgumentException(
                "position cannot be null"
            );
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
        if (amount < 0) {
            throw new IllegalArgumentException(
                "acceleration cannot be negative"
            );
        }

        speed += amount;
    }

    public void stop() {
        speed = 0;
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                "Damage cannot be negative"
            );
        }

        health = Math.max(0, health - amount);
    }

    public String getStatus() {
        return "Status: speed " + speed +
               " health: " + health;
    }

    public final void move() {
        validateOperational();
        powerPolicy.validateAvailablePower();
        performMovement();
    }
    
    protected PowerPolicy getPowerPolicy() {
        return powerPolicy;
    }

    protected abstract void performMovement();

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public double threatScore() {
        return 0.0;
    }

    private void validateOperational() {
        if (health <= 0) {
            throw new IllegalStateException("health is 0");
        }
    }
}

class Drone extends Vehicle implements Rechargeable, Armed {
    
    private double threatScore = 30.0;
    
    Drone(String id, int speed, int health, Position position, PowerPolicy policy) {
        super(id, speed, health, position, policy);
    } 
    
    @Override
    protected void performMovement() {
        System.out.println("Drone Flying");
    }
    
    @Override
    public double threatScore() {
        return threatScore;
    }
    
    @Override
    public void recharge() {
        System.out.println("Drone recharged");
        //getPowerPolicy().setPower(100);
    }
    
    @Override
    public void fireWeapon() {
        System.out.println("Drone releases shockwave");
    }
}
class Tank extends Vehicle implements FuelPowered, Armed {
    
    private double threatScore = 80.0;
    
    Tank(String id, int speed, int health, Position position, PowerPolicy policy){
        super(id, speed, health, position, policy);
    }
    
    @Override
    public void refuel() {
        System.out.println("Tank refueled");
    } 
    
    @Override
    protected void performMovement() {
        System.out.println("Tank rolling");
    }
    
    @Override
    public double threatScore() {
        return threatScore;
    }
    
    @Override
    public void fireWeapon() {
        System.out.println("Tank fires shell");
    }
}
class Aircraft extends Vehicle implements FuelPowered, Armed {
    
    private double threatScore = 90.0;
        
    Aircraft(String id, int speed, int health, Position position, PowerPolicy policy){
        super(id, speed, health, position, policy);
    }
    
    @Override
    public void refuel() {
        System.out.println("Aircraft refueled");
    }
    
    @Override
    protected void performMovement() {
        System.out.println("Aircraft flying");
    }
    
    @Override 
    public double threatScore() {
        return threatScore;
    }
    
    @Override
    public void fireWeapon() {
        System.out.println("Aircraft fires rocket");
    }
}

class Ship extends Vehicle implements FuelPowered, Armed {
    
    private double threatScore = 70.0;
    
    Ship(String id, int speed, int health, Position position, PowerPolicy policy){
        super(id, speed, health, position, policy);
    }
    
    @Override
    public void refuel() {
        System.out.println("Ship refueled");
    }
    
    @Override
    protected void performMovement() {
        System.out.println("Ship Sailing");
    }
    
    @Override
    public double threatScore() {
        return threatScore;
    }
    
    @Override
    public void fireWeapon() {
        System.out.println("Ship fires rocket");
    }
}

class TransportTruck extends Vehicle implements FuelPowered {

    TransportTruck(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy policy
    ) {
        super(id, speed, health, position, policy);
    }
    
    @Override
    public void refuel() {
        System.out.println("Transport truck refueled");
    }

    @Override
    protected void performMovement() {
        System.out.println("Transport truck driving");
    }
}

class Soldier implements Trackable, ThreatScorable, Armed {
    
    private double threatScore = 20.0;
    private Position position;
    
    Soldier(Position position){
        this.position = position;
    }
    
    @Override
    public Position getPosition() {
        return position;
    }
    
    @Override
    public double threatScore() {
        return threatScore;
    }
    
    @Override
    public void fireWeapon() {
        System.out.println("Soldier shoots rifle");
    }
}

class Missile implements Trackable, ThreatScorable, Armed {
    
    private double threatScore = 100.0;
    private Position position;
    
    Missile(Position position){
        this.position = position;
    }
    
    @Override
    public Position getPosition() {
        return position;
    }
    
    @Override
    public double threatScore() {
        return threatScore;
    }
    
    @Override
    public void fireWeapon() {
        System.out.println("Missile launches");
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
    public void setPower(int fuel) {
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
    public void setPower(int charge) {
        this.battery = charge;
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
    void setPower(int power);
}

interface FuelPowered {
    void refuel();
}

interface Rechargeable {
    void recharge();
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

class FileEventRepository implements EventRepository {

    @Override
    public void save(SimulationEvent event) {
        System.out.println("Saving event to file: ");
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

interface Armed {
    void fireWeapon();
}

class SimulationEvent {
    private final int eventId;
    private final int priority;
    private final long sequence;
    private final String description;

    SimulationEvent(int eventId, int priority, long sequence, String description) {
        this.eventId = eventId;
        this.priority = priority;
        this.sequence = sequence;
        this.description = description;
    }

    public int getEventId() {
        return eventId;
    }

    public int getPriority() {
        return priority;
    }

    public long getSequence() {
        return sequence;
    }

    public String getDescription() {
        return description;
    }
}

interface ThreatScorable {
    double threatScore();
}

class ThreatCalculator {

    public double calculateThreat(ThreatScorable threat) {
        return threat.threatScore();
    }
}












