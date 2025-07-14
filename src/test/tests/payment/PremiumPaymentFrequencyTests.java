package tests.payment;

import org.junit.jupiter.api.Test;
import payment.PremiumPaymentFrequency;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PremiumPaymentFrequencyTests {
    @Test
    public void givenAnnualPremiumFrequency_whenGettingValueInMonths_thenReturnsTwelve() {
        var premiumPaymentFrequency = PremiumPaymentFrequency.ANNUAL;

        int valueInMonths = premiumPaymentFrequency.getValueInMonths();

        assertEquals(12, valueInMonths);
    }

    @Test
    public void givenSemiAnnualPremiumFrequency_whenGettingValueInMonths_thenReturnsSix() {
        var premiumPaymentFrequency = PremiumPaymentFrequency.SEMI_ANNUAL;

        int valueInMonths = premiumPaymentFrequency.getValueInMonths();

        assertEquals(6, valueInMonths);
    }

    @Test
    public void givenQuarterlyFrequency_whenGettingValueInMonths_thenReturnsThree() {
        var premiumPaymentFrequency = PremiumPaymentFrequency.QUARTERLY;

        int valueInMonths = premiumPaymentFrequency.getValueInMonths();

        assertEquals(3, valueInMonths);
    }

    @Test
    public void givenMonthlyPremiumFrequency_whenGettingValueInMonths_thenReturnsOne() {
        var premiumPaymentFrequency = PremiumPaymentFrequency.MONTHLY;

        int valueInMonths = premiumPaymentFrequency.getValueInMonths();

        assertEquals(1, valueInMonths);
    }
}
