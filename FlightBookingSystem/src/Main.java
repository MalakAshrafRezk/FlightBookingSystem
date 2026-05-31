import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
public class Main {

    public static void main(String[] args) throws Exception {


        Scanner sc = new Scanner(System.in);
        List<User> users = FileManager.loadUsers();
        List<Flight> flights = FileManager.loadFlights();
        List<Passenger> pax = FileManager.loadPassengers();
        List<Customer> customers = users.stream()
                .filter(u -> u instanceof Customer)
                .map(u -> (Customer) u)
                .collect(Collectors.toList());
        List<Booking> bookings = FileManager.loadBookings(customers, flights, pax, new ArrayList<>());

        User cur = loginPrompt(users, sc);

        while (true) {
            cur.showMenu();
            System.out.print("Enter choice: ");
            String input = sc.nextLine();
            int ch;
            try {
                ch = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (cur instanceof Customer) {
                Customer c = (Customer) cur;
                switch (ch) {
                    case 1:
                        handleCustomerSearchAndBook(c, flights, pax, bookings, sc);
                        break;
                    case 2:
                        c.viewBookings();
                        break;
                    case 3:
                        System.out.print("Enter booking ID to cancel: ");
                        String bid = sc.nextLine();
                        if (c.cancelBooking(bid)) {
                            bookings.removeIf(b -> b.getBookingReference().equals(bid));
                            try {
                                FileManager.saveBookings(bookings);
                            } catch (IOException e) {
                                System.out.println("Error saving bookings: " + e.getMessage());
                            }
                        }
                        break;
                    case 4:
                        cur.logout();
                        cur = loginPrompt(users, sc);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }

            } else if (cur instanceof Agent) {
                Agent a = (Agent) cur;
                switch (ch) {
                    case 1:
                        handleAgentBooking(a, customers, flights, pax, bookings, sc);
                        break;
                    case 2:
                        a.manageFlights(flights);
                        break;
                    case 3:
                        System.out.print("Enter customer ID: ");
                        String custId = sc.nextLine();
                        Customer target = customers.stream()
                                .filter(ct -> ct.getUserId().equals(custId))
                                .findFirst().orElse(null);
                        if (target != null) {
                            System.out.print("Enter booking ID to cancel: ");
                            String bidA = sc.nextLine();
                            if (a.cancelBookingForCustomer(target, bidA)) {
                                bookings.removeIf(b -> b.getBookingReference().equals(bidA));
                                try {
                                    FileManager.saveBookings(bookings);
                                } catch (IOException e) {
                                    System.out.println("Error saving bookings: " + e.getMessage());
                                }
                            }
                        } else {
                            System.out.println("Customer not found.");
                        }
                        break;
                    case 4:
                        System.out.print("Enter customer ID: ");
                        String cid = sc.nextLine();
                        Customer cust = customers.stream()
                                .filter(ct -> ct.getUserId().equals(cid))
                                .findFirst()
                                .orElse(null);
                        if (cust != null) {
                            System.out.print("New Address: ");
                            String na = sc.nextLine();
                            System.out.print("New Preferences: ");
                            String np = sc.nextLine();
                            a.manageCustomerProfile(cust, na, np);
                            saveUsersToFile(users); // Save users after agent modifies customer profile
                        } else {
                            System.out.println("Customer not found.");
                        }
                        break;
                    case 5:
                        cur.logout();
                        cur = loginPrompt(users, sc);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }

            } else if (cur instanceof Administrator) {
                Administrator admin = (Administrator) cur;
                switch (ch) {
                    case 1:
                        admin.createUser(users); // دالة الإنشاء في Administrator هي المسؤولة عن إضافة المستخدم وحفظه
                        break;
                    case 2:
                        admin.modifySystemSettings();
                        break;
                    case 3:
                        admin.manageUserAccess(users); // استدعاء دالة manageUserAccess الموجودة في كائن admin
                        break;
                    case 4:
                        cur.logout();
                        cur = loginPrompt(users, sc);
                        break;
                    case 5:
                        admin.addFlight(flights); // استدعاء دالة addFlight من كائن admin
                        break;
                    case 6:
                        admin.removeFlight(flights); // استدعاء دالة removeFlight من كائن admin
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } else {
                System.out.println("Unknown user role. Exiting.");
                break;
            }
        }
        sc.close();
    }

    static User loginPrompt(List<User> users, Scanner sc) {
        while (true) {
            System.out.print("Username: ");
            String u = sc.nextLine().trim();
            System.out.print("Password: ");
            String p = sc.nextLine().trim();

            Optional<User> match = users.stream()
                    .filter(x -> x.login(u, p))
                    .findFirst();

            if (match.isPresent()) {
                return match.get();
            } else {
                System.out.println("Invalid credentials or user not found. Please try again.\n");
            }
        }
    }

    static void handleCustomerSearchAndBook(Customer c, List<Flight> flights,
                                            List<Passenger> pax, List<Booking> bookings, Scanner sc) throws Exception {
        System.out.print("Enter origin: ");
        String origin = sc.nextLine();
        System.out.print("Enter destination: ");
        String destination = sc.nextLine();

        Date searchDate = null;
        while (true) {
            System.out.print("Enter date (yyyy-MM-dd): ");
            String dateStr = sc.nextLine();
            try {
                searchDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                break;
            } catch (ParseException e) {
                System.out.println("Invalid date format.");
            }
        }

        final Date finalSearchDate = searchDate;
        List<Flight> available = flights.stream()
                .filter(f -> f.getOrigin().equalsIgnoreCase(origin) &&
                        f.getDestination().equalsIgnoreCase(destination))
                .filter(f -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(f.getDepartureTime()).equals(sdf.format(finalSearchDate));
                })
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("No available flights.");
            return;
        }

        System.out.println("Available Flights:");
        for (Flight f : available) {
            System.out.println(f);
        }

        System.out.print("Enter flight number to book: ");
        String fn = sc.nextLine();
        Flight picked = available.stream()
                .filter(f -> f.getFlightNumber().equals(fn))
                .findFirst()
                .orElse(null);

        if (picked == null) {
            System.out.println("Invalid flight number.");
            return;
        }

        if (!picked.checkAvailability()) {
            System.out.println("No available seats for this flight.");
            return;
        }

        System.out.print("Number of passengers: ");
        int n = Integer.parseInt(sc.nextLine());

        List<Passenger> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.print("Passenger " + (i + 1) + " name: ");
            String name = sc.nextLine();
            System.out.print("Passport number: ");
            String passport = sc.nextLine();

            Date dob = null;
            while (dob == null) {
                System.out.print("Date of birth (yyyy-MM-dd): ");
                String dobStr = sc.nextLine();
                try {
                    dob = new SimpleDateFormat("yyyy-MM-dd").parse(dobStr);
                } catch (ParseException e) {
                    System.out.println("Invalid date format.");
                }
            }

            list.add(new Passenger(UUID.randomUUID().toString(), name, passport, dob, ""));
        }

