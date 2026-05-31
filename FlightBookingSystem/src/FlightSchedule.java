

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FlightSchedule extends JFrame {
    private User user;
    private JTable flightTable;
    private DefaultTableModel tableModel;
    private JButton backButton;

    public FlightSchedule(User user) {
        this.user = user;
        setTitle("Flight Schedule");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadFlights();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        String[] columnNames = {"Flight No", "Airline", "Origin", "Destination", "Departure", "Arrival"};
        tableModel = new DefaultTableModel(columnNames, 0);
        flightTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(flightTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        backButton = new JButton("Back");
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            this.dispose();
            if (user.getRole().equals("Administrator")) {
                new AdminDashboard(user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Flight schedule is admin only in this version.");
            }
        });
    }

    private void loadFlights() {
        tableModel.setRowCount(0);
        try {
            List<Flight> flights = FlightDAO.getAllFlights();
            for (Flight f : flights) {
                Object[] row = {
                        f.getFlightNumber(),
                        f.getAirline(),
                        f.getOrigin(),
                        f.getDestination(),
                        formatDate(f.getDepartureTime()),
                        formatDate(f.getArrivalTime())
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading flight schedule: " + e.getMessage());
        }
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}
