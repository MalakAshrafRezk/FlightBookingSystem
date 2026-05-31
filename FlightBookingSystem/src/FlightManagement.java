

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FlightManagement extends JFrame {
    private User adminUser;
    private JTable flightTable;
    private DefaultTableModel tableModel;

    private JButton addBtn, deleteBtn, backBtn;

    public FlightManagement(User adminUser) {
        this.adminUser = adminUser;
        setTitle("Flight Management");
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

        JPanel buttonPanel = new JPanel(new FlowLayout());
        addBtn = new JButton("Add Flight");
        deleteBtn = new JButton("Delete Flight");
        backBtn = new JButton("Back");

        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(this::addFlight);
        deleteBtn.addActionListener(this::deleteFlight);
        backBtn.addActionListener(e -> {
            this.dispose();
            new AdminDashboard(adminUser).setVisible(true);
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading flights: " + e.getMessage());
        }
    }

    private void addFlight(ActionEvent e) {
        AddEditFlightDialog dialog = new AddEditFlightDialog((Frame) SwingUtilities.getWindowAncestor(this), this, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadFlights();
        }
    }

    private void deleteFlight(ActionEvent e) {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete.");
            return;
        }
        String flightNumber = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete flight " + flightNumber + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                FlightDAO.deleteFlight(flightNumber);
                loadFlights();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting flight: " + ex.getMessage());
            }
        }
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}