package payment;

import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class ContractPaymentDataTest {

    private final LocalDateTime testDate = LocalDateTime.of(2023, 1, 1, 0, 0);

    // Premium tests
    @Test
    void testValidPremium() {
        // Test with valid premium values
        ContractPaymentData data1 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);
        assertEquals(100, data1.getPremium());

        // Test with minimum valid premium
        ContractPaymentData data2 = new ContractPaymentData(1, PremiumPaymentFrequency.ANNUAL, testDate, 0);
        assertEquals(1, data2.getPremium());

        // Test with large premium value
        ContractPaymentData data3 = new ContractPaymentData(999999, PremiumPaymentFrequency.ANNUAL, testDate, 0);
        assertEquals(999999, data3.getPremium());
    }

    @Test
    void testInvalidPremium() {
        // Zero premium should throw exception
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(0, PremiumPaymentFrequency.ANNUAL, testDate, 0));

        // Negative premium should throw exception
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(-1, PremiumPaymentFrequency.ANNUAL, testDate, 0));

        // Large negative premium should throw exception
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(-99999, PremiumPaymentFrequency.ANNUAL, testDate, 0));
    }

    // Frequency tests
    @Test
    void testValidFrequency() {
        // Test with all valid frequency values
        ContractPaymentData data1 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);
        assertEquals(PremiumPaymentFrequency.ANNUAL, data1.getPremiumPaymentFrequency());

        ContractPaymentData data2 = new ContractPaymentData(100, PremiumPaymentFrequency.SEMI_ANNUAL, testDate, 0);
        assertEquals(PremiumPaymentFrequency.SEMI_ANNUAL, data2.getPremiumPaymentFrequency());

        ContractPaymentData data3 = new ContractPaymentData(100, PremiumPaymentFrequency.QUARTERLY, testDate, 0);
        assertEquals(PremiumPaymentFrequency.QUARTERLY, data3.getPremiumPaymentFrequency());

        ContractPaymentData data4 = new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY, testDate, 0);
        assertEquals(PremiumPaymentFrequency.MONTHLY, data4.getPremiumPaymentFrequency());
    }

    @Test
    void testNullFrequency() {
        // Null frequency should throw exception
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(100, null, testDate, 0));
    }

    @Test
    void testFrequencyMonthValues() {
        assertEquals(12, PremiumPaymentFrequency.ANNUAL.getValueInMonths());
        assertEquals(6, PremiumPaymentFrequency.SEMI_ANNUAL.getValueInMonths());
        assertEquals(3, PremiumPaymentFrequency.QUARTERLY.getValueInMonths());
        assertEquals(1, PremiumPaymentFrequency.MONTHLY.getValueInMonths());
    }

    // NextPaymentTime tests
    @Test
    void testValidNextPaymentTime() {
        // Test with valid nextPaymentTime
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);
        assertEquals(testDate, data.getNextPaymentTime());

        // Test with a different date
        LocalDateTime futureDate = LocalDateTime.of(2024, 6, 15, 10, 30);
        ContractPaymentData data2 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, futureDate, 0);
        assertEquals(futureDate, data2.getNextPaymentTime());

        // Test with past date
        LocalDateTime pastDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        ContractPaymentData data3 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, pastDate, 0);
        assertEquals(pastDate, data3.getNextPaymentTime());
    }

    @Test
    void testNullNextPaymentTime() {
        // Null nextPaymentTime should throw exception
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, null, 0));
    }

    // Outstanding balance tests
    @Test
    void testOutstandingBalanceValues() {
        // Test with zero balance
        ContractPaymentData data1 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);
        assertEquals(0, data1.getOutstandingBalance());

        // Test with positive balance (underpayment)
        ContractPaymentData data2 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 50);
        assertEquals(50, data2.getOutstandingBalance());

        // Test with negative balance (overpayment)
        ContractPaymentData data3 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, -25);
        assertEquals(-25, data3.getOutstandingBalance());
    }

    @Test
    void testSetOutstandingBalance() {
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);

        // Set positive value
        data.setOutstandingBalance(200);
        assertEquals(200, data.getOutstandingBalance());

        // Set negative value
        data.setOutstandingBalance(-50);
        assertEquals(-50, data.getOutstandingBalance());

        // Set zero
        data.setOutstandingBalance(0);
        assertEquals(0, data.getOutstandingBalance());
    }

    // UpdateNextPaymentTime tests
    @Test
    void testUpdateNextPaymentTime() {
        // Test for MONTHLY frequency
        ContractPaymentData monthlyData = new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY, testDate, 0);
        monthlyData.updateNextPaymentTime();
        assertEquals(testDate.plusMonths(1), monthlyData.getNextPaymentTime());

        // Test for QUARTERLY frequency
        ContractPaymentData quarterlyData = new ContractPaymentData(100, PremiumPaymentFrequency.QUARTERLY, testDate, 0);
        quarterlyData.updateNextPaymentTime();
        assertEquals(testDate.plusMonths(3), quarterlyData.getNextPaymentTime());

        // Test for SEMI_ANNUAL frequency
        ContractPaymentData semiAnnualData = new ContractPaymentData(100, PremiumPaymentFrequency.SEMI_ANNUAL, testDate, 0);
        semiAnnualData.updateNextPaymentTime();
        assertEquals(testDate.plusMonths(6), semiAnnualData.getNextPaymentTime());

        // Test for ANNUAL frequency
        ContractPaymentData annualData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);
        annualData.updateNextPaymentTime();
        assertEquals(testDate.plusMonths(12), annualData.getNextPaymentTime());

        // Test multiple successive updates
        ContractPaymentData multipleUpdates = new ContractPaymentData(100, PremiumPaymentFrequency.QUARTERLY, testDate, 0);
        multipleUpdates.updateNextPaymentTime(); // +3 months
        multipleUpdates.updateNextPaymentTime(); // +3 more months
        assertEquals(testDate.plusMonths(6), multipleUpdates.getNextPaymentTime());
    }

    // Setter method tests
    @Test
    void testSetInvalidPremium() {
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);

        // Zero premium should throw exception
        assertThrows(IllegalArgumentException.class, () -> data.setPremium(0));

        // Negative premium should throw exception
        assertThrows(IllegalArgumentException.class, () -> data.setPremium(-1));

        // Large negative premium should throw exception
        assertThrows(IllegalArgumentException.class, () -> data.setPremium(-99999));

        // Premium should remain unchanged
        assertEquals(100, data.getPremium());
    }

    @Test
    void testSetValidPremium() {
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);

        // Set valid premium
        data.setPremium(200);
        assertEquals(200, data.getPremium());

        // Set minimum valid premium
        data.setPremium(1);
        assertEquals(1, data.getPremium());

        // Set large valid premium
        data.setPremium(999999);
        assertEquals(999999, data.getPremium());
    }

    @Test
    void testSetInvalidFrequency() {
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);

        // Null frequency should throw exception
        assertThrows(IllegalArgumentException.class, () -> data.setPremiumPaymentFrequency(null));

        // Frequency should remain unchanged
        assertEquals(PremiumPaymentFrequency.ANNUAL, data.getPremiumPaymentFrequency());
    }

    @Test
    void testSetValidFrequency() {
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, testDate, 0);

        // Set to MONTHLY
        data.setPremiumPaymentFrequency(PremiumPaymentFrequency.MONTHLY);
        assertEquals(PremiumPaymentFrequency.MONTHLY, data.getPremiumPaymentFrequency());

        // Set to QUARTERLY
        data.setPremiumPaymentFrequency(PremiumPaymentFrequency.QUARTERLY);
        assertEquals(PremiumPaymentFrequency.QUARTERLY, data.getPremiumPaymentFrequency());

        // Set to SEMI_ANNUAL
        data.setPremiumPaymentFrequency(PremiumPaymentFrequency.SEMI_ANNUAL);
        assertEquals(PremiumPaymentFrequency.SEMI_ANNUAL, data.getPremiumPaymentFrequency());

        // Set to ANNUAL
        data.setPremiumPaymentFrequency(PremiumPaymentFrequency.ANNUAL);
        assertEquals(PremiumPaymentFrequency.ANNUAL, data.getPremiumPaymentFrequency());
    }

    // Constructor combinations tests
    @Test
    void testAllInvalidCombinations() {
        // Invalid premium, valid frequency, valid nextPaymentTime
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(0, PremiumPaymentFrequency.ANNUAL, testDate, 0));

        // Valid premium, invalid frequency, valid nextPaymentTime
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(100, null, testDate, 0));

        // Valid premium, valid frequency, invalid nextPaymentTime
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, null, 0));

        // All invalid parameters
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(-1, null, null, 0));
    }

    @Test
    void testMixedValidAndInvalidCombinations() {
        // Invalid premium, invalid frequency, valid nextPaymentTime
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(-1, null, testDate, 0));

        // Invalid premium, valid frequency, invalid nextPaymentTime
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(-1, PremiumPaymentFrequency.ANNUAL, null, 0));

        // Valid premium, invalid frequency, invalid nextPaymentTime
        assertThrows(IllegalArgumentException.class,
                () -> new ContractPaymentData(100, null, null, 0));
    }
}