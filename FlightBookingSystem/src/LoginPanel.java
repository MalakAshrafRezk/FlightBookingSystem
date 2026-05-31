import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class LoginPanel extends JPanel {

    private MainApp mainApp;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginPanel(MainApp mainApp) {
        this.mainApp = mainApp;

        setBackground(new Color(240, 245, 250));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("نظام حجز الطيران", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(50, 100, 150));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.ipady = 20;
        add(titleLabel, gbc);

        gbc.ipady = 0;
        gbc.gridwidth = 1;

        JLabel usernameLabel = new JLabel("اسم المستخدم:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("كلمة المرور:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);

        loginButton = new JButton("تسجيل الدخول");
        loginButton.setFont(new Font("Arial", Font.BOLD, 18));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(loginButton, gbc);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 10, 10);
        add(statusLabel, gbc);

        loginButton.addActionListener(e -> performLogin());

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocusInWindow();
                }
            }
        });
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("الرجاء إدخال اسم المستخدم وكلمة المرور.");
            return;
        }

        List<User> users = mainApp.getUsers();
        User loggedInUser = null;

        if (users != null) {
            for (User user : users) {
                //  user.login(username, password) هي الطريقة الأفضل إذا كانت موجودة ومعرفة بشكل صحيح في فئة User
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    loggedInUser = user;
                    break;
                }
            }
        } else {
            statusLabel.setText("خطأ: لم يتم تحميل بيانات المستخدمين.");
            return;
        }

        if (loggedInUser != null) {
            statusLabel.setText(" ");
            usernameField.setText("");
            passwordField.setText("");

            if (loggedInUser instanceof Administrator) {
                mainApp.navigateToPanel(MainApp.ADMIN_PANEL, loggedInUser);
            } else if (loggedInUser instanceof Agent) {
                mainApp.navigateToPanel(MainApp.AGENT_PANEL, loggedInUser);
            } else if (loggedInUser instanceof Customer) {
                mainApp.navigateToPanel(MainApp.CUSTOMER_PANEL, loggedInUser);
            } else {
                statusLabel.setText("نوع المستخدم غير معروف. الرجاء مراجعة المسؤول.");
            }
        } else {
            statusLabel.setText("اسم المستخدم أو كلمة المرور غير صحيحة.");
        }
    }

    public void resetPanel() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
        usernameField.requestFocusInWindow();
    }
}