import java.sql.*;


public class DatabaseHelper {
    static final String DB_URL = "jdbc:sqlite:flightData.sqbpro";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    userId TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL,
                    name TEXT,
                    email TEXT,
                    contactInfo TEXT,
                    role TEXT,
                    accessLevel INTEGER,
                    address TEXT,
                    preferences TEXT,
                    agency TEXT,
                    commission REAL,
                    department TEXT,
                    adminId TEXT,
                    securityLevel INTEGER
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS flights (
                    flightNumber TEXT PRIMARY KEY,
                    airline TEXT,
                    origin TEXT,
                    destination TEXT,
                    departureTime INTEGER,
                    arrivalTime INTEGER
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS seats (
                    flightNumber TEXT,
                    classType TEXT,
                    seatsAvailable INTEGER,
                    price REAL,
                    PRIMARY KEY (flightNumber, classType),
                    FOREIGN KEY (flightNumber) REFERENCES flights(flightNumber)
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS passengers (
                    passengerId TEXT PRIMARY KEY,
                    name TEXT,
                    passportNumber TEXT,
                    dateOfBirth INTEGER,
                    specialRequests TEXT
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bookings (
                    bookingReference TEXT PRIMARY KEY,
                    customerId TEXT,
                    flightNumber TEXT,
                    status TEXT,
                    paymentStatus TEXT,
                    FOREIGN KEY (customerId) REFERENCES users(userId),
                    FOREIGN KEY (flightNumber) REFERENCES flights(flightNumber)
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS booking_passengers (
                    bookingReference TEXT,
                    passengerId TEXT,
                    PRIMARY KEY (bookingReference, passengerId),
                    FOREIGN KEY (bookingReference) REFERENCES bookings(bookingReference),
                    FOREIGN KEY (passengerId) REFERENCES passengers(passengerId)
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    paymentId TEXT PRIMARY KEY,
                    bookingReference TEXT,
                    amount REAL,
                    methodType TEXT,
                    methodDetails TEXT,
                    status TEXT,
                    transactionDate INTEGER,
                    FOREIGN KEY (bookingReference) REFERENCES bookings(bookingReference)
                );
            """);

            System.out.println(" All tables created successfully.");
        } catch (SQLException e) {
            System.err.println(" Error initializing database: " + e.getMessage());
        }
    }
}