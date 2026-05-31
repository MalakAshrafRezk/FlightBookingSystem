import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

// Assuming FileManager, User, Flight, Passenger, Booking, Customer, Agent, Administrator, Payment, PaymentMethod,
// DomesticFlight, InternationalFlight (if used by FileManager) classes are defined elsewhere in the project.
// Assuming the necessary DAO or file-based loading/saving methods exist in FileManager with matching signatures.

// Helper class for logging GUI events (can be in a separate file, but included here for completeness)
class GUILogger {
    private static final String LOG_FILE = "gui_events.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String event) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String message = timestamp + " - " + event;
        System.out.println(message);
        try (java.io.FileWriter fw = new java.io.FileWriter(LOG_FILE, true)) {
            fw.write(message + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Main GUI class for the Flight Reservation System
class FlightReservationGUI { // Make the class public
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Shared fields
    private JTextField usernameField;
    private JPasswordField passwordField;
    private User currentUser;

    // Data models (loaded from/saved to files)
    private List<User> users;
    private List<Flight> flights;
    private List<Passenger> pax; // List of all passengers ever created
    private List<Booking> bookings;
    private List<Payment> payments; // List of all payments

    // Table models for displaying data
    private DefaultTableModel usersModel;
    private DefaultTableModel agentsModel;
    private DefaultTableModel flightSearchModel;
    private DefaultTableModel bookingModel;
    private DefaultTableModel adminFlightsModel; // Table model for Admin flight management
    private DefaultTableModel agentFlightsModel; // Table model for Agent flight viewing
    private DefaultTableModel scheduleModel; // Table model for Agent schedule

    public FlightReservationGUI() throws Exception {
        // Load data via FileManager as per project spec (Source 2)
        // Ensure FileManager, User, Flight, Passenger, Booking, Customer classes are available
        // Add a check for null return or handle potential exceptions from FileManager
        try {
            users = FileManager.loadUsers();
            flights = FileManager.loadFlights();
            pax = FileManager.loadPassengers();
            // Load payments BEFORE bookings, as bookings might reference payments (consistent with loadBookings signature Source 357)
            payments = FileManager.loadPayments(); // ADDED: Load payments
            // Assuming FileManager.loadBookings can handle the list of customers, flights, pax, and payments (Source 357)
            bookings = FileManager.loadBookings(
                    users.stream().filter(u -> u instanceof Customer)
                            .map(u -> (Customer) u).collect(Collectors.toList()),
                    flights, pax, payments); // MODIFIED: Pass the loaded payments list

        } catch (Exception e) {
            System.err.println("Error loading initial data: " + e.getMessage());
            e.printStackTrace();
            // Initialize lists even if loading fails to prevent NullPointerException later
            users = new ArrayList<>();
            flights = new ArrayList<>();
            pax = new ArrayList<>();
            bookings = new ArrayList<>();
            payments = new ArrayList<>(); // Initialize payments list here too
            // Consider showing an error message dialog to the user here
            JOptionPane.showMessageDialog(null, "Failed to load application data: " + e.getMessage(), "Data Loading Error", JOptionPane.ERROR_MESSAGE);
        }

        frame = new JFrame("Flight Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initLoginScreen();
        initAdminScreen();
        initManageUsers();
        initManageAgents(); // Modified to only view agents
        initAdminManageFlights(); // New screen for Admin flight management
        initCustomerScreen();
        initFlightSearch();
        initCustomerBookings();
        initAgentScreen();
        initAgentFlights(); // Agent screen to view flights only
        initAgentSchedule(); // Agent schedule screen

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        showScreen("Login");
    }

    // Method to switch between different GUI panels
    private void showScreen(String name) {
        cardLayout.show(mainPanel, name);
        // Optional: Log screen changes
        GUILogger.log("Switched to screen: " + name);

        // Refresh data when certain screens are shown to ensure tables are up-to-date
        if ("ManageUsers".equals(name)) {
            refreshUsers();
        } else if ("ManageAgents".equals(name)) {
            refreshAgents();
        } else if ("AdminManageFlights".equals(name)) {
            refreshAdminFlights();
        } else if ("Bookings".equals(name)) {
            loadCustomerBookings(); // This method already includes showing the screen
        } else if ("AgentFlights".equals(name)) {
            refreshAgentFlights(); // Ensure flights are fresh when agent views them
        } else if ("AgentSchedule".equals(name)) {
            loadSchedule(); // Ensure schedule is fresh when agent views it
        }
        // Note: refreshAgentFlights and loadSchedule are also called at screen init
    }


    private void initLoginScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));
        form.add(new JLabel("Username:"));
        usernameField = new JTextField();
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        form.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> doLogin());
        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> { usernameField.setText(""); passwordField.setText(""); });
        form.add(loginBtn);
        form.add(resetBtn);

