import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardScreen extends JFrame {
    private User currentUser;

    public DashboardScreen(User user) {
        this.currentUser = user;
        setTitle("Dashboard - Welcome " + user.getName());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName(), JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        switch (currentUser.getRole()) {
            case "Administrator":
                addButton(buttonPanel, "Manage Users", new AbstractAction("Manage Users") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(DashboardScreen.this, "Open User Management Screen");
                    }
                });
                addButton(buttonPanel, "Manage Flights", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new ManageFlightsScreen().setVisible(true);
                    }
                });
                break;
            case "Agent":
                addButton(buttonPanel, "Create Booking for Customer", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new BookingScreen((Customer) currentUser).setVisible(true);
                    }
                });
                addButton(buttonPanel, "Manage Flights", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new ManageFlightsScreen().setVisible(true);
                    }
                });
                addButton(buttonPanel, "Manage Customer Profile", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new CustomerProfileScreen((Customer) currentUser).setVisible(true);
                    }
                });
                break;
            case "Customer":
                addButton(buttonPanel, "Search Flights", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new FlightSearchScreen((Customer) currentUser).setVisible(true);
                    }
                });
                addButton(buttonPanel, "Booking History", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new BookingHistoryScreen((Customer) currentUser).setVisible(true);
                    }
                });
                break;
        }

        addButton(buttonPanel, "Logout", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DashboardScreen.this.dispose();
                new LoginScreen().setVisible(true);
            }
        });

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainPanel);
    }

    private void addButton(JPanel panel, String title, ActionListener listener) {
        JButton button = new JButton(title);
        button.addActionListener(listener);
        panel.add(button);
    }
}
