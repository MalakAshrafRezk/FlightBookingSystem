import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class CustomerProfileScreen extends JFrame {
    private final Customer customer;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField contactField;
    private JTextField addressField;
    private JTextField preferencesField;

    public CustomerProfileScreen(Customer customer) {
        this.customer = customer;
        setTitle("Customer Profile");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(Color.BLUE);
        nameField = new JTextField(customer.getName());

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.BLUE);
        emailField = new JTextField(customer.getEmail());

        JLabel contactLabel = new JLabel("Contact Info:");
        contactLabel.setForeground(Color.BLUE);
        contactField = new JTextField(customer.getContactInfo());

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setForeground(Color.BLUE);
        addressField = new JTextField(customer.getAddress());

        JLabel preferencesLabel = new JLabel("Preferences:");
        preferencesLabel.setForeground(Color.BLUE);
        preferencesField = new JTextField(customer.getPreferences());

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(Color.BLUE);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(this::saveChanges);

        panel.add(nameLabel); panel.add(nameField);
        panel.add(emailLabel); panel.add(emailField);
        panel.add(contactLabel); panel.add(contactField);
        panel.add(addressLabel); panel.add(addressField);
        panel.add(preferencesLabel); panel.add(preferencesField);
        panel.add(new JLabel()); panel.add(saveButton);

        add(panel);
    }

    private void saveChanges(ActionEvent e) {
        customer.updateProfile(nameField.getText(), emailField.getText(), contactField.getText());
        customer.setAddress(addressField.getText());
        customer.setPreferences(preferencesField.getText());

        try {
            UserDAO.saveUser(customer);
            JOptionPane.showMessageDialog(this, "Profile updated successfully.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving profile: " + ex.getMessage());
        }
    }
}
