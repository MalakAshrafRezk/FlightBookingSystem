import java.sql.*;
import java.util.*;

public class BookingDAO {

    public static void saveBooking(Booking b) throws SQLException {
        String bookingSQL = """
            INSERT OR REPLACE INTO bookings (bookingReference, customerId, flightNumber, status, paymentStatus)
            VALUES (?, ?, ?, ?, ?)
        """;

        String passengerLinkSQL = """
            INSERT OR REPLACE INTO booking_passengers (bookingReference, passengerId)
            VALUES (?, ?)
        """;

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSQL)) {
                bookingStmt.setString(1, b.getBookingReference());
                bookingStmt.setString(2, b.getCustomer().getUserId());
                bookingStmt.setString(3, b.getFlight().getFlightNumber());
                bookingStmt.setString(4, b.getStatus());
                bookingStmt.setString(5, b.getPaymentStatus());
                bookingStmt.executeUpdate();
            }

            try (PreparedStatement paxStmt = conn.prepareStatement(passengerLinkSQL)) {
                for (Passenger p : b.getPassengers()) {
                    paxStmt.setString(1, b.getBookingReference());
                    paxStmt.setString(2, p.getPassengerId());
                    paxStmt.addBatch();
                }
                paxStmt.executeBatch();
            }

            conn.commit();
        }
    }

    public static List<Booking> loadBookings(List<Customer> customers, List<Flight> flights, List<Passenger> passengers) throws SQLException {
        Map<String, Customer> customerMap = new HashMap<>();
        for (Customer c : customers) {
            customerMap.put(c.getUserId(), c);
        }

        Map<String, Flight> flightMap = new HashMap<>();
        for (Flight f : flights) {
            flightMap.put(f.getFlightNumber(), f);
        }

        Map<String, Passenger> passengerMap = new HashMap<>();
        for (Passenger p : passengers) {
            passengerMap.put(p.getPassengerId(), p);
        }

        List<Booking> bookings = new ArrayList<>();

        String sql = "SELECT * FROM bookings";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ref = rs.getString("bookingReference");
                String custId = rs.getString("customerId");
                String flightNumber = rs.getString("flightNumber");

                Customer customer = customerMap.get(custId);
                Flight flight = flightMap.get(flightNumber);
                if (customer == null || flight == null) continue;

                List<Passenger> linkedPax = getPassengersForBooking(ref, passengerMap);

                Booking b = new Booking(ref, customer, flight);

                b.setStatus(rs.getString("status"));
                b.setPaymentStatus(rs.getString("paymentStatus"));
                bookings.add(b);
            }
        }

        return bookings;
    }
    public static void updateBookingStatus(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, paymentStatus = ? WHERE bookingReference = ?;";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, booking.getStatus());
            pstmt.setString(2, booking.getPaymentStatus());
            pstmt.setString(3, booking.getBookingReference());
            pstmt.executeUpdate();
        }
    }

    public static void deleteBooking(String bookingReference) throws SQLException {
        String deleteBookingSQL = "DELETE FROM bookings WHERE bookingReference = ?";
        String deleteBookingPassengersSQL = "DELETE FROM booking_passengers WHERE bookingReference = ?";
        String deletePaymentSQL = "DELETE FROM payments WHERE bookingReference = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try (
                    PreparedStatement deleteBookingStmt = conn.prepareStatement(deleteBookingSQL);
                    PreparedStatement deletePassengersStmt = conn.prepareStatement(deleteBookingPassengersSQL);
                    PreparedStatement deletePaymentStmt = conn.prepareStatement(deletePaymentSQL)
            ) {
                deletePassengersStmt.setString(1, bookingReference);
                deletePassengersStmt.executeUpdate();

                deletePaymentStmt.setString(1, bookingReference);
                deletePaymentStmt.executeUpdate();

                deleteBookingStmt.setString(1, bookingReference);
                deleteBookingStmt.executeUpdate();

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private static List<Passenger> getPassengersForBooking(String bookingReference, Map<String, Passenger> passengerMap) throws SQLException {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT passengerId FROM booking_passengers WHERE bookingReference = ?";

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookingReference);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String pid = rs.getString("passengerId");
                    Passenger p = passengerMap.get(pid);
                    if (p != null) list.add(p);
                }
            }
        }
        return list;
    }
}