        Booking booking = c.createBooking(picked, list);
        bookings.add(booking);
        pax.addAll(list);

        // ========= Payment Start =========
        System.out.println("Choose Payment Method:");
        System.out.println("1. Credit Card");
        System.out.println("2. Bank Transfer");
        System.out.println("3. PayPal");
        System.out.print("Enter choice: ");
        int payChoice = Integer.parseInt(sc.nextLine());

        PaymentMethod method = null;

        switch (payChoice) {
            case 1:
                System.out.print("Card Number: ");
                String cardNumber = sc.nextLine();
                System.out.print("Expiry Date (MM/YY): ");
                String expiry = sc.nextLine();
                System.out.print("CVV: ");
                String cvv = sc.nextLine();
                method = new CreditCardPaymentMethod(cardNumber, expiry, cvv);
                break;

            case 2:
                System.out.print("Bank Name: ");
                String bank = sc.nextLine();
                System.out.print("Account Number: ");
                String account = sc.nextLine();
                System.out.print("SWIFT Code: ");
                String swift = sc.nextLine();
                method = new BankTransferPaymentMethod(bank, account, swift);
                break;

            case 3:
                System.out.print("PayPal Email: ");
                String email = sc.nextLine();
                method = new PayPalPaymentMethod(email);
                break;

            default:
                System.out.println("Invalid payment method.");
                return;
        }

