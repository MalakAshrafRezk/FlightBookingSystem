import java.sql.*;
import java.util.*;
import java.util.Date;

 public class FlightDAO {

    public static void saveFlight(Flight flight) throws SQLException {
        String flightSql = """
            INSERT OR REPLACE INTO flights (flightNumber, airline, origin, destination, departureTime, arrivalTime)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        String seatSql = """
            INSERT OR REPLACE INTO seats (flightNumber, classType, seatsAvailable, price)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement flightStmt = conn.prepareStatement(flightSql)) {
                flightStmt.setString(1, flight.getFlightNumber());
                flightStmt.setString(2, flight.getAirline());
                flightStmt.setString(3, flight.getOrigin());
                flightStmt.setString(4, flight.getDestination());
                flightStmt.setLong(5, flight.getDepartureTime().getTime());
                flightStmt.setLong(6, flight.getArrivalTime().getTime());
                flightStmt.executeUpdate();
            }

            try (PreparedStatement seatStmt = conn.prepareStatement(seatSql)) {
                for (Map.Entry<String, Integer> entry : flight.getAvailableSeats().entrySet()) {
                    String classType = entry.getKey();
                    int seats = entry.getValue();
                    double price = flight.getPrices().getOrDefault(classType, 0.0);
                    seatStmt.setString(1, flight.getFlightNumber());
                    seatStmt.setString(2, classType);
                    seatStmt.setInt(3, seats);
                    seatStmt.setDouble(4, price);
                    seatStmt.addBatch();
                }
                seatStmt.executeBatch();
            }

            conn.commit();
        }
    }

    public static List<Flight> loadFlights() throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String flightSql = "SELECT * FROM flights";
        String seatSql = "SELECT * FROM seats WHERE flightNumber = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement seatStmt = conn.prepareStatement(seatSql);
             Statement flightStmt = conn.createStatement();
             ResultSet rs = flightStmt.executeQuery(flightSql)) {

            while (rs.next()) {
                String flightNumber = rs.getString("flightNumber");
                Map<String, Integer> seats = new HashMap<>();
                Map<String, Double> prices = new HashMap<>();

                seatStmt.setString(1, flightNumber);
                try (ResultSet seatRs = seatStmt.executeQuery()) {
                    while (seatRs.next()) {
                        seats.put(seatRs.getString("classType"), seatRs.getInt("seatsAvailable"));
                        prices.put(seatRs.getString("classType"), seatRs.getDouble("price"));
                    }
                }

                long depTime = rs.getLong("departureTime");
                long arrTime = rs.getLong("arrivalTime");

                Date dep = depTime > 0 ? new Date(depTime) : null;
                Date arr = arrTime > 0 ? new Date(arrTime) : null;

                if (dep == null || arr == null) {
                    System.err.println("⚠ Skipping flight " + flightNumber + " due to invalid date fields.");
                    continue;
                }

                Flight flight = new DomesticFlight(
                        flightNumber,
                        rs.getString("airline"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        dep, arr, seats, prices
                );

                flights.add(flight);
            }
        }
        return flights;
    }
     public static void addFlight(Flight flight) throws SQLException {
         String sql = """
        INSERT INTO flights (flightNumber, airline, origin, destination, departureTime, arrivalTime)
        VALUES (?, ?, ?, ?, ?, ?);
    """;

         try (Connection conn = DatabaseHelper.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {

             stmt.setString(1, flight.getFlightNumber());
             stmt.setString(2, flight.getAirline());
             stmt.setString(3, flight.getOrigin());
             stmt.setString(4, flight.getDestination());
             stmt.setLong(5, flight.getDepartureTime().getTime());
             stmt.setLong(6, flight.getArrivalTime().getTime());

             stmt.executeUpdate();
         }
     }
     public static List<Flight> getAllFlights() throws SQLException {
         List<Flight> flights = new ArrayList<>();
         String sql = "SELECT * FROM flights";

         try (Connection conn = DatabaseHelper.getConnection();
              Statement stmt = conn.createStatement();
              ResultSet rs = stmt.executeQuery(sql)) {

             while (rs.next()) {
                 String flightNumber = rs.getString("flightNumber");
                 String airline = rs.getString("airline");
                 String origin = rs.getString("origin");
                 String destination = rs.getString("destination");
                 Date departure = new Date(rs.getLong("departureTime"));
                 Date arrival = new Date(rs.getLong("arrivalTime"));

                 // بيانات افتراضية للمقاعد والأسعار
                 Map<String, Integer> seats = new HashMap<>();
                 seats.put("economy", 100);

                 Map<String, Double> prices = new HashMap<>();
                 prices.put("economy", 100.0);

                 // إنشاء كائن مجهول من Flight
                 Flight flight = new Flight(flightNumber, airline, origin, destination, departure, arrival, seats, prices) {
                     @Override
                     public double calculatePrice(String cls) {
                         return prices.getOrDefault(cls, 0.0);
                     }
                 };

                 flights.add(flight);
             }
         }

         return flights;
     }
     public static void deleteFlight(String flightNumber) throws SQLException {
         String sql = "DELETE FROM flights WHERE flightNumber = ?";
         try (Connection conn = DatabaseHelper.getConnection();
              PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setString(1, flightNumber);
             stmt.executeUpdate();
         }
     }

 }
