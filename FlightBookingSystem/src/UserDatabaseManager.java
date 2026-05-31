import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabaseManager {
    private final Connection conn;

    public UserDatabaseManager(Connection conn) {
        this.conn = conn;
    }

    // Create table if not exists
    public void createUserTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                userId TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                contact TEXT,
                role TEXT NOT NULL,
                extra TEXT
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (userId, username, password, name, email, contact, role, extra) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getContactInfo());
            pstmt.setString(7, user.getClass().getSimpleName());

            String extra = "";
            if (user instanceof Customer c) {
                extra = c.getAddress() + ";" + c.getPreferences();
            } else if (user instanceof Agent a) {
                extra = a.getDepartment() + ";" + a.getCommission();
            } else if (user instanceof Administrator a) {
                extra = a.getSecurityLevel() + ";" + a.getAdminId();
            }

            pstmt.setString(8, extra);
            pstmt.executeUpdate();
        }
    }

    public List<User> loadUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String userId = rs.getString("userId");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String contact = rs.getString("contact");
                String role = rs.getString("role");
                String extra = rs.getString("extra");

                switch (role) {
                    case "Customer" -> {
                        String[] xp = extra.split(";");
                        String address = xp.length > 0 ? xp[0] : "";
                        String prefs = xp.length > 1 ? xp[1] : "";
                        users.add(new Customer(userId, username, password, name, email, contact, address, prefs));
                    }
                    case "Agent" -> {
                        String[] xp = extra.split(";");
                        String dept = xp.length > 0 ? xp[0] : "";
                        double comm = xp.length > 1 ? Double.parseDouble(xp[1]) : 0.0;
                        users.add(new Agent(userId, username, password, name, email, contact, dept, comm));
                    }
                    case "Administrator" -> {
                        String[] xp = extra.split(";");
                        int sec = xp.length > 0 ? Integer.parseInt(xp[0]) : 0;
                        String adminId = xp.length > 1 ? xp[1] : "";
                        users.add(new Administrator(userId, username, password, name, email, contact, sec, adminId));
                    }
                }
            }
        }
        return users;
    }
}
