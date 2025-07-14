package company;

import contracts.*;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;
import payment.PaymentHandler;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class InsuranceCompany {
    private final Set<AbstractContract> contracts;
    private final PaymentHandler handler;
    private LocalDateTime currentTime;

    public InsuranceCompany(LocalDateTime currentTime) {
        // Či je currentTime null
        if (currentTime == null) {
            throw new IllegalArgumentException("CurrentTime cannot be null.");
        }

        this.currentTime = currentTime;
        this.contracts = new LinkedHashSet<>();
        this.handler = new PaymentHandler(this);
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime newCurrentTime) {
        if (newCurrentTime == null) {
            throw new IllegalArgumentException("CurrentTime cannot be null.");
        }
        this.currentTime = newCurrentTime;
    }

    public Set<AbstractContract> getContracts() {
        return contracts;
    }

    public PaymentHandler getHandler() {
        return handler;
    }

    private boolean contractNumberExists(String contractNumber) {
        return contracts.stream()
                .anyMatch(contract -> contract.getContractNumber().equals(contractNumber));
    }

    private static int getTwoPercentsOfVehicleValue(PremiumPaymentFrequency proposedPaymentFrequency, Vehicle vehicleToInsure) {
        // Či je vozidlo na poistenie null
        if (vehicleToInsure == null) {
            throw new IllegalArgumentException("vehicleToInsure to insure cannot be null.");
        }

        // Či je proposedPaymentFrequency null
        if (proposedPaymentFrequency == null) {
            throw new IllegalArgumentException("ProposedPaymentFrequency cannot be null.");
        }

        int vehicleValue = vehicleToInsure.getOriginalValue();
        return (int) (vehicleValue * 0.02);
    }

    public SingleVehicleContract insureVehicle(String contractNumber, Person beneficiary, Person policyHolder,
                                               int proposedPremium, PremiumPaymentFrequency proposedPaymentFrequency,
                                               Vehicle vehicleToInsure) {
        // Či v danej poisťovni nejestvuje iná zmluva s contractNumber
        if (contractNumberExists(contractNumber)) {
            throw new IllegalArgumentException("Contract number already exists");
        }

        // Či je vozidlo na poistenie null
        if (vehicleToInsure == null) {
            throw new IllegalArgumentException("PersonsToInsure cannot be null.");
        }

        // Či je proposedPaymentFrequency null
        if (proposedPaymentFrequency == null) {
            throw new IllegalArgumentException("ProposedPaymentFrequency cannot be null.");
        }

        // Či nie je proposedPremium kladné
        if (proposedPremium <= 0) {
            throw new IllegalArgumentException("proposedPremium must be greater than 0.");
        }

        int twoPercentsOfVehicleValue = getTwoPercentsOfVehicleValue(proposedPaymentFrequency, vehicleToInsure);
        // Celková ročná čiastka
        int paymentAmount = (int) (proposedPremium * ((double)12 / proposedPaymentFrequency.getValueInMonths()));

        /* Či celková ročná čiastka, ktorú poistník zaplatí je
        väčšia alebo rovná 2% z ceny */
        if (paymentAmount < twoPercentsOfVehicleValue) {
            throw new IllegalArgumentException("Total payment amount does not meet minimum 2% requirement.");
        }

        /* V platobných dátach sa nastaví premium a premiumPaymentFrequency podľa navrhovaných hodnôt,
         nedoplatok sa nastaví na 0 a dátum ďalšej platby sa nastaví na currentTime poisťovne */
        ContractPaymentData paymentData = new ContractPaymentData(
                proposedPremium,
                proposedPaymentFrequency,
                currentTime,
                0
        );

        int halfOfVehicleValue = vehicleToInsure.getOriginalValue() / 2;

        SingleVehicleContract newContract = new SingleVehicleContract(
                contractNumber,
                this,
                beneficiary,
                policyHolder,
                paymentData,
                halfOfVehicleValue,
                vehicleToInsure
        );

        chargePremiumOnContract(newContract);

        contracts.add(newContract);
        policyHolder.addContract(newContract);

        return newContract;
    }

    public TravelContract insurePersons(String contractNumber, Person policyHolder, int proposedPremium,
                                        PremiumPaymentFrequency proposedPaymentFrequency, Set<Person> personsToInsure) {
        // Či v danej poisťovni nejestvuje iná zmluva s contractNumber
        if (contractNumberExists(contractNumber)) {
            throw new IllegalArgumentException("Contract number already exists");
        }

        // Či sú ľudia na poistenie null
        if (personsToInsure == null) {
            throw new IllegalArgumentException("PersonsToInsure cannot be null.");
        }

        // Či je proposedPaymentFrequency null
        if (proposedPaymentFrequency == null) {
            throw new IllegalArgumentException("ProposedPaymentFrequency cannot be null.");
        }

        // Či nie je proposed
        if (proposedPremium <= 0) {
            throw new IllegalArgumentException("proposedPremium must be greater than zero.");
        }

        int personsToInsureCount = personsToInsure.size();
        int fiveTimesPersonCount = 5 * personsToInsureCount;

        // Celková ročná čiastka
        int paymentAmount = (int) (proposedPremium * ((double)12 / proposedPaymentFrequency.getValueInMonths()));

        // Či celková ročná čiastka je väčšia alebo rovná 5-násobku počtu poistených osôb
        if (paymentAmount < fiveTimesPersonCount) {
            throw new IllegalArgumentException("Total payment amount does not meet minimum requirement of 5 times the number of insured persons");
        }

        ContractPaymentData paymentData = new ContractPaymentData(
                proposedPremium,
                proposedPaymentFrequency,
                currentTime,
                0
        );

        // coverageAmount nastavený na 10 násobok počtu poistených osôb
        int coverageAmount = 10 * personsToInsureCount;

        TravelContract newContract = new TravelContract(
                contractNumber,
                this,
                policyHolder,
                paymentData,
                coverageAmount,
                personsToInsure
        );

        chargePremiumOnContract(newContract);
        contracts.add(newContract);
        policyHolder.addContract(newContract);

        return newContract;
    }

    public MasterVehicleContract createMasterVehicleContract(String contractNumber, Person beneficiary, Person policyHolder) {
        // Či v danej poisťovni nejestvuje iná zmluva s contractNumber
        if (contractNumberExists(contractNumber)) {
            throw new IllegalArgumentException("Contract number already exists");
        }

        MasterVehicleContract newContract = new MasterVehicleContract(
                contractNumber,
                this,
                beneficiary,
                policyHolder
        );

        contracts.add(newContract);
        policyHolder.addContract(newContract);

        return newContract;
    }

    public void moveSingleVehicleContractToMasterVehicleContract(MasterVehicleContract masterVehicleContract,
                                                                 SingleVehicleContract singleVehicleContract) {
        // Či je masterVehicleContract null
        if (masterVehicleContract == null) {
           throw new IllegalArgumentException("Master vehicle contract is null.");
        }

        // Či je singleVehicleContract null
        if (singleVehicleContract == null) {
            throw new IllegalArgumentException("Single vehicle contract is null.");
        }

        // Či nie sú obe zmluvy aktívne
        if (!masterVehicleContract.isActive() || !singleVehicleContract.isActive()) {
            throw new InvalidContractException("Master vehicle contract or single vehicle contract is not active.");
        }

        // Či nemajú zmluvy rovnakú poisťovňu
        if (!singleVehicleContract.getInsurer().equals(this) || !masterVehicleContract.getInsurer().equals(this)) {
            throw new InvalidContractException("Contracts must belong to this insurance company.");
        }

        // Či nemajú zmluvy rovnakého poistníka
        if (!masterVehicleContract.getPolicyHolder().equals(singleVehicleContract.getPolicyHolder())) {
            throw new InvalidContractException("Contracts do not have the same policy holder.");
        }

        // Či poisťovňa obsahuje obe zmluvy
        if (!contracts.contains(singleVehicleContract) || !contracts.contains(masterVehicleContract)) {
            throw new InvalidContractException("Contracts must be in the company's contract list.");
        }

        // Či masterVehicleContract je v zozname zmlúv svojho poistníka
        if (!masterVehicleContract.getPolicyHolder().getContracts().contains(masterVehicleContract)) {
            throw new InvalidContractException("Policy holder must have the master contract in their contract list.");
        }

        // Či singleVehicleContract je v zozname zmlúv svojho poistníka
        if (!singleVehicleContract.getPolicyHolder().getContracts().contains(singleVehicleContract)) {
            throw new InvalidContractException("Policy holder must have the single contract in their contract list.");
        }

        contracts.remove(singleVehicleContract);

        // Odstránenime singleVehicleContract z množiny zmlúv poistníka
        Person policyHolder = singleVehicleContract.getPolicyHolder();
        Set<AbstractContract> policyHolderContracts = policyHolder.getContracts();
        policyHolderContracts.remove(singleVehicleContract);

        Set<SingleVehicleContract> childContracts = masterVehicleContract.getChildContracts();
        childContracts.add(singleVehicleContract);
    }

    public void chargePremiumsOnContracts() {
        for (AbstractContract contract : contracts) {
            if (contract.isActive()) {
                contract.updateBalance();
            }
        }
    }

    public void chargePremiumOnContract(AbstractContract contract) {
        ContractPaymentData paymentData = contract.getContractPaymentData();

        // Či daná zmluva má termín splatnosti pred časom currentTime alebo je zhodný s časom currentTime
        while (paymentData.getNextPaymentTime().isBefore(currentTime) ||
                paymentData.getNextPaymentTime().isEqual(currentTime)) {
            int premium = paymentData.getPremium();
            int outStandingBalance = paymentData.getOutstandingBalance();

            paymentData.setOutstandingBalance(outStandingBalance + premium);
            paymentData.updateNextPaymentTime();
        }
    }

    public void chargePremiumOnContract(MasterVehicleContract contract) {
        for (SingleVehicleContract childContract : contract.getChildContracts()) {
            chargePremiumOnContract(childContract);
        }
    }

    private static int getPayoutPerPerson(TravelContract travelContract, Set<Person> affectedPersons) {
        Set<Person> insuredPersons = travelContract.getInsuredPersons();
        // AffectedPersons musí byť množinou poistených osôb v travelContract
        if (!insuredPersons.containsAll(affectedPersons)) {
            throw new IllegalArgumentException("All affected persons must be insured under this contract");
        }

        // Či nie je travelContract aktívny
        if (!travelContract.isActive()) {
            throw new InvalidContractException("Contract is not active");
        }

        int totalCoverage = travelContract.getCoverageAmount();

        return totalCoverage / affectedPersons.size();
    }

    private static Person resolveRecipient(SingleVehicleContract contract) {
        Person beneficiary = contract.getBeneficiary();
        return (beneficiary != null) ? beneficiary : contract.getPolicyHolder();
    }

    public void processClaim(TravelContract travelContract, Set<Person> affectedPersons) {
        // Či je travelContract null
        if (travelContract == null) {
            throw new IllegalArgumentException("Travel contract is null.");
        }

        /* Či je affectedPersons null a nie je prázdna množina,
         ktorá je podmnožinou poistených osôb v travelContract
        *  */
        if (affectedPersons == null || affectedPersons.isEmpty()) {
            throw new IllegalArgumentException("Affected persons must not be null or empty.");
        }

        // Výpočet výšky poistného plnenia pre každú poškodenú osobu.
        int payoutPerPerson = getPayoutPerPerson(travelContract, affectedPersons);

        for (Person person : affectedPersons) {
            person.payout(payoutPerPerson);
        }

        travelContract.setInactive();
    }

    public void processClaim(SingleVehicleContract singleVehicleContract, int expectedDamages) {
        // Či je singleVehicleContract null
        if (singleVehicleContract == null) {
            throw new IllegalArgumentException("Single vehicle contract is null");
        }

        // Či nie je expectedDamages kladný
        if (expectedDamages <= 0) {
            throw new IllegalArgumentException("Expected damage amount to be greater than 0");
        }

        // Či nie je singleVehicleContract aktívna zmluva
        if (!singleVehicleContract.isActive()) {
            throw new InvalidContractException("Contract is not active");
        }

        Person recipient = resolveRecipient(singleVehicleContract);

        int payoutAmount = singleVehicleContract.getCoverageAmount();
        recipient.payout(payoutAmount);

        /* Ak je parameter expectedDamages väčší alebo rovný 70% hodnoty vozidla,
        tak sa to považuje za totálnu škodu a zmluva samotná sa zmení na neaktívnu. */
        int vehicleValue = singleVehicleContract.getInsuredVehicle().getOriginalValue();
        if (expectedDamages >= (int)(vehicleValue * 0.7)) {
            singleVehicleContract.setInactive();
        }
    }

}

