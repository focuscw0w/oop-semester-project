package payment;

import company.InsuranceCompany;
import contracts.AbstractContract;
import contracts.InvalidContractException;
import contracts.MasterVehicleContract;
import contracts.SingleVehicleContract;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PaymentHandler {
    private final Map<AbstractContract, Set<PaymentInstance>> paymentHistory;
    private final InsuranceCompany insurer;

    public PaymentHandler(InsuranceCompany insurer) {
        // Či je insurer null
        if (insurer == null) {
            throw new IllegalArgumentException("insurer cannot be null");
        }

        this.insurer = insurer;
        this.paymentHistory = new HashMap<>();
    }

    public Map<AbstractContract, Set<PaymentInstance>> getPaymentHistory() {
        return paymentHistory;
    }

    private int payOutstandingBalances(Set<SingleVehicleContract> childContracts, int amount) {
        for (SingleVehicleContract childContract : childContracts) {
            if (childContract.isActive()) {
                ContractPaymentData paymentData = childContract.getContractPaymentData();
                int outstandingBalance = paymentData.getOutstandingBalance();

                // Či má zmluva nedoplatok
                if (outstandingBalance > 0) {
                    // Či je dostatok financii
                    if (amount >= outstandingBalance) {
                        amount -= outstandingBalance;
                        paymentData.setOutstandingBalance(0);
                    } else {
                        // Znížime nedoplatok o zostávajúcu sumu
                        paymentData.decreaseOutstandingBalance(amount);
                        amount = 0;
                        break;
                    }
                }
            }
        }
        return amount;
    }

    private void createPrepayments(Set<SingleVehicleContract> childContracts, int amount) {
        // Či sú financie na úhradu
        while (amount > 0) {
            boolean anyPaymentMade = false;

            for (SingleVehicleContract childContract : childContracts) {
                if (childContract.isActive()) {
                    ContractPaymentData paymentData = childContract.getContractPaymentData();
                    int premium = paymentData.getPremium();

                    // Či je dostatok financii
                    if (amount >= premium) {
                        paymentData.decreaseOutstandingBalance(premium);
                        amount -= premium;
                        anyPaymentMade = true;
                    } else if (amount > 0) {
                        // Znížime nedoplatok o zostávajúcu sumu
                        paymentData.decreaseOutstandingBalance(amount);
                        amount = 0;
                        anyPaymentMade = true;
                        break;
                    }
                }
            }

            if (!anyPaymentMade) {
                break;
            }
        }
    }

    private void recordPayment(AbstractContract contract, int amount) {
        LocalDateTime paymentTime = this.insurer.getCurrentTime();
        PaymentInstance paymentInstance = new PaymentInstance(paymentTime, amount);

        // Či zmluva ešte nemá žiadne záznamy v histórii platieb
        if (!paymentHistory.containsKey(contract)) {
            // Vytvoríme novú množinu usporiadanú podľa času
            Set<PaymentInstance> payments = new TreeSet<>();
            payments.add(paymentInstance);
            paymentHistory.put(contract, payments);
        } else {
            // Ak už zmluva má históriu platieb, pridáme novú platbu do existujúcej množiny
            paymentHistory.get(contract).add(paymentInstance);
        }
    }

    private void validatePaymentRequest(AbstractContract contract, int amount) {
        // Či je contract null
        if (contract == null) {
            throw new IllegalArgumentException("Contract cannot be null.");
        }

        // Či je amount nekladný
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        // Či je contract neaktívny
        if (!contract.isActive()) {
            throw new InvalidContractException("Contract is not active.");
        }

        // Či nejde o zmluvu poisťovateľa, ktorý prevádzkuje tento PaymentHandler
        if (this.insurer != contract.getInsurer()) {
            throw new InvalidContractException("Contract does not belong to this insurer.");
        }
    }

    public void pay(MasterVehicleContract contract, int amount) {
        validatePaymentRequest(contract, amount);

        // Či neobsahuje žiadne dcérske zmluvy
        if (contract.getChildContracts().isEmpty()) {
            throw new InvalidContractException("Master contract has no child contracts");
        }

        Set<SingleVehicleContract> childContracts = contract.getChildContracts();
        int originalAmount = amount;

        // Zostávajúcu suma po úhrade všetkých nedoplatkov
        amount = payOutstandingBalances(childContracts, amount);

        // Spotrebujeme celú zostávajúcu sumu
        createPrepayments(childContracts, amount);

        recordPayment(contract, originalAmount);
    }

    public void pay(AbstractContract contract, int amount) {
        validatePaymentRequest(contract, amount);

        ContractPaymentData paymentData = contract.getContractPaymentData();
        paymentData.decreaseOutstandingBalance(amount);

        recordPayment(contract, amount);
    }
}
