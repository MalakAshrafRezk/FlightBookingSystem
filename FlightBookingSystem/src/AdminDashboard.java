
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private User adminUser;
    private JButton manageFlightsBtn, scheduleBtn, manageUsersBtn, reportsBtn, logoutBtn;

    public AdminDashboard(User adminUser) {
        this.adminUser = adminUser;
        setTitle("Administrator Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JLabel title = new JLabel("Welcome Admin: " + adminUser.getUsername(), JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));

        manageFlightsBtn = new JButton("Manage Flights");
        scheduleBtn = new JButton("View Flight Schedule");
        manageUsersBtn = new JButton("Manage Users");
        reportsBtn = new JButton("Reports");
        logoutBtn = new JButton("Logout");

        buttonPanel.add(manageFlightsBtn);
        buttonPanel.add(scheduleBtn);
        buttonPanel.add(manageUsersBtn);
        buttonPanel.add(reportsBtn);
        buttonPanel.add(logoutBtn);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Event Handlers
        manageFlightsBtn.addActionListener(e -> openFlightManagement());
        scheduleBtn.addActionListener(e -> openFlightSchedule());
        manageUsersBtn.addActionListener(e -> openUserManagement());
        reportsBtn.addActionListener(e -> openReportsViewer());
        logoutBtn.addActionListener(e -> logout());
    }

    private void openFlightManagement() {
        this.dispose();
        new FlightManagement(adminUser).setVisible(true);
    }

    private void openFlightSchedule() {
        this.dispose();
        new FlightSchedule(adminUser).setVisible(true);
    }

    private void openUserManagement() {
        JOptionPane.showMessageDialog(this, "User Management screen not implemented yet.");
    }

    private void openReportsViewer() {
        JOptionPane.showMessageDialog(this, "Reports screen not implemented yet.");
    }

    private void logout() {
        this.dispose();
        new LoginScreen().setVisible(true);
    }
}