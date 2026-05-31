import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.Iterator;

public class Customer extends User {
    private String address;
    private String preferences;
    private List<Booking> bookingHistory = new ArrayList<>();

    public Customer(String userId, String username, String password,
                    String name, String email, String contactInfo,
                    String address, String preferences) {
        super(userId, username, password, name, email, contactInfo);
        this.address = address;
        this.preferences = preferences;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public List<Booking> getBookingHistory() {
        return bookingHistory;
    }

    public List<Flight> searchFlights(List<Flight> flights, String origin, String destination, Date date) {
        List<Flight> results = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getOrigin().equalsIgnoreCase(origin)
                    && f.getDestination().equalsIgnoreCase(destination)
                    && f.getDepartureTime().equals(date)
                    && f.checkAvailability()) {
                results.add(f);
            }
        }
        return results;
    }

    public Booking createBooking(Flight flight, List<Passenger> pax) {
        // احجز مقعد لكل راكب في Economy
        for (Passenger p : pax) {
            flight.reserveSeat("Economy");
        }
        // إنشاء الحجز وإضافته إلى تاريخ الحجز
        Booking b = new Booking(UUID.randomUUID().toString(), this, flight);
        b.addPassengers(pax);
        bookingHistory.add(b);
        return b;
    }

    public void viewBookings() {
        if (bookingHistory.isEmpty()) {
            System.out.println("No bookings found.");
        } else {
            for (Booking b : bookingHistory) {
                System.out.println(b.generateItinerary());
            }
        }
    }

    public boolean cancelBooking(String bookingId) {
        Iterator<Booking> it = bookingHistory.iterator();
        while (it.hasNext()) {
            Booking b = it.next();
            if (b.getBookingId().equals(bookingId)) {
                // أرجع المقاعد التي تم حجزها
                for (Passenger p : b.getPassengers()) {
                    b.getFlight().releaseSeat("Economy");
                }
                // إزالة الحجز من التاريخ
                it.remove();
                System.out.println("Booking cancelled successfully.");
                return true;
            }
        }
        System.out.println("Booking not found.");
        return false;
    }

    @Override
    public void showMenu() {
        System.out.println("-- Customer Menu --");
        System.out.println("1. Search Flights");
        System.out.println("2. View Bookings");
        System.out.println("3. Cancel Booking");
        System.out.println("4. Logout");
    }


}