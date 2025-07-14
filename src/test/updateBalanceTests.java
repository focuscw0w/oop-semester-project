import company.InsuranceCompany;
import contracts.*;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class updateBalanceTests {

    Person legalEntity;
    Vehicle vehicle1;
    LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        legalEntity = new Person("123456");
        vehicle1 = new Vehicle("AA111AA", 15_000);
    }

    @Test
    public void testUpdateBalanceCallsCorrectChargePremiumMethod() {
        // Create a custom test implementation of InsuranceCompany to track method calls
        class TestInsuranceCompany extends InsuranceCompany {
            boolean abstractContractMethodCalled = false;
            boolean masterContractMethodCalled = false;
            AbstractContract lastContractCharged = null;

            public TestInsuranceCompany(LocalDateTime currentTime) {
                super(currentTime);
            }

            @Override
            public void chargePremiumOnContract(AbstractContract contract) {
                abstractContractMethodCalled = true;
                lastContractCharged = contract;
                super.chargePremiumOnContract(contract);
            }

            @Override
            public void chargePremiumOnContract(MasterVehicleContract contract) {
                masterContractMethodCalled = true;
                lastContractCharged = contract;
                super.chargePremiumOnContract(contract);
            }

            public void resetTracking() {
                abstractContractMethodCalled = false;
                masterContractMethodCalled = false;
                lastContractCharged = null;
            }
        }

        // Create test objects
        TestInsuranceCompany testInsurer = new TestInsuranceCompany(LocalDateTime.now());
        Person legalEntity = new Person("123456");

        // Test case 1: SingleVehicleContract should call the general method
        SingleVehicleContract singleContract = testInsurer.insureVehicle("SV001", null,
                legalEntity, 100, PremiumPaymentFrequency.MONTHLY, vehicle1);

        // Reset tracking and call updateBalance
        testInsurer.resetTracking();
        singleContract.updateBalance();

        // Verify the correct method was called
        assertTrue(testInsurer.abstractContractMethodCalled,
                "chargePremiumOnContract(AbstractContract) should be called for SingleVehicleContract");
        assertFalse(testInsurer.masterContractMethodCalled,
                "chargePremiumOnContract(MasterVehicleContract) should not be called for SingleVehicleContract");
        assertEquals(singleContract, testInsurer.lastContractCharged,
                "The single contract should be passed to the method");

        // Test case 2: MasterVehicleContract should call the specialized method
        MasterVehicleContract masterContract = testInsurer.createMasterVehicleContract("MC001", null, legalEntity);

        // Reset tracking and call updateBalance
        testInsurer.resetTracking();
        masterContract.updateBalance();

        // Verify the correct method was called
        assertFalse(testInsurer.abstractContractMethodCalled,
                "chargePremiumOnContract(AbstractContract) should not be called for MasterVehicleContract");
        assertTrue(testInsurer.masterContractMethodCalled,
                "chargePremiumOnContract(MasterVehicleContract) should be called for MasterVehicleContract");
        assertEquals(masterContract, testInsurer.lastContractCharged,
                "The master contract should be passed to the method");

        // Test case 3: Create a MasterVehicleContract with child contracts
        MasterVehicleContract parentContract = testInsurer.createMasterVehicleContract("MC002", null, legalEntity);
        SingleVehicleContract childContract1 = testInsurer.insureVehicle("CH001", null,
                legalEntity, 100, PremiumPaymentFrequency.MONTHLY, vehicle1);

        // Add child contract
        parentContract.requestAdditionOfChildContract(childContract1);

        // Reset tracking and call updateBalance on the master contract
        testInsurer.resetTracking();
        parentContract.updateBalance();

        // Verify master contract method was called (not abstract contract method)
        assertTrue(testInsurer.abstractContractMethodCalled,
                "For master contract with children, abstract method should be called");
        assertTrue(testInsurer.masterContractMethodCalled,
                "For master contract with children, master contract method should be called");
        assertEquals(childContract1, testInsurer.lastContractCharged,
                "The parent contract should be passed to the method");
    }

    @Test
    void testMoveSingleVehicleContractToMasterVehicleContract_SameInsurerAndActive() {
        // Setup test environment
        LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        InsuranceCompany insurer1 = new InsuranceCompany(testTime);
        InsuranceCompany insurer2 = new InsuranceCompany(testTime);

        // Create policyholder and vehicle
        Person policyholder = new Person("123456");
        Vehicle vehicle = new Vehicle("ABC1234", 10000);

        // Test 1: Same insurer, both active - should succeed
        MasterVehicleContract masterContract = insurer1.createMasterVehicleContract(
                "M001", null, policyholder);
        SingleVehicleContract singleContract = insurer1.insureVehicle(
                "S001", null, policyholder, 200, PremiumPaymentFrequency.MONTHLY, vehicle);

        // Should succeed
        insurer1.moveSingleVehicleContractToMasterVehicleContract(masterContract, singleContract);

        assertTrue(masterContract.getChildContracts().contains(singleContract));
        assertFalse(insurer1.getContracts().contains(singleContract));

        // Test 2: Different insurers - should throw exception
        MasterVehicleContract masterContract2 = insurer2.createMasterVehicleContract(
                "M002", null, policyholder);
        SingleVehicleContract singleContract2 = insurer1.insureVehicle(
                "S002", null, policyholder, 200, PremiumPaymentFrequency.MONTHLY, vehicle);

        assertThrows(InvalidContractException.class, () ->
                insurer1.moveSingleVehicleContractToMasterVehicleContract(masterContract2, singleContract2));

        // Test 3: Same insurer but inactive master contract - should throw exception
        MasterVehicleContract inactiveMaster = insurer1.createMasterVehicleContract(
                "M003", null, policyholder);
        inactiveMaster.setInactive();
        SingleVehicleContract singleContract3 = insurer1.insureVehicle(
                "S003", null, policyholder, 200, PremiumPaymentFrequency.MONTHLY, vehicle);

        assertThrows(InvalidContractException.class, () ->
                insurer1.moveSingleVehicleContractToMasterVehicleContract(inactiveMaster, singleContract3));

        // Test 4: Same insurer but inactive single contract - should throw exception
        MasterVehicleContract masterContract4 = insurer1.createMasterVehicleContract(
                "M004", null, policyholder);
        SingleVehicleContract inactiveSingle = insurer1.insureVehicle(
                "S004", null, policyholder, 200, PremiumPaymentFrequency.MONTHLY, vehicle);
        inactiveSingle.setInactive();

        assertThrows(InvalidContractException.class, () ->
                insurer1.moveSingleVehicleContractToMasterVehicleContract(masterContract4, inactiveSingle));
    }
}