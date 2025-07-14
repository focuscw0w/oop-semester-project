package contracts;

import company.InsuranceCompany;
import objects.Person;
import payment.ContractPaymentData;

import java.util.Objects;

public abstract class AbstractContract {
    private final String contractNumber;
    protected final InsuranceCompany insurer;
    protected final Person policyHolder;
    protected final ContractPaymentData contractPaymentData;
    protected int coverageAmount;
    protected boolean isActive;

    public AbstractContract(String contractNumber, InsuranceCompany insurer, Person policyHolder,
                            ContractPaymentData contractPaymentData, int coverageAmount) {
        // Či ide o prázdny reťazec odlišný
        if (contractNumber == null || contractNumber.isEmpty()) {
            throw new IllegalArgumentException("Contract number cannot be null or empty.");
        }

        // Či je poisťovateľ null
        if (insurer == null) {
            throw new IllegalArgumentException("Insurance company cannot be null.");
        }

        // Či je poistník null
        if (policyHolder == null) {
            throw new IllegalArgumentException("Policy holder cannot be null.");
        }

        // Či je výška poistného záporná
        if (coverageAmount < 0) {
            throw new IllegalArgumentException("Coverage amount cannot be negative.");
        }

        this.contractNumber = contractNumber;
        this.coverageAmount = coverageAmount;
        this.insurer = insurer;
        this.policyHolder = policyHolder;
        this.contractPaymentData = contractPaymentData;
        this.isActive = true;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public Person getPolicyHolder() {
        return policyHolder;
    }

    public InsuranceCompany getInsurer() {
        return insurer;
    }

    public int getCoverageAmount() {
        return coverageAmount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setInactive() {
        isActive = false;
    }

    public void setCoverageAmount(int coverageAmount) {
        if (coverageAmount < 0) {
            throw new IllegalArgumentException("Coverage amount cannot be negative.");
        }

        this.coverageAmount = coverageAmount;
    }

    public ContractPaymentData getContractPaymentData() {
        return contractPaymentData;
    }

    public void pay(int amount) {
        this.insurer.getHandler().pay(this, amount);
    }

    public void updateBalance() {
        this.insurer.chargePremiumOnContract(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractContract that = (AbstractContract) o;

        return contractNumber.equals(that.contractNumber) &&
                insurer.equals(that.insurer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractNumber, insurer);
    }
}
