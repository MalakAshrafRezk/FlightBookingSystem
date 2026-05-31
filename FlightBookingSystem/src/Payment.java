import java.util.Date;
import java.util.UUID;


class Payment {

    private String paymentId;
    private String bookingReference;
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private Date transactionDate;

    // Constructor
    public Payment(String bookingReference, double amount, PaymentMethod method) {
        this.paymentId = UUID.randomUUID().toString(); // Generate unique Payment ID
        this.bookingReference = bookingReference;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.transactionDate = new Date();
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public String getBookingReference() { return bookingReference; }
    public double getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public Date getTransactionDate() { return transactionDate; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void processPayment() {
        this.status = PaymentStatus.SUCCESSFUL;
        System.out.println("Payment processed successfully (simulated).");
    }

    public void validatePaymentDetails() throws PaymentFailedException {
        // Basic validation - Amount must be positive
        if (amount <= 0) {
            this.status = PaymentStatus.FAILED;
            throw new PaymentFailedException("Invalid payment amount.");
        }

        // Additional validation logic can be added based on the PaymentMethod (e.g., credit card number format)
        if (method instanceof CreditCardPaymentMethod) {
            CreditCardPaymentMethod creditCardMethod = (CreditCardPaymentMethod) method;
            if (!isValidCreditCardNumber(creditCardMethod.getCardNumber())) {
                this.status = PaymentStatus.FAILED;
                throw new PaymentFailedException("Invalid credit card number.");
            }
            // Add expiry date and CVV validation as needed
        }
        // ... other payment method validations
    }

    // Simple Luhn's algorithm for credit card validation (can be improved)
    private boolean isValidCreditCardNumber(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", bookingReference='" + bookingReference + '\'' +
                ", amount=" + amount +
                ", method=" + method +
                ", status=" + status +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
abstract class PaymentMethod {
    private String type;

    public PaymentMethod(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
enum PaymentStatus {
    PENDING,
    SUCCESSFUL,
    FAILED
}