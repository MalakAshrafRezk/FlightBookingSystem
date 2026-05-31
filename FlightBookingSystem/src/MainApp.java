
// المفروض أن تكون جميع فئات المشروع (User, Customer, Admin, Agent, Flight, Booking, Passenger, FileManager, Payment etc.)
// في نفس الحزمة أو يتم استيرادها بشكل صحيح.
// امثلة للاستيراد - تأكد من تطابقها مع هيكل مشروعك
import java.io.IOException;
import java.text.SimpleDateFormat; // لاستخدامها في CustomerPanel و AdminPanel
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar; // لاستخدامها في CustomerPanel
import java.util.Date; // لاستخدامها في CustomerPanel و AdminPanel
import java.util.Map; // لاستخدامها في AdminPanel (AddEditFlightDialog)
import java.util.HashMap; // لاستخدامها في AdminPanel (AddEditFlightDialog)
import java.util.UUID; // لاستخدامها في CustomerPanel و AdminPanel و AgentPanel

import javax.swing.*;
import javax.swing.table.DefaultTableModel; // لجميع الجداول
import java.awt.*;
import java.awt.event.ActionEvent;       // قد تحتاجها في بعض اللوحات إذا لم يتم استخدام Lambdas
import java.awt.event.ActionListener;    // قد تحتاجها في بعض اللوحات إذا لم يتم استخدام Lambdas
import java.awt.event.KeyAdapter;        // لـ LoginPanel
import java.awt.event.KeyEvent;          // لـ LoginPanel


