class BankTransferPaymentMethod extends PaymentMethod {
    private String bankName;
    private String accountNumber;
    private String swiftCode;

    public BankTransferPaymentMethod(String bankName, String accountNumber, String swiftCode) {
        super("Bank Transfer");
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.swiftCode = swiftCode;
    }

    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getSwiftCode() { return swiftCode; }
}

class PayPalPaymentMethod extends PaymentMethod {
    private String paypalEmail;

    public PayPalPaymentMethod(String paypalEmail) {
        super("PayPal");
        this.paypalEmail = paypalEmail;
    }

    public String getPaypalEmail() { return paypalEmail; }
}

// Custom Exception for Payment Failures
class PaymentFailedException extends Exception {
    public PaymentFailedException(String message) {
        super(message);
    }
}