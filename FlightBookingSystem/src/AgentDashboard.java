import javax.swing.*;
import java.awt.*;

public class AgentDashboard extends JFrame {
    private Agent agent;

    public AgentDashboard(Agent agent) {
        this.agent = agent;
        setTitle("Agent Dashboard");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 1, 10, 10));
        getContentPane().setBackground(Color.WHITE);

        JButton createBookingBtn = new JButton("Create Booking for Customer");
        JButton manageFlightsBtn = new JButton("Manage Flights");
        JButton cancelBookingBtn = new JButton("Cancel Customer Booking");
        JButton manageProfileBtn = new JButton("Manage Customer Profile");
        JButton logoutBtn = new JButton("Logout");

        Font font = new Font("Arial", Font.BOLD, 16);
        for (JButton btn : new JButton[]{createBookingBtn, manageFlightsBtn, cancelBookingBtn, manageProfileBtn, logoutBtn}) {
            btn.setFont(font);
            btn.setForeground(Color.BLUE);
            add(btn);
        }

        createBookingBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open Create Booking Screen here."));
        manageFlightsBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open Manage Flights Screen here."));
        cancelBookingBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open Cancel Booking Screen here."));
        manageProfileBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open Manage Customer Profile Screen here."));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen().setVisible(true);
        });
    }
}
