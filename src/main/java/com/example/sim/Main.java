import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        EventRepository memoryRepository = new InMemoryEventRepository();

        MissionController missionController =
            new MissionController(memoryRepository);

        ThreatCalculator calculator = new ThreatCalculator();

        List<Vehicle> vehicles = new ArrayList<>();
        List<Trackable> trackables = new ArrayList<>();
        List<ThreatScorable> threats = new ArrayList<>();
        List<Armed> armedAssets = new ArrayList<>();
        List<SimulationEvent> events = new ArrayList<>();


        Vehicle tank1 = new Tank(
            "bravo",
            0,
            76,
            new Position(0.0, 0.0),
            new FuelPowerPolicy(100)
        );

        Vehicle aircraft1 = new Aircraft(
            "alpha",
            50,
            100,
            new Position(1.0, 0.0),
            new FuelPowerPolicy(90)
        );

        Vehicle ship1 = new Ship(
            "oscar",
            40,
            80,
            new Position(0.0, 2.0),
            new FuelPowerPolicy(75)
        );

        Vehicle drone1 = new Drone(
            "golf",
            90,
            68,
            new Position(3.0, 0.0),
            new BatteryPowerPolicy(100)
        );

        Vehicle truck1 = new TransportTruck(
            "foxtrot",
            40,
            100,
            new Position(0.0, 3.0),
            new FuelPowerPolicy(80)
        );

        Soldier soldier1 =
            new Soldier(new Position(2.0, 2.0));

        Missile missile1 =
            new Missile(new Position(0.0, 3.0));


        vehicles.add(tank1);
        vehicles.add(aircraft1);
        vehicles.add(ship1);
        vehicles.add(drone1);
        vehicles.add(truck1);

        trackables.add(tank1);
        trackables.add(aircraft1);
        trackables.add(ship1);
        trackables.add(drone1);
        trackables.add(truck1);
        trackables.add(soldier1);
        trackables.add(missile1);

        threats.add(tank1);
        threats.add(aircraft1);
        threats.add(ship1);
        threats.add(drone1);
        threats.add(truck1);
        threats.add(soldier1);
        threats.add(missile1);

        armedAssets.add((Tank) tank1);
        armedAssets.add((Aircraft) aircraft1);
        armedAssets.add((Ship) ship1);
        armedAssets.add((Drone) drone1);
        armedAssets.add(soldier1);
        armedAssets.add(missile1);


        SimulationEvent e1 =
            new DetectionEvent(
                1,
                1,
                0,
                "Enemy detected"
            );

        SimulationEvent e2 =
            new WeatherEvent(
                2,
                3,
                1,
                "Weather update"
            );

        SimulationEvent e3 =
            new WeaponEvent(
                3,
                1,
                2,
                "Missile launch"
            );

        SimulationEvent e4 =
            new DetectionEvent(
                1,
                2,
                3,
                "Duplicate enemy detection"
            );

        events.add(e1);
        events.add(e2);
        events.add(e3);
        events.add(e4);


        System.out.println("\nVehicle movements:");

        for (Vehicle vehicle : vehicles) {
            vehicle.move();
        }


        System.out.println("\nTracked positions:");

        missionController.reportPositions(trackables);


        System.out.println("\nThreat scores:");

        for (ThreatScorable threat : threats) {
            System.out.println(
                calculator.calculateThreat(threat)
            );
        }


        System.out.println("\nFire weapons:");

        for (Armed asset : armedAssets) {
            asset.fireWeapon();
        }


        System.out.println("\nSubmitting events:");

        for (SimulationEvent event : events) {

            boolean accepted =
                missionController.submitEvent(event);

            if (!accepted) {
                System.out.println(
                    "Duplicate rejected: "
                    + event.getEventId()
                );
            }
        }


        System.out.println("\nProcessing events:");

        for (int i = 0; i < 3; i++) {

            SimulationEvent event =
                missionController.nextEvent();

            event.process();
        }
    }
}


// ======================================================
// VEHICLES
// ======================================================

abstract class Vehicle
    implements Trackable, ThreatScorable {

    private final String id;
    private int speed;
    private int health;
    private Position position;

    private final PowerPolicy powerPolicy;


    Vehicle(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy powerPolicy
    ) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "id is required"
            );
        }

        if (speed < 0) {
            throw new IllegalArgumentException(
                "speed cannot be negative"
            );
        }

        if (health < 0 || health > 100) {
            throw new IllegalArgumentException(
                "health must be between 0 and 100"
            );
        }

        this.id = id;
        this.speed = speed;
        this.health = health;

        this.position =
            Objects.requireNonNull(
                position,
                "position cannot be null"
            );

        this.powerPolicy =
            Objects.requireNonNull(
                powerPolicy,
                "powerPolicy cannot be null"
            );
    }


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
                "damage cannot be negative"
            );
        }

        health =
            Math.max(0, health - amount);
    }


    public String getStatus() {

        return "ID: " + id
            + " speed: " + speed
            + " health: " + health;
    }


    public final void move() {

        validateOperational();

        powerPolicy
            .validateAvailablePower();

        performMovement();
    }


    private void validateOperational() {

        if (health <= 0) {
            throw new IllegalStateException(
                "vehicle is not operational"
            );
        }
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
}


