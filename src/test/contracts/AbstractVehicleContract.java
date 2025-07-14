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

class AbstractVehicleContractTest {

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
        vehicle = new Vehicle("ABC123X", 10000); // Changed to 7 characters
    }

    @Test
    void testBeneficiaryCanBeNull() {
        // Create contract with null beneficiary
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, null, policyHolder,
                paymentData, 5000, vehicle);

        // Verify beneficiary is null
        assertNull(contract.getBeneficiary());
    }

    @Test
    void testPayoutGoesToBeneficiaryWhenPresent() {
        // Create contract with beneficiary
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, beneficiary, policyHolder,
                paymentData, 5000, vehicle);

        int initialPayout = beneficiary.getPaidOutAmount();

        // Process claim
        company.processClaim(contract, 3000);

        // Verify beneficiary received payout
        assertEquals(initialPayout + 5000, beneficiary.getPaidOutAmount());
    }

    @Test
    void testPayoutGoesToPolicyHolderWhenBeneficiaryIsNull() {
        // Create contract with null beneficiary
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
    void testBeneficiaryCannotBePolicyHolder() {
        // Attempt to create a contract where beneficiary is the same as policyHolder
        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract(
                    "C001", company, policyHolder, policyHolder,
                    paymentData, 5000, vehicle);
        });
    }

    @Test
    void testInsurancePayoutToDefinedBeneficiary() {
        // Create contract with beneficiary
        SingleVehicleContract contract = new SingleVehicleContract(
                "C001", company, beneficiary, policyHolder,
                paymentData, 5000, vehicle);

        int initialPayout = beneficiary.getPaidOutAmount();

        // Process claim
        company.processClaim(contract, 3000);

        // Verify beneficiary received payout
        assertEquals(initialPayout + 5000, beneficiary.getPaidOutAmount());

        // Verify policyholder did not receive the payout
        assertEquals(0, policyHolder.getPaidOutAmount());
    }
}