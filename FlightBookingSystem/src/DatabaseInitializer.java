import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DBManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // جدول المستخدمين
            String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    password TEXT NOT NULL,
                    name TEXT,
                    email TEXT,
                    contact_info TEXT,
                    role TEXT,
                    access_level INTEGER,
                    extra TEXT
                );
            """;

            // جدول الرحلات
            String createFlights = """
                CREATE TABLE IF NOT EXISTS flights (
                    flight_number TEXT PRIMARY KEY,
                    airline TEXT,
                    origin TEXT,
                    destination TEXT,
                    departure_time TEXT,
                    arrival_time TEXT,
                    seat_info TEXT,
                    price_info TEXT,
                    flight_type TEXT
                );
            """;

            // جدول الركاب
            String createPassengers = """
                CREATE TABLE IF NOT EXISTS passengers (
                    passenger_id TEXT PRIMARY KEY,
                    name TEXT,
                    passport_number TEXT,
                    date_of_birth TEXT,
                    special_requests TEXT
                );
            """;

            // جدول الحجوزات
            String createBookings = """
                CREATE TABLE IF NOT EXISTS bookings (
                    booking_reference TEXT PRIMARY KEY,
                    customer_id TEXT,
                    flight_number TEXT,
                    passenger_ids TEXT,
                    status TEXT,
                    payment_status TEXT,
                    FOREIGN KEY (customer_id) REFERENCES users(user_id),
                    FOREIGN KEY (flight_number) REFERENCES flights(flight_number)
                );
            """;

            // جدول المدفوعات
            String createPayments = """
                CREATE TABLE IF NOT EXISTS payments (
                    payment_id TEXT PRIMARY KEY,
                    booking_reference TEXT,
                    amount REAL,
                    method_type TEXT,
                    method_details TEXT,
                    status TEXT,
                    transaction_date TEXT,
                    FOREIGN KEY (booking_reference) REFERENCES bookings(booking_reference)
                );
            """;

            stmt.execute(createUsers);
            stmt.execute(createFlights);
            stmt.execute(createPassengers);
            stmt.execute(createBookings);
            stmt.execute(createPayments);

            System.out.println("Database tables created successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to initialize database: " + e.getMessage());
        }
    }
}