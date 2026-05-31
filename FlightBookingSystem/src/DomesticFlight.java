import java.util.Date;
import java.util.Map;

public class DomesticFlight extends Flight {

    // Constructor: Initializes the domestic flight with all relevant details
    public DomesticFlight(String fn, String al, String o, String d,
                          Date dep, Date arr,
                          Map<String, Integer> seats, Map<String, Double> prices) {
        // Call the super constructor of the Flight class
        super(fn, al, o, d, dep, arr, seats, prices);
    }

    // Calculate price method for domestic flights
    @Override
    public double calculatePrice(String cls) {
        // Get the base price from the price map, return 0.0 if not found
        Double basePrice = getPrices().get(cls);

        // If class is not found, print a message and return 0.0
        if (basePrice == null) {
            System.out.println("Class not found in price list. Returning default price of 0.0");
            return 0.0;
        }

        // Return the price for the specified class
        return basePrice;
    }
}