// تأكد من أن فئات المشروع مثل User, Customer, Admin, Agent, Flight, Booking, Passenger, FileManager, Payment, PaymentMethod
// CreditCardPaymentMethod, BankTransferPaymentMethod, PayPalPaymentMethod, PaymentStatus, Airline, Aircraft,
// DomesticFlight, InternationalFlight, PaymentFailedException معرفة ومتاحة.

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private CustomerPanel customerPanel;
    private AdminPanel adminPanel;
    private AgentPanel agentPanel;

    private List<User> users;
    private List<Flight> flights;
    private List<Booking> bookings;
    private List<Passenger> passengers;
    private List<Payment> payments;
    private User currentUser;

    public static final String LOGIN_PANEL = "LoginPanel";
    public static final String CUSTOMER_PANEL = "CustomerPanel";
    public static final String ADMIN_PANEL = "AdminPanel";
    public static final String AGENT_PANEL = "AgentPanel";


    public MainApp() {
        setTitle("نظام حجز الطيران");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLookAndFeel();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loadAllData();

        loginPanel = new LoginPanel(this);
        customerPanel = new CustomerPanel(this);
        adminPanel = new AdminPanel(this);
        agentPanel = new AgentPanel(this);

        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(customerPanel, CUSTOMER_PANEL);
        mainPanel.add(adminPanel, ADMIN_PANEL);
        mainPanel.add(agentPanel, AGENT_PANEL);

        add(mainPanel);
        showPanel(LOGIN_PANEL);
    }

    private void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadAllData() {
        try {
            users = FileManager.loadUsers();
            flights = FileManager.loadFlights();
            passengers = FileManager.loadPassengers();
            payments = FileManager.loadPayments();
            bookings = FileManager.loadBookings(getCustomersFromUsers(), flights, passengers, payments);

            if (users.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "لم يتم العثور على مستخدمين. قد تحتاج لإنشاء ملف users.txt مبدئيًا.",
                        "تنبيه", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "خطأ فادح في تحميل بيانات النظام: " + e.getMessage() + "\nالرجاء التأكد من وجود ملفات البيانات.",
                    "خطأ في تحميل البيانات", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveData() {
        try {
            FileManager.saveUsers(users);
            FileManager.saveFlights(flights);
            FileManager.saveBookings(bookings);
            FileManager.savePassengers(passengers);
            FileManager.savePayments(payments);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "خطأ في حفظ البيانات: " + e.getMessage(),
                    "خطأ في الحفظ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<Customer> getCustomersFromUsers() {
        List<Customer> customerList = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                if (user instanceof Customer) {
                    customerList.add((Customer) user);
                }
            }
        }
        return customerList;
    }

    public List<Agent> getAgentsFromUsers() {
        List<Agent> agentList = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                if (user instanceof Agent) {
                    agentList.add((Agent) user);
                }
            }
        }
        return agentList;
    }

    public List<Administrator> getAdministratorsFromUsers() {
        List<Administrator> adminList = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                if (user instanceof Administrator) {
                    adminList.add((Administrator) user);
                }
            }
        }
        return adminList;
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        if (LOGIN_PANEL.equals(panelName)) {
            currentUser = null;
            if(loginPanel != null) loginPanel.resetPanel(); // مسح الحقول عند العودة لشاشة الدخول
            if (customerPanel != null) customerPanel.resetPanel();
            if (adminPanel != null) adminPanel.resetPanel();
            if (agentPanel != null) agentPanel.resetPanel();
        }
    }

    // -- هذا هو السطر الذي تم تصحيحه --
    public void navigateToPanel(String panelName, User user) {
        this.currentUser = user;
        if (CUSTOMER_PANEL.equals(panelName) && user instanceof Customer) {
            customerPanel.setCurrentCustomer((Customer) user);
            cardLayout.show(mainPanel, CUSTOMER_PANEL);
        } else if (ADMIN_PANEL.equals(panelName) && user instanceof Administrator) {
            adminPanel.setCurrentAdmin((Administrator) user);
            cardLayout.show(mainPanel, ADMIN_PANEL);
        } else if (AGENT_PANEL.equals(panelName) && user instanceof Agent) {
            agentPanel.setCurrentAgent((Agent) user);
            cardLayout.show(mainPanel, AGENT_PANEL);
        } else {
            showPanel(LOGIN_PANEL);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<User> getUsers() {
        if (users == null) users = new ArrayList<>();
        return users;
    }

    public void addUser(User user) {
        if (users == null) users = new ArrayList<>();
        users.removeIf(u -> u.getUserId().equals(user.getUserId()));
        users.add(user);
        saveData();
    }

    public void removeUser(User userToRemove) {
        if (users != null && userToRemove != null) {
            users.removeIf(u -> u.getUserId().equals(userToRemove.getUserId()));
            saveData();
        }
    }

    public List<Flight> getFlights() {
        if (flights == null) flights = new ArrayList<>();
        return flights;
    }

    public void addFlight(Flight flight) {
        if (flights == null) flights = new ArrayList<>();
        flights.removeIf(f -> f.getFlightNumber().equals(flight.getFlightNumber()));
        flights.add(flight);
        saveData();
    }

    public void removeFlight(Flight flightToRemove) {
        if (flights != null && flightToRemove != null) {
            flights.removeIf(f -> f.getFlightNumber().equals(flightToRemove.getFlightNumber()));
            saveData();
        }
    }

    public List<Booking> getBookings() {
        if (bookings == null) bookings = new ArrayList<>();
        return bookings;
    }

    public List<Booking> getBookingsForCustomer(Customer customer) {
        List<Booking> customerBookings = new ArrayList<>();
        if (bookings != null && customer != null) {
            for (Booking booking : bookings) {
                if (booking.getCustomer().getUserId().equals(customer.getUserId())) {
                    customerBookings.add(booking);
                }
            }
        }
        return customerBookings;
    }

    public void addBooking(Booking booking) {
        if (bookings == null) bookings = new ArrayList<>();
        bookings.removeIf(b -> b.getBookingReference().equals(booking.getBookingReference()));
        bookings.add(booking);
        Flight bookedFlight = booking.getFlight();
        if (bookedFlight != null && booking.getPassengers() != null) {
            for(Passenger p : booking.getPassengers()){
                // يجب أن يتم تحديث المقاعد بناءً على نوع المقعد المحجوز، نفترض "Economy" كمثال
                // تأكد من أن دوال reserveSeat/releaseSeat موجودة وتعمل بشكل صحيح في فئة Flight
                try {
                    bookedFlight.reserveSeat("Economy"); // يجب أن تكون هذه الدالة موجودة في Flight
                } catch (IllegalStateException e){
                    // معالجة الحالة إذا لم تكن هناك مقاعد كافية
                    JOptionPane.showMessageDialog(this, e.getMessage(), "خطأ في حجز المقعد", JOptionPane.ERROR_MESSAGE);
                    // قد تحتاج لإلغاء الحجز هنا أو جزء منه
                    return; // إيقاف إضافة الحجز إذا فشل حجز المقعد
                }
            }
        }
        saveData();
    }

    public void cancelBooking(Booking bookingToCancel) {
        if (bookings != null && bookingToCancel != null) {
            bookings.removeIf(b -> b.getBookingReference().equals(bookingToCancel.getBookingReference()));
            Flight flightOfCancelledBooking = bookingToCancel.getFlight();
            if (flightOfCancelledBooking != null && bookingToCancel.getPassengers() != null) {
                for(Passenger p : bookingToCancel.getPassengers()){
                    flightOfCancelledBooking.releaseSeat("Economy"); // يجب أن تكون هذه الدالة موجودة في Flight
                }
            }
            saveData();
        }
    }

    public List<Passenger> getPassengers() {
        if (passengers == null) passengers = new ArrayList<>();
        return passengers;
    }

    public void addPassenger(Passenger passenger) {
        if (passengers == null) passengers = new ArrayList<>();
        passengers.add(passenger);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
    public void updateFlight(Flight oldFlight, Flight newFlight) {
        // 1. Find the old flight
        int index = flights.indexOf(oldFlight); // Assuming Flight class has a proper equals()

        // 2. Replace if found
        if (index != -1) {
            flights.set(index, newFlight);
        } else {
            System.out.println("Warning: Old flight not found in the list."); // Log or handle this
        }
    }



}