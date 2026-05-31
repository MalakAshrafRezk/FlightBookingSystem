import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.text.ParseException;
import java.io.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;


class FileManager {
    private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());
    private static final String USERS_FILE = "users.txt";
    private static final String FLIGHTS_FILE = "flights.txt";
    private static final String PASSENGERS_FILE = "passengers.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String PAYMENTS_FILE = "payments.txt";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void savePayments(List<Payment> payments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENTS_FILE))) {
            for (Payment p : payments) {
                String methodDetails = "";
                if (p.getMethod() instanceof CreditCardPaymentMethod cc) {
                    methodDetails = String.join(";",
                            "CreditCard",
                            cc.getCardNumber(),
                            cc.getExpiryDate(),
                            cc.getCvv());
                } else if (p.getMethod() instanceof BankTransferPaymentMethod bt) {
                    methodDetails = String.join(";",
                            "BankTransfer",
                            bt.getBankName(),
                            bt.getAccountNumber(),
                            bt.getSwiftCode());
                } else if (p.getMethod() instanceof PayPalPaymentMethod pp) {
                    methodDetails = String.join(";",
                            "PayPal",
                            pp.getPaypalEmail());
                }
                writer.write(String.join(",",
                        p.getPaymentId(),
                        p.getBookingReference(),
                        String.valueOf(p.getAmount()),
                        p.getMethod().getType(), // Save the general type
                        methodDetails,             // Save specific method details
                        p.getStatus().toString(),
                        DATE_FORMAT.format(p.getTransactionDate())));
                writer.newLine();
            }
        }
    }


    public static List<Payment> loadPayments() throws IOException {
        List<Payment> payments = new ArrayList<>();
        File file = new File(PAYMENTS_FILE);
        if (!file.exists()) return payments;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    LOGGER.warning("Incomplete payment line: " + line);
                    continue;
                }

                String paymentId = parts[0];
                String bookingReference = parts[1];
                double amount = Double.parseDouble(parts[2]);
                String methodType = parts[3];
                String methodDetailsStr = parts[4];
                PaymentStatus status = PaymentStatus.valueOf(parts[5]);
                Date transactionDate = null;
                try{
                    transactionDate = DATE_FORMAT.parse(parts[6]);
                } catch(ParseException e){
                    LOGGER.warning("Error parsing transaction date: " + e.getMessage() + " for line: " + line);
                    continue;
                }


                PaymentMethod method = null;
                String[] methodDetails = methodDetailsStr.split(";");
                switch (methodType) {
                    case "CreditCard":
                        if (methodDetails.length == 4) {
                            method = new CreditCardPaymentMethod(methodDetails[0], methodDetails[1], methodDetails[2]);
                        }
                        break;
                    case "BankTransfer":
                        if (methodDetails.length == 3) {
                            method = new BankTransferPaymentMethod(methodDetails[0], methodDetails[1], methodDetails[2]);
                        }
                        break;
                    case "PayPal":
                        if (methodDetails.length == 1) {
                            method = new PayPalPaymentMethod(methodDetails[0]);
                        }
                        break;
                    default:
                        LOGGER.warning("Unknown payment method type: " + methodType);
                        continue;
                }

                if (method != null) {
                    Payment payment = new Payment(bookingReference, amount, method);
                    payment.setPaymentId(paymentId);
                    payment.setStatus(status);
                    payment.setTransactionDate(transactionDate);
                    payments.add(payment);
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Error loading payments: " + e.getMessage());
        }
        return payments;
    }
    // ================== USERS ==================
    public static void saveUsers(List<User> users) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                String role = u.getClass().getSimpleName();
                String extra = "";
                if (u instanceof Customer c) {
                    extra = c.getAddress() + ";" + c.getPreferences();
                } else if (u instanceof Agent a) {
                    extra = a.getDepartment() + ";" + a.getCommission();
                } else if (u instanceof Administrator a) {
                    extra = a.getSecurityLevel() + ";" + a.getAdminId();
                }
                writer.write(String.join(",",
                        role,
                        u.getUserId(),
                        u.getUsername(),
                        u.getPassword(),
                        u.getName(),
                        u.getEmail(),
                        u.getContactInfo(),
                        extra));
                writer.newLine();
            }
        }
    }

    public static List<User> loadUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) return users;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", 8);
                if (parts.length < 8) {
                    LOGGER.warning("Incomplete user line: " + line);
                    continue;
                }

                String role = parts[0], userId = parts[1], username = parts[2],
                        password = parts[3], name = parts[4], email = parts[5],
                        contact = parts[6], extra = parts[7];

                switch (role) {
                    case "Customer" -> {
                        String[] xp = extra.split(";", 2);
                        String address = xp.length > 0 ? xp[0] : "";
                        String prefs = xp.length > 1 ? xp[1] : "";
                        users.add(new Customer(userId, username, password, name, email, contact, address, prefs));
                    }
                    case "Agent" -> {
                        String[] xp = extra.split(";", 2);
                        String dept = xp.length > 0 ? xp[0] : "";
                        double comm = xp.length > 1 ? Double.parseDouble(xp[1]) : 0.0;
                        users.add(new Agent(userId, username, password, name, email, contact, dept, comm));
                    }
                    case "Administrator" -> {
                        String[] xp = extra.split(";", 2);
                        int sec = xp.length > 0 ? Integer.parseInt(xp[0]) : 0;
                        String adminId = xp.length > 1 ? xp[1] : "";
                        users.add(new Administrator(userId, username, password, name, email, contact, sec, adminId));
                    }
                    default -> LOGGER.warning("Unknown role: " + role);
                }
            }
        }
        return users;
    }

    // ================== FLIGHTS ==================
    public static void saveFlights(List<Flight> flights) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FLIGHTS_FILE))) {
            for (Flight f : flights) {
                String seats = f.getAvailableSeats().entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .collect(Collectors.joining(";"));
                String prices = f.getPrices().entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .collect(Collectors.joining(";"));

                w.write(String.join(",",
                        f.getClass().getSimpleName(),
                        f.getFlightNumber(),
                        f.getAirline(),
                        f.getOrigin(),
                        f.getDestination(),
                        String.valueOf(f.getDepartureTime().getTime()),
                        String.valueOf(f.getArrivalTime().getTime()),
                        seats,
                        prices
                ));
                w.newLine();
            }
        }
    }

    public static List<Flight> loadFlights() throws IOException {
        List<Flight> flights = new ArrayList<>();
        File file = new File(FLIGHTS_FILE);
        if (!file.exists()) return flights;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] raw = line.split(",", -1);
                String[] parts;
                if (raw.length == 8) {
                    parts = new String[9];
                    parts[0] = "DomesticFlight";
                    System.arraycopy(raw, 0, parts, 1, 8);
                } else {
                    parts = raw;
                }

                if (parts.length < 9) {
                    LOGGER.warning("Data error: less than 9 parts: " + line);
                    continue;
                }

                String type = parts[0];
                String number = parts[1];
                String airline = parts[2];
                String origin = parts[3];
                String dest = parts[4];

                Date dep = null;
                Date arr = null;
                try {
                    dep = new Date(Long.parseLong(parts[5]));
                    arr = new Date(Long.parseLong(parts[6]));
                } catch (Exception e) {
                    LOGGER.warning("Invalid date format: " + e.getMessage());
                    continue;
                }

                Map<String, Integer> seats = new HashMap<>();
                if (!parts[7].isEmpty()) {
                    for (String seat : parts[7].split(";")) {
                        String[] sd = seat.split(":");
                        if (sd.length == 2) {
                            try { seats.put(sd[0], Integer.parseInt(sd[1])); }
                            catch (NumberFormatException e) {
                                LOGGER.warning("Invalid seat: " + seat);
                            }
                        }
                    }
                }

                Map<String, Double> prices = new HashMap<>();
                if (!parts[8].isEmpty()) {
                    for (String pr : parts[8].split(";")) {
                        String[] pd = pr.split(":");
                        if (pd.length == 2) {
                            try { prices.put(pd[0], Double.parseDouble(pd[1])); }
                            catch (NumberFormatException e) {
                                LOGGER.warning("Invalid price: " + pr);
                            }
                        }
                    }
                }

                Flight flight;
                if ("InternationalFlight".equals(type)) {
                    flight = new InternationalFlight(number, airline, origin, dest, dep, arr, seats, prices);
                } else {
                    flight = new DomesticFlight(number, airline, origin, dest, dep, arr, seats, prices);
                }
                flights.add(flight);
            }
        }
        return flights;
    }

    // ================== PASSENGERS ==================
    public static void savePassengers(List<Passenger> pax) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(PASSENGERS_FILE))) {
            for (Passenger p : pax) {
                w.write(String.join(",",
                        p.getPassengerId(),
                        p.getName(),
                        p.getPassportNumber(),
                        String.valueOf(p.getDateOfBirth().getTime()),
                        p.getSpecialRequests()));
                w.newLine();
            }
        }
    }

    public static List<Passenger> loadPassengers() throws IOException {
        List<Passenger> pax = new ArrayList<>();
        File file = new File(PASSENGERS_FILE);
        if (!file.exists()) return pax;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(",", 5);
                if (parts.length < 5) {
                    LOGGER.warning("Incomplete passenger line: " + line);
                    continue;
                }
                try {
                    pax.add(new Passenger(
                            parts[0],
                            parts[1],
                            parts[2],
                            new Date(Long.parseLong(parts[3])),
                            parts[4]
                    ));
                } catch (Exception e) {
                    LOGGER.warning("Error parsing passenger: " + e.getMessage());
                }
            }
        }
        return pax;
    }

    // ================== BOOKINGS ==================
    public static void saveBookings(List<Booking> bookings) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking b : bookings) {
                String paxIds = b.getPassengers().stream()
                        .map(Passenger::getPassengerId)
                        .collect(Collectors.joining("|"));

                w.write(String.join(",",
                        b.getBookingReference(),
                        b.getCustomer().getUserId(),
                        b.getFlight().getFlightNumber(),
                        paxIds,
                        b.getStatus(),
                        b.getPaymentStatus()));
                w.newLine();
            }
        }
    }
    public static List<Booking> loadBookings(List<Customer> customers, List<Flight> flights, List<Passenger> pax, List<Payment> payments) throws IOException {
        Map<String, Customer> custMap = new LinkedHashMap<>();
        for (Customer c : customers) {
            if (c.getUserId() != null && !custMap.containsKey(c.getUserId())) {
                custMap.put(c.getUserId(), c);
            }
        }

        Map<String, Flight> flightMap = new LinkedHashMap<>();
        for (Flight f : flights) {
            if (f.getFlightNumber() != null && !flightMap.containsKey(f.getFlightNumber())) {
                flightMap.put(f.getFlightNumber(), f);
            }
        }

        Map<String, Passenger> paxMap = new LinkedHashMap<>();
        for (Passenger p : pax) {
            if (p.getPassengerId() != null && !paxMap.containsKey(p.getPassengerId())) {
                paxMap.put(p.getPassengerId(), p);
            }
        }

        Map<String, Payment> paymentMap = new HashMap<>(); // Map for Payments
        for (Payment payment : payments) {
            if (payment.getPaymentId() != null && !paymentMap.containsKey(payment.getPaymentId())) {
                paymentMap.put(payment.getPaymentId(), payment);
            }
        }

        List<Booking> bookings = new ArrayList<>();
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) return bookings;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    LOGGER.warning("Incomplete booking line: " + line);
                    continue;
                }

                // Wrap the entire booking creation in a try-catch
                Customer c = custMap.get(parts[1]);
                Flight f = flightMap.get(parts[2]);
                if (c == null || f == null) continue;

                List<Passenger> passengerList = new ArrayList<>();
                if (!parts[3].isEmpty()) {
                    for (String id : parts[3].split("\\|")) {
                        Passenger p = paxMap.get(id);
                        if (p != null) passengerList.add(p);
                    }
                }

                Booking b = new Booking(parts[0], c, f);
                b.setStatus(parts[4]);
                b.setPaymentStatus(parts[5]);
                bookings.add(b);

            }
        } catch (IOException e) {
            LOGGER.warning("Error loading bookings: " + e.getMessage());
        }
        return bookings;
    }
    public static void deleteBooking(String ref, List<Booking> bookings) throws IOException {
        bookings.removeIf(b -> b.getBookingReference().equals(ref));
        saveBookings(bookings);
    }
}