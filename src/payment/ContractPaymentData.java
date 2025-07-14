package payment;

import java.time.LocalDateTime;

public class ContractPaymentData {
    private int premium;
    private PremiumPaymentFrequency premiumPaymentFrequency;
    private LocalDateTime nextPaymentTime;
    private int outstandingBalance;

    public ContractPaymentData(int premium, PremiumPaymentFrequency premiumPaymentFrequency,
                               LocalDateTime nextPaymentTime, int outstandingBalance) {
        // Či nie je premium kladný
        if (premium <= 0) {
            throw new IllegalArgumentException("Premium amount cannot be negative.");
        }

        // Či je premiumPaymentFrequency null
        if (premiumPaymentFrequency == null) {
            throw new IllegalArgumentException("Premium payment frequency cannot be null.");
        }

        // Či je nextPaymentTime null
        if (nextPaymentTime == null) {
            throw new IllegalArgumentException("Next payment time cannot be null.");
        }

        this.premium = premium;
        this.premiumPaymentFrequency = premiumPaymentFrequency;
        this.nextPaymentTime = nextPaymentTime;
        this.outstandingBalance = outstandingBalance;
    }

    public int getPremium() {
        return premium;
    }

    public void setPremium(int premium) {
        if (premium <= 0) {
            throw new IllegalArgumentException("Premium amount cannot be negative.");
        }
        this.premium = premium;
    }

    public void setOutstandingBalance(int outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public int getOutstandingBalance() {
        return outstandingBalance;
    }

    public void decreaseOutstandingBalance(int amount) {
        this.outstandingBalance -= amount;
    }

    public void setPremiumPaymentFrequency(PremiumPaymentFrequency premiumPaymentFrequency) {
        if (premiumPaymentFrequency == null) {
            throw new IllegalArgumentException("Premium payment frequency cannot be null.");
        }
        this.premiumPaymentFrequency = premiumPaymentFrequency;
    }

    public PremiumPaymentFrequency getPremiumPaymentFrequency() {
        return premiumPaymentFrequency;
    }

    public LocalDateTime getNextPaymentTime() {
        return nextPaymentTime;
    }

    public void updateNextPaymentTime() {
        int monthsToAdd = premiumPaymentFrequency.getValueInMonths();
        nextPaymentTime = nextPaymentTime.plusMonths(monthsToAdd);
    }
}
