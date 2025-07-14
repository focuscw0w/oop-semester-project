package payment;

public enum PremiumPaymentFrequency {
    ANNUAL(12),
    SEMI_ANNUAL(6),
    QUARTERLY(3),
    MONTHLY(1);

    private final int monthsValue;

    PremiumPaymentFrequency(int monthsValue) {
        this.monthsValue = monthsValue;
    }

    public int getValueInMonths() {
        return monthsValue;
    }
}
