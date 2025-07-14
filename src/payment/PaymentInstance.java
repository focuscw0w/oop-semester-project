package payment;

import java.time.LocalDateTime;

public class PaymentInstance implements Comparable<PaymentInstance> {
    private final LocalDateTime paymentTime;
    private final int paymentAmount;

    public PaymentInstance(LocalDateTime paymentTime, int paymentAmount) {
        // Či je paymentTime null
        if (paymentTime == null) {
            throw new IllegalArgumentException("Payment time is null");
        }

        // Či nie je paymentAmount kladný
        if (paymentAmount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        this.paymentTime = paymentTime;
        this.paymentAmount = paymentAmount;
    }

    public int compareTo(PaymentInstance other) {
        return this.paymentTime.compareTo(other.paymentTime);
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }
}
