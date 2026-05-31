
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Flight {
    private double price;

    private String flightNumber, airline, origin, destination;
    private Date departureTime, arrivalTime;
    private Map<String, Integer> availableSeats = new HashMap<>();
    private Map<String, Double> prices = new HashMap<>();

    // For composite trips (flights with connecting segments)
    private List<Flight> segments = new ArrayList<>();

    /**
     * Constructs a single Flight segment.
     */
    
    public Flight(String flightNumber, String airline, String origin,
                  String destination, Date departureTime, Date arrivalTime,
                  Map<String, Integer> seats, Map<String, Double> prices) {
        if (flightNumber == null || origin == null || destination == null || departureTime == null || arrivalTime == null) {
            throw new IllegalArgumentException("All parameters must be non-null.");
        }
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.availableSeats.putAll(seats);
        this.prices.putAll(prices);
    }

    // This constructor can be used for initializing a flight with basic details
    public Flight(String flightNumber, String origin, String destination, Date departureTime, Date arrivalTime, int capacity) {
        if (flightNumber == null || origin == null || destination == null || departureTime == null || arrivalTime == null) {
            throw new IllegalArgumentException("All parameters must be non-null.");
        }
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.availableSeats.put("economy", capacity); // Default class "economy"
        this.prices.put("economy", 100.0); // Default price
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public void setAvailableSeats(Map<String, Integer> availableSeats) {
        this.availableSeats = availableSeats;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPrices(Map<String, Double> prices) {
        this.prices = prices;
    }
    

    /**
     * Adds a connecting segment to this flight, forming a composite trip.
     */
    public void addSegment(Flight flight) {
        if (flight != null && flight.checkAvailability()) {
            segments.add(flight);
        } else {
            throw new IllegalArgumentException("Invalid or unavailable flight segment.");
        }
    }

    /**
     * Removes a segment from this composite trip.
     */
    public void removeSegment(Flight flight) {
        segments.remove(flight);
    }
    public double getPrice() {
        return this.price;
    }


    /**
     * Returns all segments: this flight plus any connecting flights.
     */
    public List<Flight> getSegments() {
        List<Flight> all = new ArrayList<>();
        all.add(this);
        all.addAll(segments);
        return List.copyOf(all);
    }

    /**
     * Calculates total price across this flight and all segments.
     */
    public double calculateTotalPrice(String cls) {
        double total = calculatePrice(cls);
        for (Flight seg : segments) {
            total += seg.calculatePrice(cls);
        }
        return total;
    }

    /**
     * Calculates total travel duration from first to last segment.
     */
    public long calculateTotalDuration() {
        List<Flight> all = getSegments();
        if (all.isEmpty()) return 0;
        Date start = all.get(0).getDepartureTime();
        Date end = all.get(all.size() - 1).getArrivalTime();
        return end.getTime() - start.getTime();
    }

    // Original getters and setters
    public String getFlightNumber() { return flightNumber; }
    public String getAirline() { return airline; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public Date getDepartureTime() { return departureTime; }
    public Date getArrivalTime() { return arrivalTime; }
    public Map<String, Integer> getAvailableSeats() { return availableSeats; }
    public Map<String, Double> getPrices() { return prices; }

    /**
     * Checks if the flight has any available seats.
     */
    public boolean checkAvailability() {
        return availableSeats.values().stream().anyMatch(c -> c > 0);
    }

    /**
     * Reserves a seat for the specified class.
     */
    public void reserveSeat(String cls) {
        availableSeats.computeIfPresent(cls, (k, v) -> v > 0 ? v - 1 : v);
    }

    /**
     * Releases a seat for the specified class.
     */
    public void releaseSeat(String cls) {
        availableSeats.computeIfPresent(cls, (k, v) -> v < 100 ? v + 1 : v);  // Increment seat count
    }

    /**
     * Abstract method to calculate the price for a specific class.
     */
    public abstract double calculatePrice(String cls);

    /**
     * Updates the schedule for the flight.
     */
    public void updateSchedule(Date newDeparture, Date newArrival) {
        this.departureTime = newDeparture;
        this.arrivalTime = newArrival;
    }

    /**
     * Converts Date to human-readable format.
     */
    private String formatDate(Date date) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * Custom toString method to display flight information.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s: %s -> %s on %s", flightNumber, origin, destination, formatDate(departureTime)));
        if (!segments.isEmpty()) {
            sb.append("\n  Connecting segments:\n");
            for (Flight seg : segments) {
                sb.append("    ").append(seg.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    // New method to generate Flight ID
    public String getFlightId() {
        return flightNumber;  // Returning flight number as Flight ID (or you can modify this based on your needs)
    }



}