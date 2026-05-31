import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class AgentPanel extends JPanel {

    private MainApp mainApp;
    private Agent currentAgent;
    private JLabel welcomeLabel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // يمكن إضافة جداول مشابهة للعميل والمسؤول إذا احتاج الوكيل لعرض الرحلات أو العملاء
    private JTable flightsResultsTable;
    private DefaultTableModel flightsResultsTableModel;
    private JTable customerBookingsTable;
    private DefaultTableModel customerBookingsTableModel;


    public AgentPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(230, 250, 230)); // خلفية مختلفة للوكيل
        welcomeLabel = new JLabel("لوحة تحكم الوكيل", SwingConstants.CENTER);
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

        tabbedPane.addTab("إنشاء حجز لعميل", createBookForCustomerPanel());
        tabbedPane.addTab("إدارة حجوزات العملاء", createManageCustomerBookingsPanel());
        tabbedPane.addTab("إدارة ملفات العملاء", createManageCustomersPanel());
        // يمكن إضافة تبويب "إدارة الرحلات" إذا كان للوكيل صلاحيات مشابهة للمسؤول

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBookForCustomerPanel(){
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("إنشاء حجز جديد لعميل"));

        // الجزء العلوي: اختيار العميل + نموذج البحث عن رحلات
        JPanel topSectionPanel = new JPanel(new BorderLayout(5,5));

        JPanel customerSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerSelectionPanel.add(new JLabel("اختر العميل:"));
        JComboBox<CustomerWrapper> customerComboBox = new JComboBox<>();
        // سيتم ملء هذا عند تحديث بيانات الوكيل أو عند تحميل اللوحة
        customerSelectionPanel.add(customerComboBox);
        topSectionPanel.add(customerSelectionPanel, BorderLayout.NORTH);

        JPanel searchFormPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField originField = new JTextField(10);
        JTextField destinationField = new JTextField(10);
        JTextField dateField = new JTextField("YYYY-MM-DD", 10);
        JButton searchFlightsButton = new JButton("بحث عن رحلات");
        searchFormPanel.add(new JLabel("من:")); searchFormPanel.add(originField);
        searchFormPanel.add(new JLabel("إلى:")); searchFormPanel.add(destinationField);
        searchFormPanel.add(new JLabel("التاريخ:")); searchFormPanel.add(dateField);
        searchFormPanel.add(searchFlightsButton);
        topSectionPanel.add(searchFormPanel, BorderLayout.CENTER);
        panel.add(topSectionPanel, BorderLayout.NORTH);

        // جدول نتائج البحث
        String[] flightColumns = {"رقم الرحلة", "الشركة", "من", "إلى", "المغادرة", "الوصول", "السعر (اقتصادي)"};
        flightsResultsTableModel = new DefaultTableModel(flightColumns, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        flightsResultsTable = new JTable(flightsResultsTableModel);
        flightsResultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(flightsResultsTable), BorderLayout.CENTER);

        JButton bookButton = new JButton("حجز الرحلة المحددة للعميل");
        bookButton.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(bookButton, BorderLayout.SOUTH);

        // Action Listeners
        searchFlightsButton.addActionListener(e -> {
            // نفس منطق البحث الموجود في CustomerPanel ولكن يجب التأكد من توفر الرحلات
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
                searchDate = inputDateFormat.parse(dateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "صيغة التاريخ غير صحيحة. استخدم YYYY-MM-DD", "خطأ في التاريخ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Flight> availableFlights = mainApp.getFlights();
            flightsResultsTableModel.setRowCount(0);
            boolean found = false;
            for (Flight flight : availableFlights) {
                Calendar cal1 = Calendar.getInstance(); cal1.setTime(searchDate);
                Calendar cal2 = Calendar.getInstance(); cal2.setTime(flight.getDepartureTime());
                boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                if (flight.getOrigin().equalsIgnoreCase(origin) &&
                        flight.getDestination().equalsIgnoreCase(destination) &&
                        sameDay && flight.checkAvailability()) {
                    flightsResultsTableModel.addRow(new Object[]{
                            flight.getFlightNumber(), flight.getAirline(), flight.getOrigin(),
                            flight.getDestination(), dateFormat.format(flight.getDepartureTime()),
                            dateFormat.format(flight.getArrivalTime()), flight.getPrices().getOrDefault("Economy", 0.0)
                    });
                    found = true;
                }
            }
            if (!found) {
                JOptionPane.showMessageDialog(panel, "لم يتم العثور على رحلات تطابق معايير البحث.", "لا توجد نتائج", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        bookButton.addActionListener(e -> {
            CustomerWrapper selectedCustomerWrapper = (CustomerWrapper) customerComboBox.getSelectedItem();
            if (selectedCustomerWrapper == null) {
                JOptionPane.showMessageDialog(panel, "الرجاء اختيار عميل أولاً.", "لم يتم اختيار عميل", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Customer selectedCustomer = selectedCustomerWrapper.getCustomer();

            int selectedRow = flightsResultsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "الرجاء تحديد رحلة من الجدول أولاً.", "لم يتم تحديد رحلة", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String flightNumber = (String) flightsResultsTableModel.getValueAt(selectedRow, 0);
            Flight selectedFlight = mainApp.getFlights().stream()
                    .filter(f -> f.getFlightNumber().equals(flightNumber))
                    .findFirst().orElse(null);

            if (selectedFlight != null && selectedCustomer != null) {
                // هنا يجب فتح نافذة لإدخال بيانات الركاب (قد تكون أكثر من راكب)
                // كمثال مبسط، سنفترض راكب واحد هو العميل نفسه
                Passenger passenger = new Passenger("P-" + UUID.randomUUID().toString().substring(0,8), selectedCustomer.getName(), "N/A_AgentBooked", new Date(), "Booked by agent");
                mainApp.addPassenger(passenger);

                List<Passenger> bookingPassengers = new ArrayList<>();
                bookingPassengers.add(passenger);

                Booking newBooking = new Booking(UUID.randomUUID().toString(), selectedCustomer, selectedFlight);
                newBooking.addPassengers(bookingPassengers);

                mainApp.addBooking(newBooking);
                JOptionPane.showMessageDialog(panel, "تم إنشاء الحجز للعميل " + selectedCustomer.getName() + " بنجاح! رقم الحجز: " + newBooking.getBookingReference(), "تم الحجز", JOptionPane.INFORMATION_MESSAGE);
                // يمكن تحديث قائمة حجوزات العميل في التبويب الآخر إذا كان مفتوحًا لنفس العميل
            }
        });
        return panel;
    }

    private JPanel createManageCustomerBookingsPanel(){
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("إدارة حجوزات العملاء"));
        // مشابه لتبويب عرض الحجوزات عند العميل ولكن مع إمكانية البحث عن حجز برقم أو باسم عميل
        // وإمكانية الإلغاء أو التعديل (التعديل أكثر تعقيدًا)

        // حقل بحث عن حجز
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField bookingRefSearchField = new JTextField(15);
        JButton searchBookingButton = new JButton("بحث عن حجز (بالرقم)");
        searchPanel.add(new JLabel("رقم الحجز:"));
        searchPanel.add(bookingRefSearchField);
        searchPanel.add(searchBookingButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        String[] bookingColumns = {"رقم الحجز", "العميل", "الرحلة", "من", "إلى", "المغادرة", "الحالة"};
        customerBookingsTableModel = new DefaultTableModel(bookingColumns, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        customerBookingsTable = new JTable(customerBookingsTableModel);
        customerBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(customerBookingsTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBookingButton = new JButton("إلغاء الحجز المحدد");
        // JButton modifyBookingButton = new JButton("تعديل الحجز المحدد");
        buttonsPanel.add(cancelBookingButton);
        // buttonsPanel.add(modifyBookingButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        searchBookingButton.addActionListener(e -> {
            String bookingRef = bookingRefSearchField.getText().trim();
            if (bookingRef.isEmpty()) {
                loadAllBookingsForTable(); // عرض كل الحجوزات إذا كان البحث فارغًا
                return;
            }
            customerBookingsTableModel.setRowCount(0);
            Booking foundBooking = mainApp.getBookings().stream()
                    .filter(b -> b.getBookingReference().equalsIgnoreCase(bookingRef))
                    .findFirst().orElse(null);
            if (foundBooking != null) {
                Flight flight = foundBooking.getFlight();
                customerBookingsTableModel.addRow(new Object[]{
                        foundBooking.getBookingReference(),
                        foundBooking.getCustomer().getName(),
                        flight.getFlightNumber(),
                        flight.getOrigin(),
                        flight.getDestination(),
                        dateFormat.format(flight.getDepartureTime()),
                        foundBooking.getStatus()
                });
            } else {
                JOptionPane.showMessageDialog(panel, "لم يتم العثور على حجز بهذا الرقم.", "غير موجود", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        cancelBookingButton.addActionListener(e -> {
            int selectedRow = customerBookingsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "الرجاء تحديد حجز من الجدول.", "لم يتم التحديد", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String bookingRefToCancel = (String) customerBookingsTableModel.getValueAt(selectedRow, 0);
            Booking bookingToCancel = mainApp.getBookings().stream()
                    .filter(b -> b.getBookingReference().equals(bookingRefToCancel))
                    .findFirst().orElse(null);
            if (bookingToCancel != null) {
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "هل أنت متأكد من إلغاء الحجز رقم: " + bookingRefToCancel + "؟",
                        "تأكيد الإلغاء", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    mainApp.cancelBooking(bookingToCancel);
                    JOptionPane.showMessageDialog(panel, "تم إلغاء الحجز بنجاح.", "تم الإلغاء", JOptionPane.INFORMATION_MESSAGE);
                    loadAllBookingsForTable(); // إعادة تحميل البيانات
                }
            }
        });


        return panel;
    }

    private JPanel createManageCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("إدارة ملفات العملاء"));
        // جدول لعرض العملاء مع زر لتعديل بيانات العميل المحدد (مشابه لما في AdminPanel ولكن موجه للعملاء فقط)
        // أو حقل بحث عن عميل ثم عرض بياناته وتعديلها

        // كمثال: حقل بحث بسيط عن عميل بالاسم لعرض بياناته
        JPanel searchCustomerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField customerNameSearchField = new JTextField(15);
        JButton searchCustomerButton = new JButton("بحث عن عميل (بالاسم)");
        JTextArea customerDetailsArea = new JTextArea(10, 30);
        customerDetailsArea.setEditable(false);

        searchCustomerPanel.add(new JLabel("اسم العميل:"));
        searchCustomerPanel.add(customerNameSearchField);
        searchCustomerPanel.add(searchCustomerButton);

        panel.add(searchCustomerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(customerDetailsArea), BorderLayout.CENTER);

        // JButton editCustomerProfileButton = new JButton("تعديل ملف العميل المحدد"); // يحتاج إلى تحديد عميل
        // panel.add(editCustomerProfileButton, BorderLayout.SOUTH);

        searchCustomerButton.addActionListener(e -> {
            String nameToSearch = customerNameSearchField.getText().trim().toLowerCase();
            if (nameToSearch.isEmpty()) {
                customerDetailsArea.setText("الرجاء إدخال اسم للبحث.");
                return;
            }
            Customer foundCustomer = mainApp.getCustomersFromUsers().stream()
                    .filter(c -> c.getName().toLowerCase().contains(nameToSearch))
                    .findFirst().orElse(null);
            if (foundCustomer != null) {
                StringBuilder details = new StringBuilder();
                details.append("معرف العميل: ").append(foundCustomer.getUserId()).append("\n");
                details.append("اسم المستخدم: ").append(foundCustomer.getUsername()).append("\n");
                details.append("الاسم: ").append(foundCustomer.getName()).append("\n");
                details.append("البريد: ").append(foundCustomer.getEmail()).append("\n");
                details.append("الهاتف: ").append(foundCustomer.getContactInfo()).append("\n");
                details.append("العنوان: ").append(foundCustomer.getAddress()).append("\n");
                details.append("التفضيلات: ").append(foundCustomer.getPreferences()).append("\n");
                customerDetailsArea.setText(details.toString());
            } else {
                customerDetailsArea.setText("لم يتم العثور على عميل بهذا الاسم.");
            }
        });

        return panel;
    }


    public void setCurrentAgent(Agent agent) {
        this.currentAgent = agent;
        if (agent != null) {
            welcomeLabel.setText("لوحة تحكم الوكيل: " + agent.getName());
            // ملء ComboBox العملاء
            JPanel bookForCustomerPanel = (JPanel)((JTabbedPane)getComponent(1)).getComponentAt(0); // لوحة الحجز لعميل
            JPanel topSectionPanel = (JPanel)bookForCustomerPanel.getComponent(0); // اللوحة العلوية فيها الكومبو بوكس
            JPanel customerSelectionPanel = (JPanel)topSectionPanel.getComponent(0);
            JComboBox<CustomerWrapper> customerComboBox = (JComboBox<CustomerWrapper>)customerSelectionPanel.getComponent(1);

            customerComboBox.removeAllItems();
            for (Customer cust : mainApp.getCustomersFromUsers()) {
                customerComboBox.addItem(new CustomerWrapper(cust));
            }

            loadAllBookingsForTable(); // تحميل الحجوزات في التبويب الثاني

        } else {
            welcomeLabel.setText("لوحة تحكم الوكيل");
            if (flightsResultsTableModel != null) flightsResultsTableModel.setRowCount(0);
            if (customerBookingsTableModel != null) customerBookingsTableModel.setRowCount(0);
            // مسح ComboBox العملاء
            JPanel bookForCustomerPanel = (JPanel)((JTabbedPane)getComponent(1)).getComponentAt(0);
            JPanel topSectionPanel = (JPanel)bookForCustomerPanel.getComponent(0);
            JPanel customerSelectionPanel = (JPanel)topSectionPanel.getComponent(0);
            JComboBox<CustomerWrapper> customerComboBox = (JComboBox<CustomerWrapper>)customerSelectionPanel.getComponent(1);
            customerComboBox.removeAllItems();
        }
    }

    private void loadAllBookingsForTable(){
        if (customerBookingsTableModel == null) return;
        customerBookingsTableModel.setRowCount(0);
        List<Booking> allBookings = mainApp.getBookings();
        for(Booking booking : allBookings){
            Flight flight = booking.getFlight();
            customerBookingsTableModel.addRow(new Object[]{
                    booking.getBookingReference(),
                    booking.getCustomer().getName(),
                    flight.getFlightNumber(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    dateFormat.format(flight.getDepartureTime()),
                    booking.getStatus()
            });
        }
    }

    public void resetPanel() {
        setCurrentAgent(null);
    }

    // فئة داخلية لتغليف العميل وعرض اسمه في JComboBox
    private static class CustomerWrapper {
        private Customer customer;
        public CustomerWrapper(Customer customer) { this.customer = customer; }
        public Customer getCustomer() { return customer; }
        @Override public String toString() { return customer.getName() + " (" + customer.getUserId() + ")"; }
    }
}
