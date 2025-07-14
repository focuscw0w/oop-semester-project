package tests.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ContractPaymentDataTests {
    private int premium;
    private PremiumPaymentFrequency premiumPaymentFrequency;
    private LocalDateTime nextPaymentTime;
    private int outstandingBalance;

    @BeforeEach
    public void setUp() {
        premium = 20;
        premiumPaymentFrequency = PremiumPaymentFrequency.SEMI_ANNUAL;
        nextPaymentTime = LocalDateTime.now().plusMonths(2);
        outstandingBalance = 15;
    }

    @Test
    public void givenPremiumIsZero_whenCreatingContractPaymentData_thenThrowsIllegalArgumentException() {
        premium = 0;

        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime, outstandingBalance)
        );
    }

    @Test
    public void givenPremiumIsNegative_whenCreatingContractPaymentData_thenThrowsIllegalArgumentException() {
        premium = -10;

        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime, outstandingBalance)
        );
    }

    @Test
    public void givenPremiumPaymentFrequencyIsNull_whenCreatingContractPaymentData_thenThrowsIllegalArgumentException() {
        premiumPaymentFrequency = null;

        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime, outstandingBalance)
        );
    }

    @Test
    public void givenNextPaymentTimeIsNull_whenCreatingContractPaymentData_thenThrowsIllegalArgumentException() {
        nextPaymentTime = null;

        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime, outstandingBalance)
        );
    }

    @Test
    public void givenValidData_whenCreatingContractPaymentData_thenValuesAreSet() {
        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertEquals(premium, contractPaymentData.getPremium());
        assertEquals(premiumPaymentFrequency, contractPaymentData.getPremiumPaymentFrequency());
        assertEquals(nextPaymentTime, contractPaymentData.getNextPaymentTime());
        assertEquals(outstandingBalance, contractPaymentData.getOutstandingBalance());
    }

    @Test
    public void givenPaymentDataWithAnnualFrequency_whenUpdatingNextPaymentTime_thenNextPaymentTimeIsOneYearLater() {
        premiumPaymentFrequency = PremiumPaymentFrequency.ANNUAL;
        var nextPaymentTime = LocalDateTime.of(2025, 11, 13, 0, 0);
        var expectedNextPaymentTime = LocalDateTime.of(2026, 11, 13, 0, 0);

        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertEquals(nextPaymentTime, contractPaymentData.getNextPaymentTime());

        contractPaymentData.updateNextPaymentTime();

        assertEquals(expectedNextPaymentTime, contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenPaymentDataWithSemiAnnualFrequency_whenUpdatingNextPaymentTime_thenNextPaymentTimeIsSixMonthsLater() {
        premiumPaymentFrequency = PremiumPaymentFrequency.SEMI_ANNUAL;
        var nextPaymentTime = LocalDateTime.of(2025, 11, 13, 0, 0);
        var expectedNextPaymentTime = LocalDateTime.of(2026, 5, 13, 0, 0);

        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertEquals(nextPaymentTime, contractPaymentData.getNextPaymentTime());

        contractPaymentData.updateNextPaymentTime();

        assertEquals(expectedNextPaymentTime, contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenPaymentDataWithQuarterlyFrequency_whenUpdatingNextPaymentTime_thenNextPaymentTimeIsThreeMonthsLater() {
        premiumPaymentFrequency = PremiumPaymentFrequency.QUARTERLY;
        var nextPaymentTime = LocalDateTime.of(2025, 11, 13, 0, 0);
        var expectedNextPaymentTime = LocalDateTime.of(2026, 2, 13, 0, 0);

        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertEquals(nextPaymentTime, contractPaymentData.getNextPaymentTime());

        contractPaymentData.updateNextPaymentTime();

        assertEquals(expectedNextPaymentTime, contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenPaymentDataWithMonthlyFrequency_whenUpdatingNextPaymentTime_thenNextPaymentTimeIsOneMonthLater() {
        premiumPaymentFrequency = PremiumPaymentFrequency.MONTHLY;
        var nextPaymentTime = LocalDateTime.of(2025, 11, 13, 0, 0);
        var expectedNextPaymentTime = LocalDateTime.of(2025, 12, 13, 0, 0);

        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertEquals(nextPaymentTime, contractPaymentData.getNextPaymentTime());

        contractPaymentData.updateNextPaymentTime();

        assertEquals(expectedNextPaymentTime, contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenNewPremiumIsZero_whenSettingPremium_thenThrowsIllegalArgumentException() {
        var newPremium = 0;

        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertThrows(IllegalArgumentException.class, () -> contractPaymentData.setPremium(newPremium));
    }

    @Test
    public void givenNewPremiumIsNegative_whenSettingPremium_thenThrowsIllegalArgumentException() {
        var newPremium = -10;

        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertThrows(IllegalArgumentException.class, () -> contractPaymentData.setPremium(newPremium));

    }

    @Test
    public void givenNewPremiumIsPositive_whenSettingPremium_thenNewPremiumIsSet() {
        var newPremium = 10;

        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        contractPaymentData.setPremium(newPremium);

        assertEquals(newPremium, contractPaymentData.getPremium());
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, 0, 10})
    public void givenNewOutstandingBalance_whenSettingOutstandingBalance_thenNewOutstandingBalanceIsSet(int outstandingBalance) {
        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        contractPaymentData.setOutstandingBalance(outstandingBalance);

        assertEquals(outstandingBalance, contractPaymentData.getOutstandingBalance());
    }

    @Test
    public void givenNewPremiumPaymentFrequencyIsNull_whenSettingPremiumPaymentFrequency_thenThrowsIllegalArgumentException() {
        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertThrows(IllegalArgumentException.class, () -> contractPaymentData.setPremiumPaymentFrequency(null));
    }

    @Test
    public void givenNewPremiumPaymentFrequency_whenSettingPremiumPaymentFrequency_thenNewPremiumPaymentFrequencyIsSet() {
        var newPremiumPaymentFrequency = PremiumPaymentFrequency.MONTHLY;
        var contractPaymentData = new ContractPaymentData(premium, premiumPaymentFrequency, nextPaymentTime,
                outstandingBalance);

        assertNotEquals(newPremiumPaymentFrequency, contractPaymentData.getPremiumPaymentFrequency());

        contractPaymentData.setPremiumPaymentFrequency(newPremiumPaymentFrequency);

        assertEquals(newPremiumPaymentFrequency, contractPaymentData.getPremiumPaymentFrequency());
    }

}