        panel.add(form, BorderLayout.CENTER);
        mainPanel.add(panel, "Login");
    }

    private void doLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword()).trim();
        GUILogger.log("Login attempt for user: " + u);
        Optional<User> match = users.stream().filter(x -> x.login(u, p)).findFirst();
        if (!match.isPresent()) {
            JOptionPane.showMessageDialog(frame, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            GUILogger.log("Login failed for user: " + u);
            return;
        }
        currentUser = match.get();
        GUILogger.log("Login successful for user: " + u + " (" + currentUser.getRole() + ")");

        // Clear fields after successful login
        usernameField.setText("");
        passwordField.setText("");

        // Navigate to the appropriate dashboard based on user role
        if (currentUser instanceof Administrator) showScreen("Admin");
        else if (currentUser instanceof Agent) showScreen("Agent");
        else if (currentUser instanceof Customer) showScreen("Customer"); // Add Customer login
        else { // Handle unknown role - should not happen with current user types
            JOptionPane.showMessageDialog(frame, "Unknown user role.", "Error", JOptionPane.ERROR_MESSAGE);
            currentUser = null; // Clear current user if role is unknown
            showScreen("Login"); // Return to login
        }
    }

    private void initAdminScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(2, 3, 20, 20));
        buttons.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        // Changed "Manage Flights" to direct Admin to their specific flight management screen (consistent with GUI structure)
        String[] names = {"Manage Users","Manage Agents","Manage Flights","Settings","Reports","Logout"};
        for (String name : names) {
            JButton btn = new JButton(name);
            btn.addActionListener(e -> {
                GUILogger.log("Admin action: " + name);
                if (name.equals("Manage Users")) {
                    showScreen("ManageUsers"); // refreshUsers() called in showScreen
                } else if (name.equals("Manage Agents")) {
                    showScreen("ManageAgents"); // refreshAgents() called in showScreen
                } else if (name.equals("Manage Flights")) {
                    showScreen("AdminManageFlights"); // refreshAdminFlights() called in showScreen
                }
                // TODO: Implement actions for Settings and Reports buttons based on PDF tasks (Source 9)
                else if (name.equals("Settings")) {
                    // Based on Administrator.modifySystemSettings() in PDF (Source 9, 273-281)
                    JOptionPane.showMessageDialog(frame, "Modify System Settings functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } else if (name.equals("Reports")) {
                    // PDF mentions Admin reports but doesn't detail GUI for it.
                    JOptionPane.showMessageDialog(frame, "View Reports functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
                else if (name.equals("Logout")) {
                    currentUser = null; // Clear current user on logout (Source 8, 216-217, 299)
                    showScreen("Login");
                }
            });
            buttons.add(btn);
        }
        panel.add(buttons, BorderLayout.CENTER);
        mainPanel.add(panel, "Admin");
    }

    private void initManageUsers() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("User Management", SwingConstants.CENTER), BorderLayout.NORTH);
        usersModel = new DefaultTableModel(new String[]{"ID","Name","Email","Role","Status"},0);
        JTable table = new JTable(usersModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel ctrl = new JPanel(new FlowLayout());
        JButton add = new JButton("Add User");
        add.addActionListener(e -> {
            // TODO: Implement GUI logic to collect user details (role, name, etc.)
            // and then create the appropriate User object (Customer, Agent, Administrator)
            // and add it to the 'users' list and save. This replaces Administrator.createUser (Source 9, 260-273).
            JOptionPane.showMessageDialog(frame, "Add User functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
            // After adding user via GUI, you would refresh and save:
            // refreshUsers();
            // saveUsers();
        });
        JButton del = new JButton("Delete"); del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                if (currentUser instanceof Administrator) {
                    String userIdToDelete = (String) usersModel.getValueAt(r, 0); // Get ID from table model

                    // Prevent deleting the currently logged-in admin or potentially the last admin
                    if (currentUser != null && currentUser.getUserId().equals(userIdToDelete)) {
                        JOptionPane.showMessageDialog(frame, "Cannot delete the currently logged-in user.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    // TODO: Add check if this is the last admin and prevent deletion if necessary

                    User userToDelete = users.stream()
                            .filter(u -> u.getUserId().equals(userIdToDelete))
                            .findFirst()
                            .orElse(null);

                    if (userToDelete != null) {
                        if (users.remove(userToDelete)) { // Remove from the list
                            usersModel.removeRow(r); // Remove from the table model
                            saveUsers(); // Save the changes (Source 8, 273, 284)
                            JOptionPane.showMessageDialog(frame, "User deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            GUILogger.log("Admin deleted user: " + userIdToDelete);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Error removing user from list.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Selected user not found in data.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "You do not have permission to delete users.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Select a user to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton back = new JButton("Back"); back.addActionListener(e -> showScreen("Admin"));
        ctrl.add(add); ctrl.add(del); ctrl.add(back);
        panel.add(ctrl, BorderLayout.SOUTH);
        mainPanel.add(panel,"ManageUsers");
        // refreshUsers() called when showing screen via showScreen()
    }
    private void refreshUsers() {
        usersModel.setRowCount(0);
        if (users == null) return; // Prevent NullPointerException
        for (User u : users) {
            // Assuming getAccessLevelString provides a role like "Admin", "Agent", "Customer" (Source 228-229)
            // Assuming getRole() returns the role string (Source 214-215)
            usersModel.addRow(new Object[]{u.getUserId(),u.getName(),u.getEmail(),u.getRole(),u.getAccessLevelString()});
        }
    }
    private void saveUsers() {
        try {
            FileManager.saveUsers(users); // (Source 8, 273, 284)
        } catch(IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving user data.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initManageAgents() {
        JPanel panel = new JPanel(new BorderLayout());
        // Changed title as this screen will now only view agents (consistent with GUI structure)
        panel.add(new JLabel("View Agents", SwingConstants.CENTER), BorderLayout.NORTH);
        agentsModel = new DefaultTableModel(new String[]{"ID","Name","Agency","Commission","Status"},0);
        JTable table = new JTable(agentsModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel ctrl = new JPanel(new FlowLayout());
        // Removed Add and Delete buttons from here, as user management (including agents) is in Manage Users screen
        JButton back = new JButton("Back"); back.addActionListener(e->showScreen("Admin"));
        ctrl.add(back); // Only back button remains
        panel.add(ctrl,BorderLayout.SOUTH);
        mainPanel.add(panel,"ManageAgents");
        // refreshAgents() called when showing screen via showScreen()
    }
    private void refreshAgents() {
        agentsModel.setRowCount(0);
        if (users == null) return; // Prevent NullPointerException
        for (User u : users) {
            if (u instanceof Agent) {
                Agent a = (Agent)u;
                // Assuming getDepartment is the agency name and getCommission exists in Agent class (Source 141, 149, 321)
                agentsModel.addRow(new Object[]{a.getUserId(),a.getName(),a.getDepartment(),a.getCommission(),a.getAccessLevelString()});
            }
        }
    }
    // saveUsers() call removed from here as adding/deleting agents is handled in ManageUsers

    // New screen for Admin flight management
    private JTable adminFlightTable;
    private void initAdminManageFlights() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Admin Manage Flights", SwingConstants.CENTER), BorderLayout.NORTH);
        adminFlightsModel = new DefaultTableModel(new String[]{"No","Origin","Dest","Dep","Arr","Seats"},0);
        adminFlightTable = new JTable(adminFlightsModel);
        panel.add(new JScrollPane(adminFlightTable), BorderLayout.CENTER);
        JPanel ctrl = new JPanel(new FlowLayout());

        JButton add = new JButton("Add Flight");
        add.addActionListener(e -> {
            // TODO: Implement GUI logic to collect flight details (number, origin, etc.)
            // and then create a Flight object and add it to the 'flights' list and save.
            // This replaces the console-based Administrator.addFlight(flights) call (Source 9, 285-296).
            JOptionPane.showMessageDialog(frame, "Add Flight functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
            // After adding flight via GUI, you would refresh and save:
            // refreshAdminFlights();
            // saveFlights();
        });

        JButton del = new JButton("Delete Flight");
        del.addActionListener(e -> {
            int r = adminFlightTable.getSelectedRow();
            if(r >= 0){
                if (currentUser instanceof Administrator) {
                    String flightNumberToDelete = (String) adminFlightsModel.getValueAt(r, 0); // Get Flight No from table model
                    Flight flightToDelete = flights.stream()
                            .filter(f -> f.getFlightNumber().equals(flightNumberToDelete))
                            .findFirst()
                            .orElse(null);

                    if (flightToDelete != null) {
                        if (flights.remove(flightToDelete)) { // Remove from the list
                            adminFlightsModel.removeRow(r); // Remove from the table model
                            saveFlights(); // Save the changes (Source 27, 295, 298)
                            // TODO: In a real system, you might also need to handle deleting bookings associated with this flight
                            JOptionPane.showMessageDialog(frame, "Flight " + flightToDelete.getFlightNumber() + " deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            GUILogger.log("Admin deleted flight: " + flightNumberToDelete);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Error removing flight from list.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Selected flight not found in data.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "You do not have permission to delete flights.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(frame, "Select a flight to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton back = new JButton("Back");
        back.addActionListener(e -> showScreen("Admin"));

        ctrl.add(add);
        ctrl.add(del);
        ctrl.add(back);
        panel.add(ctrl, BorderLayout.SOUTH);
        mainPanel.add(panel, "AdminManageFlights");
        // refreshAdminFlights() called when showing screen via showScreen()
    }

    // Helper method to format Date for display in tables
    private String formatDateForDisplay(Date date) {
        if (date == null) return "N/A";
        // Use a format suitable for table display
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }


    private void refreshAdminFlights() {
        adminFlightsModel.setRowCount(0);
        if (flights == null) return; // Prevent NullPointerException
        for(Flight f:flights) {
            // Assuming getFlightNumber, getOrigin, getDestination, getDepartureTime, getArrivalTime, getAvailableSeats exist in Flight (Source 49-50, 65-68)
            // Assuming getAvailableSeats returns a Map and "economy" key exists (Source 50, 67)
            Integer economySeats = f.getAvailableSeats().get("economy");
            adminFlightsModel.addRow(new Object[]{f.getFlightNumber(),f.getOrigin(),f.getDestination(),
                    formatDateForDisplay(f.getDepartureTime()), // Format date
                    formatDateForDisplay(f.getArrivalTime()), // Format date
                    economySeats != null ? economySeats : 0});
        }
    }

    private void saveFlights() {
        try {
            FileManager.saveFlights(flights); // (Source 27, 295, 298)
        } catch(IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving flight data.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void initCustomerScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Customer Dashboard", SwingConstants.CENTER), BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(2,2,20,20));
        buttons.setBorder(BorderFactory.createEmptyBorder(50,100,50,100));
        JButton search = new JButton("Search Flights"); search.addActionListener(e->showScreen("Search"));
        JButton book = new JButton("My Bookings"); book.addActionListener(e->loadCustomerBookings()); // loadCustomerBookings shows the screen
        // TODO: Add "Update Profile" button functionality based on User/Customer.updateProfile task (Source 217-221, 39)
        JButton updateProfileBtn = new JButton("Update Profile");
        updateProfileBtn.addActionListener(e -> {
            // TODO: Implement GUI logic to collect updated profile details (name, email, contact, address, preferences, etc.)
            // and call currentUser.updateProfile(...) or a specific Customer method, then save users.
            if (!(currentUser instanceof Customer)) {
                JOptionPane.showMessageDialog(frame, "Access denied.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(frame, "Update Profile functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
            // After updating profile via GUI, you would save:
            // saveUsers();
        });


        JButton lo = new JButton("Logout"); lo.addActionListener(e->{
            currentUser = null; // Clear current user on logout
            showScreen("Login");
        });

        buttons.add(search);
        buttons.add(book);
        buttons.add(updateProfileBtn); // Add the new button here
        buttons.add(lo); // Logout button remains here

        panel.add(buttons,BorderLayout.CENTER);
        mainPanel.add(panel,"Customer");
    }

    private JTable flightTable;
    private void initFlightSearch() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Flight Search", SwingConstants.CENTER), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(3,4,10,10)); form.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JTextField from = new JTextField(), to = new JTextField(), date = new JTextField();
        form.add(new JLabel("From:")); form.add(from);
        form.add(new JLabel("To:")); form.add(to);
        form.add(new JLabel("Date (yyyy-MM-dd):")); form.add(date);
        JButton btn = new JButton("Search"); btn.addActionListener(e->doFlightSearch(from.getText(),to.getText(),date.getText())); form.add(btn);
        panel.add(form,BorderLayout.NORTH);
        flightSearchModel = new DefaultTableModel(new String[]{"No","From","To","Dep","Arr","Price"},0);
        flightTable = new JTable(flightSearchModel);
        panel.add(new JScrollPane(flightTable),BorderLayout.CENTER);
        JPanel ctrl = new JPanel(new FlowLayout());
        JButton bookBtn = new JButton("Book"); bookBtn.addActionListener(e->doFlightBooking());
        JButton back = new JButton("Back"); back.addActionListener(e->showScreen("Customer"));
        ctrl.add(bookBtn); ctrl.add(back);
        panel.add(ctrl,BorderLayout.SOUTH);
        mainPanel.add(panel,"Search");
    }
    private void doFlightSearch(String origin, String dest, String dt) {
        flightSearchModel.setRowCount(0);
        if (!(currentUser instanceof Customer)) {
            JOptionPane.showMessageDialog(frame, "Only customers can search for flights.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Use SimpleDateFormat with strict parsing
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false); // Disallow lenient date parsing
            Date d = dateFormat.parse(dt);

            // Calling the searchFlights method from the Customer class in the PDF (Source 2, 11, 234-236)
            List<Flight> res = ((Customer)currentUser).searchFlights(flights, origin, dest, d);
            for (Flight f : res) {
                // Assuming getDepartureTime, getArrivalTime, calculateTotalPrice exist in Flight (Source 61-62, 65-68)
                // Assuming "economy" class exists for price calculation (Source 57, 61)
                // Format date/time for display if needed, currently using Date object directly
                flightSearchModel.addRow(new Object[]{f.getFlightNumber(),f.getOrigin(),f.getDestination(),
                        f.getDepartureTime(),f.getArrivalTime(), f.calculateTotalPrice("economy")}); // Assuming "economy" class
            }
            if(res.isEmpty()) JOptionPane.showMessageDialog(frame,"No flights found for the given criteria.");
            GUILogger.log("Customer search: From=" + origin + ", To=" + dest + ", Date=" + dt + " found " + res.size() + " flights.");
        } catch(ParseException e) {
            JOptionPane.showMessageDialog(frame,"Invalid date format. Please use yyyy-MM-dd.", "Date Format Error", JOptionPane.ERROR_MESSAGE);
            GUILogger.log("Customer search failed: Invalid date format '" + dt + "'");
        } catch (Exception e) { // Catch other potential exceptions from searchFlights
            JOptionPane.showMessageDialog(frame,"An error occurred during flight search: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            GUILogger.log("Customer search failed: " + e.getMessage());
        }
    }
    private void doFlightBooking() {
        int r = flightTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(frame, "Select a flight to book.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!(currentUser instanceof Customer)) {
            JOptionPane.showMessageDialog(frame, "Only customers can book flights.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String flightNo = (String)flightSearchModel.getValueAt(r,0);
        Flight sel = flights.stream().filter(f->f.getFlightNumber().equals(flightNo)).findFirst().orElse(null);
        if(sel!=null) {
            // Simple one-passenger booking for now.
            // TODO: Implement GUI for collecting passenger details if multiple passengers are allowed,
            // including name, passport, DOB, requests.
            // Assuming Passenger constructor takes id, name, passport, dob, requests (Source 16, 17)
            // Assuming createBooking method exists in Customer and works with Flight and List<Passenger> (Source 2, 17, 236-237)
            try {
                // Create a basic passenger using current user's name; passport/dob/requests are placeholders for GUI implementation
                // In a real GUI, you would prompt the user for passenger details here.
                Passenger passenger = new Passenger(UUID.randomUUID().toString(), currentUser.getName(), "N/A", new Date(), "");
                List<Passenger> passengerList = Collections.singletonList(passenger);

                // Check if flight has availability for the specific class being booked (assuming economy for now)
                if (sel.getAvailableSeats().getOrDefault("economy", 0) <= 0) {
                    JOptionPane.showMessageDialog(frame, "No available seats in economy class on this flight.", "Booking Failed", JOptionPane.WARNING_MESSAGE);
                    GUILogger.log("Booking failed for flight " + flightNo + ": No economy seats available.");
                    return;
                }

                // Calling the createBooking method from the Customer class in the PDF (Source 2, 17, 236-237)
                // This method is assumed to handle reserving the seat(s) within the Flight object per PDF (Source 236)
                Booking b = ((Customer)currentUser).createBooking(sel, passengerList);

                // Add the new booking and the passenger(s) to the main lists
                bookings.add(b);
                // Add new passengers to the main pax list IF they are not already there.
                // Simple addAll might add duplicates if a passenger flies multiple times.
                // A more robust approach would check if passenger already exists in pax list.
                // For now, addAll is acceptable based on the simple Passenger creation above.
                pax.addAll(passengerList); // ADDED based on PDF console logic (Source 17)

                // TODO: Implement payment processing logic here based on PDF (Source 18-22)
                // After successful payment, update booking status and save payments.
                // For now, we assume payment is pending or handled later and save the data.

                saveBookings(); // (Source 3, 5, 26)
                saveFlights(); // Save flights because seats were reserved by createBooking (Source 27, 295, 298)
                savePassengers(); // Save the updated passenger list // ADDED (Source 17, 23)
                savePayments(); // Save payments list (even if payment status is pending) // ADDED

                // Refresh the bookings screen after booking
                loadCustomerBookings(); // loadCustomerBookings shows the screen

                JOptionPane.showMessageDialog(frame, "Booking successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                GUILogger.log("Booking successful: Reference=" + b.getBookingReference() + ", Flight=" + flightNo + ", Customer=" + currentUser.getUserId());
            } catch (Exception e) { // Catch potential exceptions during booking creation or seat reservation
                JOptionPane.showMessageDialog(frame, "An error occurred during booking: " + e.getMessage(), "Booking Failed", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                GUILogger.log("Booking failed for flight " + flightNo + ": " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Selected flight not found in data.", "Error", JOptionPane.ERROR_MESSAGE);
            GUILogger.log("Booking failed: Selected flight " + flightNo + " not found.");
        }
    }

    private void savePassengers() {
        try {
            FileManager.savePassengers(pax); // Added method to save passengers (Source 17, 23)
        } catch(IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving passenger data.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePayments() {
        try {
            FileManager.savePayments(payments); // Added method to save payments (Source 305-308)
        } catch(IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving payment data.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private JTable bookingTable;
    private void initCustomerBookings() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("My Bookings", SwingConstants.CENTER),BorderLayout.NORTH);
        bookingModel = new DefaultTableModel(new String[]{"ID","Flight","Date","Status"},0);
        bookingTable = new JTable(bookingModel);
        panel.add(new JScrollPane(bookingTable),BorderLayout.CENTER);
        JPanel ctrl = new JPanel(new FlowLayout());
        JButton cancel = new JButton("Cancel"); cancel.addActionListener(e->cancelBooking());
        JButton back = new JButton("Back"); back.addActionListener(e->showScreen("Customer"));
        ctrl.add(cancel); ctrl.add(back); panel.add(ctrl,BorderLayout.SOUTH);
        mainPanel.add(panel,"Bookings");
    }
    private void loadCustomerBookings() {
        bookingModel.setRowCount(0);
        if (!(currentUser instanceof Customer)) {
            // This shouldn't happen if reached from Customer dashboard, but defensive check
            JOptionPane.showMessageDialog(frame, "Access denied.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            showScreen("Login"); // Or appropriate screen
            return;
        }
        // Assuming getCustomer() and getBookingReference(), getFlight(), getDepartureTime(), getStatus() exist in Booking (Source 246-252)
        if (bookings == null) return; // Prevent NullPointerException if bookings failed to load
        for(Booking b: bookings) {
            // Check if currentUser is a Customer and if booking belongs to this customer
            if(currentUser instanceof Customer && b.getCustomer().equals(currentUser)) {
                // Format date/time for display
                bookingModel.addRow(new Object[]{b.getBookingReference(),b.getFlight().getFlightNumber(),
                        formatDateForDisplay(b.getFlight().getDepartureTime()), // Format date
                        b.getStatus()});
            }
        }
        showScreen("Bookings"); // Show the screen after loading
        GUILogger.log("Customer " + currentUser.getUserId() + " viewing bookings. Found " + bookingModel.getRowCount() + " bookings.");
    }
    private void cancelBooking() {
        int r = bookingTable.getSelectedRow();
        if(r < 0) {
            JOptionPane.showMessageDialog(frame, "Select a booking to cancel.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!(currentUser instanceof Customer)) {
            JOptionPane.showMessageDialog(frame, "Only customers can cancel bookings.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String)bookingModel.getValueAt(r,0);
        // Find the booking in the main bookings list
        Booking toRemove = bookings.stream().filter(b->b.getBookingReference().equals(id)).findFirst().orElse(null);

        // Assuming getCustomer() and getFlight() exist in Booking (Source 247, 248), and releaseSeat() exists in Flight (Source 70-71)
        // Check if the booking exists, belongs to the current user, and hasn't been cancelled already (optional check)
        if(toRemove!=null && currentUser instanceof Customer && toRemove.getCustomer().equals(currentUser) /* && !"Cancelled".equals(toRemove.getStatus()) */) {
            try {
                // Release seats for all passengers in the booking
                // Assuming getPassengers() exists in Booking (Source 248)
                for(Passenger p : toRemove.getPassengers()) {
                    // Assuming booking was for economy class - adjust if different classes are handled
                    toRemove.getFlight().releaseSeat("economy"); // Release seat per PDF Customer.cancelBooking logic (Source 240)
                }

                // Remove the booking from the main list
                bookings.remove(toRemove);

                // Save the changes
                saveBookings(); // (Source 3, 5, 26)
                saveFlights(); // Save flights because seats were released (Source 27, 295, 298)
                // Saving passengers and payments is typically not needed on cancellation unless their status changes,
                // which is not implied by the PDF's console cancelBooking logic.

                // Refresh the bookings screen after cancellation
                loadCustomerBookings(); // loadCustomerBookings shows the screen

                JOptionPane.showMessageDialog(frame, "Booking cancelled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                GUILogger.log("Customer " + currentUser.getUserId() + " cancelled booking: " + id);
            } catch (Exception e) { // Catch potential exceptions during seat release or saving
                JOptionPane.showMessageDialog(frame, "An error occurred during cancellation: " + e.getMessage(), "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                GUILogger.log("Cancellation failed for booking " + id + ": " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Selected booking not found or does not belong to you.", "Error", JOptionPane.ERROR_MESSAGE);
            GUILogger.log("Cancellation failed: Booking " + id + " not found or does not belong to " + currentUser.getUserId());
        }
    }

    private void initAgentScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Agent Dashboard", SwingConstants.CENTER),BorderLayout.NORTH);
        JPanel b = new JPanel(new GridLayout(3,2,20,20)); // Increased rows for new buttons
        b.setBorder(BorderFactory.createEmptyBorder(50,100,50,100));

        // "View Flights" and "Schedule" are GUI implementations replacing the single "Manage Flights" from PDF console (Source 5)
        JButton viewFlights = new JButton("View Flights");
        viewFlights.addActionListener(e->showScreen("AgentFlights"));

        JButton sch = new JButton("Schedule"); sch.addActionListener(e->showScreen("AgentSchedule")); // showScreen calls loadSchedule

        // TODO: Add "Book for Customer" button functionality based on Agent.handleAgentBooking task (Source 5, 23-26)
        JButton bookForCustomerBtn = new JButton("Book for Customer");
        bookForCustomerBtn.addActionListener(e -> {
            // TODO: Implement GUI logic for agent to book for a customer
            if (!(currentUser instanceof Agent)) {
                JOptionPane.showMessageDialog(frame, "Access denied.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(frame, "Book for Customer functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        // TODO: Add "Cancel Customer Booking" button functionality based on Agent.cancelBookingForCustomer task (Source 5, 3-5)
        JButton cancelCustomerBookingBtn = new JButton("Cancel Customer Booking");
        cancelCustomerBookingBtn.addActionListener(e -> {
            // TODO: Implement GUI logic for agent to cancel a customer's booking
            if (!(currentUser instanceof Agent)) {
                JOptionPane.showMessageDialog(frame, "Access denied.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(frame, "Cancel Customer Booking functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        // TODO: Add "Manage Customer Profile" button functionality based on Agent.manageCustomerProfile task (Source 6-7)
        JButton manageCustomerProfileBtn = new JButton("Manage Customer Profile");
        manageCustomerProfileBtn.addActionListener(e -> {
            // TODO: Implement GUI logic for agent to manage customer profiles
            if (!(currentUser instanceof Agent)) {
                JOptionPane.showMessageDialog(frame, "Access denied.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(frame, "Manage Customer Profile functionality needs GUI implementation.", "Info", JOptionPane.INFORMATION_MESSAGE);
            // After managing profile via GUI, you would save users:
            // saveUsers();
        });


        JButton lo = new JButton("Logout"); lo.addActionListener(e->{
            currentUser = null; // Clear current user on logout
            showScreen("Login");
        });

        b.add(viewFlights); // Add the button here
        b.add(sch); // Add the schedule button here
        b.add(bookForCustomerBtn); // Add the new button
        b.add(cancelCustomerBookingBtn); // Add the new button
        b.add(manageCustomerProfileBtn); // Add the new button
        b.add(lo); // Logout button remains here

        panel.add(b,BorderLayout.CENTER); mainPanel.add(panel,"Agent");
    }
    private JTable agentFlightTable;
    private void initAgentFlights() {
        JPanel panel = new JPanel(new BorderLayout());
        // Change title to reflect this screen is for viewing flights only for the Agent (consistent with GUI structure)
        panel.add(new JLabel("View Flights (Agent)", SwingConstants.CENTER),BorderLayout.NORTH);
        agentFlightsModel = new DefaultTableModel(new String[]{"No","Origin","Dest","Dep","Arr","Seats"},0);
        agentFlightTable = new JTable(agentFlightsModel);
        panel.add(new JScrollPane(agentFlightTable),BorderLayout.CENTER);
        JPanel ctrl = new JPanel(new FlowLayout());
        // Removed Add and Delete buttons from here as they are Admin tasks in the PDF
        JButton back = new JButton("Back"); back.addActionListener(e->showScreen("Agent"));
        ctrl.add(back); // Only back button remains
        panel.add(ctrl,BorderLayout.SOUTH);
        mainPanel.add(panel,"AgentFlights");
        // refreshAgentFlights() called when screen is initialized or shown via showScreen()
    }
    private void refreshAgentFlights() {
        agentFlightsModel.setRowCount(0);
        if (flights == null) return; // Prevent NullPointerException
        for(Flight f:flights) {
            // Assuming getFlightNumber, getOrigin, getDestination, getDepartureTime, getArrivalTime, getAvailableSeats exist in Flight (Source 49-50, 65-68)
            // Assuming getAvailableSeats returns a Map and "economy" key exists (Source 50, 67)
            Integer economySeats = f.getAvailableSeats().get("economy");
            // Format date/time for display
            agentFlightsModel.addRow(new Object[]{f.getFlightNumber(),f.getOrigin(),f.getDestination(),
                    formatDateForDisplay(f.getDepartureTime()), // Format date
                    formatDateForDisplay(f.getArrivalTime()), // Format date
                    economySeats != null ? economySeats : 0});
        }
    }

    private JTable scheduleTable;
    private void initAgentSchedule() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Agent Schedule", SwingConstants.CENTER),BorderLayout.NORTH);
        scheduleModel = new DefaultTableModel(new String[]{"No","Origin","Dest","Dep","Arr"},0);
        scheduleTable = new JTable(scheduleModel);
        panel.add(new JScrollPane(scheduleTable),BorderLayout.CENTER);
        JPanel ctrl = new JPanel(new FlowLayout());
        // Schedule is view-only in this GUI implementation
        JButton back = new JButton("Back"); back.addActionListener(e->showScreen("Agent"));
        ctrl.add(back);
        panel.add(ctrl,BorderLayout.SOUTH);
        mainPanel.add(panel,"AgentSchedule");
        // loadSchedule() called when screen is initialized or shown via showScreen()
    }

    private void loadSchedule() {
        if (!(currentUser instanceof Agent)) {
            // This shouldn't happen if reached from Agent dashboard, but defensive check
            JOptionPane.showMessageDialog(frame, "Access denied.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            showScreen("Login"); // Or appropriate screen
            return;
        }
        scheduleModel.setRowCount(0);
        if (flights == null) return; // Prevent NullPointerException

        // This is a basic schedule showing all flights.
        // A more complex schedule might filter flights based on agent assignment
        // or display upcoming bookings handled by the agent.
        for(Flight f : flights) {
            // Format date/time for display
            scheduleModel.addRow(new Object[]{f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                    formatDateForDisplay(f.getDepartureTime()), // Format date
                    formatDateForDisplay(f.getArrivalTime())}); // Format date
        }
        GUILogger.log("Agent " + currentUser.getUserId() + " viewing schedule. Loaded " + scheduleModel.getRowCount() + " flights.");
    }


    // Helper method to save all bookings to file
    private void saveBookings() {
        try {
            FileManager.saveBookings(bookings); // (Source 3, 5, 26)
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving booking data.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Main method to run the GUI - This would be the entry point of your application
    public static void main(String[] args) {
        // It's good practice to initialize database/files once at application startup
        // Assuming DatabaseHelper.initializeDatabase() or similar exists
        // DatabaseHelper.initializeDatabase(); // Example call if using DB

        SwingUtilities.invokeLater(() -> {
            try {
                new FlightReservationGUI();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to start the application: " + e.getMessage(), "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}