class Tank
    extends Vehicle
    implements FuelPowered, Armed {

    private static final double THREAT_SCORE = 80.0;


    Tank(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy policy
    ) {
        super(
            id,
            speed,
            health,
            position,
            policy
        );
    }


    @Override
    protected void performMovement() {
        System.out.println(
            "Tank rolling"
        );
    }


    @Override
    public void refuel() {
        System.out.println(
            "Tank refueled"
        );
    }


    @Override
    public void fireWeapon() {
        System.out.println(
            "Tank fires shell"
        );
    }


    @Override
    public double threatScore() {
        return THREAT_SCORE;
    }
}


class Aircraft
    extends Vehicle
    implements FuelPowered, Armed {

    private static final double THREAT_SCORE = 90.0;


    Aircraft(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy policy
    ) {
        super(
            id,
            speed,
            health,
            position,
            policy
        );
    }


    @Override
    protected void performMovement() {
        System.out.println(
            "Aircraft flying"
        );
    }


    @Override
    public void refuel() {
        System.out.println(
            "Aircraft refueled"
        );
    }


    @Override
    public void fireWeapon() {
        System.out.println(
            "Aircraft fires rocket"
        );
    }


    @Override
    public double threatScore() {
        return THREAT_SCORE;
    }
}


class Ship
    extends Vehicle
    implements FuelPowered, Armed {

    private static final double THREAT_SCORE = 70.0;


    Ship(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy policy
    ) {
        super(
            id,
            speed,
            health,
            position,
            policy
        );
    }


    @Override
    protected void performMovement() {
        System.out.println(
            "Ship sailing"
        );
    }


    @Override
    public void refuel() {
        System.out.println(
            "Ship refueled"
        );
    }


    @Override
    public void fireWeapon() {
        System.out.println(
            "Ship fires rocket"
        );
    }


    @Override
    public double threatScore() {
        return THREAT_SCORE;
    }
}


class Drone
    extends Vehicle
    implements Rechargeable, Armed {

    private static final double THREAT_SCORE = 30.0;


    Drone(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy policy
    ) {
        super(
            id,
            speed,
            health,
            position,
            policy
        );
    }


    @Override
    protected void performMovement() {
        System.out.println(
            "Drone flying"
        );
    }


    @Override
    public void recharge() {
        System.out.println(
            "Drone recharged"
        );
    }


    @Override
    public void fireWeapon() {
        System.out.println(
            "Drone releases shockwave"
        );
    }


    @Override
    public double threatScore() {
        return THREAT_SCORE;
    }
}


class TransportTruck
    extends Vehicle
    implements FuelPowered {

    TransportTruck(
        String id,
        int speed,
        int health,
        Position position,
        PowerPolicy policy
    ) {
        super(
            id,
            speed,
            health,
            position,
            policy
        );
    }


    @Override
    protected void performMovement() {
        System.out.println(
            "Transport truck driving"
        );
    }


    @Override
    public void refuel() {
        System.out.println(
            "Transport truck refueled"
        );
    }

    // inherits threatScore() == 0.0
}


// ======================================================
// NON-VEHICLE ASSETS
// ======================================================

class Soldier
    implements Trackable,
               ThreatScorable,
               Armed {

    private static final double THREAT_SCORE = 20.0;

    private final Position position;


    Soldier(Position position) {

        this.position =
            Objects.requireNonNull(position);
    }


    @Override
    public Position getPosition() {
        return position;
    }


    @Override
    public double threatScore() {
        return THREAT_SCORE;
    }


    @Override
    public void fireWeapon() {
        System.out.println(
            "Soldier shoots rifle"
        );
    }
}


class Missile
    implements Trackable,
               ThreatScorable,
               Armed {

    private static final double THREAT_SCORE = 100.0;

    private final Position position;


    Missile(Position position) {

        this.position =
            Objects.requireNonNull(position);
    }


    @Override
    public Position getPosition() {
        return position;
    }


    @Override
    public double threatScore() {
        return THREAT_SCORE;
    }


    @Override
    public void fireWeapon() {
        System.out.println(
            "Missile launches"
        );
    }
}


// ======================================================
// CAPABILITY INTERFACES
// ======================================================

interface Trackable {
    Position getPosition();
}


interface ThreatScorable {
    double threatScore();
}


interface Armed {
    void fireWeapon();
}


