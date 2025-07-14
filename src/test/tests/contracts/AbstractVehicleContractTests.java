package tests.contracts;

import company.InsuranceCompany;
import contracts.AbstractVehicleContract;
import objects.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AbstractVehicleContractTests {
    private String contractNumber;
    private InsuranceCompany insurer;
    private Person policyHolder;
    private Person beneficiary;
    private Person beneficiaryCopy;
    private ContractPaymentData contractPaymentData;
    private int coverageAmount;


    @BeforeEach
    public void setUp() {
        contractNumber = "123456";
        insurer = new InsuranceCompany(LocalDateTime.now());
        policyHolder = new Person("123456");
        beneficiary = new Person("411211532");
        beneficiaryCopy = new Person("411211532");
        contractPaymentData = new ContractPaymentData(5, PremiumPaymentFrequency.MONTHLY,
                LocalDateTime.now().plusMonths(1), 0);
        coverageAmount = 1000;
    }

    @Test
    public void givenValidData_whenCreatingContract_thenPropertiesAreSet() {
        AbstractVehicleContract contract = new TestVehicleContract(contractNumber, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount);

        assertEquals(contractNumber, contract.getContractNumber());
        assertEquals(insurer, contract.getInsurer());
        assertEquals(beneficiary, contract.getBeneficiary());
        assertEquals(policyHolder, contract.getPolicyHolder());
        assertEquals(contractPaymentData, contract.getContractPaymentData());
        assertEquals(coverageAmount, contract.getCoverageAmount());
    }

    @Test
    public void givenBeneficiaryEqualsPolicyHolder_whenCreatingContract_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TestVehicleContract(contractNumber, insurer, beneficiary, beneficiaryCopy, contractPaymentData, coverageAmount)
        );
    }

    @Test
    public void givenBeneficiaryIsNull_whenCreatingContract_thenContractIsCreated() {
        beneficiary = null;

        var testVehicleContract = new TestVehicleContract(contractNumber, insurer, beneficiary, beneficiaryCopy, contractPaymentData, coverageAmount);

        assertNotNull(testVehicleContract);
    }

    @Test
    public void givenBeneficiaryIsNull_whenSettingBeneficiary_thenBeneficiaryIsSetToNull() {
        var testVehicleContract = new TestVehicleContract(contractNumber, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount);
        testVehicleContract.setBeneficiary(null);

        assertNull(testVehicleContract.getBeneficiary());
    }

    private static class TestVehicleContract extends AbstractVehicleContract {
        public TestVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary,
                                   Person policyHolder, ContractPaymentData contractPaymentData, int coverageAmount) {
            super(contractNumber, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount);
        }
    }
}