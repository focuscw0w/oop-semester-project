package tests.contracts;

import company.InsuranceCompany;
import contracts.SingleVehicleContract;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SingleVehicleContractTests {
    private String contractNumber;
    private InsuranceCompany insurer;
    private Person policyHolder;
    private Person beneficiary;
    private ContractPaymentData contractPaymentData;
    private int coverageAmount;
    private Vehicle vehicleToInsure;

    @BeforeEach
    public void setUp() {
        contractNumber = "123456";
        insurer = new InsuranceCompany(LocalDateTime.now());
        policyHolder = new Person("124526");
        beneficiary = new Person("413554");
        contractPaymentData = new ContractPaymentData(5, PremiumPaymentFrequency.MONTHLY,
                LocalDateTime.now().plusMonths(1), 0);
        coverageAmount = 1000;
        vehicleToInsure = new Vehicle("BA133CD", 10000);
    }

    @Test
    public void givenValidData_whenCreatingContract_thenPropertiesAreSet() {
        SingleVehicleContract contract = new SingleVehicleContract(contractNumber, insurer, beneficiary, policyHolder,
                contractPaymentData, coverageAmount, vehicleToInsure);

        assertEquals(contractNumber, contract.getContractNumber());
        assertEquals(insurer, contract.getInsurer());
        assertEquals(beneficiary, contract.getBeneficiary());
        assertEquals(policyHolder, contract.getPolicyHolder());
        assertEquals(contractPaymentData, contract.getContractPaymentData());
        assertEquals(coverageAmount, contract.getCoverageAmount());
        assertEquals(vehicleToInsure, contract.getInsuredVehicle());
    }

    @Test
    public void givenVehicleToInsureIsNull_whenCreatingContract_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new SingleVehicleContract(contractNumber, insurer, beneficiary, policyHolder, contractPaymentData,
                        coverageAmount, null)
        );
    }

    @Test
    public void givenContractPaymentDataIsNull_whenCreatingContract_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new SingleVehicleContract(contractNumber, insurer, beneficiary, policyHolder, null,
                        coverageAmount, vehicleToInsure)
        );
    }
}