package payment;

import contracts.InvalidContractException;
import org.junit.jupiter.api.Test;
import payment.PaymentHandler;
import company.InsuranceCompany;
import contracts.AbstractContract;
import contracts.SingleVehicleContract;
import payment.PaymentInstance;
import objects.Person;
import objects.Vehicle;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentHandlerTest {

    private final LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 0, 0);

    @Test
    void testValidInsurerInConstructor() {
        // Create a valid insurer instance
        InsuranceCompany insurer = new InsuranceCompany(testTime);

        // Test with valid insurer
        PaymentHandler handler = new PaymentHandler(insurer);
        assertNotNull(handler);
    }

    @Test
    void testNullInsurerInConstructor() {
        // Null insurer should throw exception
        assertThrows(IllegalArgumentException.class, () -> new PaymentHandler(null));
    }

    @Test
    void testPaymentHistoryInitialization() {
        InsuranceCompany insurer = new InsuranceCompany(testTime);
        PaymentHandler handler = new PaymentHandler(insurer);

        // Verify that payment history is initialized (not null)
        Map<AbstractContract, Set<PaymentInstance>> paymentHistory = handler.getPaymentHistory();
        assertNotNull(paymentHistory);

        // Verify that payment history is empty initially
        assertTrue(paymentHistory.isEmpty());
    }

    @Test
    void testPaymentHistoryImmutability() {
        InsuranceCompany insurer = new InsuranceCompany(testTime);
        PaymentHandler handler = new PaymentHandler(insurer);

        // Get the initial reference to payment history
        Map<AbstractContract, Set<PaymentInstance>> initialHistory = handler.getPaymentHistory();
        assertNotNull(initialHistory);

        // Get the reference again and verify it's the same object
        Map<AbstractContract, Set<PaymentInstance>> secondReference = handler.getPaymentHistory();
        assertSame(initialHistory, secondReference);
    }

    @Test
    void testPayWithNullContract() {
        InsuranceCompany insurer = new InsuranceCompany(testTime);
        PaymentHandler handler = new PaymentHandler(insurer);

        // Pay with null contract should throw exception
        assertThrows(IllegalArgumentException.class, () -> handler.pay(null, 100));
    }

    @Test
    void testPayWithNonPositiveAmount() {
        InsuranceCompany insurer = new InsuranceCompany(testTime);
        PaymentHandler handler = new PaymentHandler(insurer);

        // Create different Person objects for policyholder and beneficiary
        Person policyholder = new Person("8004172022");  // Valid male birth number
        Person beneficiary = new Person("8054176383");   // Valid female birth number

        Vehicle vehicle = new Vehicle("ABC123D", 10000);
        SingleVehicleContract contract = insurer.insureVehicle(
                "C001", beneficiary, policyholder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle
        );

        // Pay with zero amount should throw exception
        assertThrows(IllegalArgumentException.class, () -> handler.pay(contract, 0));

        // Pay with negative amount should throw exception
        assertThrows(IllegalArgumentException.class, () -> handler.pay(contract, -10));
    }

    @Test
    void testPayWithInactiveContract() {
        InsuranceCompany insurer = new InsuranceCompany(testTime);
        PaymentHandler handler = new PaymentHandler(insurer);

        // Create different Person objects for policyholder and beneficiary
        Person policyholder = new Person("8004172022");  // Valid male birth number
        Person beneficiary = new Person("8054176383");   // Valid female birth number

        Vehicle vehicle = new Vehicle("ABC123D", 10000);
        SingleVehicleContract contract = insurer.insureVehicle(
                "C001", beneficiary, policyholder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle
        );

        // Set the contract inactive
        contract.setInactive();

        // Paying for inactive contract should throw InvalidContractException
        assertThrows(InvalidContractException.class, () -> handler.pay(contract, 100));
    }

    @Test
    void testPayWithContractFromDifferentInsurer() {
        InsuranceCompany insurer1 = new InsuranceCompany(testTime);
        InsuranceCompany insurer2 = new InsuranceCompany(testTime);
        PaymentHandler handler = new PaymentHandler(insurer1);

        // Create different Person objects for policyholder and beneficiary
        Person policyholder = new Person("8004172022");  // Valid male birth number
        Person beneficiary = new Person("8054176383");   // Valid female birth number

        Vehicle vehicle = new Vehicle("ABC123D", 10000);
        SingleVehicleContract contract = insurer2.insureVehicle(
                "C001", beneficiary, policyholder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle
        );

        // Paying for contract from different insurer should throw InvalidContractException
        assertThrows(InvalidContractException.class, () -> handler.pay(contract, 100));
    }
}