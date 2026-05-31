import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.ArrayList;

public class PaymentDAO {

    public static void savePayment(Payment p) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO payments (paymentId, bookingReference, amount, methodType, methodDetails, status, transactionDate)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        String methodType = p.getMethod().getType();
        String methodDetails = "";

        if (p.getMethod() instanceof CreditCardPaymentMethod cc) {
            methodDetails = cc.getCardNumber() + ";" + cc.getExpiryDate() + ";" + cc.getCvv();
        } else if (p.getMethod() instanceof BankTransferPaymentMethod bt) {
            methodDetails = bt.getBankName() + ";" + bt.getAccountNumber() + ";" + bt.getSwiftCode();
        } else if (p.getMethod() instanceof PayPalPaymentMethod pp) {
            methodDetails = pp.getPaypalEmail();
        }

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getPaymentId());
            pstmt.setString(2, p.getBookingReference());
            pstmt.setDouble(3, p.getAmount());
            pstmt.setString(4, methodType);
            pstmt.setString(5, methodDetails);
            pstmt.setString(6, p.getStatus().toString());
            pstmt.setLong(7, p.getTransactionDate().getTime());
            pstmt.executeUpdate();
        }
    }

    public static List<Payment> loadPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String paymentId = rs.getString("paymentId");
                String bookingRef = rs.getString("bookingReference");
                double amount = rs.getDouble("amount");
                String methodType = rs.getString("methodType");
                String methodDetails = rs.getString("methodDetails");
                PaymentStatus status = PaymentStatus.valueOf(rs.getString("status"));
                Date transactionDate = new Date(rs.getLong("transactionDate"));

                PaymentMethod method = null;
                String[] parts = methodDetails.split(";");
                switch (methodType) {
                    case "CreditCard" -> method = new CreditCardPaymentMethod(parts[0], parts[1], parts[2]);
                    case "BankTransfer" -> method = new BankTransferPaymentMethod(parts[0], parts[1], parts[2]);
                    case "PayPal" -> method = new PayPalPaymentMethod(parts[0]);
                }

                Payment payment = new Payment(bookingRef, amount, method);
                payment.setPaymentId(paymentId);
                payment.setStatus(status);
                payment.setTransactionDate(transactionDate);
                payments.add(payment);
            }
        }
        return payments;
    }
    public static void deletePayment(String paymentId) throws SQLException {
        String sql = "DELETE FROM payments WHERE paymentId = ?";

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paymentId);
            pstmt.executeUpdate();
        }
    }

    // Update the status of a payment (if needed)
    public static void updatePaymentStatus(String paymentId, PaymentStatus status) throws SQLException {
        String sql = "UPDATE payments SET status = ? WHERE paymentId = ?";

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.toString());
            pstmt.setString(2, paymentId);
            pstmt.executeUpdate();
        }
    }

}
