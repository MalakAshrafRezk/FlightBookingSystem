import java.util.ArrayList;
import java.util.List;

class Airline {
    private String airlineId;
    private String name;
    private List<Aircraft> fleet;  // Aggregation: Airline has a list of Aircrafts

    public Airline(String airlineId, String name) {
        this.airlineId = airlineId;
        this.name = name;
        this.fleet = new ArrayList<>();
    }

    public String getAirlineId() { return airlineId; }
    public void setAirlineId(String airlineId) { this.airlineId = airlineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Aircraft> getFleet() {
        return new ArrayList<>(fleet);  // defensive copy
    }

    public void addAircraft(Aircraft aircraft) {
        if (!fleet.contains(aircraft)) {
            fleet.add(aircraft);
        }
    }

    public void removeAircraft(Aircraft aircraft) {
        fleet.remove(aircraft);
    }

    @Override
    public String toString() {
        return "Airline{" +
                "ID='" + airlineId + '\'' +
                ", Name='" + name + '\'' +
                ", Fleet=" + fleet +
                '}';
    }
}
