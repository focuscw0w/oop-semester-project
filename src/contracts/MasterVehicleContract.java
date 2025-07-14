package contracts;

import company.InsuranceCompany;
import objects.LegalForm;
import objects.Person;

import java.util.LinkedHashSet;
import java.util.Set;

public class MasterVehicleContract extends AbstractVehicleContract {
    private final Set<SingleVehicleContract> childContracts;

    public MasterVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary,
                                 Person policyHolder) {
        super(contractNumber, insurer, beneficiary, policyHolder, null, 0);

        // Či je poistík právnická osoba
        LegalForm formOfPolicyHolder = policyHolder.getLegalForm();
        if (formOfPolicyHolder != LegalForm.LEGAL) {
            throw new IllegalArgumentException("The Policy Holder is not Legal.");
        }

        this.childContracts = new LinkedHashSet<>();
    }

    public Set<SingleVehicleContract> getChildContracts() {
        return childContracts;
    }

    public void requestAdditionOfChildContract(SingleVehicleContract contract) {
        insurer.moveSingleVehicleContractToMasterVehicleContract(this, contract);
    }

    @Override
    public boolean isActive() {
        if (childContracts.isEmpty()) {
            return super.isActive();
        }

        for (SingleVehicleContract child : childContracts) {
            if (child.isActive()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setInactive() {
        for (SingleVehicleContract child : childContracts) {
            child.setInactive();
        }
        super.setInactive();
    }

    @Override
    public void pay(int amount) {
        this.insurer.getHandler().pay(this, amount);
    }

    @Override
    public void updateBalance() {
        this.getInsurer().chargePremiumOnContract(this);
    }
}
