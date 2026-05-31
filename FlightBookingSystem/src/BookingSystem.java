import java.util.List;
import java.util.ArrayList;

public class BookingSystem {

    private List<User> users = new ArrayList<>();
    private List<Flight> flights = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();
    private List<Payment> payments = new ArrayList<>();  // Add payments list

    // Constructor (if needed)
    public BookingSystem() {
    }

    // Methods for User Management (add, find, etc.) - omitted for brevity
    // You'll need to implement these based on your User class design

    // Methods for Flight Management (add, search, etc.) - omitted for brevity
    // You'll need to implement these based on your Flight class design

    // Methods for Booking Management (create, cancel, etc.) - omitted for brevity
    // You'll need to implement these based on your Booking class design

    public Payment processPayment(String bookingReference, double amount, PaymentMethod method) throws PaymentFailedException {
        Booking booking = findBooking(bookingReference); // Assuming you have a method to find a booking
        if (booking == null) {
            throw new PaymentFailedException("Booking not found.");
        }

        Payment payment = new Payment(bookingReference, amount, method);
        try {
            payment.validatePaymentDetails();
            payment.processPayment(); // Simulate payment processing
            booking.setPayment(payment);
            booking.setPaymentStatus(payment.getStatus().toString());
            payments.add(payment);
            if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
                booking.confirmBooking();
            }
            return payment;
        } catch (PaymentFailedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            booking.setPaymentStatus(payment.getStatus().toString());
            throw e; // Propagate the exception
        }
    }

    private Booking findBooking(String bookingReference) {
        for (Booking booking : bookings) {
            if (booking.getBookingReference().equals(bookingReference)) {
                return booking;
            }
        }
        return null;
    }

    // Method to display payment details
    public void displayPaymentDetails(String bookingReference) {
        Booking booking = findBooking(bookingReference);
        if (booking != null && booking.getPayment() != null) {
            System.out.println("Payment Details for Booking " + bookingReference + ":");
            System.out.println(booking.getPayment()); // Assuming Payment has a meaningful toString()
        } else {
            System.out.println("No payment details found for Booking " + bookingReference);
        }
    }

    // Example of adding a booking (you'll need to adapt this to your needs)
    public void addBooking(Booking booking) {
        this.bookings.add(booking);
    }

    // Example of adding a user (you'll need to adapt this to your needs)
    public void addUser(User user) {
        this.users.add(user);
    }

    // Example of adding a flight (you'll need to adapt this to your needs)
    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }

    // Getter methods (if needed)
    public List<User> getUsers() {
        return users;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public List<Payment> getPayments() {
        return payments;
    }
}
