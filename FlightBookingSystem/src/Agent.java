import java.util.List;

public class Agent extends User {
    private String department;
    private double commission;

    public Agent(String userId, String username, String password,
                 String name, String email, String contactInfo,
                 String department, double commission) {
        super(userId, username, password, name, email, contactInfo, 2); // Set access level to 2 for Agent
        this.department = department;
        this.commission = commission;
        super.role = "Agent"; // Initialize role
    }

    public String getDepartment() {
        return department;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    /**
     * إنشاء حجز نيابة عن العميل
     */
    public Booking createBookingForCustomer(Customer customer, Flight flight, List<Passenger> passengers) {
        if (flight.checkAvailability()) {
            return customer.createBooking(flight, passengers);
        } else {
            System.out.println("Flight is not available.");
            return null;
        }
    }

    /**
     * إدارة الرحلات
     */
    public void manageFlights(List<Flight> flights) {
        System.out.println("Managing flights, total: " + flights.size());
        // يمكن إضافة منطق لإضافة/حذف الرحلات هنا
    }

    /**
     * إلغاء حجز نيابة عن العميل
     */
    public boolean cancelBookingForCustomer(Customer customer, String bookingId) {
        List<Booking> history = customer.getBookingHistory();

        for (Booking booking : history) {
            if (booking.getBookingReference().equals(bookingId)) {
                booking.cancelBooking();
                System.out.println("Booking cancelled successfully.");
                return true;
            }
        }
        System.out.println("Booking not found.");
        return false;
    }

    /**
     * تعديل بيانات العميل (العنوان والتفضيلات)
     */
    public void manageCustomerProfile(Customer customer, String newAddress, String newPreferences) {
        customer.setAddress(newAddress);
        customer.setPreferences(newPreferences);
        System.out.println("Customer profile updated successfully.");
    }

    @Override
    public void showMenu() {
        System.out.println("-- Agent Menu --");
        System.out.println("1. Create Booking for Customer");
        System.out.println("2. Manage Flights");
        System.out.println("3. Cancel Booking for Customer");
        System.out.println("4. Manage Customer Profile");
        System.out.println("5. Logout");
    }


}
