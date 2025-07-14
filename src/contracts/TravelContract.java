package contracts;

import company.InsuranceCompany;
import objects.LegalForm;
import objects.Person;
import payment.ContractPaymentData;

import java.util.HashSet;
import java.util.Set;

public class TravelContract extends AbstractContract {
    private final Set<Person> insuredPersons;

    public TravelContract(String contractNumber, InsuranceCompany insurer, Person policyHolder,
                          ContractPaymentData contractPaymentData, int coverageAmount,
                          Set<Person> personsToInsure) {
        super(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);

        // Či je personsToInsure null alebo prázdna množina
        if (personsToInsure == null || personsToInsure.isEmpty()) {
            throw new IllegalArgumentException("Persons to insure cannot be null or empty");
        }

        // Či je contractPaymentData null
        if (contractPaymentData == null) {
            throw new IllegalArgumentException("Contract payment data cannot be null");
        }

        // Či sú všetky poistené osoby fyzické osoby
        for (Person person : personsToInsure) {
            if (person.getLegalForm() != LegalForm.NATURAL) {
                throw new IllegalArgumentException("Only natural persons can be insured in travel contract");
            }
        }

        this.insuredPersons = personsToInsure;
    }

    public Set<Person> getInsuredPersons() {
        return insuredPersons;
    }
}
