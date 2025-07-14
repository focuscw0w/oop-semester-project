package tests.shared;


import company.InsuranceCompany;
import contracts.AbstractContract;
import objects.Person;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

public class TestContract extends AbstractContract {
    public TestContract(String contractNumber, InsuranceCompany insurer, Person policyHolder,
                        ContractPaymentData contractPaymentData, int coverageAmount) {
        super(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);
    }

    public static TestContract Create(String contractNumber) {
        InsuranceCompany insurer = new InsuranceCompany(LocalDateTime.now());
        Person policyHolder = new Person("12345678");
        ContractPaymentData contractPaymentData = new ContractPaymentData(5, PremiumPaymentFrequency.MONTHLY,
                LocalDateTime.now().plusMonths(1), 0);
        int coverageAmount = 1000;
        return new TestContract(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);
    }

    public static TestContract Create(String contractNumber, InsuranceCompany insurer) {
        Person policyHolder = new Person("12345678");
        ContractPaymentData contractPaymentData = new ContractPaymentData(5, PremiumPaymentFrequency.MONTHLY,
                LocalDateTime.now().plusMonths(1), 0);
        int coverageAmount = 1000;
        return new TestContract(contractNumber, insurer, policyHolder, contractPaymentData, coverageAmount);
    }
}