
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class UserManagement extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, backButton;
    private User adminUser;

    public UserManagement(User adminUser) {
        this.adminUser = adminUser;
        setTitle("User Management");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        String[] columnNames = {"User ID", "Username", "Name", "Email", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0);
        userTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add User");
        deleteButton = new JButton("Delete User");
        backButton = new JButton("Back");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addNewUser());
        deleteButton.addActionListener(e -> deleteSelectedUser());
        backButton.addActionListener(e -> {
            this.dispose();
            new AdminDashboard(adminUser).setVisible(true);
        });
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try {
            List<User> users = UserDAO.loadUsers();
            for (User user : users) {
                Object[] row = {
                        user.getUserId(),
                        user.getUsername(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (UserDAO.deleteUserById(userId)) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully.");
                    loadUsers();
                } else {
                    JOptionPane.showMessageDialog(this, "User could not be deleted.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }
    }

    private void addNewUser() {
        UserRole[] roles = UserRole.values();
        UserRole selectedRole = (UserRole) JOptionPane.showInputDialog(this, "Select role:", "User Role",
                JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);

        if (selectedRole == null) return;

        String username = JOptionPane.showInputDialog(this, "Enter username:");
        String password = JOptionPane.showInputDialog(this, "Enter password:");
        String name = JOptionPane.showInputDialog(this, "Enter full name:");
        String email = JOptionPane.showInputDialog(this, "Enter email:");
        String contact = JOptionPane.showInputDialog(this, "Enter contact info:");

        String userId = UUID.randomUUID().toString();
        User user = null;

        switch (selectedRole) {
            case CUSTOMER -> {
                String address = JOptionPane.showInputDialog(this, "Enter address:");
                String preferences = JOptionPane.showInputDialog(this, "Enter preferences:");
                user = new Customer(userId, username, password, name, email, contact, address, preferences);
            }
            case AGENT -> {
                String department = JOptionPane.showInputDialog(this, "Enter department:");
                double commission = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter commission:"));
                user = new Agent(userId, username, password, name, email, contact, department, commission);
            }
            case ADMINISTRATOR -> {
                String adminId = JOptionPane.showInputDialog(this, "Enter admin ID:");
                int level = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter security level:"));
                user = new Administrator(userId, username, password, name, email, contact, level, adminId);
            }
        }

        if (user != null) {
            try {
                UserDAO.saveUser(user);
                JOptionPane.showMessageDialog(this, "User added successfully.");
                loadUsers();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error saving user: " + e.getMessage());
            }
        }
    }
}
enum UserRole {
    CUSTOMER("Customer"),
    AGENT("Agent"),
    ADMINISTRATOR("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
