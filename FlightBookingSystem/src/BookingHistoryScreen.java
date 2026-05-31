import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class BookingHistoryScreen extends JFrame {
    private Customer customer;
    private JTextArea historyArea;

    public BookingHistoryScreen(Customer customer) {
        this.customer = customer;
        setTitle("Booking History");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Your Booking History", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.BLUE);
        titleLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(titleLabel, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        historyArea.setForeground(Color.BLUE);
        historyArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(historyArea);
        add(scrollPane, BorderLayout.CENTER);

        loadBookingHistory();
    }

    private void loadBookingHistory() {
        List<Booking> bookings = customer.getBookingHistory();
        if (bookings.isEmpty()) {
            historyArea.setText("You have no bookings yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Booking b : bookings) {
                sb.append("------------------------------\n");
                sb.append(b.generateItinerary());
                sb.append("\n");
            }
            historyArea.setText(sb.toString());
        }
    }

    // To launch standalone
    public static void main(String[] args) {
        // Dummy customer for testing if needed
        // BookingHistoryScreen screen = new BookingHistoryScreen(dummyCustomer);
        // screen.setVisible(true);
    }
}

