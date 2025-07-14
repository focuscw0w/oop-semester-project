package contracts;

import static org.junit.jupiter.api.Assertions.*;

import company.InsuranceCompany;
import objects.LegalForm;
import objects.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;


import java.util.HashSet;
import java.util.Set;

public class TravelContractTest {

    private InsuranceCompany company;
    private Person naturalPerson;
    private Person legalPerson;
    private ContractPaymentData paymentData;
    private int coverageAmount;
    private String contractNumber;
    private Set<Person> personsToInsure;

    @BeforeEach
    public void setUp() {
        company = new InsuranceCompany(LocalDateTime.now());
        naturalPerson = new Person("5601036056");  // Valid birth number
        legalPerson = new Person("12345678");      // Valid registration number
        paymentData = new ContractPaymentData(
                1000,  // premium
                PremiumPaymentFrequency.MONTHLY,  // premiumPaymentFrequency
                LocalDateTime.now().plusMonths(1),  // nextPaymentTime
                0  // outstandingBalance
        );        coverageAmount = 10000;
        contractNumber = "TC123456";

        personsToInsure = new HashSet<>();
        personsToInsure.add(new Person("1352156597")); // Another natural person
    }

    @Test
    public void testValidTravelContractCreation() {
        TravelContract contract = new TravelContract(
                contractNumber, company, naturalPerson,
                paymentData, coverageAmount, personsToInsure);

        assertNotNull(contract);
        assertEquals(contractNumber, contract.getContractNumber());
        assertEquals(company, contract.getInsurer());
        assertEquals(naturalPerson, contract.getPolicyHolder());
        assertEquals(coverageAmount, contract.getCoverageAmount());
        assertEquals(personsToInsure, contract.getInsuredPersons());
        assertTrue(contract.isActive());
    }

    @Test
    public void testNullInsuredPersonsSet() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract(
                    contractNumber, company, naturalPerson,
                    paymentData, coverageAmount, null);
        });
    }

    @Test
    public void testEmptyInsuredPersonsSet() {
        Set<Person> emptySet = new HashSet<>();
        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract(
                    contractNumber, company, naturalPerson,
                    paymentData, coverageAmount, emptySet);
        });
    }

    @Test
    public void testNullContractPaymentData() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract(
                    contractNumber, company, naturalPerson,
                    null, coverageAmount, personsToInsure);
        });
    }

    @Test
    public void testLegalPersonInInsuredPersonsSet() {
        Set<Person> mixedPersons = new HashSet<>();
        mixedPersons.add(naturalPerson);
        mixedPersons.add(legalPerson);  // Adding a legal person which should cause exception

        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract(
                    contractNumber, company, naturalPerson,
                    paymentData, coverageAmount, mixedPersons);
        });
    }

    @Test
    public void testSetInactive() {
        TravelContract contract = new TravelContract(
                contractNumber, company, naturalPerson,
                paymentData, coverageAmount, personsToInsure);

        assertTrue(contract.isActive());
        contract.setInactive();
        assertFalse(contract.isActive());
    }

    @Test
    public void testPolicyHolderNotInInsuredPersonsSet() {
        TravelContract contract = new TravelContract(
                contractNumber, company, naturalPerson,
                paymentData, coverageAmount, personsToInsure);

        // The policyHolder doesn't need to be in the insuredPersons
        assertFalse(contract.getInsuredPersons().contains(naturalPerson));
    }

    @Test
    public void testPolicyHolderInInsuredPersonsSet() {
        // Create a set that includes the policy holder
        Set<Person> personsWithPolicyHolder = new HashSet<>(personsToInsure);
        personsWithPolicyHolder.add(naturalPerson);

        TravelContract contract = new TravelContract(
                contractNumber, company, naturalPerson,
                paymentData, coverageAmount, personsWithPolicyHolder);

        // Both cases are valid - policyHolder can be part of insuredPersons
        assertTrue(contract.getInsuredPersons().contains(naturalPerson));
    }
}