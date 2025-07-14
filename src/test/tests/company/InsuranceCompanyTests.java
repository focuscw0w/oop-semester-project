package tests.company;

import company.InsuranceCompany;
import contracts.*;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;
import tests.shared.TestContract;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InsuranceCompanyTests {
    String contractNumber;
    Person beneficiary;
    Person policyHolder;
    Person policyHolder2;
    int proposedPremium;
    PremiumPaymentFrequency proposedPaymentFrequency;
    String licencePlate;
    int originalPrice;
    Vehicle vehicleToInsure;
    LocalDateTime currentTime;
    InsuranceCompany insuranceCompany;
    InsuranceCompany insuranceCompany2;
    Set<Person> personsToInsure;

    @BeforeEach
    public void setUp() {
        contractNumber = "a2352fs";
        beneficiary = new Person("7201011235");
        policyHolder = new Person("132453");
        policyHolder2 = new Person("142453");
        proposedPremium = 25;
        proposedPaymentFrequency = PremiumPaymentFrequency.QUARTERLY;
        licencePlate = "BA111PZ";
        originalPrice = 1000;
        vehicleToInsure = new Vehicle(licencePlate, originalPrice);
        currentTime = LocalDateTime.now();
        insuranceCompany = new InsuranceCompany(currentTime);
        insuranceCompany2 = new InsuranceCompany(currentTime);
        var person1 = new Person("1001011231");
        var person2 = new Person("7201011235");
        var person3 = new Person("0402114911");
        personsToInsure = new HashSet<>();
        personsToInsure.add(person1);
        personsToInsure.add(person2);
        personsToInsure.add(person3);
    }

    @Test
    public void givenCurrentTimeIsNull_whenCreatingInsuranceCompany_thenThrowsIllegalArgumentException() {
        LocalDateTime currentTime = null;

        assertThrows(IllegalArgumentException.class, () -> new InsuranceCompany(currentTime));
    }

    @Test
    public void givenCurrentTime_whenCreatingInsuranceCompany_thenCurrentTimeIsSet() {
        var currentTime = LocalDateTime.now();
        var insuranceCompany = new InsuranceCompany(currentTime);

        assertEquals(currentTime, insuranceCompany.getCurrentTime());
    }

    @Test
    public void givenCurrentTime_whenCreatingInsuranceCompany_thenPropertiesAreSet() {
        var currentTime = LocalDateTime.now();
        var insuranceCompany = new InsuranceCompany(currentTime);

        assertNotNull(insuranceCompany.getHandler());
        assertNotNull(insuranceCompany.getContracts());
        assertNotNull(insuranceCompany.getCurrentTime());
    }

    @Test
    public void givenNewCurrentTimeIsNull_whenSettingCurrentTime_thenThrowsIllegalArgumentException() {
        assertEquals(currentTime, insuranceCompany.getCurrentTime());

        LocalDateTime newCurrentTime = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.setCurrentTime(newCurrentTime));
    }

    @Test
    public void givenNewCurrentTime_whenSettingCurrentTime_thenCurrentTimeIsSet() {
        assertEquals(currentTime, insuranceCompany.getCurrentTime());

        var newCurrentTime = currentTime.plusDays(1);

        insuranceCompany.setCurrentTime(newCurrentTime);

        assertEquals(newCurrentTime, insuranceCompany.getCurrentTime());
    }

    @Test
    public void givenContractNumberIsNull_whenInsuringVehicle_thenThrowsIllegalArgumentException() {
        contractNumber = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        ));
    }

    @Test
    public void givenProposedPaymentFrequencyIsNull_whenInsuringVehicle_thenThrowsIllegalArgumentException(){
        proposedPaymentFrequency = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        ));
    }

    @Test
    public void givenVehicleIsNull_whenInsuringVehicle_thenThrowsIllegalArgumentException(){
        vehicleToInsure = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        ));
    }

    @Test
    public void givenYearlyPremiumIsLessThanTwoPercent_whenInsuringVehicle_thenThrowsIllegalArgumentException() {
        proposedPremium = 20;
        proposedPaymentFrequency = PremiumPaymentFrequency.QUARTERLY;
        originalPrice = 4500;
        vehicleToInsure = new Vehicle(licencePlate, originalPrice);
        var yearlyPremium = proposedPremium * 12 / proposedPaymentFrequency.getValueInMonths();

        assertTrue(yearlyPremium < (originalPrice * 2) / 100);

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        ));
    }

    @Test
    public void givenInsuranceCompanyHasContractWithContractNumber_whenInsuringVehicleWithTheSameContractNumber_thenThrowsIllegalArgumentException() {
        insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        ));
    }

    @Test
    public void givenValidData_whenInsuringVehicle_thenReturnsValidSingleVehicleContract() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertEquals(insuranceCompany, singleVehicleContract.getInsurer());
        assertEquals(contractNumber, singleVehicleContract.getContractNumber());
        assertEquals(beneficiary, singleVehicleContract.getBeneficiary());
        assertEquals(policyHolder, singleVehicleContract.getPolicyHolder());
        assertNotNull(singleVehicleContract.getContractPaymentData());
        assertEquals(vehicleToInsure, singleVehicleContract.getInsuredVehicle());
        assertNotEquals(0, singleVehicleContract.getCoverageAmount());
    }

    @Test
    public void givenValidData_whenInsuringVehicle_thenCoverageAmountIsHalfThePriceOfInsuredVehicle() {
        var insurance = new InsuranceCompany(currentTime);

        SingleVehicleContract singleVehicleContract = insurance.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertEquals(originalPrice / 2, singleVehicleContract.getCoverageAmount());
    }

    @Test
    public void givenValidData_whenInsuringVehicle_thenContractPaymentDataIsValid() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        ContractPaymentData contractPaymentData = singleVehicleContract.getContractPaymentData();

        assertEquals(proposedPremium, contractPaymentData.getPremium());
        assertEquals(proposedPremium, contractPaymentData.getOutstandingBalance());
        assertEquals(proposedPaymentFrequency, contractPaymentData.getPremiumPaymentFrequency());
        assertEquals(currentTime.plusMonths(proposedPaymentFrequency.getValueInMonths()), contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenValidData_whenInsuringVehicle_thenSingleVehicleContractIsAddedToInsuranceCompany() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertTrue(insuranceCompany.getContracts().contains(singleVehicleContract));
    }

    @Test
    public void givenValidData_whenInsuringVehicle_thenSingleVehicleContractIsAddedToPolicyHolder() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertTrue(policyHolder.getContracts().contains(singleVehicleContract));
    }

    @Test
    public void givenContractWithPaymentTimeIsBeforeCurrentTime_whenChargingPremiumOnContract_thenIncreasesOutstandingBalanceByPremiumUntilCurrentTimeIsBeforeNextPaymentTime() {
        int numberOfTimesToPay = 3;

        AbstractContract contract  = new TestContract(
                contractNumber,
                insuranceCompany,
                policyHolder,
                new ContractPaymentData(
                        proposedPremium,
                        proposedPaymentFrequency,
                        currentTime.minusMonths((numberOfTimesToPay - 1) * proposedPaymentFrequency.getValueInMonths()),
                        100
                ),
                100
        );

        ContractPaymentData contractPaymentData = contract.getContractPaymentData();
        int outstandingBalance = contractPaymentData.getOutstandingBalance();

        insuranceCompany.chargePremiumOnContract(contract);

        assertEquals(outstandingBalance + numberOfTimesToPay * proposedPremium , contractPaymentData.getOutstandingBalance());
        assertEquals(currentTime.plusMonths(proposedPaymentFrequency.getValueInMonths()), contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenContractWithPaymentTimeIsEqualToCurrentTime_whenChargingPremiumOnContract_thenIncreasesOutstandingBalanceByPremiumUntilCurrentTimeIsBeforeNextPaymentTime() {
        AbstractContract contract  = new TestContract(
                contractNumber,
                insuranceCompany,
                policyHolder,
                new ContractPaymentData(
                        proposedPremium,
                        proposedPaymentFrequency,
                        currentTime,
                        100
                ),
                100
        );

        ContractPaymentData contractPaymentData = contract.getContractPaymentData();
        int outstandingBalance = contractPaymentData.getOutstandingBalance();

        insuranceCompany.chargePremiumOnContract(contract);

        assertEquals(outstandingBalance + proposedPremium, contractPaymentData.getOutstandingBalance());
        assertEquals(currentTime.plusMonths(proposedPaymentFrequency.getValueInMonths()), contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenYearlyPremiumIsLowerThanFiveFoldOfInsuredPeople_whenInsuringPersons_thenThrowIllegalArgumentException() {
        proposedPremium = 5;
        proposedPaymentFrequency = PremiumPaymentFrequency.ANNUAL;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insurePersons(
           contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        ));
    }

    @Test
    public void givenProposedPaymentFrequencyIsNull_whenInsuringPersons_thenThrowIllegalArgumentException() {
        proposedPaymentFrequency = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        ));
    }

    @Test
    public void givenPersonsToInsureIsNull_whenInsuringPersons_thenThrowIllegalArgumentException() {
        personsToInsure = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        ));
    }

    @Test
    public void givenInsuranceCompanyHasContractWithContractNumber_whenInsuringPersonsWithTheSameContractNumber_thenThrowsIllegalArgumentException() {
        insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        ));
    }

    @Test
    public void givenValidData_whenInsuringPersons_thenCoverageAmountIsTenfoldTheNumberOfInsuredPersons() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );
        assertEquals(personsToInsure.size() * 10, travelContract.getCoverageAmount());
    }

    @Test
    public void givenValidData_whenInsuringPersons_thenContractPaymentDataIsValid() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );

        ContractPaymentData contractPaymentData = travelContract.getContractPaymentData();

        assertEquals(proposedPremium, contractPaymentData.getPremium());
        assertEquals(proposedPremium, contractPaymentData.getOutstandingBalance());
        assertEquals(proposedPaymentFrequency, contractPaymentData.getPremiumPaymentFrequency());
        assertEquals(currentTime.plusMonths(proposedPaymentFrequency.getValueInMonths()), contractPaymentData.getNextPaymentTime());
    }

    @Test
    public void givenValidData_whenInsuringPersons_thenReturnsValidTravelContract() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );

        assertEquals(insuranceCompany, travelContract.getInsurer());
        assertEquals(contractNumber, travelContract.getContractNumber());
        assertEquals(policyHolder, travelContract.getPolicyHolder());
        assertNotNull(travelContract.getContractPaymentData());
        assertEquals(personsToInsure, travelContract.getInsuredPersons());
        assertNotEquals(0, travelContract.getCoverageAmount());
    }

    @Test
    public void givenValidData_whenInsuringPersons_thenTravelContractIsAddedToInsuranceCompany() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );

        assertTrue(insuranceCompany.getContracts().contains(travelContract));
    }

    @Test
    public void givenValidData_whenInsuringPersons_thenTravelContractIsAddedToPolicyHolder() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );

        assertTrue(policyHolder.getContracts().contains(travelContract));
    }

    @Test
    public void givenContractNumberIsNull_whenCreatingMasterVehicleContract_thenThrowsIllegalArgumentException() {
        contractNumber = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.createMasterVehicleContract(
                contractNumber, beneficiary, policyHolder
        ));
    }

    @Test
    public void givenInsuranceCompanyHasContractWithContractNumber_whenCreatingMasterVehicleContractWithTheSameContractNumber_thenThrowsIllegalArgumentException() {
        insuranceCompany.insureVehicle(
                contractNumber, beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.createMasterVehicleContract(
                contractNumber, beneficiary, policyHolder
        ));
    }

    @Test
    public void givenValidData_whenCreatingMasterVehicleContract_thenMasterVehicleContractIsAddedToInsuranceCompany() {
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(contractNumber, beneficiary, policyHolder);

        assertTrue(insuranceCompany.getContracts().contains(masterVehicleContract));
    }

    @Test
    public void givenValidData_whenCreatingMasterVehicleContract_thenMasterVehicleContractIsAddedToPolicyHolder() {
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(contractNumber, beneficiary, policyHolder);

        assertTrue(policyHolder.getContracts().contains(masterVehicleContract));
    }

    @Test
    public void givenSingleVehicleContractIsNull_whenMovingSingleVehicleContractToMasterVehicleContract_thenThrowsIllegalArgumentException() {
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(
                masterVehicleContract, null
        ));
    }

    @Test
    public void givenMasterVehicleContractIsNull_whenMovingSingleVehicleContractToMasterVehicleContract_thenThrowsIllegalArgumentException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(
                null, singleVehicleContract
        ));
    }

    @Test
    public void givenMasterVehicleContractIsInactive_whenMovingSingleVehicleContractToMasterVehicleContract_thenThrowsInvalidContractException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );
        masterVehicleContract.setInactive();

        assertThrows(InvalidContractException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(
                masterVehicleContract, singleVehicleContract
        ));
    }

    @Test
    public void givenSingleVehicleContractIsInactive_whenMovingSingleVehicleContractToMasterVehicleContract_thenThrowsInvalidContractException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );
        singleVehicleContract.setInactive();

        assertThrows(InvalidContractException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(
                masterVehicleContract, singleVehicleContract
        ));
    }

    @Test
    public void givenMasterVehicleContractBelongsToDifferentInsuranceCompany_whenMovingSingleVehicleContractToMasterVehicleContract_thenThrowsInvalidContractException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany2.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );

        assertThrows(InvalidContractException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(
                masterVehicleContract, singleVehicleContract
        ));
    }

    @Test
    public void givenSingleVehicleContractBelongsToDifferentInsuranceCompany_whenMovingSingleVehicleContractToMasterVehicleContract_thenThrowsInvalidContractException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany2.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );

        assertThrows(InvalidContractException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(
                masterVehicleContract, singleVehicleContract
        ));
    }

    @Test
    public void givenSingleVehicleContractAndMasterVehicleContractHaveDifferentPolicyHolder_whenMovingSingleVehicleContractToMasterVehicleContract_thenThrowsInvalidContractException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder2, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );

        assertThrows(InvalidContractException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(
                masterVehicleContract, singleVehicleContract
        ));
    }

    @Test
    public void givenValidContract_whenMovingSingleVehicleContractToMasterVehicleContract_thenSingleVehicleContractIsRemovedFromInsuranceContracts(){
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );

        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterVehicleContract, singleVehicleContract);

        assertFalse(insuranceCompany.getContracts().contains(singleVehicleContract));
    }

    @Test
    public void givenValidContract_whenMovingSingleVehicleContractToMasterVehicleContract_thenSingleVehicleContractIsRemovedFromPolicyHolder(){
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );

        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterVehicleContract, singleVehicleContract);

        assertFalse(policyHolder.getContracts().contains(singleVehicleContract));
    }

    @Test
    public void givenValidContract_whenMovingSingleVehicleContractToMasterVehicleContract_thenSingleVehicleContractIsAddedToMasterVehicleContract(){
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        MasterVehicleContract masterVehicleContract = insuranceCompany.createMasterVehicleContract(
                "g222t32w", beneficiary, policyHolder
        );

        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(masterVehicleContract, singleVehicleContract);

        assertTrue(masterVehicleContract.getChildContracts().contains(singleVehicleContract));
    }

    @Test
    public void givenSingleVehicleContractIsNull_whenProcessingClaim_thenThrowsIllegalArgumentException() {
        SingleVehicleContract singleVehicleContract = null;
        int expectedDamages = 100;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(singleVehicleContract, expectedDamages));
    }

    @Test
    public void givenExpectedDamagesIsZero_whenProcessingClaim_thenThrowsIllegalArgumentException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        int expectedDamages = 0;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(singleVehicleContract, expectedDamages));
    }

    @Test
    public void givenExpectedDamagesIsNegative_whenProcessingClaim_thenThrowsIllegalArgumentException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        int expectedDamages = -100;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(singleVehicleContract, expectedDamages));
    }

    @Test
    public void givenSingleVehicleContractIsInactive_whenProcessingClaim_thenThrowsInvalidContractException() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        int expectedDamages = 100;
        singleVehicleContract.setInactive();

        assertThrows(InvalidContractException.class, () -> insuranceCompany.processClaim(singleVehicleContract, expectedDamages));
    }

    @Test
    public void givenSingleVehicleContractHasBeneficiary_whenProcessingClaim_thenBeneficiaryIsPayedOutTheCoverageAmount() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        int expectedDamages = 100;

        insuranceCompany.processClaim(singleVehicleContract, expectedDamages);

        assertEquals(singleVehicleContract.getCoverageAmount(), singleVehicleContract.getBeneficiary().getPaidOutAmount());
    }

    @Test
    public void givenSingleVehicleContractDoesNotHaveBeneficiary_whenProcessingClaim_thenPolicyHolderIsPayedOutTheCoverageAmount() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", null, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        int expectedDamages = 100;

        insuranceCompany.processClaim(singleVehicleContract, expectedDamages);

        assertEquals(singleVehicleContract.getCoverageAmount(), singleVehicleContract.getPolicyHolder().getPaidOutAmount());
    }

    @Test
    public void givenExpectedDamagesIsEqualTo70PercentOfVehiclesOriginalValue_whenProcessingClaim_thenSetsSingleVehicleContractToInactive() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        int expectedDamages = vehicleToInsure.getOriginalValue() * 70 / 100;

        insuranceCompany.processClaim(singleVehicleContract, expectedDamages);

        assertFalse(singleVehicleContract.isActive());
    }

    @Test
    public void givenExpectedDamagesIsGreaterThan70PercentOfVehiclesOriginalValue_whenProcessingClaim_thenSetsSingleVehicleContractToInactive() {
        SingleVehicleContract singleVehicleContract = insuranceCompany.insureVehicle(
                "g23313", beneficiary, policyHolder, proposedPremium, proposedPaymentFrequency, vehicleToInsure
        );
        int expectedDamages = vehicleToInsure.getOriginalValue() * 70 / 100 + 150;

        insuranceCompany.processClaim(singleVehicleContract, expectedDamages);

        assertFalse(singleVehicleContract.isActive());
    }

    @Test
    public void givenTravelContractIsNull_whenProcessingClaim_thenThrowsIllegalArgumentException() {
        TravelContract travelContract = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(travelContract, personsToInsure));
    }

    @Test
    public void givenPersonsToInsureIsNull_whenProcessingClaim_thenThrowsIllegalArgumentException() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );
        personsToInsure = null;

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(travelContract, personsToInsure));
    }

    @Test
    public void givenPersonsToInsureIsEmpty_whenProcessingClaim_thenThrowsIllegalArgumentException() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );
        personsToInsure = new HashSet<>();

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(travelContract, personsToInsure));
    }

    @Test
    public void givenAffectedPersonsIsNotSubsetOfInsuredPersons_whenProcessingClaim_thenThrowsIllegalArgumentException() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );
        var affectedPersons = new HashSet<Person>();
        affectedPersons.add(new Person("1001011231"));
        affectedPersons.add(new Person("7201011235"));
        affectedPersons.add(new Person("0402114911"));
        affectedPersons.add(new Person("9402163573"));

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(travelContract, affectedPersons));
    }

    @Test
    public void givenTravelContractIsInactive_whenProcessingClaim_thenThrowsInvalidContractException() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );
        var affectedPersons = personsToInsure;
        travelContract.setInactive();

        assertThrows(InvalidContractException.class, () -> insuranceCompany.processClaim(travelContract, affectedPersons));
    }

    @Test
    public void givenValidData_whenProcessingClaim_thenCoverageAmountIsDistributedToAffectedPersons() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );
        var affectedPersons = personsToInsure;

        insuranceCompany.processClaim(travelContract, affectedPersons);

        for (var person : affectedPersons)
            assertEquals(travelContract.getCoverageAmount() / affectedPersons.size(), person.getPaidOutAmount());
    }

    @Test
    public void givenValidData_whenProcessingClaim_thenTravelContractIsSetToInactive() {
        TravelContract travelContract = insuranceCompany.insurePersons(
                contractNumber, policyHolder, proposedPremium, proposedPaymentFrequency, personsToInsure
        );
        var affectedPersons = personsToInsure;

        insuranceCompany.processClaim(travelContract, affectedPersons);

        assertFalse(travelContract.isActive());
    }
}