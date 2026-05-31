import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminPanel extends JPanel {

    private MainApp mainApp;
    private Administrator currentAdmin;
    private JLabel welcomeLabel;
    private JTable usersTable, flightsTable; // Tables to display users and flights
    private DefaultTableModel usersTableModel, flightsTableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public AdminPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(250, 230, 230)); // Slightly different background for admin
        welcomeLabel = new JLabel("Admin Control Panel", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.addActionListener(e -> mainApp.showPanel(MainApp.LOGIN_PANEL));
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setOpaque(false);
        logoutPanel.add(logoutButton);
        topPanel.add(logoutPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 16));

        tabbedPane.addTab("Manage Users", createManageUsersPanel());
        tabbedPane.addTab("Manage Flights", createManageFlightsPanel());
        // You can add a "System Settings" tab if needed

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createManageUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("Manage Users"));

        usersTableModel = new DefaultTableModel(new String[]{"User ID", "Username", "Full Name", "Role", "Email"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,10));
        JButton addUserButton = new JButton("Add New User");
        JButton removeUserButton = new JButton("Remove Selected User");
        // JButton editUserButton = new JButton("Edit Selected User"); // Can be added later

        buttonsPanel.add(addUserButton);
        // buttonsPanel.add(editUserButton);
        buttonsPanel.add(removeUserButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        addUserButton.addActionListener(e -> {
            CreateUserDialog dialog = new CreateUserDialog((Frame) SwingUtilities.getWindowAncestor(this), mainApp);
            dialog.setVisible(true);
            loadUsersData(); // Update the table after adding
        });

        removeUserButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a user from the table first.", "No User Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String userIdToRemove = (String) usersTableModel.getValueAt(selectedRow, 0);
            if (currentAdmin != null && currentAdmin.getUserId().equals(userIdToRemove)) {
                JOptionPane.showMessageDialog(panel, "You cannot delete the admin account you are currently using.", "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User userToRemove = mainApp.getUsers().stream()
                    .filter(u -> u.getUserId().equals(userIdToRemove))
                    .findFirst().orElse(null);
            if (userToRemove != null) {
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete the user: " + userToRemove.getName() + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    mainApp.removeUser(userToRemove);
                    loadUsersData();
                    JOptionPane.showMessageDialog(panel, "User deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        return panel;
    }

    private JPanel createManageFlightsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("Manage Flights"));

        flightsTableModel = new DefaultTableModel(new String[]{"Flight Number", "Airline", "Origin", "Destination", "Departure", "Arrival", "Available Seats"}, 0){
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        flightsTable = new JTable(flightsTableModel);
        flightsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(flightsTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,10));
        JButton addFlightButton = new JButton("Add New Flight");
        JButton removeFlightButton = new JButton("Remove Selected Flight");
        // JButton editFlightButton = new JButton("Edit Selected Flight");

        buttonsPanel.add(addFlightButton);
        // buttonsPanel.add(editFlightButton);
        buttonsPanel.add(removeFlightButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        addFlightButton.addActionListener(e -> {
            AddEditFlightDialog dialog = new AddEditFlightDialog((Frame) SwingUtilities.getWindowAncestor(this), mainApp, null);
            dialog.setVisible(true);
            loadFlightsData();
        });

        removeFlightButton.addActionListener(e -> {
            int selectedRow = flightsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a flight from the table first.", "No Flight Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String flightNumberToRemove = (String) flightsTableModel.getValueAt(selectedRow, 0);
            Flight flightToRemove = mainApp.getFlights().stream()
                    .filter(f -> f.getFlightNumber().equals(flightNumberToRemove))
                    .findFirst().orElse(null);
            if (flightToRemove != null) {
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete flight number: " + flightToRemove.getFlightNumber() + "?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Before deleting the flight, check if there are any active bookings associated with it
                    boolean hasBookings = mainApp.getBookings().stream().anyMatch(b -> b.getFlight().getFlightNumber().equals(flightNumberToRemove) && !"Cancelled".equals(b.getStatus()));
                    if(hasBookings){
                        JOptionPane.showMessageDialog(panel, "Cannot delete the flight because there are active bookings associated with it.", "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    mainApp.removeFlight(flightToRemove);
                    loadFlightsData();
                    JOptionPane.showMessageDialog(panel, "Flight deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        return panel;
    }


    public void setCurrentAdmin(Administrator admin) {
        this.currentAdmin = admin;
        if (admin != null) {
            welcomeLabel.setText("Admin Control Panel: " + admin.getName());
            loadUsersData();
            loadFlightsData();
        } else {
            welcomeLabel.setText("Admin Control Panel");
            if(usersTableModel != null) usersTableModel.setRowCount(0);
            if(flightsTableModel != null) flightsTableModel.setRowCount(0);
        }
    }

    private void loadUsersData() {
        usersTableModel.setRowCount(0);
        List<User> allUsers = mainApp.getUsers();
        if (allUsers != null) {
            for (User user : allUsers) {
                usersTableModel.addRow(new Object[]{
                        user.getUserId(),
                        user.getUsername(),
                        user.getName(),
                        user.getClass().getSimpleName(), // Customer, Admin, Agent
                        user.getEmail()
                });
            }
        }
    }

    private void loadFlightsData() {
        flightsTableModel.setRowCount(0);
        List<Flight> allFlights = mainApp.getFlights();
        if (allFlights != null) {
            for (Flight flight : allFlights) {
                // Calculate total available seats as a simple example
                int totalAvailableSeats = flight.getAvailableSeats().values().stream().mapToInt(Integer::intValue).sum();
                flightsTableModel.addRow(new Object[]{
                        flight.getFlightNumber(),
                        flight.getAirline(),
                        flight.getOrigin(),
                        flight.getDestination(),
                        dateFormat.format(flight.getDepartureTime()),
                        dateFormat.format(flight.getArrivalTime()),
                        totalAvailableSeats
                });
            }
        }
    }

    public void resetPanel() {
        setCurrentAdmin(null);
    }
}

// --- Dialogs ---
// Each dialog should ideally be in its own .java file

class CreateUserDialog extends JDialog {
    private MainApp mainApp;
    private JTextField usernameField, nameField, emailField, contactField, addressField, preferencesField, departmentField, adminIdField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JSpinner commissionSpinner, securityLevelSpinner; // For Agent and Administrator

    public CreateUserDialog(Frame owner, MainApp mainApp) {
        super(owner, "Create / Edit User", true);
        this.mainApp = mainApp;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        // Common fields
        gbc.gridx=0; gbc.gridy=y; add(new JLabel("Role:"), gbc);
        roleComboBox = new JComboBox<>(new String[]{"Customer", "Agent", "Administrator"});
        gbc.gridx=1; gbc.gridy=y++; add(roleComboBox, gbc);

        gbc.gridx=0; gbc.gridy=y; add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx=1; gbc.gridy=y++; add(usernameField, gbc);

        gbc.gridx=0; gbc.gridy=y; add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx=1; gbc.gridy=y++; add(passwordField, gbc);

        gbc.gridx=0; gbc.gridy=y; add(new JLabel("Full Name:"), gbc);
        nameField = new JTextField(15);
        gbc.gridx=1; gbc.gridy=y++; add(nameField, gbc);

        gbc.gridx=0; gbc.gridy=y; add(new JLabel("Email:"), gbc);
        emailField = new JTextField(15);
        gbc.gridx=1; gbc.gridy=y++; add(emailField, gbc);

        gbc.gridx=0; gbc.gridy=y; add(new JLabel("Contact Info:"), gbc);
        contactField = new JTextField(15);
        gbc.gridx=1; gbc.gridy=y++; add(contactField, gbc);

        // Customer-specific fields
        JLabel addressLabel = new JLabel("Address:");
        addressField = new JTextField(15);
        JLabel preferencesLabel = new JLabel("Preferences:");
        preferencesField = new JTextField(15);

        // Agent-specific fields
        JLabel departmentLabel = new JLabel("Department/Agency:");
        departmentField = new JTextField(15);
        JLabel commissionLabel = new JLabel("Commission (0.0-1.0):");
        commissionSpinner = new JSpinner(new SpinnerNumberModel(0.1, 0.0, 1.0, 0.01));

        // Administrator-specific fields
        JLabel securityLevelLabel = new JLabel("Security Level (Number):");
        securityLevelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JLabel adminIdLabel = new JLabel("Admin ID:"); // Can be auto-generated
        adminIdField = new JTextField(15);
        adminIdField.setEnabled(false); // Usually not edited manually


        // Add dynamic fields based on the selected role
        roleComboBox.addActionListener(e -> updateDynamicFields((String)roleComboBox.getSelectedItem(), gbc));
        updateDynamicFields("Customer", gbc); // Initial setup


        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        gbc.gridx=0; gbc.gridy=GridBagConstraints.RELATIVE; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.SOUTHEAST;
        add(buttonsPanel, gbc);

        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> dispose());

        pack(); // Automatically adjust dialog size
        setLocationRelativeTo(owner);
    }

    private void updateDynamicFields(String role, GridBagConstraints gbcMaster) {
        // Remove old dynamic fields if they exist
        // This is a simple approach; a more efficient way to manage components might be needed
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel && (((JLabel)comp).getText().equals("Address:") || ((JLabel)comp).getText().equals("Preferences:") || ((JLabel)comp).getText().equals("Department/Agency:") || ((JLabel)comp).getText().equals("Commission (0.0-1.0):") || ((JLabel)comp).getText().equals("Security Level (Number):") || ((JLabel)comp).getText().equals("Admin ID:"))) {
                remove(comp);
            }
            if (comp == addressField || comp == preferencesField || comp == departmentField || comp == commissionSpinner || comp == securityLevelSpinner || comp == adminIdField) {
                remove(comp);
            }
        }

        GridBagConstraints gbc = (GridBagConstraints) gbcMaster.clone(); // Use a clone to avoid affecting the main gbc
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST; // Reset

        int dynamicFieldY = 6; // Start after the common fields

        if ("Customer".equals(role)) {
            gbc.gridx = 0; gbc.gridy = dynamicFieldY; add(new JLabel("Address:"), gbc);
            gbc.gridx = 1; gbc.gridy = dynamicFieldY++; add(addressField, gbc);
            gbc.gridx = 0; gbc.gridy = dynamicFieldY; add(new JLabel("Preferences:"), gbc);
            gbc.gridx = 1; gbc.gridy = dynamicFieldY++; add(preferencesField, gbc);
        } else if ("Agent".equals(role)) {
            gbc.gridx = 0; gbc.gridy = dynamicFieldY; add(new JLabel("Department/Agency:"), gbc);
            gbc.gridx = 1; gbc.gridy = dynamicFieldY++; add(departmentField, gbc);
            gbc.gridx = 0; gbc.gridy = dynamicFieldY; add(new JLabel("Commission (0.0-1.0):"), gbc);
            gbc.gridx = 1; gbc.gridy = dynamicFieldY++; add(commissionSpinner, gbc);
            gbc.gridx = 0; gbc.gridy = dynamicFieldY; add(new JLabel("Department/Agency:"), gbc);
            gbc.gridx = 1; gbc.gridy = dynamicFieldY++; add(departmentField, gbc);
        } else if ("Administrator".equals(role)) {
            gbc.gridx = 0; gbc.gridy = dynamicFieldY; add(new JLabel("Security Level (Number):"), gbc);
            gbc.gridx =1; gbc.gridy = dynamicFieldY++; add(securityLevelSpinner, gbc);
            gbc.gridx = 0; gbc.gridy = dynamicFieldY; add(new JLabel("Admin ID:"), gbc);
            gbc.gridx = 1; gbc.gridy = dynamicFieldY++; add(adminIdField, gbc);
        }

        revalidate();
        repaint();
        pack(); // Re-adjust dialog size after adding fields
    }


    private void saveUser() {
        String role = (String) roleComboBox.getSelectedItem();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String name = nameField.getText();
        String email = emailField.getText();
        String contactInfo = contactField.getText();

        User newUser = null;

        switch (role) {
            case "Customer":
                String address = addressField.getText();
                String preferences = preferencesField.getText();
                newUser = new Customer(UUID.randomUUID().toString(), username, password, name, email, contactInfo, address, preferences);
                break;
            case "Agent":
                String department = departmentField.getText();
                double commission = (Double) commissionSpinner.getValue();
                newUser = new Agent(UUID.randomUUID().toString(), username, password, name, email, contactInfo, department, commission);
                break;
            case "Administrator":
                int securityLevel = (Integer) securityLevelSpinner.getValue();
                String adminId = UUID.randomUUID().toString(); // Generate Admin ID automatically
                newUser = new Administrator(UUID.randomUUID().toString(), username, password, name, email, contactInfo, securityLevel, adminId);
                break;
        }

        if (newUser != null) {
            mainApp.addUser(newUser);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create user. Please check the data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


