package company;

import company.InsuranceCompany;
import contracts.InvalidContractException;
import contracts.MasterVehicleContract;
import contracts.SingleVehicleContract;
import contracts.TravelContract;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.Test;
import payment.PaymentHandler;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InsuranceCompanyTest {

    LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 0, 0);

    @Test
    void testConstructorWithValidTime() {
        // Test creation with valid time parameter
        InsuranceCompany company = new InsuranceCompany(testTime);
        assertNotNull(company);
    }

    @Test
    void testConstructorWithNullTime() {
        // Test that constructor throws IllegalArgumentException when provided null time
        assertThrows(IllegalArgumentException.class, () -> new InsuranceCompany(null));
    }

    @Test
    void testConstructorInitializesContracts() {
        // Test that constructor initializes contracts set
        InsuranceCompany company = new InsuranceCompany(testTime);

        // Get the contracts set
        Set<?> contracts = company.getContracts();

        // Verify that contracts is initialized and empty
        assertNotNull(contracts);
        assertTrue(contracts.isEmpty());
    }

    @Test
    void testConstructorInitializesPaymentHandler() {
        // Test that constructor initializes payment handler
        InsuranceCompany company = new InsuranceCompany(testTime);

        // Get the payment handler
        PaymentHandler handler = company.getHandler();

        // Verify that handler is initialized
        assertNotNull(handler);
    }

    @Test
    void testImmutabilityOfAttributes() {
        // Create insurance company
        InsuranceCompany company = new InsuranceCompany(testTime);

        // Get initial references
        Set<?> initialContracts = company.getContracts();
        PaymentHandler initialHandler = company.getHandler();

        // Get references again to verify they're the same objects
        Set<?> secondContracts = company.getContracts();
        PaymentHandler secondHandler = company.getHandler();

        // Verify immutability
        assertSame(initialContracts, secondContracts);
        assertSame(initialHandler, secondHandler);
    }

    @Test
    void testSetCurrentTime() {
        // Create insurance company
        InsuranceCompany company = new InsuranceCompany(testTime);

        // Set valid new time
        LocalDateTime newTime = LocalDateTime.of(2023, 2, 1, 0, 0);
        company.setCurrentTime(newTime);
        assertEquals(newTime, company.getCurrentTime());

        // Test setting null time should throw exception
        assertThrows(IllegalArgumentException.class, () -> company.setCurrentTime(null));
    }

    @Test
    void testInsureVehicleWithValidParameters() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8004172022");
        Person beneficiary = new Person("8054176383");
        Vehicle vehicle = new Vehicle("ABC123D", 10000);

        SingleVehicleContract contract = company.insureVehicle(
                "C001", beneficiary, policyholder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle
        );

        assertNotNull(contract);
        assertTrue(company.getContracts().contains(contract));
        assertEquals("C001", contract.getContractNumber());
        assertEquals(company, contract.getInsurer());
        assertEquals(beneficiary, contract.getBeneficiary());
        assertEquals(policyholder, contract.getPolicyHolder());
    }

    @Test
    void testInsureVehicleWithDuplicateContractNumber() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8004172022");
        Person beneficiary = new Person("8054176383");
        Vehicle vehicle = new Vehicle("ABC123D", 10000);

        company.insureVehicle(
                "C001", beneficiary, policyholder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle
        );

        // Try to create another contract with the same number
        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle(
                        "C001", beneficiary, policyholder, 200,
                        PremiumPaymentFrequency.MONTHLY, vehicle
                )
        );
    }

    @Test
    void testInsureVehicleWithTooLowPremium() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8004172022");
        Person beneficiary = new Person("8054176383");
        Vehicle vehicle = new Vehicle("ABC123D", 10000);

        // Premium too low (less than 2% of vehicle value per year)
        // 10 × 12 = 120 < 200 (2% of 10000)
        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle(
                        "C001", beneficiary, policyholder, 10,
                        PremiumPaymentFrequency.MONTHLY, vehicle
                )
        );
    }

    @Test
    void testInsureVehicleWithInvalidParameters() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8004172022");
        Person beneficiary = new Person("8054176383");
        Vehicle vehicle = new Vehicle("ABC123D", 10000);

        // Null vehicle
        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle(
                        "C001", beneficiary, policyholder, 200,
                        PremiumPaymentFrequency.MONTHLY, null
                )
        );

        // Null payment frequency
        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle(
                        "C001", beneficiary, policyholder, 200,
                        null, vehicle
                )
        );

        // Non-positive premium
        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle(
                        "C001", beneficiary, policyholder, 0,
                        PremiumPaymentFrequency.MONTHLY, vehicle
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle(
                        "C001", beneficiary, policyholder, -100,
                        PremiumPaymentFrequency.MONTHLY, vehicle
                )
        );
    }

    @Test
    void testInsurePersonsWithValidParameters() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("200417657");

        Set<Person> personsToInsure = Set.of(
                new Person("8054176174"),
                new Person("8004172022")
        );

        TravelContract contract = company.insurePersons(
                "T001", policyholder, 20,
                PremiumPaymentFrequency.MONTHLY, personsToInsure
        );

        assertNotNull(contract);
        assertTrue(company.getContracts().contains(contract));
        assertEquals("T001", contract.getContractNumber());
        assertEquals(company, contract.getInsurer());
        assertEquals(policyholder, contract.getPolicyHolder());
        assertEquals(personsToInsure, contract.getInsuredPersons());
        assertEquals(20, contract.getContractPaymentData().getPremium());
        assertEquals(20, contract.getCoverageAmount());
    }

    @Test
    void testInsurePersonsWithDuplicateContractNumber() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8303225469");

        Set<Person> personsToInsure = Set.of(
                new Person("8351067065"),
                new Person("205417531")
        );

        company.insurePersons(
                "T001", policyholder, 20,
                PremiumPaymentFrequency.MONTHLY, personsToInsure
        );

        // Try to create another contract with the same number
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, 20,
                        PremiumPaymentFrequency.MONTHLY, personsToInsure
                )
        );
    }

    @Test
    void testInsurePersonsWithTooLowPremium() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("2454172941");

        Set<Person> personsToInsure = Set.of(
                new Person("2404176258"),
                new Person("1004170376"),
                new Person("7551017628")
        );

        // Premium too low (less than 5 × number of insured persons)
        // totalPremiumValue = proposedPremium * (12 / proposedPaymentFrequency.getValueInMonths());
        // (totalPremiumValue < 5 * personsToInsure.size())
        // 1 * (12 / 12) = 1
        // 1 < 10
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, 1,
                        PremiumPaymentFrequency.ANNUAL, personsToInsure
                )
        );

        // 1 * (12 / 6) = 2
        // 2 < 10
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T002", policyholder, 1,
                        PremiumPaymentFrequency.SEMI_ANNUAL, personsToInsure
                )
        );

        // 1 * (12 / 3) = 4
        // 4 < 10
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T003", policyholder, 1,
                        PremiumPaymentFrequency.QUARTERLY, personsToInsure
                )
        );

        // 1 * (12 / 1) = 12
        // 12 < 10
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T003", policyholder, 1,
                        PremiumPaymentFrequency.MONTHLY, personsToInsure
                )
        );
    }

    @Test
    void testInsurePersonsWithInvalidParameters() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8004179942");

        Set<Person> personsToInsure = Set.of(
                new Person("205417597"),
                new Person("0054170380")
        );

        // Null persons set
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, 20,
                        PremiumPaymentFrequency.MONTHLY, null
                )
        );

        // Empty persons set
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, 20,
                        PremiumPaymentFrequency.MONTHLY, Set.of()
                )
        );

        // Null payment frequency
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, 20,
                        null, personsToInsure
                )
        );

        // Non-positive premium
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, 0,
                        PremiumPaymentFrequency.MONTHLY, personsToInsure
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, -10,
                        PremiumPaymentFrequency.MONTHLY, personsToInsure
                )
        );
    }

    @Test
    void MasterTestInsurePersonsWithDuplicateContractNumber() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8303225469");

        Set<Person> personsToInsure = Set.of(
                new Person("8351067065"),
                new Person("205417531")
        );

        company.insurePersons(
                "T001", policyholder, 20,
                PremiumPaymentFrequency.MONTHLY, personsToInsure
        );

        // Try to create another contract with the same number
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons(
                        "T001", policyholder, 20,
                        PremiumPaymentFrequency.MONTHLY, personsToInsure
                )
        );
    }
    @Test
    void testMoveSingleVehicleContractToMasterVehicleContractWithNullParameters() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        // Create legal entity with 8-digit registration number
        Person legalEntity = new Person("12345678");
        Vehicle vehicle = new Vehicle("ABC123D", 10000);

        // Create master contract
        MasterVehicleContract masterContract = company.createMasterVehicleContract(
                "M001", null, legalEntity);

        // Create single vehicle contract
        SingleVehicleContract singleContract = company.insureVehicle(
                "V001", null, legalEntity, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        // Test with null master contract parameter
        assertThrows(IllegalArgumentException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(null, singleContract));

        // Test with null single contract parameter
        assertThrows(IllegalArgumentException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(masterContract, null));
    }

    @Test
    void testChargePremiumOnContractAbstractContract() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("8004172022");
        Vehicle vehicle = new Vehicle("ABC123D", 10000);

        // Create contract with monthly payment of 200
        SingleVehicleContract contract = company.insureVehicle(
                "C001", null, policyholder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle
        );

        // Initial setup - premium already charged during contract creation
        // Get the original nextPaymentTime (should be one month after creation)
        LocalDateTime originalNextPaymentTime = contract.getContractPaymentData().getNextPaymentTime();
        int originalBalance = contract.getContractPaymentData().getOutstandingBalance();

        // Set time to 2 months after contract creation
        LocalDateTime twoMonthsLater = testTime.plusMonths(2);
        company.setCurrentTime(twoMonthsLater);

        // Charge premium manually
        company.chargePremiumOnContract(contract);

        // Verify:
        // 1. Outstanding balance increased by 2 months of premium (200 * 2 = 400)
        assertEquals(originalBalance + 400, contract.getContractPaymentData().getOutstandingBalance());

        // 2. Next payment time should be 3 months after original creation time
        assertEquals(originalNextPaymentTime.plusMonths(2),
                contract.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    void testChargePremiumOnMasterVehicleContract() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        // Create legal entity with 8-digit registration number for master contract
        Person legalEntity = new Person("12345678");
        Vehicle vehicle1 = new Vehicle("ABC123A", 10000);
        Vehicle vehicle2 = new Vehicle("XYZ789B", 15000);

        // Create master contract
        MasterVehicleContract masterContract = company.createMasterVehicleContract(
                "M001", null, legalEntity);

        // Create single vehicle contracts
        SingleVehicleContract singleContract1 = company.insureVehicle(
                "V001", null, legalEntity, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle1);

        SingleVehicleContract singleContract2 = company.insureVehicle(
                "V002", null, legalEntity, 300,
                PremiumPaymentFrequency.MONTHLY, vehicle2);

        // Move contracts to master contract
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, singleContract1);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, singleContract2);

        // Store initial balances and payment times
        int initialBalance1 = singleContract1.getContractPaymentData().getOutstandingBalance();
        int initialBalance2 = singleContract2.getContractPaymentData().getOutstandingBalance();
        LocalDateTime initialNextPayment1 = singleContract1.getContractPaymentData().getNextPaymentTime();
        LocalDateTime initialNextPayment2 = singleContract2.getContractPaymentData().getNextPaymentTime();

        // Advance time by 2 months
        LocalDateTime twoMonthsLater = testTime.plusMonths(2);
        company.setCurrentTime(twoMonthsLater);

        // Charge premium on master contract
        company.chargePremiumOnContract(masterContract);

        // Verify each child contract had premium charged
        // Contract 1: Balance should increase by 2 months premium (200 * 2 = 400)
        assertEquals(initialBalance1 + 400, singleContract1.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialNextPayment1.plusMonths(2), singleContract1.getContractPaymentData().getNextPaymentTime());

        // Contract 2: Balance should increase by 2 months premium (300 * 2 = 600)
        assertEquals(initialBalance2 + 600, singleContract2.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialNextPayment2.plusMonths(2), singleContract2.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    void testChargePremiumsOnContracts() {
        InsuranceCompany company = new InsuranceCompany(testTime);

        // Create policy holders
        Person naturalPerson = new Person("8004172022");
        Person legalEntity = new Person("12345678");

        // Create vehicles with proper format (at least 7 chars, only A-Z and numbers)
        Vehicle vehicle1 = new Vehicle("ABC1234", 10000);
        Vehicle vehicle2 = new Vehicle("XYZ7890", 15000);

        // Create different types of contracts
        SingleVehicleContract singleContract = company.insureVehicle(
                "S001", null, naturalPerson, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle1);

        // Create master contract with a child contract
        MasterVehicleContract masterContract = company.createMasterVehicleContract(
                "M001", null, legalEntity);

        SingleVehicleContract childContract = company.insureVehicle(
                "C001", null, legalEntity, 300,
                PremiumPaymentFrequency.MONTHLY, vehicle2);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, childContract);

        // Store initial states
        int initialSingleBalance = singleContract.getContractPaymentData().getOutstandingBalance();
        int initialChildBalance = childContract.getContractPaymentData().getOutstandingBalance();
        LocalDateTime initialSingleNextPayment = singleContract.getContractPaymentData().getNextPaymentTime();
        LocalDateTime initialChildNextPayment = childContract.getContractPaymentData().getNextPaymentTime();

        // Advance time by 3 months
        LocalDateTime threeMonthsLater = testTime.plusMonths(3);
        company.setCurrentTime(threeMonthsLater);

        // Charge premiums on all contracts
        company.chargePremiumsOnContracts();

        // Verify single contract was updated
        assertEquals(initialSingleBalance + 600, singleContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialSingleNextPayment.plusMonths(3), singleContract.getContractPaymentData().getNextPaymentTime());

        // Verify child contract in master contract was updated
        assertEquals(initialChildBalance + 900, childContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialChildNextPayment.plusMonths(3), childContract.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    void testProcessClaimSingleVehicleContract() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("2404182814");
        Person beneficiary = new Person("300418712");
        Vehicle vehicle = new Vehicle("ABC1234", 10000);

        // Create a contract with coverage amount of 5000 (50% of vehicle value)
        SingleVehicleContract contract = company.insureVehicle(
                "C001", beneficiary, policyholder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle
        );

        // Initial balance for beneficiary and policyholder
        int initialBeneficiaryBalance = beneficiary.getPaidOutAmount();
        int initialPolicyholderBalance = policyholder.getPaidOutAmount();

        // Process claim with damages less than 70% of vehicle value
        company.processClaim(contract, 6000);

        // Verify:
        // 1. Beneficiary received payout (coverageAmount = 5000)
        assertEquals(initialBeneficiaryBalance + 5000, beneficiary.getPaidOutAmount());
        // 2. Contract is still active (damage < 70% of vehicle value)
        assertTrue(contract.isActive());

        // Process claim with damages >= 70% of vehicle value (total loss)
        company.processClaim(contract, 7000); // 70% of 10000

        // Verify:
        // 1. Beneficiary received another payout
        assertEquals(initialBeneficiaryBalance + 10000, beneficiary.getPaidOutAmount());
        // 2. Contract is now inactive (total loss)
        assertFalse(contract.isActive());

        // Test with null contract
        assertThrows(IllegalArgumentException.class, () ->
                company.processClaim((SingleVehicleContract)null, 5000));

        // Test with invalid damage amount
        assertThrows(IllegalArgumentException.class, () ->
                company.processClaim(contract, 0));
        assertThrows(IllegalArgumentException.class, () ->
                company.processClaim(contract, -1000));

        // Test with inactive contract
        assertThrows(InvalidContractException.class, () ->
                company.processClaim(contract, 5000));

        // Test with no beneficiary (payout to policyholder)
        SingleVehicleContract contractNoBeneficiary = company.insureVehicle(
                "C002", null, policyholder, 200,
                PremiumPaymentFrequency.MONTHLY, new Vehicle("DEF5678", 10000)
        );

        int policyholderBalanceBeforeClaim = policyholder.getPaidOutAmount();
        company.processClaim(contractNoBeneficiary, 5000);

        // Verify policyholder received payout
        assertEquals(policyholderBalanceBeforeClaim + 5000, policyholder.getPaidOutAmount());
    }

    @Test
    void testProcessClaimTravelContract() {
        InsuranceCompany company = new InsuranceCompany(testTime);
        Person policyholder = new Person("7501223521");

        // Create insured persons
        Person person1 = new Person("1003184336");
        Person person2 = new Person("2403261784");
        Person person3 = new Person("250104734");
        Set<Person> insuredPersons = Set.of(person1, person2, person3);

        // Create travel contract with premium/coverage of 30
        TravelContract contract = company.insurePersons(
                "T001", policyholder, 30,
                PremiumPaymentFrequency.MONTHLY, insuredPersons
        );

        // Initial payout amounts
        int initialPerson1PaidOut = person1.getPaidOutAmount();
        int initialPerson2PaidOut = person2.getPaidOutAmount();

        // Process claim for two of the three insured persons
        Set<Person> affectedPersons = Set.of(person1, person2);
        company.processClaim(contract, affectedPersons);

        // Verify:
        // 1. Each affected person received equal payout (30 / 2 = 15)
        assertEquals(initialPerson1PaidOut + 15, person1.getPaidOutAmount());
        assertEquals(initialPerson2PaidOut + 15, person2.getPaidOutAmount());

        // 2. Contract is now inactive
        assertFalse(contract.isActive());

        // Test with null contract
        assertThrows(IllegalArgumentException.class, () ->
                company.processClaim((TravelContract)null, affectedPersons));

        // Test with null affected persons
        assertThrows(IllegalArgumentException.class, () ->
                company.processClaim(contract, null));

        // Test with empty affected persons
        assertThrows(IllegalArgumentException.class, () ->
                company.processClaim(contract, Set.of()));

        // Test with persons not in the insured set
        Person outsider = new Person("50317817");
        assertThrows(IllegalArgumentException.class, () ->
                company.processClaim(contract, Set.of(person1, outsider)));

        // Test with inactive contract
        // (Contract already inactive from previous test)
        assertThrows(InvalidContractException.class, () ->
                company.processClaim(contract, affectedPersons));
    }
}