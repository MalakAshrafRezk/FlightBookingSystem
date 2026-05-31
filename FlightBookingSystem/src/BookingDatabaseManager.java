import java.sql.*;
import java.util.*;

public class BookingDatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:flight_booking.db";

    public static void initializeBookingTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS bookings (
                booking_reference TEXT PRIMARY KEY,
                customer_id TEXT NOT NULL,
                flight_number TEXT NOT NULL,
                passenger_ids TEXT NOT NULL,
                status TEXT NOT NULL,
                payment_status TEXT NOT NULL,
                FOREIGN KEY (customer_id) REFERENCES users(user_id),
                FOREIGN KEY (flight_number) REFERENCES flights(flight_number)
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertBooking(Booking booking) {
        String sql = "INSERT INTO bookings (booking_reference, customer_id, flight_number, passenger_ids, status, payment_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, booking.getBookingReference());
            pstmt.setString(2, booking.getCustomer().getUserId());
            pstmt.setString(3, booking.getFlight().getFlightNumber());

            // Collect passenger IDs as a single string
            List<Passenger> passengers = booking.getPassengers();
            String passengerIds = String.join("|", passengers.stream().map(Passenger::getPassengerId).toList());

            pstmt.setString(4, passengerIds);
            pstmt.setString(5, booking.getStatus());
            pstmt.setString(6, booking.getPaymentStatus());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Booking> loadBookings(List<Customer> customers, List<Flight> flights, List<Passenger> allPassengers) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings";

        Map<String, Customer> customerMap = new HashMap<>();
        for (Customer c : customers) {
            customerMap.put(c.getUserId(), c);
        }

        Map<String, Flight> flightMap = new HashMap<>();
        for (Flight f : flights) {
            flightMap.put(f.getFlightNumber(), f);
        }

        Map<String, Passenger> passengerMap = new HashMap<>();
        for (Passenger p : allPassengers) {
            passengerMap.put(p.getPassengerId(), p);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String ref = rs.getString("booking_reference");
                String custId = rs.getString("customer_id");
                String flightNumber = rs.getString("flight_number");
                String passengerIdsStr = rs.getString("passenger_ids");
                String status = rs.getString("status");
                String paymentStatus = rs.getString("payment_status");

                Customer c = customerMap.get(custId);
                Flight f = flightMap.get(flightNumber);

                if (c == null || f == null) continue;

                // 1. إنشاء الحجز بدون ركاب
                Booking booking = new Booking(ref, c, f);

                // 2. إضافة الركاب إلى الحجز باستخدام composition
                for (String pid : passengerIdsStr.split("\\|")) {
                    Passenger p = passengerMap.get(pid);
                    if (p != null) {
                        booking.addPassenger(
                                p.getPassengerId(),
                                p.getName(),
                                p.getPassportNumber(),
                                p.getDateOfBirth(),
                                p.getSpecialRequests()
                        );
                    }
                }

                // 3. إعداد الحالة وحالة الدفع
                booking.setStatus(status);
                booking.setPaymentStatus(paymentStatus);

                bookings.add(booking);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bookings;
    }

}
