import java.sql.*;
import java.util.*;
import java.util.Date;
import java.sql.SQLException;

public class PassengerDAO {

    public static void savePassenger(Passenger p) throws SQLException {
        String sql = "INSERT OR REPLACE INTO passengers (passengerId, name, passportNumber, dateOfBirth, specialRequests) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getPassengerId());
            pstmt.setString(2, p.getName());
            pstmt.setString(3, p.getPassportNumber());
            pstmt.setLong(4, p.getDateOfBirth().getTime());
            pstmt.setString(5, p.getSpecialRequests());
            pstmt.executeUpdate();
        }
    }
    public static List<Passenger> loadPassengers() throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT * FROM passengers";

        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Passenger p = new Passenger(
                        rs.getString("passengerId"),
                        rs.getString("name"),
                        rs.getString("passportNumber"),
                        new Date(rs.getLong("dateOfBirth")),
                        rs.getString("specialRequests")
                );
                passengers.add(p);
            }
        }
        return passengers;
    }


    public static List<Passenger> loadAllPassengers() throws SQLException {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT * FROM passengers";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Passenger p = new Passenger(
                        rs.getString("passengerId"),
                        rs.getString("name"),
                        rs.getString("passportNumber"),
                        new Date(rs.getLong("dateOfBirth")),
                        rs.getString("specialRequests")
                );
                list.add(p);
            }
        }
        return list;
    }
}