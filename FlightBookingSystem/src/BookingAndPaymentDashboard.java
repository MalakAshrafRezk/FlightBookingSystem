import javax.swing.*;
import java.awt.*;

public class BookingAndPaymentDashboard extends JFrame {
    private User currentUser;

    public BookingAndPaymentDashboard(User user) {
        this.currentUser = user;
        setTitle("Booking & Payment Management Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Welcome " + currentUser.getUsername(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JButton createBookingBtn = new JButton("Create Booking");
        JButton viewBookingsBtn = new JButton("View All Bookings");
        JButton makePaymentBtn = new JButton("Make Payment");
        JButton managePassengersBtn = new JButton("Manage Passengers");
        JButton backBtn = new JButton("Back to Dashboard");
        JButton logoutBtn = new JButton("Logout");

        buttonPanel.add(createBookingBtn);
        buttonPanel.add(viewBookingsBtn);
        buttonPanel.add(makePaymentBtn);
        buttonPanel.add(managePassengersBtn);
        buttonPanel.add(backBtn);
        buttonPanel.add(logoutBtn);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Event Handlers
        createBookingBtn.addActionListener(e -> openCreateBooking());
        viewBookingsBtn.addActionListener(e -> openViewBookings());
        makePaymentBtn.addActionListener(e -> openMakePayment());
        managePassengersBtn.addActionListener(e -> openManagePassengers());
        backBtn.addActionListener(e -> goBack());
        logoutBtn.addActionListener(e -> logout());
    }

    private void openCreateBooking() {
        // TODO: open CreateBookingFrame
    }

    private void openViewBookings() {
        // TODO: open BookingListFrame
    }

    private void openMakePayment() {
        // TODO: open PaymentFrame
    }

    private void openManagePassengers() {
        // TODO: open PassengerManagementFrame
    }

    private void goBack() {
        this.dispose();
        new AdminDashboard(currentUser).setVisible(true);
    }

    private void logout() {
        this.dispose();
        new LoginScreen().setVisible(true);
    }

    public static void main(String[] args) {
        // Dummy test with placeholder admin user
        User testAdmin = new Administrator("A001", "admin", "pass123", "Admin Name", "admin@example.com", "1234567890", 5, "ADM01");
        new BookingAndPaymentDashboard(testAdmin).setVisible(true);
    }
}
