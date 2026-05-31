

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FlightSearch extends JFrame {
    private User currentUser;
    private JComboBox<String> originCombo, destinationCombo;
    private JTextField dateField;
    private JTable flightTable;
    private DefaultTableModel tableModel;

    public FlightSearch(User user) {
        this.currentUser = user;
        setTitle("Flight Search");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout());

        originCombo = new JComboBox<>(new String[]{"Cairo", "Jeddah", "Dubai", "London"});
        destinationCombo = new JComboBox<>(new String[]{"Cairo", "Jeddah", "Dubai", "London"});
        dateField = new JTextField(10);  // Expected format: yyyy-MM-dd

        topPanel.add(new JLabel("From:"));
        topPanel.add(originCombo);
        topPanel.add(new JLabel("To:"));
        topPanel.add(destinationCombo);
        topPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        topPanel.add(dateField);

        JButton searchButton = new JButton("Search");
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{
                "Flight#", "Airline", "Origin", "Destination", "Departure", "Arrival", "Seats", "Price"
        }, 0);
        flightTable = new JTable(tableModel);
        add(new JScrollPane(flightTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton bookButton = new JButton("Book Flight");
        JButton backButton = new JButton("Back");
        bottomPanel.add(bookButton);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Events
        searchButton.addActionListener(e -> searchFlights());
        backButton.addActionListener(e -> {
            this.dispose();
            JOptionPane.showMessageDialog(this, "Return to main screen depending on user role.");
            // هنا يمكنك إعادة المستخدم إلى الشاشة الرئيسية
        });

        bookButton.addActionListener(e -> {
            int row = flightTable.getSelectedRow();
            if (row >= 0) {
                String flightNumber = (String) flightTable.getValueAt(row, 0);
                JOptionPane.showMessageDialog(this, "You selected flight: " + flightNumber + "\nYou can proceed to booking screen here.");
                // يتم ربط هذا الزر بواجهة حجز جديدة لاحقاً
            } else {
                JOptionPane.showMessageDialog(this, "Please select a flight first.");
            }
        });
    }

    private void searchFlights() {
        try {
            List<Flight> allFlights = FlightDAO.getAllFlights();
            String origin = (String) originCombo.getSelectedItem();
            String destination = (String) destinationCombo.getSelectedItem();
            String dateInput = dateField.getText().trim();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date selectedDate = df.parse(dateInput);

            List<Flight> filtered = allFlights.stream()
                    .filter(f -> f.getOrigin().equalsIgnoreCase(origin))
                    .filter(f -> f.getDestination().equalsIgnoreCase(destination))
                    .filter(f -> {
                        String flightDate = df.format(f.getDepartureTime());
                        return flightDate.equals(df.format(selectedDate));
                    })
                    .collect(Collectors.toList());

            tableModel.setRowCount(0); // Clear previous data
            for (Flight f : filtered) {
                int seats = f.getAvailableSeats().getOrDefault("economy", 0);
                double price = f.getPrices().getOrDefault("economy", 0.0);
                tableModel.addRow(new Object[]{
                        f.getFlightNumber(),
                        f.getAirline(),
                        f.getOrigin(),
                        f.getDestination(),
                        df.format(f.getDepartureTime()),
                        df.format(f.getArrivalTime()),
                        seats,
                        price
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
