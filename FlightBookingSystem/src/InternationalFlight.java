import java.util.Date;
import java.util.Map;

public class InternationalFlight extends Flight {

    // Constructor: Initializes the international flight with all relevant details
    public InternationalFlight(String fn, String al, String o, String d,
                               Date dep, Date arr,
                               Map<String, Integer> seats, Map<String, Double> prices) {
        super(fn, al, o, d, dep, arr, seats, prices);
    }

    // Calculate price method with a 20% increase for international flights
    @Override
    public double calculatePrice(String cls) {
        // Get the base price from the map
        Double basePrice = getPrices().get(cls);

        // If the class is not available in the price map, return 0.0 (or throw an exception)
        if (basePrice == null) {
            System.out.println("Class not found in price list.");
            return 0.0;
        }

        // Return the price with a 20% increase for international flights
        return basePrice * 1.20;
    }
}
