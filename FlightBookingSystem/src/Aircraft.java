public class Aircraft {
    private String aircraftId;
    private String model;
    private int capacity;

    public Aircraft(String aircraftId, String model, int capacity) {
        this.aircraftId = aircraftId;
        this.model = model;
        this.capacity = capacity;
    }

    // Getters and Setters
    public String getAircraftId() { return aircraftId; }
    public void setAircraftId(String aircraftId) { this.aircraftId = aircraftId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return "Aircraft{" +
                "ID='" + aircraftId + '\'' +
                ", Model='" + model + '\'' +
                ", Capacity=" + capacity +
                '}';
    }
}