        double totalAmount;
        try {
            totalAmount = booking.calculateTotalCost("Economy");
        } catch (PaymentFailedException e) {
            System.out.println("Payment failed: " + e.getMessage());
            booking.setPaymentStatus("Failed");
            return;
        }

        Payment payment = new Payment(booking.getBookingReference(), totalAmount, method);

        // ==== تعطيل التحقق بخوارزمية Luhn عند الدفع (تخطي validate) ====
        try {
            // ❗ لا تستدعي payment.validatePaymentDetails() لتخطي Luhn
            payment.processPayment(); // مباشرة بدون التحقق من الرقم
            booking.setPayment(payment);
            booking.setPaymentStatus(payment.getStatus().toString());
            System.out.println("Payment completed successfully.");
        } catch (Exception e) {
            System.out.println(" Payment failed: " + e.getMessage());
            booking.setPaymentStatus("Failed");
            return;
        }

        try {
            FileManager.saveBookings(bookings);
            FileManager.savePassengers(pax);
        } catch (IOException e) {
            System.out.println("Error saving bookings: " + e.getMessage());
        }

        System.out.println("Booking successful:\n" + booking.generateItinerary());
    }





    static void handleAgentBooking(Agent a, List<Customer> customers,
                                   List<Flight> flights, List<Passenger> pax,
                                   List<Booking> bookings, Scanner sc) throws Exception {
        System.out.print("Enter customer ID: ");
        String custId = sc.nextLine();
        Customer c = customers.stream()
                .filter(ct -> ct.getUserId().equals(custId))
                .findFirst()
                .orElse(null);
        if (c == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter Flight Number: ");
        String flightNumber = sc.nextLine();
        Flight flight = flights.stream()
                .filter(f -> f.getFlightNumber().equals(flightNumber))
                .findFirst()
                .orElse(null);
        if (flight == null) {
            System.out.println("Flight not found.");
            return;
        }

        Booking b = a.createBookingForCustomer(c, flight, pax);
        if (b != null) {
            bookings.add(b);
            try {
                FileManager.saveBookings(bookings);
            } catch (IOException e) {
                System.out.println("Error saving bookings: " + e.getMessage());
            }
            System.out.println("Booking created successfully:\n" + b.generateItinerary());
        }
    }

    static boolean isValidPassword(String password) {
        if (password.length() < 6) return false;

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasLetter && hasDigit;
    }


    public static void saveUsersToFile(List<User> users) {
        try {
            FileManager.saveUsers(users);
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public static void saveFlightsToFile(List<Flight> flights) {
        try {
            FileManager.saveFlights(flights);
        } catch (IOException e) {
            System.out.println("Error saving flights: " + e.getMessage());
        }
    }

    public static void manageUserAccess(List<User> users) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Manage User Access Menu:");
        System.out.println("1. List All Users");
        System.out.println("2. Remove User");
        System.out.print("Enter choice: ");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.println("Listing All Users:");
                for (User user : users) {
                    System.out.println(user);
                }
                break;
            case 2:
                System.out.print("Enter User ID to remove: ");
                String userIdToRemove = sc.nextLine();
                User userToRemove = users.stream()
                        .filter(u -> u.getUserId().equals(userIdToRemove))
                        .findFirst().orElse(null);
                if (userToRemove != null) {
                    users.remove(userToRemove);
                    System.out.println("User removed successfully.");
                    saveUsersToFile(users);
                } else {
                    System.out.println("User not found.");
                }
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }


}