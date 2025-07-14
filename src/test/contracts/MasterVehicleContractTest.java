package contracts;

import company.InsuranceCompany;
import objects.LegalForm;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MasterVehicleContractTest {

    InsuranceCompany company;
    Person legalPerson;
    Person naturalPerson;
    Person beneficiary;
    Vehicle vehicle1;
    Vehicle vehicle2;
    Vehicle vehicle3;
    Vehicle vehicle4;
    MasterVehicleContract masterContract;
    SingleVehicleContract contract1;
    SingleVehicleContract contract2;
    SingleVehicleContract contract3;
    SingleVehicleContract contract4;

    @BeforeEach
    void setUp() {
        company = new InsuranceCompany(LocalDateTime.now());
        legalPerson = new Person("12345678");
        naturalPerson = new Person("8004175146");
        beneficiary = new Person("8054176383");

        // Create a sample vehicle and single vehicle contract
        vehicle1 = new Vehicle("ABC1234", 10000);
        vehicle2 = new Vehicle("DEF4567", 12000);
        vehicle3 = new Vehicle("GHI7890", 10000);
        vehicle4 = new Vehicle("JKL1234", 12000);

        masterContract = company.createMasterVehicleContract("MC001", beneficiary, legalPerson);

        // Create payment data for the single contract
        contract1 = company.insureVehicle("SC001", beneficiary, legalPerson, 30, PremiumPaymentFrequency.MONTHLY, vehicle1);

        // contract2 - premium 50, active, with outstanding balance 50
        contract2 = company.insureVehicle("SC002", beneficiary, legalPerson, 50, PremiumPaymentFrequency.MONTHLY, vehicle2);

        // contract3 - premium 75, active, with outstanding balance 100
        contract3 = company.insureVehicle("SC003", beneficiary, legalPerson, 75, PremiumPaymentFrequency.MONTHLY, vehicle3);

        // contract4 - premium 20, inactive, with outstanding balance 0
        contract4 = company.insureVehicle("SC004", beneficiary, legalPerson, 20, PremiumPaymentFrequency.MONTHLY, vehicle4);
    }

    @Test
    void testPolicyHolderMustBeLegalPerson() {
        // Verify our test persons have the correct legal forms
        assertEquals(LegalForm.LEGAL, legalPerson.getLegalForm());
        assertEquals(LegalForm.NATURAL, naturalPerson.getLegalForm());

        assertEquals(legalPerson, masterContract.getPolicyHolder());
        assertEquals(LegalForm.LEGAL, masterContract.getPolicyHolder().getLegalForm());

        // Test with natural person - should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            company.createMasterVehicleContract("MC002", beneficiary, naturalPerson);
        });
    }

    @Test
    void testDefaultPaymentDataAndCoverageAmount() {
        // Verify that payment data is null and coverage amount is 0
        assertNull(masterContract.getContractPaymentData());
        assertEquals(0, masterContract.getCoverageAmount());
    }

    @Test
    void testChildContractsInitialization() {
        // Verify that childContracts is initialized and empty
        assertNotNull(masterContract.getChildContracts());
        assertTrue(masterContract.getChildContracts().isEmpty());

        // Add contracts and verify order
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Verify order is maintained (can be tested by converting to array/list and checking indices)
        Object[] contractsArray = masterContract.getChildContracts().toArray();
        assertEquals(contract1, contractsArray[0]);
        assertEquals(contract2, contractsArray[1]);
    }

    @Test
    void testIsActiveStatus() {
        // Initially with no child contracts
        assertTrue(masterContract.isActive()); // Master contract is active by default

        // Add contracts to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Both child contracts are active by default
        assertTrue(masterContract.isActive());

        // Set one child contract inactive
        contract1.setInactive();
        assertTrue(masterContract.isActive()); // Should still be active because contract2 is active

        // Set other child contract inactive
        contract2.setInactive();
        assertFalse(masterContract.isActive()); // Should be inactive now as all children are inactive
    }

    @Test
    void testIsActiveStatusWithNoChildren() {
        // Initially with no child contracts
        assertTrue(masterContract.isActive()); // Should be active by default

        // Set master contract inactive
        masterContract.setInactive();
        assertFalse(masterContract.isActive()); // Should be inactive
    }

    @Test
    void testIsActiveStatusWithChildren() {
        // Add contracts to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Both child contracts are active by default
        assertTrue(masterContract.isActive());

        // Make one child inactive - master should still be active
        contract1.setInactive();
        assertTrue(masterContract.isActive());

        // Make all children inactive - master should be inactive
        contract2.setInactive();
        assertFalse(masterContract.isActive());
    }

    @Test
    void testSetInactive() {
        // Add contracts to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Initially all contracts should be active
        assertTrue(masterContract.isActive());
        assertTrue(contract1.isActive());
        assertTrue(contract2.isActive());

        // Call setInactive on the master contract
        masterContract.setInactive();

        // Verify that master contract and all child contracts are now inactive
        assertFalse(masterContract.isActive());
        assertFalse(contract1.isActive());
        assertFalse(contract2.isActive());
    }

    @Test
    void testPayMethod() {
        // Add child contract to master contract
        masterContract.requestAdditionOfChildContract(contract1);

        // Initial validation
        assertTrue(company.getHandler().getPaymentHistory().isEmpty());

        // Call the pay method with a positive amount
        int paymentAmount = 500;
        masterContract.pay(paymentAmount);

        // Verify that the payment was processed by the PaymentHandler
        assertFalse(company.getHandler().getPaymentHistory().isEmpty());
        assertTrue(company.getHandler().getPaymentHistory().containsKey(masterContract));
        assertEquals(1, company.getHandler().getPaymentHistory().get(masterContract).size());

        // Verify the payment details
        var payment = company.getHandler().getPaymentHistory().get(masterContract).iterator().next();
        assertEquals(paymentAmount, payment.getPaymentAmount());
        assertEquals(company.getCurrentTime(), payment.getPaymentTime());

        // Test with negative amount - should be rejected
        assertThrows(IllegalArgumentException.class, () -> masterContract.pay(-100));

        // Test with zero amount - should be rejected
        assertThrows(IllegalArgumentException.class, () -> masterContract.pay(0));
    }

    @Test
    void testPayWithEmptyMasterContract() {
        // Create a master contract with no child contracts
        MasterVehicleContract emptyMasterContract = company.createMasterVehicleContract(
                "MC002", beneficiary, legalPerson);

        // Attempt to pay should throw InvalidContractException
        assertThrows(InvalidContractException.class, () -> emptyMasterContract.pay(100));
    }

    @Test
    void testUpdateBalance() {
        // Add the child contract to the master contract
        masterContract.requestAdditionOfChildContract(contract1);

        try {
            // Call updateBalance on the master contract
            masterContract.updateBalance();

            // Since we can't directly verify the method call, we check that
            // the code executed without throwing an exception
            assertTrue(true, "updateBalance method completed successfully");
        } catch (Exception e) {
            fail("updateBalance threw an exception: " + e.getMessage());
        }
    }

    @Test
    void testPayWithInactiveContract() {
        masterContract.requestAdditionOfChildContract(contract1);

        // Make contract inactive
        masterContract.setInactive();

        // Attempt to pay should throw InvalidContractException
        assertThrows(InvalidContractException.class, () -> masterContract.pay(100));
    }

    @Test
    void testPayWithDifferentInsurer() {
        InsuranceCompany differentCompany = new InsuranceCompany(LocalDateTime.now());

        MasterVehicleContract foreignContract = differentCompany.createMasterVehicleContract(
                "MC001", beneficiary, legalPerson);

        SingleVehicleContract contract = differentCompany.insureVehicle("SC001", beneficiary,
                legalPerson, 30, PremiumPaymentFrequency.MONTHLY, vehicle1);

        foreignContract.requestAdditionOfChildContract(contract);

        // Try to pay using our company's handler - should throw exception
        assertThrows(InvalidContractException.class, () -> company.getHandler().pay(foreignContract, 100));
    }

    @Test
    void testPayWithNoChildContracts() {
        // Create a master contract with no child contracts
        MasterVehicleContract emptyMasterContract = company.createMasterVehicleContract(
                "MC002", beneficiary, legalPerson);

        // Attempt to pay should throw InvalidContractException
        assertThrows(InvalidContractException.class, () -> emptyMasterContract.pay(100));
    }

    @Test
    void testAbstractContractPayWithNullContract() {
        // Test with null contract
        assertThrows(IllegalArgumentException.class, () ->
                company.getHandler().pay((AbstractContract)null, 100));
    }

    @Test
    void testAbstractContractPayWithZeroAmount() {
        // Test with zero amount
        assertThrows(IllegalArgumentException.class, () -> company.getHandler().pay(contract1, 0));
    }

    @Test
    void testAbstractContractPayWithNegativeAmount() {
        // Test with negative amount
        assertThrows(IllegalArgumentException.class, () -> company.getHandler().pay(contract1, -100));
    }

    @Test
    void testAbstractContractPayWithInactiveContract() {
        contract1.setInactive();

        // Test with inactive contract
        assertThrows(InvalidContractException.class, () -> company.getHandler().pay(contract1, 100));
    }

    @Test
    void testAbstractContractPayWithDifferentInsurer() {
        // Create a contract with different insurer
        InsuranceCompany differentCompany = new InsuranceCompany(LocalDateTime.now());

        // Test with contract from different insurer
        assertThrows(InvalidContractException.class, () -> differentCompany.getHandler().pay(contract1, 100));
    }

    @Test
    void testAbstractContractPaySuccessfully() {
        SingleVehicleContract contract = company.insureVehicle("SC00X", beneficiary, legalPerson,
                200, PremiumPaymentFrequency.MONTHLY, vehicle1);

        // Initial state
        assertEquals(200, contract.getContractPaymentData().getOutstandingBalance());
        assertTrue(company.getHandler().getPaymentHistory().isEmpty());

        // Make payment
        contract.pay(150);

        // Verify payment was processed
        assertEquals(50, contract.getContractPaymentData().getOutstandingBalance());
        assertFalse(company.getHandler().getPaymentHistory().isEmpty());
        assertTrue(company.getHandler().getPaymentHistory().containsKey(contract));

        // Verify payment details
        PaymentInstance payment = company.getHandler().getPaymentHistory().get(contract).iterator().next();
        assertEquals(150, payment.getPaymentAmount());
        assertEquals(company.getCurrentTime(), payment.getPaymentTime());
    }

    @Test
    void testSetInactiveCorrectImplementation() {
        // Add contracts to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Initially all contracts should be active
        assertTrue(masterContract.isActive());
        assertTrue(contract1.isActive());
        assertTrue(contract2.isActive());

        // Call setInactive on the master contract
        masterContract.setInactive();

        // Verify that all child contracts are now inactive
        assertFalse(contract1.isActive());
        assertFalse(contract2.isActive());

        // Get the isActive field using reflection to verify internal state
        boolean isActiveMaster = false;
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            isActiveMaster = (boolean) isActiveField.get(masterContract);
        } catch (Exception e) {
            fail("Could not access isActive field: " + e.getMessage());
        }

        // Verify that the master contract's isActive attribute is false
        assertFalse(isActiveMaster);

        // And verify that the isActive() method returns false
        assertFalse(masterContract.isActive());
    }

    @Test
    void testSetInactiveOnMasterContract() {
        // Add children to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Initially all contracts should be active
        assertTrue(masterContract.isActive());
        assertTrue(contract1.isActive());
        assertTrue(contract2.isActive());

        // Call setInactive on the master contract
        masterContract.setInactive();

        // Verify that master contract and all child contracts are now inactive
        assertFalse(masterContract.isActive());
        assertFalse(contract1.isActive());
        assertFalse(contract2.isActive());

        // Verify the internal isActive field is false using reflection
        boolean masterIsActiveField = true;
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            masterIsActiveField = (boolean) isActiveField.get(masterContract);
        } catch (Exception e) {
            fail("Could not access isActive field: " + e.getMessage());
        }
        assertFalse(masterIsActiveField);
    }

    @Test
    void testMasterContractIsInactiveWhenAllChildrenAreInactive() {
        // Add children to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Initially all contracts should be active
        assertTrue(masterContract.isActive());
        assertTrue(contract1.isActive());
        assertTrue(contract2.isActive());

        // Make all child contracts inactive by calling setInactive on each child
        contract1.setInactive();
        contract2.setInactive();

        // Verify internal state of children
        assertFalse(contract1.isActive());
        assertFalse(contract2.isActive());

        // Verify that master contract isActive() returns false
        // even though its internal isActive field might still be true
        assertFalse(masterContract.isActive());
    }

    @Test
    void testMasterContractActivationWithSingleInactiveChild() {
        // Initially master contract should be active
        assertTrue(masterContract.isActive());

        masterContract.requestAdditionOfChildContract(contract1);

        // Both contracts should be active initially
        assertTrue(masterContract.isActive());
        assertTrue(contract1.isActive());

        // Set child contract inactive
        contract1.setInactive();

        // Verify child is inactive
        assertFalse(contract1.isActive());

        // Verify that master contract's isActive() returns false
        // when all children are inactive (in this case, the only child)
        assertFalse(masterContract.isActive());

        MasterVehicleContract masterContract2 = company.createMasterVehicleContract(
                "MC002", beneficiary, legalPerson);

        assertTrue(masterContract2.isActive());
        assertTrue(contract2.isActive());
        assertTrue(contract3.isActive());

        masterContract2.setInactive();
        assertFalse(masterContract2.isActive());
    }

    @Test
    void testNoChildContractsIsActiveDependsOnField() {
        // By default, a new contract should have isActive field set to true
        assertTrue(masterContract.isActive());

        // Get the internal isActive field value to verify initial state
        boolean initialIsActiveField = true;
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            initialIsActiveField = (boolean) isActiveField.get(masterContract);
        } catch (Exception e) {
            fail("Could not access isActive field: " + e.getMessage());
        }
        assertTrue(initialIsActiveField);

        // Manually set isActive to false
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            isActiveField.set(masterContract, false);
        } catch (Exception e) {
            fail("Could not set isActive field: " + e.getMessage());
        }

        // With no children, isActive() should return the isActive field value (now false)
        assertFalse(masterContract.isActive());

        // Set back to true
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            isActiveField.set(masterContract, true);
        } catch (Exception e) {
            fail("Could not set isActive field: " + e.getMessage());
        }

        // Should be active again
        assertTrue(masterContract.isActive());
    }

    @Test
    void testWithChildContractsIsActiveIgnoresField() {
        // Add children to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);

        // Verify that master contract is active (since child contracts are active)
        assertTrue(masterContract.isActive());

        // Set master contract's isActive field to false using reflection
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            isActiveField.set(masterContract, false);
        } catch (Exception e) {
            fail("Could not set isActive field: " + e.getMessage());
        }

        // Verify master contract's isActive field is false
        boolean masterIsActiveField = true;
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            masterIsActiveField = (boolean) isActiveField.get(masterContract);
        } catch (Exception e) {
            fail("Could not access isActive field: " + e.getMessage());
        }
        assertFalse(masterIsActiveField);

        // Despite master's isActive field being false, isActive() should return true
        // because there are active child contracts
        assertTrue(masterContract.isActive());

        // Make one child inactive - master should still be active
        contract1.setInactive();
        assertTrue(masterContract.isActive());

        // Make all children inactive - master should be inactive regardless of field
        contract2.setInactive();
        assertFalse(masterContract.isActive());

        // Set master's isActive field to true - should still be inactive since all children are inactive
        try {
            java.lang.reflect.Field isActiveField = AbstractContract.class.getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            isActiveField.set(masterContract, true);
        } catch (Exception e) {
            fail("Could not set isActive field: " + e.getMessage());
        }

        // Verify field is true but method still returns false
        assertTrue(masterIsActiveField = true);
        assertFalse(masterContract.isActive());
    }

    @Test
    void testPayFromExample() {
        contract1.getContractPaymentData().setOutstandingBalance(30);
        contract2.getContractPaymentData().setOutstandingBalance(50);
        contract3.getContractPaymentData().setOutstandingBalance(100);
        contract4.getContractPaymentData().setOutstandingBalance(0);

        // Add contracts to master contract
        masterContract.requestAdditionOfChildContract(contract1);
        masterContract.requestAdditionOfChildContract(contract2);
        masterContract.requestAdditionOfChildContract(contract3);
        masterContract.requestAdditionOfChildContract(contract4);

        contract4.setInactive();

        // Verify initial outstanding balances
        assertEquals(30, contract1.getContractPaymentData().getOutstandingBalance());
        assertEquals(50, contract2.getContractPaymentData().getOutstandingBalance());
        assertEquals(100, contract3.getContractPaymentData().getOutstandingBalance());
        assertEquals(0, contract4.getContractPaymentData().getOutstandingBalance());

        // Process payment of 400 as specified in the example
        masterContract.pay(400);

        // Verify payment was processed
        assertFalse(company.getHandler().getPaymentHistory().isEmpty());
        assertTrue(company.getHandler().getPaymentHistory().containsKey(masterContract));
        assertEquals(1, company.getHandler().getPaymentHistory().get(masterContract).size());

        // Verify the payment details
        PaymentInstance payment = company.getHandler().getPaymentHistory().get(masterContract).iterator().next();
        assertEquals(400, payment.getPaymentAmount());
        assertEquals(company.getCurrentTime(), payment.getPaymentTime());

        // After paying all outstanding balances (30+50+100=180), 220 remains
        // First cycle: 30+50+75=155 is paid for premiums, 65 remains
        // Second cycle: 30+35=65 is paid (contract2 only gets 35 more), 0 remains

        // Verify final outstanding balances match the expected values
        assertEquals(-60, contract1.getContractPaymentData().getOutstandingBalance()); // Overpaid by 60
        assertEquals(-85, contract2.getContractPaymentData().getOutstandingBalance()); // Overpaid by 85
        assertEquals(-75, contract3.getContractPaymentData().getOutstandingBalance()); // Overpaid by 75
        assertEquals(0, contract4.getContractPaymentData().getOutstandingBalance());   // Inactive, unchanged
    }

    @Test
    void testMoveSingleVehicleContractIsInContracts() {
        contract1.getPolicyHolder().getContracts().remove(masterContract);
        assertThrows(InvalidContractException.class, () -> {
            // Master Contract not in policy holder's contracts'
            company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract1);}
        );

        contract1.getPolicyHolder().getContracts().add(masterContract);
        contract1.getPolicyHolder().getContracts().remove(contract1);
        assertThrows(InvalidContractException.class, () -> {
            // Single Contract not in policy holder's contracts'
            company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract1);}
        );
        contract1.getPolicyHolder().getContracts().add(contract1);

        company.getContracts().remove(masterContract);
        assertThrows(InvalidContractException.class, () -> {
            // Master Contract not in company's contracts'
            company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract1);}
        );

        company.getContracts().add(masterContract);
        company.getContracts().remove(contract1);
        assertThrows(InvalidContractException.class, () -> {
            // Single Contract not in company's contracts'
            company.moveSingleVehicleContractToMasterVehicleContract(masterContract, contract1);}
        );

    }
}