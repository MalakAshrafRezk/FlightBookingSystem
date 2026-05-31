
class CreditCardPaymentMethod extends PaymentMethod {
    private String cardNumber;
    private String expiryDate;
    private String cvv;


    public CreditCardPaymentMethod(String cardNumber, String expiryDate, String cvv) {
        super("Credit Card");
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;

    }

    public String getCardNumber() { return cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public String getCvv() { return cvv; }


    // Additional validation methods for credit card details can be added
}