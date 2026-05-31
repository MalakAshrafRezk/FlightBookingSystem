import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

class Booking {
    private String bookingReference;
    private Customer customer;
    private Flight flight;
    private List<Passenger> passengers;
    private String status = "Reserved";
    private String paymentStatus = "Pending";
    private Payment payment;

    public Booking(String bookingReference, Customer customer, Flight flight) {
        this.bookingReference = bookingReference;
        this.customer = customer;
        this.flight = flight;
        this.passengers = new ArrayList<>();
    }

    // Getters
    public String getBookingReference() { return bookingReference; }
    public String getBookingId() { return bookingReference; }
    public Customer getCustomer() { return customer; }
    public Flight getFlight() { return flight; }
    public List<Passenger> getPassengers() { return List.copyOf(passengers); }
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    // Status updates
    public void confirmBooking() { this.status = "Confirmed"; }
    public void cancelBooking()  { this.status = "Cancelled"; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    // Composition: manage passengers internally
    public void addPassenger(String passengerId, String name, String passportNumber, java.util.Date dob, String specialRequests) {
        Passenger p = new Passenger(passengerId, name, passportNumber, dob, specialRequests);
        passengers.add(p);
    }

    public void removePassenger(String passengerId) {
        passengers.removeIf(p -> p.getPassengerId().equals(passengerId));
    }

    public double calculateTotalCost(String cls) throws PaymentFailedException {
        double pricePerPassenger = flight.calculateTotalPrice(cls);
        if (pricePerPassenger <= 0) {
            throw new PaymentFailedException("Invalid price calculation. Payment failed.");
        }
        return pricePerPassenger * passengers.size();
    }

    public String generateItinerary(String cls) {
        StringBuilder sb = new StringBuilder();
        sb.append("Booking Reference: ").append(bookingReference).append("\n");
        sb.append("Customer: ").append(customer.getName()).append("\n");
        sb.append("Flight: ").append(flight).append("\n");
        sb.append("Passengers:\n");
        for (Passenger p : passengers) {
            sb.append("  - ").append(p.getPassengerDetails()).append("\n");
        }
        sb.append("Payment Status: ").append(paymentStatus).append("\n");
        if (payment != null) {
            sb.append("Payment Method: ").append(payment.getMethod().getType()).append("\n");
            sb.append("Payment Amount: ").append(payment.getAmount()).append("\n");
        }

        return sb.toString();
    }

    public String generateItinerary() {
        return generateItinerary("Economy");
    }

    @Override
    public String toString() {
        return generateItinerary();
    }
    public void addPassengers(List<Passenger> passengerList) {
        for (Passenger p : passengerList) {
            addPassenger(p.getPassengerId(), p.getName(), p.getPassportNumber(), p.getDateOfBirth(), p.getSpecialRequests());
        }
    }
    public void addPassenger(String passengerId, String name, String passportNumber, Date dateOfBirth, String specialRequests) {
        Passenger p = new Passenger(passengerId, name, passportNumber, dateOfBirth, specialRequests);
        this.passengers.add(p);
    }


}
