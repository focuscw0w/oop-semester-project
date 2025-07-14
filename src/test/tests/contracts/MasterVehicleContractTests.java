package tests.contracts;

import company.InsuranceCompany;
import contracts.MasterVehicleContract;
import contracts.SingleVehicleContract;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

class MasterVehicleContractTests {
    private String contractNumber;
    private String contractNumber1;
    private String contractNumber2;
    private String contractNumber3;
    private InsuranceCompany insurer;
    private Person policyHolder;
    private Person beneficiary;
    private ContractPaymentData contractPaymentData;
    private int coverageAmount;
    private Vehicle vehicleToInsure;
    private SingleVehicleContract singleVehicleContract1;
    private SingleVehicleContract singleVehicleContract2;
    private SingleVehicleContract singleVehicleContract3;

    @BeforeEach
    public void setUp() {
        contractNumber = "123455";
        contractNumber1 = "123456";
        contractNumber2 = "123457";
        contractNumber3 = "123458";
        insurer = new InsuranceCompany(LocalDateTime.now());
        policyHolder = new Person("124526");
        beneficiary = new Person("413554");
        contractPaymentData = new ContractPaymentData(5, PremiumPaymentFrequency.MONTHLY,
                LocalDateTime.now().plusMonths(1), 0);
        coverageAmount = 1000;
        vehicleToInsure = new Vehicle("BA133CD", 10000);
        singleVehicleContract1 = new SingleVehicleContract(contractNumber1, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount, vehicleToInsure);
        singleVehicleContract2 = new SingleVehicleContract(contractNumber2, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount, vehicleToInsure);
        singleVehicleContract3 = new SingleVehicleContract(contractNumber3, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount, vehicleToInsure);
    }

    @Test
    public void givenPolicyHolderIsNotLegalPerson_whenCreatingContract_thenThrowsIllegalArgumentException() {
        var notLegalPerson = new Person("7201011235");

        assertThrows(IllegalArgumentException.class, () ->
                new MasterVehicleContract(contractNumber, insurer, beneficiary, notLegalPerson)
        );
    }

    @Test
    public void givenValidData_whenCreatingContract_thenChildContractsIsNotNull() {
        var contract = new MasterVehicleContract(contractNumber, insurer, beneficiary, policyHolder);

        assertNotNull(contract.getChildContracts());
        assertEquals(LinkedHashSet.class, contract.getChildContracts().getClass());
    }

    @Test
    public void givenMasterContractWhereAllChildContractsAreInactive_whenCheckingActivity_thenContractIsInactive() {
        var masterVehicleContract = new MasterVehicleContract(contractNumber, insurer, beneficiary, policyHolder);

        masterVehicleContract.getChildContracts().add(singleVehicleContract1);
        masterVehicleContract.getChildContracts().add(singleVehicleContract2);
        masterVehicleContract.getChildContracts().add(singleVehicleContract3);

        singleVehicleContract1.setInactive();
        singleVehicleContract2.setInactive();
        singleVehicleContract3.setInactive();

        assertFalse(masterVehicleContract.isActive());
    }

    @Test
    public void givenMasterContractWithNoChildren_whenCheckingActivity_thenContractIsActive() {
        var masterVehicleContract = new MasterVehicleContract(contractNumber, insurer, beneficiary, policyHolder);

        assertTrue(masterVehicleContract.isActive());
    }

    @Test
    public void givenMasterContractWithNoChildren_whenSettingInactive_thenContractIsNotActive() {
        var masterVehicleContract = new MasterVehicleContract(contractNumber, insurer, beneficiary, policyHolder);

        masterVehicleContract.setInactive();

        assertFalse(masterVehicleContract.isActive());
    }

    @Test
    public void givenMasterContractWithChildContracts_whenSettingInactive_thenChildContractsAreNotActive() {
        var masterVehicleContract = new MasterVehicleContract(contractNumber, insurer, beneficiary, policyHolder);

        masterVehicleContract.getChildContracts().add(singleVehicleContract1);
        masterVehicleContract.getChildContracts().add(singleVehicleContract2);
        masterVehicleContract.getChildContracts().add(singleVehicleContract3);

        assertTrue(masterVehicleContract.getChildContracts().stream().allMatch(childContract -> childContract.isActive()));

        masterVehicleContract.setInactive();

        assertTrue(masterVehicleContract.getChildContracts().stream().noneMatch(childContract -> childContract.isActive()));
    }


    @Test
    public void givenMasterContract_whenSettingInactive_thenAttributeIsActiveIsFalse() {
        var masterVehicleContract = new MasterVehicleContract(contractNumber, insurer, beneficiary, policyHolder);

        assertTrue(masterVehicleContract.isActive());

        masterVehicleContract.setInactive();

        assertFalse(masterVehicleContract.isActive());
    }
}