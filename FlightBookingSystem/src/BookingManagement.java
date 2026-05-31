
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class BookingManagement extends JFrame {
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JButton deleteBtn, refreshBtn, searchBtn;
    private JTextField searchField;

    private List<Booking> bookings;

    public BookingManagement() {
        setTitle("Booking Management");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        loadBookings();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Header Panel with Search
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        searchBtn = new JButton("Search");
        refreshBtn = new JButton("Refresh");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by Booking Ref:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new Object[]{
                "Booking Ref", "Customer", "Flight", "#Passengers", "Status", "Payment Status"
        }, 0);
        bookingTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteBtn = new JButton("Delete Booking");
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        deleteBtn.addActionListener(e -> deleteSelectedBooking());
        refreshBtn.addActionListener(e -> loadBookings());
        searchBtn.addActionListener(e -> searchBooking());
    }

    private void loadBookings() {
        try {
            List<Customer> customers = UserDAO.loadUsers().stream()
                    .filter(u -> u instanceof Customer).map(u -> (Customer) u).toList();
            List<Flight> flights = FlightDAO.loadFlights();
            List<Passenger> passengers = PassengerDAO.loadPassengers();

            bookings = BookingDAO.loadBookings(customers, flights, passengers);

            tableModel.setRowCount(0);
            for (Booking b : bookings) {
                tableModel.addRow(new Object[]{
                        b.getBookingReference(),
                        b.getCustomer().getUsername(),
                        b.getFlight().getFlightNumber(),
                        b.getPassengers().size(),
                        b.getStatus(),
                        b.getPaymentStatus()
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage());
        }
    }

    private void deleteSelectedBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow >= 0) {
            String ref = tableModel.getValueAt(selectedRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete booking: " + ref + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    BookingDAO.deleteBooking(ref);
                    loadBookings();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting booking: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a booking to delete.");
        }
    }

    private void searchBooking() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadBookings();
            return;
        }

        tableModel.setRowCount(0);
        for (Booking b : bookings) {
            if (b.getBookingReference().contains(keyword)) {
                tableModel.addRow(new Object[]{
                        b.getBookingReference(),
                        b.getCustomer().getUsername(),
                        b.getFlight().getFlightNumber(),
                        b.getPassengers().size(),
                        b.getStatus(),
                        b.getPaymentStatus()
                });
            }
        }
    }
}
