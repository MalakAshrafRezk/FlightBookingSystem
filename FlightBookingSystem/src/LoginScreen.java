

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginScreen() {
        setTitle("Flight Reservation System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLUE);
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.BLUE);
        formPanel.add(userLabel);
        usernameField = new JTextField();
        formPanel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.BLUE);
        formPanel.add(passLabel);
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        loginButton = new JButton("Login");
        formPanel.add(new JLabel());
        formPanel.add(loginButton);

        panel.add(formPanel, BorderLayout.CENTER);
        add(panel);

        loginButton.addActionListener(this::handleLogin);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            List<User> users = UserDAO.loadUsers();
            for (User user : users) {
                if (user.login(username, password)) {
                    JOptionPane.showMessageDialog(this, "Login successful as: " + user.getRole());
                    openDashboardForRole(user);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error accessing database: " + ex.getMessage());
        }
    }

    private void openDashboardForRole(User user) {
        this.dispose();
        String role = user.getRole();
        switch (role) {
            case "Administrator" -> new AdminDashboard((Administrator) user).setVisible(true);
            case "Agent" -> new AgentDashboard((Agent) user).setVisible(true);
            case "Customer" -> new CustomerDashboard((Customer) user).setVisible(true);
            default -> JOptionPane.showMessageDialog(this, "Unknown user role: " + role);
        }
    }
}
