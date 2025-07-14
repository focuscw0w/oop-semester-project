package contracts;

import company.InsuranceCompany;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;

public class SingleVehicleContract extends AbstractVehicleContract {
    private final Vehicle insuredVehicle;

    public SingleVehicleContract(String contractNumber, InsuranceCompany insurer, Person beneficiary,
                                 Person policyHolder, ContractPaymentData contractPaymentData,
                                 int coverageAmount, Vehicle vehicleToInsure) {
        super(contractNumber, insurer, beneficiary, policyHolder, contractPaymentData, coverageAmount);

        // Či je vozidlo null
        if (vehicleToInsure == null) {
            throw new IllegalArgumentException("Invalid vehicle.");
        }

        // Či je contractPaymentData null
        if (contractPaymentData == null) {
            throw new IllegalArgumentException("Invalid contractPaymentData.");
        }

        this.insuredVehicle = vehicleToInsure;
    }

    public Vehicle getInsuredVehicle() {
        return insuredVehicle;
    }
}
