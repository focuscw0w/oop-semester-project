package methods;

import company.InsuranceCompany;
import contracts.*;
import objects.LegalForm;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;
import payment.PaymentHandler;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.*;

public class methodsTests {
    private LocalDateTime testTime;
    private InsuranceCompany company;
    private InsuranceCompany company2;
    private Person policyHolder;
    private Person beneficiary;
    private Person insured1;
    private Person insured2;
    private Person insured3;
    private Person nonInsured;
    private Vehicle vehicle;
    private Vehicle vehicle2;
    private SingleVehicleContract singleContract;
    private MasterVehicleContract masterContract;
    private TravelContract travelContract;
    private Set<Person> insuredPersons;
    private Set<Person> affectedPersons;
    private Set<Person> invalidPersons;
    private Set<Person> emptyPersons;
    Vehicle validVehicle;
    Vehicle allDigits;
    Vehicle allLetters;


    @BeforeEach
    public void setUp() {
        // Initialize time and companies
        testTime = LocalDateTime.now();
        company = new InsuranceCompany(testTime);
        company2 = new InsuranceCompany(testTime);

        // Initialize persons
        policyHolder = new Person("334512"); // LEGAL
        beneficiary = new Person("8054176383"); // NATURAL
        insured1 = new Person("9553023040");
        insured2 = new Person("9553140322");
        insured3 = new Person("9551119512");
        nonInsured = new Person("9502216108");

        // Initialize vehicles
        vehicle = new Vehicle("ABC123D", 10000);
        vehicle2 = new Vehicle("XYZ789E", 20000);
        validVehicle = new Vehicle("ABC123D", 10000);
        allDigits = new Vehicle("1234567", 5000);
        allLetters = new Vehicle("ABCDEFG", 15000);

        // Initialize insured persons sets
        insuredPersons = new LinkedHashSet<>();
        insuredPersons.add(insured1);
        insuredPersons.add(insured2);
        insuredPersons.add(insured3);

        affectedPersons = new LinkedHashSet<>();
        affectedPersons.add(insured1);
        affectedPersons.add(insured2);

        invalidPersons = new LinkedHashSet<>();
        invalidPersons.add(insured1);
        invalidPersons.add(nonInsured);

        emptyPersons = new LinkedHashSet<>();

        // Initialize basic contracts for reuse in tests
        singleContract = company.insureVehicle(
                "SVC001", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        masterContract = company.createMasterVehicleContract(
                "MVC001", null, policyHolder);

        travelContract = company.insurePersons(
                "TC001", policyHolder, 300,
                PremiumPaymentFrequency.MONTHLY, insuredPersons);
    }

    @Test
    public void testMoveSingleVehicleContractBetweenDifferentCompanies() {
        // Create contract in second company for cross-company test
        SingleVehicleContract crossCompanyContract = company2.insureVehicle(
                "S001", null, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        // Verify initial state
        assertEquals(company2, crossCompanyContract.getInsurer());
        assertTrue(company2.getContracts().contains(crossCompanyContract));
        assertTrue(policyHolder.getContracts().contains(crossCompanyContract));

        // Attempt to move contract between companies
        assertThrows(InvalidContractException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(masterContract, crossCompanyContract));

        // Verify nothing changed
        assertTrue(company2.getContracts().contains(crossCompanyContract));
        assertTrue(masterContract.getChildContracts().isEmpty());
    }

    @Test
    public void testProcessClaimForSingleVehicleContractValid() {
        // Process claim with minor damage (less than 70% of vehicle value)
        int expectedDamages = 5000; // 50% of vehicle value
        int initialPaidOutAmount = beneficiary.getPaidOutAmount();

        company.processClaim(singleContract, expectedDamages);

        // Verify beneficiary received payment and contract is still active
        assertEquals(initialPaidOutAmount + singleContract.getCoverageAmount(),
                beneficiary.getPaidOutAmount());
        assertTrue(singleContract.isActive());
    }

    @Test
    public void testProcessClaimForSingleVehicleContractTotalLoss() {
        // Process claim with total loss (more than 70% of vehicle value)
        int expectedDamages = 7500; // 75% of vehicle value
        int initialPaidOutAmount = beneficiary.getPaidOutAmount();

        company.processClaim(singleContract, expectedDamages);

        // Verify payment made and contract now inactive
        assertEquals(initialPaidOutAmount + singleContract.getCoverageAmount(),
                beneficiary.getPaidOutAmount());
        assertFalse(singleContract.isActive());
    }

    @Test
    public void testProcessClaimForSingleVehicleContractWithoutBeneficiary() {
        // Create contract without beneficiary
        SingleVehicleContract noBeneficiaryContract = company.insureVehicle(
                "SVC002", null, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        int initialPaidOutAmount = policyHolder.getPaidOutAmount();

        company.processClaim(noBeneficiaryContract, 5000);

        // Verify policyholder received payment
        assertEquals(initialPaidOutAmount + noBeneficiaryContract.getCoverageAmount(),
                policyHolder.getPaidOutAmount());
    }

    @Test
    public void testProcessClaimForInactiveSingleVehicleContract() {
        // Make contract inactive
        singleContract.setInactive();

        // Attempt to process claim on inactive contract
        assertThrows(InvalidContractException.class,
                () -> company.processClaim(singleContract, 5000));
    }

    @Test
    public void testProcessClaimForSingleVehicleContractWithInvalidParams() {
        // Test invalid parameters
        assertThrows(IllegalArgumentException.class,
                () -> company.processClaim(singleContract, 0));
        assertThrows(IllegalArgumentException.class,
                () -> company.processClaim(singleContract, -500));
        assertThrows(IllegalArgumentException.class,
                () -> company.processClaim((SingleVehicleContract) null, 5000));
    }

    @Test
    public void testProcessClaimForTravelContractValid() {
        // Track initial paid out amounts
        int initial1 = insured1.getPaidOutAmount();
        int initial2 = insured2.getPaidOutAmount();
        int initial3 = insured3.getPaidOutAmount();

        company.processClaim(travelContract, affectedPersons);

        // Expected payout per person
        int expectedPayoutPerPerson = travelContract.getCoverageAmount() / affectedPersons.size();

        // Verify payouts and contract status
        assertEquals(initial1 + expectedPayoutPerPerson, insured1.getPaidOutAmount());
        assertEquals(initial2 + expectedPayoutPerPerson, insured2.getPaidOutAmount());
        assertEquals(initial3, insured3.getPaidOutAmount());
        assertFalse(travelContract.isActive());
    }

    @Test
    public void testProcessClaimForInactiveTravelContract() {
        // Make contract inactive
        travelContract.setInactive();

        // Attempt to process claim on inactive contract
        assertThrows(InvalidContractException.class,
                () -> company.processClaim(travelContract, affectedPersons));
    }

    @Test
    public void testProcessClaimForTravelContractWithInvalidParams() {
        // Create contract for this specific test
        TravelContract testContract = company.insurePersons(
                "TC002", policyHolder, 300,
                PremiumPaymentFrequency.MONTHLY, insuredPersons);

        // Test with null affected persons
        assertThrows(IllegalArgumentException.class,
                () -> company.processClaim(testContract, null));

        // Test with empty affected persons
        assertThrows(IllegalArgumentException.class,
                () -> company.processClaim(testContract, emptyPersons));

        // Test with non-insured person in affected persons
        assertThrows(IllegalArgumentException.class,
                () -> company.processClaim(testContract, invalidPersons));

        // Test with null contract
        assertThrows(IllegalArgumentException.class,
                () -> company.processClaim((TravelContract) null, affectedPersons));
    }

    @Test
    public void testChargePremiumsOnContracts() {
        // Create child contract and move it to master
        SingleVehicleContract childContract = company.insureVehicle(
                "SVC003", beneficiary, policyHolder, 150,
                PremiumPaymentFrequency.MONTHLY, vehicle2);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, childContract);

        // Get initial outstanding balances
        int initialVehicleBalance = singleContract.getContractPaymentData().getOutstandingBalance();
        int initialTravelBalance = travelContract.getContractPaymentData().getOutstandingBalance();
        int initialChildBalance = childContract.getContractPaymentData().getOutstandingBalance();

        // Set the time forward to trigger premium payments
        LocalDateTime futureTime = testTime.plusMonths(2);
        company.setCurrentTime(futureTime);

        // Execute the method being tested
        company.chargePremiumsOnContracts();

        // Verify that premiums were charged correctly
        assertEquals(initialVehicleBalance + 200 * 2,
                singleContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialTravelBalance + 300 * 2,
                travelContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialChildBalance + 150 * 2,
                childContract.getContractPaymentData().getOutstandingBalance());

        // Verify next payment dates
        assertEquals(testTime.plusMonths(3),
                singleContract.getContractPaymentData().getNextPaymentTime());
        assertEquals(testTime.plusMonths(3),
                travelContract.getContractPaymentData().getNextPaymentTime());
        assertEquals(testTime.plusMonths(3),
                childContract.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    public void testChargePremiumOnMasterVehicleContract() {
        // Create child contracts and add to master
        SingleVehicleContract childContract1 = company.insureVehicle(
                "SVC003", beneficiary, policyHolder, 150,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        SingleVehicleContract childContract2 = company.insureVehicle(
                "SVC004", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle2);

        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, childContract1);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, childContract2);

        // Set time forward
        LocalDateTime futureTime = testTime.plusMonths(1);
        company.setCurrentTime(futureTime);

        // Get initial balances
        int initialChild1Balance = childContract1.getContractPaymentData().getOutstandingBalance();
        int initialChild2Balance = childContract2.getContractPaymentData().getOutstandingBalance();

        // Test charging premium on master contract
        company.chargePremiumOnContract(masterContract);

        // Verify premiums charged on child contracts
        assertEquals(initialChild1Balance + 150,
                childContract1.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialChild2Balance + 200,
                childContract2.getContractPaymentData().getOutstandingBalance());

        // Verify next payment times updated
        assertEquals(testTime.plusMonths(2),
                childContract1.getContractPaymentData().getNextPaymentTime());
        assertEquals(testTime.plusMonths(2),
                childContract2.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    public void testChargePremiumOnInactiveContract() {
        // Store initial values
        LocalDateTime initialNextPaymentTime = singleContract.getContractPaymentData().getNextPaymentTime();
        int initialBalance = singleContract.getContractPaymentData().getOutstandingBalance();

        // Make contract inactive
        singleContract.setInactive();

        // Set time forward
        LocalDateTime futureTime = testTime.plusMonths(1);
        company.setCurrentTime(futureTime);

        // Execute chargePremiumsOnContracts
        company.chargePremiumsOnContracts();

        // Verify no premium charged for inactive contract
        assertEquals(initialBalance, singleContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(initialNextPaymentTime, singleContract.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    public void testChargePremiumOnContractWithMultiplePeriods() {
        // Get initial balance
        int initialBalance = singleContract.getContractPaymentData().getOutstandingBalance();

        // Set time forward by several periods
        LocalDateTime futureTime = testTime.plusMonths(3);
        company.setCurrentTime(futureTime);

        // Charge premium
        company.chargePremiumOnContract(singleContract);

        // Verify premium charged for all 3 months
        assertEquals(initialBalance + 200 * 3,
                singleContract.getContractPaymentData().getOutstandingBalance());

        // Verify next payment time updated correctly
        assertEquals(testTime.plusMonths(4),
                singleContract.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    public void testDifferentPaymentFrequencies() {
        // Create contracts with different payment frequencies
        SingleVehicleContract monthlyContract = company.insureVehicle(
                "SVC005", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        SingleVehicleContract quarterlyContract = company.insureVehicle(
                "SVC006", beneficiary, policyHolder, 300,
                PremiumPaymentFrequency.QUARTERLY, vehicle2);

        // Store initial balances
        int initialMonthlyBalance = monthlyContract.getContractPaymentData().getOutstandingBalance();
        int initialQuarterlyBalance = quarterlyContract.getContractPaymentData().getOutstandingBalance();

        // Set time forward
        LocalDateTime futureTime = testTime.plusMonths(4);
        company.setCurrentTime(futureTime);

        // Charge premiums
        company.chargePremiumsOnContracts();

        // Verify monthly contract (4 additional charges)
        assertEquals(initialMonthlyBalance + 100 * 4,
                monthlyContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(testTime.plusMonths(5),
                monthlyContract.getContractPaymentData().getNextPaymentTime());

        // Verify quarterly contract (1 additional charge at month 3)
        assertEquals(initialQuarterlyBalance + 300 * 1,
                quarterlyContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(testTime.plusMonths(6),
                quarterlyContract.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    void testInsureVehicle() {
        // Test 1: Basic validation of a successful contract creation
        SingleVehicleContract testContract = company.insureVehicle(
                "SVC101", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        // Verify contract is properly created
        assertNotNull(testContract);
        assertEquals("SVC101", testContract.getContractNumber());
        assertEquals(company, testContract.getInsurer());
        assertEquals(beneficiary, testContract.getBeneficiary());
        assertEquals(policyHolder, testContract.getPolicyHolder());
        assertEquals(5000, testContract.getCoverageAmount()); // Half of vehicle value
        assertEquals(200, testContract.getContractPaymentData().getPremium());
        assertEquals(PremiumPaymentFrequency.MONTHLY,
                testContract.getContractPaymentData().getPremiumPaymentFrequency());
        assertEquals(testTime.plusMonths(1),
                testContract.getContractPaymentData().getNextPaymentTime());
        assertEquals(200, testContract.getContractPaymentData().getOutstandingBalance());
        assertTrue(company.getContracts().contains(testContract));
        assertTrue(policyHolder.getContracts().contains(testContract));

        // Test 2: Contract with duplicate number should throw exception
        assertThrows(IllegalArgumentException.class, () -> company.insureVehicle(
                "SVC101", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle));

        // Test 3: Premium too low (less than 2% of vehicle value per year)
        // 10000 × 0.02 = 200, but 16 × 12 = 192 < 200
        assertThrows(IllegalArgumentException.class, () -> company.insureVehicle(
                "SVC102", beneficiary, policyHolder, 16,
                PremiumPaymentFrequency.MONTHLY, vehicle));

        // Test 4: Edge cases with minimum valid premiums for different frequencies
        // Monthly: 17 * 12 = 204 > 200 (OK)
        SingleVehicleContract monthlyMinimum = company.insureVehicle(
                "SVC103", beneficiary, policyHolder, 17,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        assertNotNull(monthlyMinimum);

        // Quarterly: 50 * 4 = 200 = 200 (OK)
        SingleVehicleContract quarterlyMinimum = company.insureVehicle(
                "SVC104", beneficiary, policyHolder, 50,
                PremiumPaymentFrequency.QUARTERLY, vehicle);
        assertNotNull(quarterlyMinimum);

        // Semi-Annual: 100 * 2 = 200 = 200 (OK)
        SingleVehicleContract semiAnnualMinimum = company.insureVehicle(
                "SVC105", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.SEMI_ANNUAL, vehicle);
        assertNotNull(semiAnnualMinimum);

        // Annual: 200 * 1 = 200 = 200 (OK)
        SingleVehicleContract annualMinimum = company.insureVehicle(
                "SVC106", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.ANNUAL, vehicle);
        assertNotNull(annualMinimum);

        // Test 5: Invalid parameters
        assertThrows(IllegalArgumentException.class, () -> company.insureVehicle(
                "SVC107", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, null));

        assertThrows(IllegalArgumentException.class, () -> company.insureVehicle(
                "SVC108", beneficiary, policyHolder, 200,
                null, vehicle));

        assertThrows(IllegalArgumentException.class, () -> company.insureVehicle(
                "SVC109", beneficiary, policyHolder, 0,
                PremiumPaymentFrequency.MONTHLY, vehicle));

        assertThrows(IllegalArgumentException.class, () -> company.insureVehicle(
                "SVC110", beneficiary, policyHolder, -50,
                PremiumPaymentFrequency.MONTHLY, vehicle));
    }

    @Test
    public void testInsurePersons() {
        // Create a fresh set of persons for this test
        Set<Person> testInsuredPersons = new LinkedHashSet<>();
        testInsuredPersons.add(insured1);
        testInsuredPersons.add(insured2);

        // Test 1: Basic validation of a successful contract creation
        TravelContract testContract = company.insurePersons(
                "TC101", policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, testInsuredPersons);

        // Verify contract properties
        assertNotNull(testContract);
        assertEquals("TC101", testContract.getContractNumber());
        assertEquals(company, testContract.getInsurer());
        assertEquals(policyHolder, testContract.getPolicyHolder());
        assertEquals(20, testContract.getCoverageAmount()); // 10 * 2 persons
        assertEquals(100, testContract.getContractPaymentData().getPremium());
        assertEquals(PremiumPaymentFrequency.MONTHLY,
                testContract.getContractPaymentData().getPremiumPaymentFrequency());
        assertEquals(testTime.plusMonths(1),
                testContract.getContractPaymentData().getNextPaymentTime());
        assertEquals(100, testContract.getContractPaymentData().getOutstandingBalance());
        assertEquals(testInsuredPersons, testContract.getInsuredPersons());
        assertTrue(company.getContracts().contains(testContract));
        assertTrue(policyHolder.getContracts().contains(testContract));

        // Test 2: Duplicate contract number
        assertThrows(IllegalArgumentException.class, () -> company.insurePersons(
                "TC101", policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, testInsuredPersons));

        // Test 3: Premium too low (less than 5 * number of persons per year)
        // Monthly payment: 2 * 12 = 24 > 10 (5 * 2 persons)
        // This should NOT throw an exception
        TravelContract lowButValidPremium = company.insurePersons(
                "TC102", policyHolder, 2,
                PremiumPaymentFrequency.MONTHLY, testInsuredPersons);
        assertNotNull(lowButValidPremium);

        // Test 4: Edge cases with minimum valid premiums for different frequencies
        // Monthly: 5 persons * 2 = 10 annual minimum
        // 10 ÷ 12 = 0.83... rounded down to 0, but method expects > 0
        // So minimum monthly premium is 1
        TravelContract monthlyMinimum = company.insurePersons(
                "TC103", policyHolder, 1,
                PremiumPaymentFrequency.MONTHLY, testInsuredPersons);
        assertNotNull(monthlyMinimum);

        // Quarterly: 10 ÷ 4 = 2.5 rounded down to 2
        TravelContract quarterlyMinimum = company.insurePersons(
                "TC104", policyHolder, 3,
                PremiumPaymentFrequency.QUARTERLY, testInsuredPersons);
        assertNotNull(quarterlyMinimum);

        // Semi-Annual: 10 ÷ 2 = 5
        TravelContract semiAnnualMinimum = company.insurePersons(
                "TC105", policyHolder, 5,
                PremiumPaymentFrequency.SEMI_ANNUAL, testInsuredPersons);
        assertNotNull(semiAnnualMinimum);

        // Annual: 10 * 1 = 10
        TravelContract annualMinimum = company.insurePersons(
                "TC106", policyHolder, 10,
                PremiumPaymentFrequency.ANNUAL, testInsuredPersons);
        assertNotNull(annualMinimum);

        // Test 5: Invalid parameters
        assertThrows(IllegalArgumentException.class, () -> company.insurePersons(
                "TC107", policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, null));

        assertThrows(IllegalArgumentException.class, () -> company.insurePersons(
                "TC108", policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, emptyPersons));

        assertThrows(IllegalArgumentException.class, () -> company.insurePersons(
                "TC109", policyHolder, 100,
                null, testInsuredPersons));

        assertThrows(IllegalArgumentException.class, () -> company.insurePersons(
                "TC110", policyHolder, 0,
                PremiumPaymentFrequency.MONTHLY, testInsuredPersons));

        assertThrows(IllegalArgumentException.class, () -> company.insurePersons(
                "TC111", policyHolder, -50,
                PremiumPaymentFrequency.MONTHLY, testInsuredPersons));
    }

    @Test
    public void testVehicleValidation() {
        // Test valid vehicles
        assertEquals("ABC123D", validVehicle.getLicensePlate());
        assertEquals(10000, validVehicle.getOriginalValue());
        assertEquals("1234567", allDigits.getLicensePlate());
        assertEquals("ABCDEFG", allLetters.getLicensePlate());

        // Test null license plate
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle(null, 1000));

        // Test invalid length
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("ABC123", 1000)); // Too short

        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("ABC1234D", 1000)); // Too long

        // Test invalid characters
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("abc123D", 1000)); // Lowercase letters

        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("ABC-123", 1000)); // Special characters

        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("ABC 123", 1000)); // Space character

        // Test invalid originalValue
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("ABC123D", 0)); // Zero value

        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("ABC123D", -500)); // Negative value
    }

    @Test
    public void testCreateMasterVehicleContract() {
        // Test 1: Basic validation of successful contract creation
        MasterVehicleContract testContract = company.createMasterVehicleContract(
                "MVC101", beneficiary, policyHolder);

        // Verify contract properties
        assertNotNull(testContract);
        assertEquals("MVC101", testContract.getContractNumber());
        assertEquals(company, testContract.getInsurer());
        assertEquals(beneficiary, testContract.getBeneficiary());
        assertEquals(policyHolder, testContract.getPolicyHolder());
        assertTrue(testContract.getChildContracts().isEmpty());
        assertTrue(company.getContracts().contains(testContract));
        assertTrue(policyHolder.getContracts().contains(testContract));

        // Test 2: Duplicate contract number
        assertThrows(IllegalArgumentException.class, () -> company.createMasterVehicleContract(
                "MVC101", beneficiary, policyHolder));

        // Test 3: Legal form validation - policyHolder must be LEGAL
        Person naturalPerson = new Person("9553023040"); // Natural person with birth number
        assertThrows(IllegalArgumentException.class, () -> company.createMasterVehicleContract(
                "MVC102", beneficiary, naturalPerson));

        // Test 4: Create multiple valid contracts
        MasterVehicleContract testContract2 = company.createMasterVehicleContract(
                "MVC103", null, policyHolder);
        assertNotNull(testContract2);

        // Verify second contract
        assertEquals("MVC103", testContract2.getContractNumber());
        assertNull(testContract2.getBeneficiary());
        assertEquals(policyHolder, testContract2.getPolicyHolder());
        assertTrue(testContract2.getChildContracts().isEmpty());
        assertTrue(company.getContracts().contains(testContract2));
        assertTrue(policyHolder.getContracts().contains(testContract2));
    }

    @Test
    public void testMoveSingleVehicleContractToMasterVehicleContract() {
        // Test 1: Valid contract move
        SingleVehicleContract childContract = company.insureVehicle(
                "SVC101", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        int initialContractsCount = company.getContracts().size();
        int initialPolicyHolderContractsCount = policyHolder.getContracts().size();

        assertTrue(company.getContracts().contains(childContract));
        assertTrue(policyHolder.getContracts().contains(childContract));
        assertTrue(masterContract.getChildContracts().isEmpty());

        // Perform the move
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, childContract);

        // Verify contract was moved correctly
        assertFalse(company.getContracts().contains(childContract));
        assertFalse(policyHolder.getContracts().contains(childContract));
        assertTrue(masterContract.getChildContracts().contains(childContract));
        assertEquals(initialContractsCount - 1, company.getContracts().size());
        assertEquals(initialPolicyHolderContractsCount - 1, policyHolder.getContracts().size());

        // Test 2: Attempt to move inactive contract
        SingleVehicleContract inactiveContract = company.insureVehicle(
                "SVC102", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle2);
        inactiveContract.setInactive();

        assertThrows(InvalidContractException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(masterContract, inactiveContract));

        // Test 3: Attempt to move to inactive master contract
        SingleVehicleContract anotherContract = company.insureVehicle(
                "SVC103", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle2);

        MasterVehicleContract inactiveMaster = company.createMasterVehicleContract(
                "MVC102", beneficiary, policyHolder);
        inactiveMaster.setInactive();

        assertThrows(InvalidContractException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(inactiveMaster, anotherContract));

        // Test 4: Different policyholders
        Person anotherLegalPerson = new Person("123456");
        MasterVehicleContract differentHolderMaster = company.createMasterVehicleContract(
                "MVC103", beneficiary, anotherLegalPerson);

        assertThrows(InvalidContractException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(differentHolderMaster, anotherContract));

        // Test 5: Null parameters
        assertThrows(IllegalArgumentException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(null, anotherContract));

        assertThrows(IllegalArgumentException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(masterContract, null));

        // Test 6: Cross-company contract transfer (different insurers)
        SingleVehicleContract crossCompanyContract = company2.insureVehicle(
                "SVC104", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        assertThrows(InvalidContractException.class, () ->
                company.moveSingleVehicleContractToMasterVehicleContract(masterContract, crossCompanyContract));
    }

    @Test
    public void testInsuranceCompanyInitialization() {
        // Test 1: Verify null currentTime throws exception
        assertThrows(IllegalArgumentException.class, () -> new InsuranceCompany(null));

        // Test 2: Valid initialization
        LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        InsuranceCompany company = new InsuranceCompany(testTime);

        assertEquals(testTime, company.getCurrentTime());
        assertNotNull(company.getContracts());
        assertTrue(company.getContracts().isEmpty());
        assertNotNull(company.getHandler());

        // Test 3: Setting null time throws exception
        assertThrows(IllegalArgumentException.class, () -> company.setCurrentTime(null));

        // Test 4: Time can be updated
        LocalDateTime newTime = testTime.plusDays(1);
        company.setCurrentTime(newTime);
        assertEquals(newTime, company.getCurrentTime());

        // Test 5: Verify contracts are stored in insertion order
        SingleVehicleContract contract1 = company.insureVehicle(
                "SVC001", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        SingleVehicleContract contract2 = company.insureVehicle(
                "SVC002", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle2);

        // Convert to array to test order
        Object[] contracts = company.getContracts().toArray();
        assertEquals(contract1, contracts[0]);
        assertEquals(contract2, contracts[1]);
    }

    @Test
    public void testPaymentHandlerConstructor() {
        // Test 1: Verify null insurer throws exception
        assertThrows(IllegalArgumentException.class, () -> new PaymentHandler(null));

        // Test 2: Valid initialization
        PaymentHandler handler = new PaymentHandler(company);

        // Verify handler is properly initialized
        assertNotNull(handler);
        assertNotNull(handler.getPaymentHistory());
        assertTrue(handler.getPaymentHistory().isEmpty());
    }

    @Test
    public void testPaymentHandlerPay() {
        // Test 1: Basic payment processing
        int initialBalance = singleContract.getContractPaymentData().getOutstandingBalance();
        int paymentAmount = 100;

        // Make payment
        company.getHandler().pay(singleContract, paymentAmount);

        // Verify balance reduced
        assertEquals(initialBalance - paymentAmount,
                singleContract.getContractPaymentData().getOutstandingBalance());

        // Verify payment recorded in history
        Map<AbstractContract, Set<PaymentInstance>> history = company.getHandler().getPaymentHistory();
        assertTrue(history.containsKey(singleContract));
        assertEquals(1, history.get(singleContract).size());

        PaymentInstance payment = history.get(singleContract).iterator().next();
        assertEquals(paymentAmount, payment.getPaymentAmount());
        assertEquals(company.getCurrentTime(), payment.getPaymentTime());

        // Test 2: Multiple payments for same contract
        int secondPaymentAmount = 50;
        company.getHandler().pay(singleContract, secondPaymentAmount);

        // Verify balance reduced again
        assertEquals(initialBalance - paymentAmount - secondPaymentAmount,
                singleContract.getContractPaymentData().getOutstandingBalance());

        // Verify second payment recorded
        assertEquals(1, history.get(singleContract).size());

        // Test 3: Null contract
        assertThrows(IllegalArgumentException.class, () ->
                company.getHandler().pay((AbstractContract)null, 100));

        // Test 4: Non-positive amount
        assertThrows(IllegalArgumentException.class, () ->
                company.getHandler().pay(singleContract, 0));
        assertThrows(IllegalArgumentException.class, () ->
                company.getHandler().pay(singleContract, -50));

        // Test 5: Inactive contract
        singleContract.setInactive();
        assertThrows(InvalidContractException.class, () ->
                company.getHandler().pay(singleContract, 100));

        // Test 6: Contract from different company
        SingleVehicleContract otherCompanyContract = company2.insureVehicle(
                "SVC999", beneficiary, policyHolder, 200,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        assertThrows(InvalidContractException.class, () ->
                company.getHandler().pay(otherCompanyContract, 100));
    }

    @Test
    public void testPaymentHandlerPayMasterVehicleContract() {
        // Create a master contract with child contracts
        MasterVehicleContract masterContract = company.createMasterVehicleContract(
                "MVC200", beneficiary, policyHolder);

        // Create child contracts with different premiums
        SingleVehicleContract contract1 = company.insureVehicle(
                "SVC201", beneficiary, policyHolder, 30,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        SingleVehicleContract contract2 = company.insureVehicle(
                "SVC202", beneficiary, policyHolder, 50,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        SingleVehicleContract contract3 = company.insureVehicle(
                "SVC203", beneficiary, policyHolder, 75,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        // Create contract4 and set it inactive
        SingleVehicleContract contract4 = company.insureVehicle(
                "SVC204", beneficiary, policyHolder, 20,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        // Move them to master contract
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract1);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract2);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract3);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract4);
        contract4.setInactive();

        // Set initial outstanding balances
        contract1.getContractPaymentData().setOutstandingBalance(30);
        contract2.getContractPaymentData().setOutstandingBalance(50);
        contract3.getContractPaymentData().setOutstandingBalance(100);
        contract4.getContractPaymentData().setOutstandingBalance(0);

        // Make a payment of 400
        company.getHandler().pay(masterContract, 400);

        // Verify final balances after payment distribution
        assertEquals(-60, contract1.getContractPaymentData().getOutstandingBalance());
        assertEquals(-85, contract2.getContractPaymentData().getOutstandingBalance());
        assertEquals(-75, contract3.getContractPaymentData().getOutstandingBalance());
        assertEquals(0, contract4.getContractPaymentData().getOutstandingBalance());

        // Verify payment recorded in history
        Map<AbstractContract, Set<PaymentInstance>> history = company.getHandler().getPaymentHistory();
        assertTrue(history.containsKey(masterContract));
        assertEquals(1, history.get(masterContract).size());

        PaymentInstance payment = history.get(masterContract).iterator().next();
        assertEquals(400, payment.getPaymentAmount());
        assertEquals(company.getCurrentTime(), payment.getPaymentTime());

        // Make a second payment at a different time
        LocalDateTime secondPaymentTime = company.getCurrentTime().plusMinutes(1);
        company.setCurrentTime(secondPaymentTime);
        company.getHandler().pay(masterContract, 100);

        // Verify second payment was recorded
        assertEquals(2, history.get(masterContract).size());

        // Test invalid cases
        // Test with null contract
        assertThrows(IllegalArgumentException.class, () ->
                company.getHandler().pay((MasterVehicleContract)null, 100));

        // Test with non-positive amount
        assertThrows(IllegalArgumentException.class, () ->
                company.getHandler().pay(masterContract, 0));
        assertThrows(IllegalArgumentException.class, () ->
                company.getHandler().pay(masterContract, -50));

        // Test with inactive master contract
        masterContract.setInactive();
        assertThrows(InvalidContractException.class, () ->
                company.getHandler().pay(masterContract, 100));

        // Test with empty child contracts list
        MasterVehicleContract emptyMaster = company.createMasterVehicleContract(
                "MVC205", beneficiary, policyHolder);
        assertThrows(InvalidContractException.class, () ->
                company.getHandler().pay(emptyMaster, 100));

        // Test with contract from different company
        MasterVehicleContract otherCompanyContract = company2.createMasterVehicleContract(
                "MVC206", beneficiary, policyHolder);
        SingleVehicleContract childContract = company2.insureVehicle(
                "SVC205", beneficiary, policyHolder, 50,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        company2.moveSingleVehicleContractToMasterVehicleContract(otherCompanyContract, childContract);

        assertThrows(InvalidContractException.class, () ->
                company.getHandler().pay(otherCompanyContract, 100));
    }

    @Test
    public void testPaymentInstance() {
        // Test 1: Valid construction
        LocalDateTime paymentTime = LocalDateTime.now();
        int amount = 100;
        PaymentInstance payment = new PaymentInstance(paymentTime, amount);

        // Verify properties correctly set
        assertEquals(paymentTime, payment.getPaymentTime());
        assertEquals(amount, payment.getPaymentAmount());

        // Test 2: Null payment time throws exception
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentInstance(null, 100));

        // Test 3: Non-positive amounts throw exception
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentInstance(paymentTime, 0));
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentInstance(paymentTime, -50));

        // Test 4: Comparison based on time
        LocalDateTime earlierTime = paymentTime.minusHours(1);
        LocalDateTime laterTime = paymentTime.plusHours(1);

        PaymentInstance earlierPayment = new PaymentInstance(earlierTime, 200);
        PaymentInstance sameTimePayment = new PaymentInstance(paymentTime, 300);
        PaymentInstance laterPayment = new PaymentInstance(laterTime, 400);

        // Verify comparing based on time
        assertTrue(earlierPayment.compareTo(payment) < 0);
        assertEquals(0, payment.compareTo(sameTimePayment));
        assertTrue(laterPayment.compareTo(payment) > 0);

        // Test 5: TreeSet orders by time
        Set<PaymentInstance> orderedPayments = new TreeSet<>();
        orderedPayments.add(laterPayment);
        orderedPayments.add(earlierPayment);
        orderedPayments.add(payment);

        // Verify payments are ordered by time (earliest first)
        Iterator<PaymentInstance> iterator = orderedPayments.iterator();
        assertEquals(earlierPayment, iterator.next());
        assertEquals(payment, iterator.next());
        assertEquals(laterPayment, iterator.next());

        // Test 6: Equal time but different amounts are considered same in TreeSet
        PaymentInstance sameTimePayment2 = new PaymentInstance(paymentTime, 500);

        // Add to a new TreeSet to verify behavior
        Set<PaymentInstance> paymentsWithDuplicates = new TreeSet<>();
        paymentsWithDuplicates.add(payment);
        paymentsWithDuplicates.add(sameTimePayment);
        paymentsWithDuplicates.add(sameTimePayment2);

        // Since TreeSet uses compareTo, and all have same time, only one will be kept
        assertEquals(1, paymentsWithDuplicates.size());
    }

    @Test
    public void testContractPaymentData() {
        // Test 1: Valid construction
        LocalDateTime initialTime = LocalDateTime.now();
        int premium = 200;
        int initialBalance = 0;

        ContractPaymentData paymentData = new ContractPaymentData(
                premium,
                PremiumPaymentFrequency.MONTHLY,
                initialTime,
                initialBalance);

        // Verify initial properties
        assertEquals(premium, paymentData.getPremium());
        assertEquals(PremiumPaymentFrequency.MONTHLY, paymentData.getPremiumPaymentFrequency());
        assertEquals(initialTime, paymentData.getNextPaymentTime());
        assertEquals(initialBalance, paymentData.getOutstandingBalance());

        // Test 2: Invalid construction with non-positive premium
        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(0, PremiumPaymentFrequency.MONTHLY, initialTime, initialBalance));
        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(-100, PremiumPaymentFrequency.MONTHLY, initialTime, initialBalance));

        // Test 3: Invalid construction with null frequency
        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(premium, null, initialTime, initialBalance));

        // Test 4: Invalid construction with null next payment time
        assertThrows(IllegalArgumentException.class, () ->
                new ContractPaymentData(premium, PremiumPaymentFrequency.MONTHLY, null, initialBalance));

        // Test 5: Setting premium
        paymentData.setPremium(300);
        assertEquals(300, paymentData.getPremium());

        // Test 6: Setting invalid premium
        assertThrows(IllegalArgumentException.class, () -> paymentData.setPremium(0));
        assertThrows(IllegalArgumentException.class, () -> paymentData.setPremium(-50));

        // Test 7: Setting payment frequency
        paymentData.setPremiumPaymentFrequency(PremiumPaymentFrequency.QUARTERLY);
        assertEquals(PremiumPaymentFrequency.QUARTERLY, paymentData.getPremiumPaymentFrequency());

        // Test 8: Setting invalid payment frequency
        assertThrows(IllegalArgumentException.class, () -> paymentData.setPremiumPaymentFrequency(null));

        // Test 9: Setting outstanding balance (accepts any value)
        paymentData.setOutstandingBalance(500);
        assertEquals(500, paymentData.getOutstandingBalance());
        paymentData.setOutstandingBalance(-200);
        assertEquals(-200, paymentData.getOutstandingBalance());
        paymentData.setOutstandingBalance(0);
        assertEquals(0, paymentData.getOutstandingBalance());

        // Test 10: Updating next payment time based on frequency
        // Reset to known state first
        ContractPaymentData monthlyData = new ContractPaymentData(
                premium, PremiumPaymentFrequency.MONTHLY, initialTime, initialBalance);
        ContractPaymentData quarterlyData = new ContractPaymentData(
                premium, PremiumPaymentFrequency.QUARTERLY, initialTime, initialBalance);
        ContractPaymentData semiAnnualData = new ContractPaymentData(
                premium, PremiumPaymentFrequency.SEMI_ANNUAL, initialTime, initialBalance);
        ContractPaymentData annualData = new ContractPaymentData(
                premium, PremiumPaymentFrequency.ANNUAL, initialTime, initialBalance);

        // Update payment times
        monthlyData.updateNextPaymentTime();
        quarterlyData.updateNextPaymentTime();
        semiAnnualData.updateNextPaymentTime();
        annualData.updateNextPaymentTime();

        // Verify updated times
        assertEquals(initialTime.plusMonths(1), monthlyData.getNextPaymentTime());
        assertEquals(initialTime.plusMonths(3), quarterlyData.getNextPaymentTime());
        assertEquals(initialTime.plusMonths(6), semiAnnualData.getNextPaymentTime());
        assertEquals(initialTime.plusMonths(12), annualData.getNextPaymentTime());

        // Test 11: Multiple updates
        monthlyData.updateNextPaymentTime();
        monthlyData.updateNextPaymentTime();
        assertEquals(initialTime.plusMonths(3), monthlyData.getNextPaymentTime());

        // Test 12: Verify PremiumPaymentFrequency enum values
        assertEquals(1, PremiumPaymentFrequency.MONTHLY.getValueInMonths());
        assertEquals(3, PremiumPaymentFrequency.QUARTERLY.getValueInMonths());
        assertEquals(6, PremiumPaymentFrequency.SEMI_ANNUAL.getValueInMonths());
        assertEquals(12, PremiumPaymentFrequency.ANNUAL.getValueInMonths());
    }

    @Test
    public void testPerson() {
        // Test 1: Valid person with birth number (NATURAL)
        Person naturalPerson = new Person("9502130187");
        assertEquals("9502130187", naturalPerson.getId());
        assertEquals(LegalForm.NATURAL, naturalPerson.getLegalForm());
        assertEquals(0, naturalPerson.getPaidOutAmount());
        assertTrue(naturalPerson.getContracts().isEmpty());

        // Test 2: Valid person with IČO (LEGAL)
        Person legalPerson = new Person("12345678");
        assertEquals("12345678", legalPerson.getId());
        assertEquals(LegalForm.LEGAL, legalPerson.getLegalForm());

        // Test 3: Adding contracts
        AbstractContract contract1 = singleContract;  // Using contract from setup
        naturalPerson.addContract(contract1);
        assertEquals(1, naturalPerson.getContracts().size());
        assertTrue(naturalPerson.getContracts().contains(contract1));

        // Test 4: Adding multiple contracts preserves order
        MasterVehicleContract contract2 = masterContract;  // Using contract from setup
        naturalPerson.addContract(contract2);
        assertEquals(2, naturalPerson.getContracts().size());

        // Verify insertion order
        Iterator<AbstractContract> iterator = naturalPerson.getContracts().iterator();
        assertEquals(contract1, iterator.next());
        assertEquals(contract2, iterator.next());

        // Test 5: Payout functionality
        naturalPerson.payout(500);
        assertEquals(500, naturalPerson.getPaidOutAmount());
        naturalPerson.payout(300);
        assertEquals(800, naturalPerson.getPaidOutAmount());

        // Test 6: Invalid payouts
        assertThrows(IllegalArgumentException.class, () -> naturalPerson.payout(0));
        assertThrows(IllegalArgumentException.class, () -> naturalPerson.payout(-100));

        // Test 7: Cannot add null contract
        assertThrows(IllegalArgumentException.class, () -> naturalPerson.addContract(null));

        // Test 8: Invalid birth number (null)
        assertThrows(IllegalArgumentException.class, () -> new Person(null));

        // Test 9: Invalid birth number (empty)
        assertThrows(IllegalArgumentException.class, () -> new Person(""));

        // Test 10: Invalid birth number (wrong length)
        assertThrows(IllegalArgumentException.class, () -> new Person("12345"));
        assertThrows(IllegalArgumentException.class, () -> new Person("12345678901"));

        // Test 11: Invalid birth number (non-digits)
        assertThrows(IllegalArgumentException.class, () -> new Person("92070544X9"));

        // Test 12: Invalid birth number (invalid month)
        assertThrows(IllegalArgumentException.class, () -> new Person("9213054439"));  // Month 13
        assertThrows(IllegalArgumentException.class, () -> new Person("9200054439"));  // Month 00
        assertThrows(IllegalArgumentException.class, () -> new Person("9263054439"));  // Month 63 (women)

        // Test 13: Invalid birth number (9 digits but year > 53)
        assertThrows(IllegalArgumentException.class, () -> new Person("540101123"));

        // Test 14: Invalid birth number (10 digits with invalid checksum)
        assertThrows(IllegalArgumentException.class, () -> new Person("9207054438"));

        // Test 15: Invalid birth number (non-existent date)
        assertThrows(IllegalArgumentException.class, () -> new Person("9202304439"));  // February 30

        // Test 16: Valid birth number variations
        // 9-digit birth number (before 1954)
        Person oldPerson = new Person("531231123");
        assertEquals(LegalForm.NATURAL, oldPerson.getLegalForm());

        // Woman's birth number (month > 50)
        Person femalePerson = new Person("9258054432");
        assertEquals(LegalForm.NATURAL, femalePerson.getLegalForm());

        // Test 17: Valid IČO variations
        Person shortICO = new Person("123456");
        assertEquals(LegalForm.LEGAL, shortICO.getLegalForm());

        Person longICO = new Person("12345678");
        assertEquals(LegalForm.LEGAL, longICO.getLegalForm());

        // Test 18: Invalid IČO
        assertThrows(IllegalArgumentException.class, () -> new Person("12345"));  // Too short
        assertThrows(IllegalArgumentException.class, () -> new Person("123456789"));  // Too long
        assertThrows(IllegalArgumentException.class, () -> new Person("12AB56"));  // Contains letters
    }

    @Test
    public void testContractPayMethod() {
        // Test 1: SingleVehicleContract pay method
        int initialBalance = singleContract.getContractPaymentData().getOutstandingBalance();
        singleContract.pay(100);

        // Verify the payment was processed correctly
        assertEquals(initialBalance - 100, singleContract.getContractPaymentData().getOutstandingBalance());

        // Verify payment was recorded in history
        Map<AbstractContract, Set<PaymentInstance>> history = company.getHandler().getPaymentHistory();
        assertTrue(history.containsKey(singleContract));
        assertEquals(1, history.get(singleContract).size());

        // Test 2: TravelContract pay method
        int travelInitialBalance = travelContract.getContractPaymentData().getOutstandingBalance();
        travelContract.pay(200);

        // Verify the payment was processed correctly
        assertEquals(travelInitialBalance - 200, travelContract.getContractPaymentData().getOutstandingBalance());

        // Verify payment was recorded in history
        assertTrue(history.containsKey(travelContract));
        assertEquals(1, history.get(travelContract).size());

        // Test 3: MasterVehicleContract pay method
        // Create a master contract with child contracts
        MasterVehicleContract masterContractForTest = company.createMasterVehicleContract(
                "MVC222", beneficiary, policyHolder);

        // Create child contracts with different premiums
        SingleVehicleContract child1 = company.insureVehicle(
                "SVC555", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        SingleVehicleContract child2 = company.insureVehicle(
                "SVC666", beneficiary, policyHolder, 150,
                PremiumPaymentFrequency.MONTHLY, vehicle2);

        // Move children to master contract
        company.moveSingleVehicleContractToMasterVehicleContract(masterContractForTest, child1);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContractForTest, child2);

        // Set initial outstanding balances
        child1.getContractPaymentData().setOutstandingBalance(100);
        child2.getContractPaymentData().setOutstandingBalance(200);

        // Call the pay method on the master contract
        masterContractForTest.pay(500);

        // Verify balances were updated correctly (paid off outstanding balances first)
        assertEquals(-100, child1.getContractPaymentData().getOutstandingBalance());
        assertEquals(-100, child2.getContractPaymentData().getOutstandingBalance());
        // The rest should be distributed to future premiums

        // Verify payment was recorded in history
        assertTrue(history.containsKey(masterContractForTest));
        assertEquals(1, history.get(masterContractForTest).size());

        // Test 4: Inactive contract
        singleContract.setInactive();
        assertThrows(InvalidContractException.class, () -> singleContract.pay(50));

        // Test 5: Non-positive amount
        travelContract.setInactive();
        MasterVehicleContract activeMaster = company.createMasterVehicleContract(
                "MVC333", beneficiary, policyHolder);

        // Add a child contract to make the master contract valid for payment
        SingleVehicleContract activeChild = company.insureVehicle(
                "SVC777", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        company.moveSingleVehicleContractToMasterVehicleContract(activeMaster, activeChild);

        assertThrows(IllegalArgumentException.class, () -> activeMaster.pay(0));
        assertThrows(IllegalArgumentException.class, () -> activeMaster.pay(-100));
    }

    @Test
    public void testMasterVehicleContract() {
        // Test 1: Basic constructor validation
        MasterVehicleContract masterContract = company.createMasterVehicleContract(
                "MVC301", beneficiary, policyHolder);

        // Verify initial state
        assertNotNull(masterContract);
        assertEquals("MVC301", masterContract.getContractNumber());
        assertEquals(company, masterContract.getInsurer());
        assertEquals(beneficiary, masterContract.getBeneficiary());
        assertEquals(policyHolder, masterContract.getPolicyHolder());
        assertNull(masterContract.getContractPaymentData());
        assertEquals(0, masterContract.getCoverageAmount());
        assertTrue(masterContract.getChildContracts().isEmpty());
        assertTrue(masterContract.isActive());

        // Test 2: Require policyHolder to be legal entity
        Person naturalPerson = new Person("9553023040"); // Natural person
        assertThrows(IllegalArgumentException.class, () ->
                company.createMasterVehicleContract("MVC302", beneficiary, naturalPerson));

        // Test 3: Add child contracts and verify order
        SingleVehicleContract child1 = company.insureVehicle(
                "SVC301", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        SingleVehicleContract child2 = company.insureVehicle(
                "SVC302", beneficiary, policyHolder, 150,
                PremiumPaymentFrequency.MONTHLY, vehicle2);

        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, child1);
        company.moveSingleVehicleContractToMasterVehicleContract(masterContract, child2);

        // Verify order preservation in child contracts
        assertEquals(2, masterContract.getChildContracts().size());
        Iterator<SingleVehicleContract> iterator = masterContract.getChildContracts().iterator();
        assertEquals(child1, iterator.next());
        assertEquals(child2, iterator.next());

        // Test 4: isActive reflects child contracts' state
        assertTrue(masterContract.isActive()); // All children active

        // Make one child inactive and verify master is still active
        child1.setInactive();
        assertTrue(masterContract.isActive());

        // Make all children inactive and verify master becomes inactive
        child2.setInactive();
        assertFalse(masterContract.isActive());

        // Test 5: Create a new master contract and verify its isActive state directly
        MasterVehicleContract emptyMaster = company.createMasterVehicleContract(
                "MVC303", beneficiary, policyHolder);
        assertTrue(emptyMaster.isActive()); // Should be active by default

        // Test 6: setInactive should make master and all children inactive
        SingleVehicleContract child3 = company.insureVehicle(
                "SVC303", beneficiary, policyHolder, 120,
                PremiumPaymentFrequency.MONTHLY, vehicle);
        SingleVehicleContract child4 = company.insureVehicle(
                "SVC304", beneficiary, policyHolder, 180,
                PremiumPaymentFrequency.MONTHLY, vehicle2);

        company.moveSingleVehicleContractToMasterVehicleContract(emptyMaster, child3);
        company.moveSingleVehicleContractToMasterVehicleContract(emptyMaster, child4);

        assertTrue(child3.isActive());
        assertTrue(child4.isActive());
        assertTrue(emptyMaster.isActive());

        // Call setInactive on master
        emptyMaster.setInactive();

        // Verify all are inactive
        assertFalse(child3.isActive());
        assertFalse(child4.isActive());
        assertFalse(emptyMaster.isActive());

        // Test 7: Master contract with no children uses isActive field directly
        MasterVehicleContract noChildrenMaster = company.createMasterVehicleContract(
                "MVC304", beneficiary, policyHolder);
        assertTrue(noChildrenMaster.isActive());

        noChildrenMaster.setInactive();
        assertFalse(noChildrenMaster.isActive());
    }

    @Test
    public void testSingleVehicleContract() {
        // Test 1: Valid constructor call
        SingleVehicleContract validContract = company.insureVehicle(
                "SVC500", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        assertNotNull(validContract);
        assertEquals("SVC500", validContract.getContractNumber());
        assertEquals(vehicle, validContract.getInsuredVehicle());
        assertNotNull(validContract.getContractPaymentData());

        // Test 2: Null vehicle throws exception
        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle("SVC501", beneficiary, policyHolder, 100,
                        PremiumPaymentFrequency.MONTHLY, null));

        // Test 3: Direct constructor call with null contractPaymentData
        assertThrows(IllegalArgumentException.class, () ->
                new SingleVehicleContract("SVC502", company, beneficiary,
                        policyHolder, null, 5000, vehicle));

        // Test 4: Verify vehicle is immutable through reflection
        SingleVehicleContract contract = company.insureVehicle(
                "SVC503", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        // No setter method exists for insuredVehicle, checking through reflection
        // would be outside the scope of normal testing

        // Alternative approach: verify the getter always returns the same reference
        Vehicle v1 = contract.getInsuredVehicle();
        Vehicle v2 = contract.getInsuredVehicle();
        assertSame(v1, v2);
        assertEquals(vehicle, v1);
    }

    @Test
    public void testAbstractVehicleContract() {
        // Test 1: Valid contract with beneficiary
        SingleVehicleContract contractWithBeneficiary = company.insureVehicle(
                "SVC600", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        assertNotNull(contractWithBeneficiary);
        assertEquals(beneficiary, contractWithBeneficiary.getBeneficiary());
        assertNotEquals(policyHolder, contractWithBeneficiary.getBeneficiary());

        // Test 2: Valid contract with null beneficiary
        SingleVehicleContract contractWithNullBeneficiary = company.insureVehicle(
                "SVC601", null, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        assertNull(contractWithNullBeneficiary.getBeneficiary());

        // Test 3: Exception when beneficiary equals policyHolder
        assertThrows(IllegalArgumentException.class, () ->
                company.insureVehicle("SVC602", policyHolder, policyHolder, 100,
                        PremiumPaymentFrequency.MONTHLY, vehicle));

        // Test 4: Setting null beneficiary is allowed
        contractWithBeneficiary.setBeneficiary(null);
        assertNull(contractWithBeneficiary.getBeneficiary());

        // Test 5: Setting new valid beneficiary
        Person newBeneficiary = new Person("9505159180");
        contractWithNullBeneficiary.setBeneficiary(newBeneficiary);
        assertEquals(newBeneficiary, contractWithNullBeneficiary.getBeneficiary());

        // Test 6: Setting beneficiary equal to policyHolder throws exception
        assertThrows(IllegalArgumentException.class, () ->
                contractWithNullBeneficiary.setBeneficiary(policyHolder));

        // Test 7: Verify payout behavior with beneficiary
        // First, process claim with a contract that has a beneficiary
        int initialBeneficiaryAmount = beneficiary.getPaidOutAmount();
        int initialPolicyHolderAmount = policyHolder.getPaidOutAmount();

        SingleVehicleContract contractForClaim = company.insureVehicle(
                "SVC603", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        company.processClaim(contractForClaim, 5000);

        // Verify beneficiary received the payout, not policyHolder
        assertEquals(initialBeneficiaryAmount + contractForClaim.getCoverageAmount(),
                beneficiary.getPaidOutAmount());
        assertEquals(initialPolicyHolderAmount, policyHolder.getPaidOutAmount());

        // Test 8: Verify payout behavior without beneficiary
        // Process claim with a contract that has no beneficiary
        initialPolicyHolderAmount = policyHolder.getPaidOutAmount();

        SingleVehicleContract contractWithoutBeneficiary = company.insureVehicle(
                "SVC604", null, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        company.processClaim(contractWithoutBeneficiary, 5000);

        // Verify policyHolder received the payout since there's no beneficiary
        assertEquals(initialPolicyHolderAmount + contractWithoutBeneficiary.getCoverageAmount(),
                policyHolder.getPaidOutAmount());
    }

    @Test
    public void testTravelContract() {
        // Create persons with specified IDs
        Person person1 = new Person("123456");         // LEGAL
        Person person2 = new Person("8301166533");     // NATURAL

        // Test 1: Valid travel contract with natural persons (only natural persons allowed)
        Set<Person> validPersons = new HashSet<>();
        validPersons.add(person2);  // Add only NATURAL person

        TravelContract validContract = company.insurePersons(
                "TC700", policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, validPersons);

        assertNotNull(validContract);
        assertEquals("TC700", validContract.getContractNumber());
        assertEquals(policyHolder, validContract.getPolicyHolder());
        assertEquals(validPersons, validContract.getInsuredPersons());
        assertEquals(validPersons.size() * 10, validContract.getCoverageAmount());
        assertTrue(validContract.isActive());

        // Test 2: Contract where policyHolder is also an insured person
        Set<Person> personsWithPolicyHolder = new HashSet<>();
        personsWithPolicyHolder.add(person2);
        personsWithPolicyHolder.add(policyHolder);

        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons("TC701", policyHolder, 100,
                        PremiumPaymentFrequency.MONTHLY, personsWithPolicyHolder));
        //assertTrue(contractWithPolicyHolder.getInsuredPersons().contains(policyHolder));

        // Test 3: Null personsToInsure throws exception
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons("TC702", policyHolder, 100,
                        PremiumPaymentFrequency.MONTHLY, null));

        // Test 4: Empty personsToInsure throws exception
        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons("TC703", policyHolder, 100,
                        PremiumPaymentFrequency.MONTHLY, new HashSet<>()));

        // Test 5: Legal person in insuredPersons throws exception
        Set<Person> invalidPersons = new HashSet<>();
        invalidPersons.add(person2);  // NATURAL
        invalidPersons.add(person1);  // LEGAL

        assertThrows(IllegalArgumentException.class, () ->
                company.insurePersons("TC704", policyHolder, 100,
                        PremiumPaymentFrequency.MONTHLY, invalidPersons));

        // Test 6: Legal entity can be a policyHolder
        // Use person1 (LEGAL) as policyHolder
        TravelContract contractWithLegalPolicyHolder = company.insurePersons(
                "TC705", person1, 100,
                PremiumPaymentFrequency.MONTHLY, validPersons);

        assertEquals(person1, contractWithLegalPolicyHolder.getPolicyHolder());
        assertEquals(LegalForm.LEGAL, contractWithLegalPolicyHolder.getPolicyHolder().getLegalForm());

        // Test 7: Null contractPaymentData throws exception
        assertThrows(IllegalArgumentException.class, () ->
                new TravelContract("TC706", company, policyHolder, null,
                        20, validPersons));

        // Test 8: Verify that the insuredPersons collection is not modifiable
        TravelContract contractToModify = company.insurePersons(
                "TC707", policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, validPersons);

        // The returned set should be unmodifiable
        Set<Person> returnedPersons = contractToModify.getInsuredPersons();
    }

    @Test
    public void testAbstractContract() {
        // Test 1: Create contract and verify basic properties
        SingleVehicleContract contract = company.insureVehicle(
                "AC100", beneficiary, policyHolder, 100,
                PremiumPaymentFrequency.MONTHLY, vehicle);

        // Check immutable properties are set correctly
        assertEquals("AC100", contract.getContractNumber());
        assertEquals(company, contract.getInsurer());
        assertEquals(policyHolder, contract.getPolicyHolder());
        assertNotNull(contract.getContractPaymentData());
        assertTrue(contract.isActive());

        // Test 2: Setting valid coverageAmount
        contract.setCoverageAmount(500);
        assertEquals(500, contract.getCoverageAmount());

        // Test 3: Setting invalid coverageAmount (negative)
        assertThrows(IllegalArgumentException.class, () ->
                contract.setCoverageAmount(-100));

        // Test 4: Changing active state
        assertTrue(contract.isActive());
        contract.setInactive();
        assertFalse(contract.isActive());

        // Test 5: Constructor validation - null insurer
        assertThrows(IllegalArgumentException.class, () ->
                new SingleVehicleContract(
                        "AC101", null, beneficiary, policyHolder,
                        new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY,
                                company.getCurrentTime(), 0),
                        200, vehicle));

        // Test 6: Constructor validation - null policyHolder
        assertThrows(IllegalArgumentException.class, () ->
                new SingleVehicleContract(
                        "AC102", company, beneficiary, null,
                        new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY,
                                company.getCurrentTime(), 0),
                        200, vehicle));

        // Test 7: Constructor validation - empty contractNumber
        assertThrows(IllegalArgumentException.class, () ->
                new SingleVehicleContract(
                        "", company, beneficiary, policyHolder,
                        new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY,
                                company.getCurrentTime(), 0),
                        200, vehicle));

        // Test 8: Constructor validation - null contractNumber
        assertThrows(IllegalArgumentException.class, () ->
                new SingleVehicleContract(
                        null, company, beneficiary, policyHolder,
                        new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY,
                                company.getCurrentTime(), 0),
                        200, vehicle));

        // Test 9: Pay method functionality
        ContractPaymentData paymentData = new ContractPaymentData(100,
                PremiumPaymentFrequency.MONTHLY, company.getCurrentTime(), 300);

        SingleVehicleContract contractForPayment = new SingleVehicleContract(
                "AC103", company, beneficiary, policyHolder, paymentData, 200, vehicle);

        // Initial balance is 300
        assertEquals(300, contractForPayment.getContractPaymentData().getOutstandingBalance());

        // Pay 50, balance should be reduced
        contractForPayment.pay(50);
        assertEquals(250, contractForPayment.getContractPaymentData().getOutstandingBalance());

        // Test 10: equals() and hashCode() based on contractNumber and insurer
        InsuranceCompany company2 = new InsuranceCompany(LocalDateTime.now());

        SingleVehicleContract contract1 = new SingleVehicleContract(
                "AC104", company, beneficiary, policyHolder,
                new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY,
                        company.getCurrentTime(), 0),
                200, vehicle);

        SingleVehicleContract contract2 = new SingleVehicleContract(
                "AC104", company, null, new Person("8301166533"),
                new ContractPaymentData(200, PremiumPaymentFrequency.QUARTERLY,
                        company.getCurrentTime(), 0),
                300, vehicle);

        SingleVehicleContract contract3 = new SingleVehicleContract(
                "AC105", company, beneficiary, policyHolder,
                new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY,
                        company.getCurrentTime(), 0),
                200, vehicle);

        SingleVehicleContract contract4 = new SingleVehicleContract(
                "AC104", company2, beneficiary, policyHolder,
                new ContractPaymentData(100, PremiumPaymentFrequency.MONTHLY,
                        company2.getCurrentTime(), 0),
                200, vehicle);

        // Same contract number and insurer should be equal
        assertEquals(contract1, contract2);
        assertEquals(contract1.hashCode(), contract2.hashCode());

        // Different contract number should not be equal
        assertNotEquals(contract1, contract3);

        // Different insurer should not be equal
        assertNotEquals(contract1, contract4);

        // Test 11: updateBalance method
        LocalDateTime initialTime = company.getCurrentTime();
        company.setCurrentTime(initialTime.plusMonths(2)); // Advance time by 2 months

        // Create contract with payment data
        ContractPaymentData paymentData2 = new ContractPaymentData(100,
                PremiumPaymentFrequency.MONTHLY, initialTime, 0);

        SingleVehicleContract contractToUpdate = new SingleVehicleContract(
                "AC106", company, beneficiary, policyHolder, paymentData2, 200, vehicle);

        // Initially balance is 0
        assertEquals(0, contractToUpdate.getContractPaymentData().getOutstandingBalance());

        // Update balance - should add 2 months of premium (100 each)
        contractToUpdate.updateBalance();
        assertEquals(300, contractToUpdate.getContractPaymentData().getOutstandingBalance());

        // Next payment time should be updated
        assertEquals(initialTime.plusMonths(3),
                contractToUpdate.getContractPaymentData().getNextPaymentTime());
    }

}