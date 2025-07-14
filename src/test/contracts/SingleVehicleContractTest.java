package contracts;

import company.InsuranceCompany;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SingleVehicleContractTest {

    private InsuranceCompany company;
    private Person policyHolder;
    private Person beneficiary;
    private ContractPaymentData paymentData;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        company = new InsuranceCompany(LocalDateTime.now());
        policyHolder = new Person("8004175146");
        beneficiary = new Person("8054176383");
        paymentData = new ContractPaymentData(
                100,
                PremiumPaymentFrequency.MONTHLY,
                LocalDateTime.now(),
                0);
        vehicle = new Vehicle("ABC123X", 10000);
    }

    @Test
    void testConstructorWithValidParameters() {
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, beneficiary, policyHolder,
                paymentData, 5000, vehicle);

        assertEquals("C001", contract.getContractNumber());
        assertEquals(company, contract.getInsurer());
        assertEquals(beneficiary, contract.getBeneficiary());
        assertEquals(policyHolder, contract.getPolicyHolder());
        assertEquals(paymentData, contract.getContractPaymentData());
        assertEquals(5000, contract.getCoverageAmount());
        assertEquals(vehicle, contract.getInsuredVehicle());
        assertTrue(contract.isActive());
    }

    @Test
    void testConstructorWithNullBeneficiary() {
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, null, policyHolder,
                paymentData, 5000, vehicle);

        assertNull(contract.getBeneficiary());
    }

    @Test
    void testConstructorWithNullVehicle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract(
                    "C001", company, beneficiary, policyHolder,
                    paymentData, 5000, null);
        });
    }

    @Test
    void testProcessClaimForDamage() {
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, beneficiary, policyHolder,
                paymentData, 5000, vehicle);

        int initialPayout = beneficiary.getPaidOutAmount();

        // Process claim with damage less than 70% of vehicle value
        company.processClaim(contract, 3000);

        // Verify beneficiary received payout
        assertEquals(initialPayout + 5000, beneficiary.getPaidOutAmount());
        // Contract should still be active
        assertTrue(contract.isActive());
    }

    @Test
    void testProcessClaimForTotalLoss() {
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, beneficiary, policyHolder,
                paymentData, 5000, vehicle);

        // Process claim with damage more than 70% of vehicle value (10000)
        company.processClaim(contract, 7500);

        // Contract should be inactive after total loss
        assertFalse(contract.isActive());
    }

    @Test
    void testPayoutGoesToPolicyHolderWhenNoBeneficiary() {
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, null, policyHolder,
                paymentData, 5000, vehicle);

        int initialPayout = policyHolder.getPaidOutAmount();

        // Process claim
        company.processClaim(contract, 3000);

        // Verify policyholder received payout
        assertEquals(initialPayout + 5000, policyHolder.getPaidOutAmount());
    }

    @Test
    void testConstructorWithNullPaymentData() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract(
                    "C001", company, beneficiary, policyHolder,
                    null, 5000, vehicle);
        });
    }
}