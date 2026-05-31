import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException; // تم اضافته
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CustomerPanel extends JPanel {

    private MainApp mainApp;
    private Customer currentCustomer;
    private JLabel welcomeLabel;
    private JTable flightsTable;
    private DefaultTableModel flightsTableModel;
    private JTable bookingsTable;
    private DefaultTableModel bookingsTableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // حقول تعديل الملف الشخصي - جعلها أعضاء في الفئة للوصول إليها من setCurrentCustomer
    private JTextField profileNameField, profileEmailField, profileContactField, profileAddressField, profilePreferencesField;
    private JPasswordField profileCurrentPasswordField, profileNewPasswordField, profileConfirmNewPasswordField;


    public CustomerPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 240, 250));
        welcomeLabel = new JLabel("مرحباً بك!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("تسجيل الخروج");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.addActionListener(e -> mainApp.showPanel(MainApp.LOGIN_PANEL));
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setOpaque(false);
        logoutPanel.add(logoutButton);
        topPanel.add(logoutPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 16));

        tabbedPane.addTab("البحث عن رحلات وحجزها", createSearchFlightsPanel());
        tabbedPane.addTab("عرض حجوزاتي وإلغاؤها", createViewBookingsPanel());
        tabbedPane.addTab("تعديل الملف الشخصي", createEditProfilePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createSearchFlightsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("البحث عن رحلات"));

        JPanel searchFormPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JTextField originField = new JTextField(10);
        JTextField destinationField = new JTextField(10);
        JTextField dateField = new JTextField("YYYY-MM-DD", 10);
        JButton searchButton = new JButton("بحث");

        searchFormPanel.add(new JLabel("من:"));
        searchFormPanel.add(originField);
        searchFormPanel.add(new JLabel("إلى:"));
        searchFormPanel.add(destinationField);
        searchFormPanel.add(new JLabel("التاريخ:"));
        searchFormPanel.add(dateField);
        searchFormPanel.add(searchButton);
        panel.add(searchFormPanel, BorderLayout.NORTH);

        String[] flightColumns = {"رقم الرحلة", "الشركة", "من", "إلى", "المغادرة", "الوصول", "السعر (اقتصادي)"};
        flightsTableModel = new DefaultTableModel(flightColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        flightsTable = new JTable(flightsTableModel);
        flightsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightsTable.setFillsViewportHeight(true);
        flightsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(new JScrollPane(flightsTable), BorderLayout.CENTER);

        JButton bookFlightButton = new JButton("حجز الرحلة المحددة");
        bookFlightButton.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(bookFlightButton, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> {
            String origin = originField.getText().trim();
            String destination = destinationField.getText().trim();
            String dateStr = dateField.getText().trim();

            if (origin.isEmpty() || destination.isEmpty() || dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "الرجاء ملء جميع حقول البحث.", "نقص في المعلومات", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date searchDate = null;
            try {
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                inputDateFormat.setLenient(false); // لجعل التحويل صارماً
                searchDate = inputDateFormat.parse(dateStr);
            } catch (ParseException ex) { // تم تغيير Exception إلى ParseException
                JOptionPane.showMessageDialog(panel, "صيغة التاريخ غير صحيحة. استخدم yyyy-MM-dd", "خطأ في التاريخ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Flight> allFlights = mainApp.getFlights();
            flightsTableModel.setRowCount(0);
            boolean found = false;
            for (Flight flight : allFlights) {
                Calendar cal1 = Calendar.getInstance(); cal1.setTime(searchDate);
                Calendar cal2 = Calendar.getInstance(); cal2.setTime(flight.getDepartureTime());
                boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                if (flight.getOrigin().equalsIgnoreCase(origin) &&
                        flight.getDestination().equalsIgnoreCase(destination) &&
                        sameDay && flight.checkAvailability()) { // تأكد من وجود checkAvailability في Flight
                    flightsTableModel.addRow(new Object[]{
                            flight.getFlightNumber(),
                            flight.getAirline(),
                            flight.getOrigin(),
                            flight.getDestination(),
                            dateFormat.format(flight.getDepartureTime()),
                            dateFormat.format(flight.getArrivalTime()),
                            flight.getPrices().getOrDefault("Economy", 0.0)
                    });
                    found = true;
                }
            }
            if (!found) {
                JOptionPane.showMessageDialog(panel, "لم يتم العثور على رحلات تطابق معايير البحث.", "لا توجد نتائج", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        bookFlightButton.addActionListener(e -> {
            int selectedRow = flightsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "الرجاء تحديد رحلة من الجدول أولاً.", "لم يتم تحديد رحلة", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String flightNumber = (String) flightsTableModel.getValueAt(selectedRow, 0);
            Flight selectedFlight = mainApp.getFlights().stream()
                    .filter(f -> f.getFlightNumber().equals(flightNumber))
                    .findFirst().orElse(null);

            if (selectedFlight != null && currentCustomer != null) {
                // كمثال بسيط، إنشاء راكب واحد هو العميل نفسه
                // يجب أن يكون هناك معرف فريد للراكب، يمكن استخدام UUID
                Passenger passenger = new Passenger("P-" + UUID.randomUUID().toString().substring(0,8),
                        currentCustomer.getName(),
                        "N/A", // رقم جواز السفر، قد يحتاج لإدخاله
                        new Date(), // تاريخ الميلاد، قد يحتاج لإدخاله
                        "None"); // طلبات خاصة
                mainApp.addPassenger(passenger);

                List<Passenger> bookingPassengers = new ArrayList<>();
                bookingPassengers.add(passenger);

                Booking newBooking = new Booking(UUID.randomUUID().toString(), currentCustomer, selectedFlight);
                newBooking.addPassengers(bookingPassengers);

                try {
                    //  selectedFlight.reserveSeat("Economy"); // افترض حجز مقعد واحد لكل راكب، يجب تحسينه
                    // المنطق الأفضل هو أن يتم استدعاء reserveSeat داخل addBooking في MainApp لكل راكب
                    mainApp.addBooking(newBooking); // دالة addBooking في MainApp ستقوم بحجز المقاعد
                    JOptionPane.showMessageDialog(panel, "تم إنشاء الحجز بنجاح! رقم الحجز: " + newBooking.getBookingReference(), "تم الحجز", JOptionPane.INFORMATION_MESSAGE);
                    loadCustomerBookings();
                    // تحديث عرض المقاعد في جدول البحث إن أمكن
                    int currentlySelectedRowInSearch = flightsTable.getSelectedRow();
                    searchButton.doClick(); // إعادة البحث لتحديث عدد المقاعد إن تغير
                    if(currentlySelectedRowInSearch != -1 && currentlySelectedRowInSearch < flightsTable.getRowCount()){
                        flightsTable.setRowSelectionInterval(currentlySelectedRowInSearch, currentlySelectedRowInSearch);
                    }

                } catch (IllegalStateException exBooking) {
                    // إذا حدث خطأ أثناء حجز المقعد (مثلاً لا توجد مقاعد كافية)
                    JOptionPane.showMessageDialog(panel, exBooking.getMessage(), "خطأ في الحجز", JOptionPane.ERROR_MESSAGE);
                    // يجب إزالة الراكب الذي أضيف مؤقتًا إذا فشل الحجز
                    // passengers.remove(passenger); // إذا كان Passenger يضاف فقط عند نجاح الحجز
                }
            }
        });
        return panel;
    }

    private JPanel createViewBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("حجوزاتي"));

        String[] bookingColumns = {"رقم الحجز", "الرحلة", "من", "إلى", "تاريخ المغادرة", "الحالة", "حالة الدفع"};
        bookingsTableModel = new DefaultTableModel(bookingColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        bookingsTable = new JTable(bookingsTableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.setFillsViewportHeight(true);
        bookingsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("إلغاء الحجز المحدد");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        buttonsPanel.add(cancelButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> {
            int selectedRow = bookingsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "الرجاء تحديد حجز من الجدول أولاً.", "لم يتم تحديد حجز", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String bookingRef = (String) bookingsTableModel.getValueAt(selectedRow, 0);
            Booking bookingToCancel = mainApp.getBookings().stream()
                    .filter(b -> b.getBookingReference().equals(bookingRef))
                    .findFirst().orElse(null);

            if (bookingToCancel != null) {
                if (currentCustomer != null && bookingToCancel.getCustomer().getUserId().equals(currentCustomer.getUserId())) {
                    if ("Reserved".equalsIgnoreCase(bookingToCancel.getStatus()) || "Pending".equalsIgnoreCase(bookingToCancel.getPaymentStatus())) {
                        int confirm = JOptionPane.showConfirmDialog(panel,
                                "هل أنت متأكد من رغبتك في إلغاء الحجز رقم: " + bookingRef + "؟",
                                "تأكيد الإلغاء", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            mainApp.cancelBooking(bookingToCancel);
                            JOptionPane.showMessageDialog(panel, "تم إلغاء الحجز بنجاح.", "تم الإلغاء", JOptionPane.INFORMATION_MESSAGE);
                            loadCustomerBookings();
                        }
                    } else {
                        JOptionPane.showMessageDialog(panel, "لا يمكن إلغاء هذا الحجز لأنه مؤكد أو مدفوع. يرجى الاتصال بخدمة العملاء.", "لا يمكن الإلغاء", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "خطأ: الحجز المحدد لا يخصك.", "خطأ في الصلاحية", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return panel;
    }

    private JPanel createEditProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("تعديل الملف الشخصي"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; // محاذاة العناصر لليسار

        profileNameField = new JTextField(20);
        profileEmailField = new JTextField(20);
        profileContactField = new JTextField(20);
        profileAddressField = new JTextField(20);
        profilePreferencesField = new JTextField(20);
        profileCurrentPasswordField = new JPasswordField(20);
        profileNewPasswordField = new JPasswordField(20);
        profileConfirmNewPasswordField = new JPasswordField(20);
        JButton saveButton = new JButton("حفظ التعديلات");

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("الاسم:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profileNameField, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("البريد الإلكتروني:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profileEmailField, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("معلومات الاتصال:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profileContactField, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("العنوان:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profileAddressField, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("التفضيلات:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profilePreferencesField, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("كلمة المرور الحالية:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profileCurrentPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("كلمة المرور الجديدة:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profileNewPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("تأكيد كلمة المرور الجديدة:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; panel.add(profileConfirmNewPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // عدم تمديد الزر
        panel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            if (currentCustomer == null) return;

            String newName = profileNameField.getText().trim();
            String newEmail = profileEmailField.getText().trim();
            String newContact = profileContactField.getText().trim();
            String newAddress = profileAddressField.getText().trim();
            String newPreferences = profilePreferencesField.getText().trim();
            String currentPass = new String(profileCurrentPasswordField.getPassword());
            String newPass = new String(profileNewPasswordField.getPassword());
            String confirmNewPass = new String(profileConfirmNewPasswordField.getPassword());

            boolean profileUpdated = false;
            if (!newName.isEmpty() && !newName.equals(currentCustomer.getName())) {
                currentCustomer.setName(newName); profileUpdated = true;
            }
            if (!newEmail.isEmpty() && !newEmail.equals(currentCustomer.getEmail())) {
                // يمكنك إضافة تحقق من صحة صيغة البريد الإلكتروني هنا
                currentCustomer.setEmail(newEmail); profileUpdated = true;
            }
            if (!newContact.isEmpty() && !newContact.equals(currentCustomer.getContactInfo())) {
                currentCustomer.setContactInfo(newContact); profileUpdated = true;
            }
            if (currentCustomer instanceof Customer) { // التأكد من أن المستخدم هو عميل بالفعل
                if (!newAddress.isEmpty() && !newAddress.equals(((Customer)currentCustomer).getAddress())) {
                    ((Customer)currentCustomer).setAddress(newAddress); profileUpdated = true;
                }
                if (!newPreferences.isEmpty() && !newPreferences.equals(((Customer)currentCustomer).getPreferences())) {
                    ((Customer)currentCustomer).setPreferences(newPreferences); profileUpdated = true;
                }
            }


            boolean passwordChanged = false;
            if (!newPass.isEmpty()) {
                if (!currentCustomer.verifyPassword(currentPass)) {
                    JOptionPane.showMessageDialog(panel, "كلمة المرور الحالية غير صحيحة.", "خطأ", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!newPass.equals(confirmNewPass)) {
                    JOptionPane.showMessageDialog(panel, "كلمتا المرور الجديدتان غير متطابقتين.", "خطأ", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    currentCustomer.setPassword(newPass); // يجب أن تتحقق setPassword من الشروط
                    passwordChanged = true;
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage(), "خطأ في كلمة المرور", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (profileUpdated || passwordChanged) {
                mainApp.saveData();
                JOptionPane.showMessageDialog(panel, "تم تحديث الملف الشخصي بنجاح.", "تم التحديث", JOptionPane.INFORMATION_MESSAGE);
                welcomeLabel.setText("مرحباً بك، " + currentCustomer.getName()); // تحديث رسالة الترحيب
                if(passwordChanged){
                    profileCurrentPasswordField.setText("");
                    profileNewPasswordField.setText("");
                    profileConfirmNewPasswordField.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(panel, "لم يتم إجراء أي تغييرات.", "معلومات", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        return panel;
    }

    public void setCurrentCustomer(Customer customer) {
        this.currentCustomer = customer;
        if (customer != null) {
            welcomeLabel.setText("مرحباً بك، " + customer.getName());
            loadCustomerBookings();

            // ملء حقول تعديل الملف الشخصي بالبيانات الحالية
            profileNameField.setText(customer.getName());
            profileEmailField.setText(customer.getEmail());
            profileContactField.setText(customer.getContactInfo());
            if (customer instanceof Customer) { // التأكد مرة أخرى قبل الوصول لـ address و preferences
                profileAddressField.setText(((Customer)customer).getAddress());
                profilePreferencesField.setText(((Customer)customer).getPreferences());
            }
            profileCurrentPasswordField.setText("");
            profileNewPasswordField.setText("");
            profileConfirmNewPasswordField.setText("");

        } else {
            welcomeLabel.setText("مرحباً بك!");
            if (flightsTableModel != null) flightsTableModel.setRowCount(0);
            if (bookingsTableModel != null) bookingsTableModel.setRowCount(0);
        }
    }

    private void loadCustomerBookings() {
        if (bookingsTableModel == null) return;
        bookingsTableModel.setRowCount(0);
        if (currentCustomer != null) {
            List<Booking> customerBookings = mainApp.getBookingsForCustomer(currentCustomer);
            for (Booking booking : customerBookings) {
                Flight flight = booking.getFlight();
                bookingsTableModel.addRow(new Object[]{
                        booking.getBookingReference(),
                        flight.getFlightNumber(),
                        flight.getOrigin(),
                        flight.getDestination(),
                        dateFormat.format(flight.getDepartureTime()),
                        booking.getStatus(),
                        booking.getPaymentStatus()
                });
            }
        }
    }

    public void resetPanel() {
        setCurrentCustomer(null);
        // مسح حقول تعديل الملف الشخصي عند الخروج
        profileNameField.setText("");
        profileEmailField.setText("");
        profileContactField.setText("");
        profileAddressField.setText("");
        profilePreferencesField.setText("");
        profileCurrentPasswordField.setText("");
        profileNewPasswordField.setText("");
        profileConfirmNewPasswordField.setText("");
    }
}