interface FuelPowered {
    void refuel();
}


interface Rechargeable {
    void recharge();
}


record Position(
    double x,
    double y
) {}


// ======================================================
// POWER POLICIES
// ======================================================

interface PowerPolicy {

    void validateAvailablePower();

    void setPower(int power);
}


class FuelPowerPolicy
    implements PowerPolicy {

    private int fuel;


    FuelPowerPolicy(int fuel) {
        this.fuel = fuel;
    }


    @Override
    public void validateAvailablePower() {

        if (fuel <= 0) {
            throw new IllegalStateException(
                "Not enough fuel"
            );
        }
    }


    @Override
    public void setPower(int fuel) {
        this.fuel = fuel;
    }
}


class BatteryPowerPolicy
    implements PowerPolicy {

    private int battery;


    BatteryPowerPolicy(int battery) {
        this.battery = battery;
    }


    @Override
    public void validateAvailablePower() {

        if (battery <= 5) {
            throw new IllegalStateException(
                "Battery too low"
            );
        }
    }


    @Override
    public void setPower(int battery) {
        this.battery = battery;
    }
}


// ======================================================
// EVENTS
// ======================================================

abstract class SimulationEvent {

    private final int eventId;
    private final int priority;
    private final long sequence;
    private final String description;


    SimulationEvent(
        int eventId,
        int priority,
        long sequence,
        String description
    ) {

        this.eventId = eventId;
        this.priority = priority;
        this.sequence = sequence;

        this.description =
            Objects.requireNonNull(
                description
            );
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


    public abstract void process();
}


class DetectionEvent
    extends SimulationEvent {

    DetectionEvent(
        int eventId,
        int priority,
        long sequence,
        String description
    ) {
        super(
            eventId,
            priority,
            sequence,
            description
        );
    }


    @Override
    public void process() {
        System.out.println(
            "Detection event: "
            + getDescription()
        );
    }
}


class WeatherEvent
    extends SimulationEvent {

    WeatherEvent(
        int eventId,
        int priority,
        long sequence,
        String description
    ) {
        super(
            eventId,
            priority,
            sequence,
            description
        );
    }


    @Override
    public void process() {
        System.out.println(
            "Weather event: "
            + getDescription()
        );
    }
}


class WeaponEvent
    extends SimulationEvent {

    WeaponEvent(
        int eventId,
        int priority,
        long sequence,
        String description
    ) {
        super(
            eventId,
            priority,
            sequence,
            description
        );
    }


    @Override
    public void process() {
        System.out.println(
            "Weapon event: "
            + getDescription()
        );
    }
}


// ======================================================
// EVENT REPOSITORIES
// ======================================================

interface EventRepository {
    void save(SimulationEvent event);
}


class InMemoryEventRepository
    implements EventRepository {

    private final List<SimulationEvent> events =
        new CopyOnWriteArrayList<>();


    @Override
    public void save(
        SimulationEvent event
    ) {
        events.add(event);

        System.out.println(
            "Event saved in memory: "
            + event.getEventId()
        );
    }


    public List<SimulationEvent> getEvents() {
        return List.copyOf(events);
    }
}


class FileEventRepository
    implements EventRepository {

    @Override
    public void save(
        SimulationEvent event
    ) {
        System.out.println(
            "Saving event to file: "
            + event.getEventId()
        );
    }
}


// ======================================================
// MISSION CONTROLLER
// ======================================================

class MissionController {

    private final EventRepository repository;

    private final Set<Integer> seenEventIds =
        ConcurrentHashMap.newKeySet();

    private final PriorityBlockingQueue<SimulationEvent>
        eventQueue =
            new PriorityBlockingQueue<>(
                11,
                Comparator
                    .comparingInt(
                        SimulationEvent::getPriority
                    )
                    .thenComparingLong(
                        SimulationEvent::getSequence
                    )
            );


    MissionController(
        EventRepository repository
    ) {

        this.repository =
            Objects.requireNonNull(
                repository,
                "repository cannot be null"
            );
    }


    public synchronized boolean submitEvent(
        SimulationEvent event
    ) {

        Objects.requireNonNull(
            event,
            "event cannot be null"
        );

        if (!seenEventIds.add(
            event.getEventId()
        )) {
            return false;
        }

        repository.save(event);

        eventQueue.offer(event);

        return true;
    }


    public SimulationEvent nextEvent()
        throws InterruptedException {

        return eventQueue.take();
    }


    public void reportPositions(
        List<Trackable> trackables
    ) {

        for (Trackable trackable : trackables) {

            System.out.println(
                trackable.getPosition()
            );
        }
    }
}

// ======================================================
// THREAT SERVICE
// ======================================================

class ThreatCalculator {

    public double calculateThreat(
        ThreatScorable threat
    ) {
        return threat.threatScore();
    }
}