import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

public class BookingScreen extends JFrame {
    private Customer customer;
    private JTable flightsTable;
    private JButton bookButton, refreshButton;
    private JComboBox<String> classSelector;

    private List<Flight> availableFlights;

    public BookingScreen(Customer customer) {
        this.customer = customer;
        setTitle("Flight Booking");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadFlights();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Available Flights", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.NORTH);

        flightsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        classSelector = new JComboBox<>(new String[] {"economy", "business"});
        bookButton = new JButton("Book Flight");
        refreshButton = new JButton("Refresh");

        bottomPanel.add(new JLabel("Class:"));
        bottomPanel.add(classSelector);
        bottomPanel.add(bookButton);
        bottomPanel.add(refreshButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);

        bookButton.addActionListener(e -> handleBooking());
        refreshButton.addActionListener(e -> loadFlights());
    }

    private void loadFlights() {
        try {
            availableFlights = FlightDAO.getAllFlights();
            DefaultTableModel model = new DefaultTableModel(new String[] {
                    "Flight #", "Airline", "Origin", "Destination", "Departure", "Arrival"
            }, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (Flight f : availableFlights) {
                model.addRow(new Object[] {
                        f.getFlightNumber(), f.getAirline(), f.getOrigin(),
                        f.getDestination(), sdf.format(f.getDepartureTime()), sdf.format(f.getArrivalTime())
                });
            }
            flightsTable.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading flights: " + e.getMessage());
        }
    }

    private void handleBooking() {
        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a flight to book.");
            return;
        }

        String selectedClass = (String) classSelector.getSelectedItem();
        Flight selectedFlight = availableFlights.get(selectedRow);

        try {
            List<Passenger> passengers = PassengerDAO.loadPassengers();
            List<Passenger> selectedPassengers = new ArrayList<>();

            // نعرض اختيار الركاب عبر قائمة مبسطة
            for (Passenger p : passengers) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Include passenger: " + p.getName() + "?", "Select Passenger",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    selectedPassengers.add(p);
                }
            }

            if (selectedPassengers.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No passengers selected.");
                return;
            }

            Booking booking = customer.createBooking(selectedFlight, selectedPassengers);
            BookingDAO.saveBooking(booking);
            JOptionPane.showMessageDialog(this, "Booking successful! Reference: " + booking.getBookingReference());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing booking: " + ex.getMessage());
        }
    }
}
