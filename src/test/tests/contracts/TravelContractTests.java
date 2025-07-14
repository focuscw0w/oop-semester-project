package tests.contracts;

import company.InsuranceCompany;
import contracts.TravelContract;
import objects.LegalForm;
import objects.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TravelContractTests {
    private String contractNumber;
    private InsuranceCompany insurer;
    private Person policyHolder;
    private ContractPaymentData contractPaymentData;
    private int coverageAmount;
    private Set<Person> personsToInsure;

    @BeforeEach
    public void setUp() {
        contractNumber = "123456";
        insurer = new InsuranceCompany(LocalDateTime.now());
        policyHolder = new Person("124543");
        contractPaymentData = new ContractPaymentData(5, PremiumPaymentFrequency.MONTHLY,
                LocalDateTime.now().plusMonths(1), 0);
        coverageAmount = 1000;
        Person person1 = new Person("525101234");
        Person person2 = new Person("7201011235");
        Person person3 = new Person("1001011231");
        personsToInsure = new HashSet<>();
        personsToInsure.add(person1);
        personsToInsure.add(person2);
        personsToInsure.add(person3);
    }

    @Test
    public void givenValidData_whenCreatingContract_thenPropertiesAreSet() {
        TravelContract contract = new TravelContract(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount, personsToInsure);

        assertEquals(contractNumber, contract.getContractNumber());
        assertEquals(insurer, contract.getInsurer());
        assertEquals(policyHolder, contract.getPolicyHolder());
        assertEquals(contractPaymentData, contract.getContractPaymentData());
        assertEquals(coverageAmount, contract.getCoverageAmount());
        assertEquals(personsToInsure, contract.getInsuredPersons());
    }

    @Test
    public void givenPersonsToInsureIsNull_whenCreatingContract_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TravelContract(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount, null)
        );
    }

    @Test
    public void givenPersonsToInsureIsEmpty_whenCreatingContract_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TravelContract(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount, new HashSet<>())
        );
    }

    @Test
    public void givenContractPaymentDataIsNull_whenCreatingContract_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TravelContract(contractNumber, insurer, policyHolder, null, coverageAmount, personsToInsure)
        );
    }

    @Test
    public void givenPersonsToInsureContainsLegalPerson_whenCreatingContract_thenThrowsIllegalArgumentException() {
        var legalPerson = new Person("425523");
        assertEquals(LegalForm.LEGAL, legalPerson.getLegalForm());

        personsToInsure.add(legalPerson);

        assertThrows(IllegalArgumentException.class, () ->
                new TravelContract(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount, personsToInsure)
        );
    }
}