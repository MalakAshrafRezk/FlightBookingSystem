import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Administrator extends User {

    private int securityLevel;
    private String adminId;

    public Administrator(String userId, String username, String password, String name, String email, String contactInfo, int securityLevel, String adminId) {
        super(userId, username, password, name, email, contactInfo, 1);
        this.securityLevel = securityLevel;
        this.adminId = adminId;
        super.role = "Administrator";
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }


    @Override
    public void showMenu() {
        System.out.println("Admin Menu:");
        System.out.println("1. Create User");
        System.out.println("2. Modify System Settings");
        System.out.println("3. Manage User Access");
        System.out.println("4. Logout");
        System.out.println("5. Add Flight");
        System.out.println("6. Remove Flight");
    }

    public void createUser(List<User> users) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter User Type (customer, agent, administrator): ");
        String userType = sc.nextLine().toLowerCase();

        System.out.print("Enter User ID: ");
        String userId = sc.nextLine();
        System.out.print("Enter Username: ");
        String username = sc.nextLine();

        String password;
        while (true) {
            System.out.print("Enter Password: ");
            password = sc.nextLine();
            if (!Main.isValidPassword(password)) {
                System.out.println("Password must be at least 6 characters long and contain letters and numbers.");
            } else {
                break;
            }
        }

        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Contact Info: ");
        String contactInfo = sc.nextLine();

        User newUser = null;

        switch (userType) {
            case "customer":
                System.out.print("Enter Address: ");
                String address = sc.nextLine();
                System.out.print("Enter Preferences: ");
                String preferences = sc.nextLine();
                newUser = new Customer(userId, username, password, name, email, contactInfo, address, preferences);
                break;

            case "agent":
                System.out.print("Enter Agency Name: ");
                String agency = sc.nextLine();

                double salary = 0;
                while (true) {
                    System.out.print("Enter Salary: ");
                    String salaryInput = sc.nextLine();
                    try {
                        salary = Double.parseDouble(salaryInput);
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println(" Invalid salary. Please enter a valid number (e.g., 3500.50).");
                    }
                }

                newUser = new Agent(userId, username, password, name, email, contactInfo, agency, salary);
                break;

            case "administrator":
                System.out.print("Enter Department: ");
                String department = sc.nextLine();
                System.out.print("Enter Access Level (int): ");
                int accessLevel = 0;
                while (true) {
                    try {
                        accessLevel = Integer.parseInt(sc.nextLine());
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println(" Invalid access level. Please enter an integer.");
                    }
                }
                newUser = new Administrator(userId, username, password, name, email, contactInfo, accessLevel, "AdminID" + userId);
                break;

            default:
                System.out.println("Invalid user type.");
                return;
        }

        if (newUser != null) {
            users.add(newUser);
            System.out.println(" User created successfully.");
            Main.saveUsersToFile(users);
        }
    }


    public void modifySystemSettings() {
        Scanner sc = new Scanner(System.in);
        System.out.println("1. Modify Max Passengers Per Flight");
        System.out.println("2. Modify Default Price for Economy Class");
        System.out.println("3. Modify Booking Timeout");
        System.out.print("Enter the setting to modify (1/2/3): ");
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter new max passengers per flight: ");
                int newMaxPassengers = sc.nextInt();
                System.out.println("Max passengers per flight updated to: " + newMaxPassengers);
                break;
            case 2:
                System.out.print("Enter new price for Economy Class: ");
                double newPrice = sc.nextDouble();
                System.out.println("Default price for Economy Class updated to: " + newPrice);
                break;
            case 3:
                System.out.print("Enter new booking timeout (in minutes): ");
                int newTimeout = sc.nextInt();
                System.out.println("Booking timeout updated to: " + newTimeout + " minutes");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }




    public void manageUserAccess(List<User> users) {
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
                    Main.saveUsersToFile(users);
                } else {
                    System.out.println("User not found.");
                }
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    public void addFlight(List<Flight> flights) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Flight Number: ");
        String flightNumber = sc.nextLine();
        System.out.print("Enter Airline: ");
        String airline = sc.nextLine();
        System.out.print("Enter Origin: ");
        String origin = sc.nextLine();
        System.out.print("Enter Destination: ");
        String destination = sc.nextLine();
        System.out.print("Enter Departure Date (yyyy-MM-dd HH:mm:ss): ");
        String departureDateString = sc.nextLine();
        System.out.print("Enter Arrival Date (yyyy-MM-dd HH:mm:ss): ");
        String arrivalDateString = sc.nextLine();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date departureDate = null;
        Date arrivalDate = null;

        try {
            departureDate = dateFormat.parse(departureDateString);
            arrivalDate = dateFormat.parse(arrivalDateString);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd HH:mm:ss");
            return;
        }

        Map<String, Integer> availableSeats = new HashMap<>();
        Map<String, Double> prices = new HashMap<>();

        System.out.print("Enter Seat Types and Available Seats (e.g., Economy:100,Business:50): ");
        String seatsInput = sc.nextLine();
        String[] seatsPairs = seatsInput.split(",");
        for (String pair : seatsPairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                availableSeats.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            } else {
                System.out.println("Invalid seat format: " + pair);
                return;
            }
        }

        System.out.print("Enter Seat Prices (e.g., Economy:200.0,Business:500.0): ");
        String pricesInput = sc.nextLine();
        String[] pricePairs = pricesInput.split(",");
        for (String pair : pricePairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                prices.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
            } else {
                System.out.println("Invalid price format: " + pair);
                return;
            }
        }

        Flight newFlight = new DomesticFlight(flightNumber, airline, origin, destination, departureDate, arrivalDate, availableSeats, prices);
        flights.add(newFlight);
        System.out.println("Flight added successfully.");
        Main.saveFlightsToFile(flights);
    }

    public void removeFlight(List<Flight> flights) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Flight Number to remove: ");
        String flightNumber = sc.nextLine();
        boolean removed = false;
        for (int i = 0; i < flights.size(); i++) {
            Flight flight = flights.get(i);
            if (flight.getFlightNumber().equals(flightNumber)) {
                flights.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            System.out.println("Flight removed successfully.");
            Main.saveFlightsToFile(flights);
        } else {
            System.out.println("Flight not found.");
        }
    }

    @Override
    public void logout() {
        System.out.println("Administrator " + getUsername() + " logged out.");
    }
}