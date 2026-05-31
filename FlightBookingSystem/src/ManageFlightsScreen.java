import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageFlightsScreen extends JFrame {
    private JTable flightsTable;
    private DefaultTableModel tableModel;
    private JButton addFlightButton, deleteFlightButton;

    public ManageFlightsScreen() {
        setTitle("Manage Flights - Admin");
        setSize(800, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadFlights();

        setVisible(true);
    }

    private void initUI() {
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Flight Management Panel", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLUE);
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Flight #", "Airline", "Origin", "Destination", "Departure", "Arrival"}, 0);
        flightsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(Color.WHITE);
        addFlightButton = new JButton("Add Flight");
        deleteFlightButton = new JButton("Delete Flight");
        addFlightButton.setForeground(Color.BLUE);
        deleteFlightButton.setForeground(Color.BLUE);

        addFlightButton.addActionListener(this::handleAddFlight);
        deleteFlightButton.addActionListener(this::handleDeleteFlight);

        controlPanel.add(addFlightButton);
        controlPanel.add(deleteFlightButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadFlights() {
        try {
            tableModel.setRowCount(0);
            List<Flight> flights = FlightDAO.getAllFlights();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (Flight f : flights) {
                tableModel.addRow(new Object[]{
                        f.getFlightNumber(), f.getAirline(), f.getOrigin(),
                        f.getDestination(), sdf.format(f.getDepartureTime()), sdf.format(f.getArrivalTime())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading flights: " + e.getMessage());
        }
    }

    private void handleAddFlight(ActionEvent e) {
        JTextField numberField = new JTextField();
        JTextField airlineField = new JTextField();
        JTextField originField = new JTextField();
        JTextField destinationField = new JTextField();
        JTextField depField = new JTextField("yyyy-MM-dd HH:mm");
        JTextField arrField = new JTextField("yyyy-MM-dd HH:mm");

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Flight Number:")); panel.add(numberField);
        panel.add(new JLabel("Airline:")); panel.add(airlineField);
        panel.add(new JLabel("Origin:")); panel.add(originField);
        panel.add(new JLabel("Destination:")); panel.add(destinationField);
        panel.add(new JLabel("Departure DateTime:")); panel.add(depField);
        panel.add(new JLabel("Arrival DateTime:")); panel.add(arrField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Flight", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String fn = numberField.getText();
                String al = airlineField.getText();
                String o = originField.getText();
                String d = destinationField.getText();
                Date dep = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(depField.getText());
                Date arr = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(arrField.getText());

                Map<String, Integer> seats = new HashMap<>();
                seats.put("economy", 100);
                Map<String, Double> prices = new HashMap<>();
                prices.put("economy", 100.0);

                Flight flight = new DomesticFlight(fn, al, o, d, dep, arr, seats, prices);
                FlightDAO.saveFlight(flight);
                loadFlights();
                JOptionPane.showMessageDialog(this, "Flight added successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void handleDeleteFlight(ActionEvent e) {
        int selected = flightsTable.getSelectedRow();
        if (selected >= 0) {
            String flightNumber = (String) flightsTable.getValueAt(selected, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete flight " + flightNumber + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    FlightDAO.deleteFlight(flightNumber);
                    loadFlights();
                    JOptionPane.showMessageDialog(this, "Flight deleted successfully.");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting flight: " + ex.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete.");
        }
    }
}