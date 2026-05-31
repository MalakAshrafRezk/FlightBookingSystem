import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CustomerDashboard extends JFrame {
    private Customer customer;

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
        setTitle("Customer Dashboard");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));
        getContentPane().setBackground(Color.WHITE);

        JButton profileBtn = new JButton("Edit Profile");
        JButton searchFlightsBtn = new JButton("Search Flights");
        JButton bookingHistoryBtn = new JButton("View Booking History");
        JButton logoutBtn = new JButton("Logout");

        Font btnFont = new Font("Arial", Font.BOLD, 16);
        profileBtn.setFont(btnFont);
        searchFlightsBtn.setFont(btnFont);
        bookingHistoryBtn.setFont(btnFont);
        logoutBtn.setFont(btnFont);

        profileBtn.setForeground(Color.BLUE);
        searchFlightsBtn.setForeground(Color.BLUE);
        bookingHistoryBtn.setForeground(Color.BLUE);
        logoutBtn.setForeground(Color.BLUE);

        add(profileBtn);
        add(searchFlightsBtn);
        add(bookingHistoryBtn);
        add(logoutBtn);

        profileBtn.addActionListener(e -> new CustomerProfileScreen(customer).setVisible(true));
        searchFlightsBtn.addActionListener(this::openFlightSearch);
        bookingHistoryBtn.addActionListener(e -> new BookingHistoryScreen(customer).setVisible(true));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen().setVisible(true);
        });
    }

    private void openFlightSearch(ActionEvent e) {
        try {
            new FlightSearchScreen(customer).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open flight search screen.");
        }
    }
}
