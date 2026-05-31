import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static void saveUser(User user) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO users (userId, username, password, name, email, contactInfo, role, accessLevel, 
            address, preferences, agency, commission, department, adminId, securityLevel)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getContactInfo());
            pstmt.setString(7, user.getRole());
            pstmt.setInt(8, user.getAccessLevel());

            if (user instanceof Customer c) {
                pstmt.setString(9, c.getAddress());
                pstmt.setString(10, c.getPreferences());
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setNull(12, Types.DOUBLE);
                pstmt.setNull(13, Types.VARCHAR);
                pstmt.setNull(14, Types.VARCHAR);
                pstmt.setNull(15, Types.INTEGER);
            } else if (user instanceof Agent a) {
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setString(11, a.getDepartment());
                pstmt.setDouble(12, a.getCommission());
                pstmt.setNull(13, Types.VARCHAR);
                pstmt.setNull(14, Types.VARCHAR);
                pstmt.setNull(15, Types.INTEGER);
            } else if (user instanceof Administrator a) {
                pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.VARCHAR);
                pstmt.setNull(11, Types.VARCHAR);
                pstmt.setNull(12, Types.DOUBLE);
                pstmt.setNull(13, Types.VARCHAR); // لأنه لا يوجد department
                pstmt.setString(14, a.getAdminId());
                pstmt.setInt(15, a.getSecurityLevel());
            }

            pstmt.executeUpdate();
        }
    }

    public static List<User> loadUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users;";
        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String role = rs.getString("role");
                switch (role) {
                    case "Customer" -> users.add(new Customer(
                            rs.getString("userId"), rs.getString("username"), rs.getString("password"),
                            rs.getString("name"), rs.getString("email"), rs.getString("contactInfo"),
                            rs.getString("address"), rs.getString("preferences")));
                    case "Agent" -> users.add(new Agent(
                            rs.getString("userId"), rs.getString("username"), rs.getString("password"),
                            rs.getString("name"), rs.getString("email"), rs.getString("contactInfo"),
                            rs.getString("agency"), rs.getDouble("commission")));
                    case "Administrator" -> users.add(new Administrator(
                            rs.getString("userId"), rs.getString("username"), rs.getString("password"),
                            rs.getString("name"), rs.getString("email"), rs.getString("contactInfo"),
                            rs.getInt("securityLevel"), rs.getString("adminId")));
                }
            }
        }
        return users;
    }
    public static boolean deleteUserById(String userId) throws SQLException {
        String sql = "DELETE FROM users WHERE userId = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

}