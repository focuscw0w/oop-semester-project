package tests.payment;

import company.InsuranceCompany;
import contracts.AbstractContract;
import contracts.InvalidContractException;
import contracts.MasterVehicleContract;
import contracts.SingleVehicleContract;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PaymentHandler;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;
import tests.shared.TestContract;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentHandlerTests {
    private InsuranceCompany insurer;

    private AbstractContract contract;

    private AbstractContract contract2;

    private MasterVehicleContract masterVehicleContract;

    private MasterVehicleContract masterVehicleContract2;

    private int amount;

    private PaymentHandler paymentHandler;

    private Person beneficiary;

    private Person policyHolder;

    @BeforeEach
    public void setUp() {
        insurer = new InsuranceCompany(LocalDateTime.now());

        contract = TestContract.Create("141244", insurer);
        contract2 = TestContract.Create("141511", insurer);

        policyHolder = new Person("124526");
        beneficiary = new Person("413554");

        var childContract1 = new SingleVehicleContract(
                "5141235",
                insurer,
                beneficiary,
                policyHolder,
                new ContractPaymentData(
                        30,
                        PremiumPaymentFrequency.MONTHLY,
                        LocalDateTime.now(),
                        30
                ),
                100,
                new Vehicle("BAF134G", 1000)
        );
        var childContract2 = new SingleVehicleContract(
                "513253",
                insurer,
                beneficiary,
                policyHolder,
                new ContractPaymentData(
                        50,
                        PremiumPaymentFrequency.MONTHLY,
                        LocalDateTime.now(),
                        50
                ),
                100,
                new Vehicle("BAF234G", 1000)
        );
        var childContract3 = new SingleVehicleContract(
                "5326543",
                insurer,
                beneficiary,
                policyHolder,
                new ContractPaymentData(
                        75,
                        PremiumPaymentFrequency.MONTHLY,
                        LocalDateTime.now(),
                        100
                ),
                100,
                new Vehicle("BAF334G", 1000)
        );
        var childContract4 = new SingleVehicleContract(
                "153626",
                insurer,
                beneficiary,
                policyHolder,
                new ContractPaymentData(
                        20,
                        PremiumPaymentFrequency.MONTHLY,
                        LocalDateTime.now(),
                        0
                ),
                100,
                new Vehicle("BAF434G", 1000)
        );
        childContract4.setInactive();

        var childContract5 = new SingleVehicleContract(
                "864547",
                insurer,
                beneficiary,
                policyHolder,
                new ContractPaymentData(
                        10,
                        PremiumPaymentFrequency.MONTHLY,
                        LocalDateTime.now(),
                        30
                ),
                100,
                new Vehicle("BAF534G", 1000)
        );
        var childContract6 = new SingleVehicleContract(
                "534267",
                insurer,
                beneficiary,
                policyHolder,
                new ContractPaymentData(
                        15,
                        PremiumPaymentFrequency.MONTHLY,
                        LocalDateTime.now(),
                        30
                ),
                100,
                new Vehicle("BAF634G", 1000)
        );

        masterVehicleContract = new MasterVehicleContract("523443", insurer, beneficiary, policyHolder);
        masterVehicleContract2 = new MasterVehicleContract("143323", insurer, beneficiary, policyHolder);

        masterVehicleContract.getChildContracts().add(childContract1);
        masterVehicleContract.getChildContracts().add(childContract2);
        masterVehicleContract.getChildContracts().add(childContract3);
        masterVehicleContract.getChildContracts().add(childContract4);

        masterVehicleContract2.getChildContracts().add(childContract5);
        masterVehicleContract2.getChildContracts().add(childContract6);

        amount = 100;

        paymentHandler = new PaymentHandler(insurer);
    }

    @Test
    public void givenInsuranceCompanyIsNull_whenCreatingPaymentHandler_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PaymentHandler(null));
    }

    @Test
    public void givenValidInsurer_whenCreatingPaymentHandler_thenPropertiesAreSet() {
        assertNotNull(paymentHandler.getPaymentHistory());
    }

    @Test
    public void givenContractIsNull_whenPaying_thenThrowsIllegalArgumentException() {
        contract = null;

        assertThrows(IllegalArgumentException.class, () -> paymentHandler.pay(contract, amount));
    }

    @Test
    public void givenContractMasterVehicleContractIsNull_whenPaying_thenThrowsIllegalArgumentException() {
        masterVehicleContract = null;

        assertThrows(IllegalArgumentException.class, () -> paymentHandler.pay(masterVehicleContract, amount));
    }

    @Test
    public void givenAmountIsZero_whenPaying_thenThrowsIllegalArgumentException() {
        amount = 0;

        assertThrows(IllegalArgumentException.class, () -> paymentHandler.pay(contract, amount));
    }

    @Test
    public void givenAmountIsZero_whenPayingMasterVehicleContract_thenThrowsIllegalArgumentException() {
        amount = 0;

        assertThrows(IllegalArgumentException.class, () -> paymentHandler.pay(masterVehicleContract, amount));
    }

    @Test
    public void givenAmountIsNegative_whenPaying_thenThrowsIllegalArgumentException() {
        amount = -100;

        assertThrows(IllegalArgumentException.class, () -> paymentHandler.pay(contract, amount));
    }

    @Test
    public void givenAmountIsNegative_whenPayingMasterVehicleContract_thenThrowsIllegalArgumentException() {
        amount = -100;

        assertThrows(IllegalArgumentException.class, () -> paymentHandler.pay(masterVehicleContract, amount));
    }

    @Test
    public void givenContractIsInactive_whenPaying_thenThrowsInvalidContractException() {
        contract.setInactive();

        assertThrows(InvalidContractException.class, () -> paymentHandler.pay(contract, amount));
    }

    @Test
    public void givenContractIsInactive_whenPayingMasterVehicleContract_thenThrowsInvalidContractException() {
        masterVehicleContract.setInactive();

        assertThrows(InvalidContractException.class, () -> paymentHandler.pay(masterVehicleContract, amount));
    }

    @Test
    public void givenContractBelongsToDifferentInsurer_whenPaying_thenThrowsInvalidContractException() {
        contract = TestContract.Create("142145", new InsuranceCompany(LocalDateTime.now()));

        assertThrows(InvalidContractException.class, () -> paymentHandler.pay(contract, amount));
    }

    @Test
    public void givenContractBelongsToDifferentInsurer_whenPayingMasterVehicleContract_thenThrowsInvalidContractException() {
        masterVehicleContract = new MasterVehicleContract("142145", new InsuranceCompany(LocalDateTime.now()), beneficiary, policyHolder);

        assertThrows(InvalidContractException.class, () -> paymentHandler.pay(masterVehicleContract, amount));
    }

    @Test
    public void givenValidContractAndAmount_whenPaying_thenOutstandingBalanceOfContractIsLoweredByAmount() {
        int currentOutstandingBalance = contract.getContractPaymentData().getOutstandingBalance();

        paymentHandler.pay(contract, amount);

        assertEquals(currentOutstandingBalance - amount, contract.getContractPaymentData().getOutstandingBalance());
    }

    @Test
    public void givenValidContractAndAmount_whenPayingMasterVehicleContract_thenOutstandingBalanceOfChildContractsIsLoweredByAmount() {
        var expectedOutstandingBalance = new HashMap<String, Integer>();
        expectedOutstandingBalance.put("5141235", -60);
        expectedOutstandingBalance.put("513253", -85);
        expectedOutstandingBalance.put("5326543", -75);
        expectedOutstandingBalance.put("153626", 0);

        amount = 400;

        paymentHandler.pay(masterVehicleContract, amount);

        for (var childContract : masterVehicleContract.getChildContracts()) {
            assertEquals(expectedOutstandingBalance.get(childContract.getContractNumber()),
                    childContract.getContractPaymentData().getOutstandingBalance());
        }
    }

    @Test
    public void givenMasterVehicleContractWithNoChildContracts_whenPaying_thenThrowsInvalidContractException() {
        masterVehicleContract = new MasterVehicleContract("15tg324r", insurer, beneficiary, policyHolder);

        assertThrows(InvalidContractException.class, () -> paymentHandler.pay(masterVehicleContract, amount));
    }

    @Test
    public void givenValidContractAndAmount_whenPaying_thenThereIsPaymentInstanceForContractWithGivenAmount() {
        paymentHandler.pay(contract, amount);

        Set<PaymentInstance> paymentInstances = paymentHandler.getPaymentHistory().get(contract);

        assertTrue(paymentInstances.stream()
                .anyMatch((instance) -> instance.getPaymentAmount() == amount)
        );
    }

    @Test
    public void givenValidContractAndAmount_whenPayingMasterVehicleContract_thenThereIsPaymentInstanceForContractWithGivenAmount() {
        paymentHandler.pay(masterVehicleContract, amount);

        Set<PaymentInstance> paymentInstances = paymentHandler.getPaymentHistory().get(masterVehicleContract);

        assertTrue(paymentInstances.stream()
                .anyMatch((instance) -> instance.getPaymentAmount() == amount)
        );
    }

    @Test
    public void givenValidContractAndAmount_whenPaying_thenThereIsPaymentInstanceForContractWithPaymentTimeOfInsurer() {
        paymentHandler.pay(contract, amount);

        Set<PaymentInstance> paymentInstances = paymentHandler.getPaymentHistory().get(contract);

        assertTrue(paymentInstances.stream()
                .anyMatch((instance) -> instance.getPaymentTime() == insurer.getCurrentTime())
        );
    }

    @Test
    public void givenValidContractAndAmount_whenPayingMasterVehicleContract_thenThereIsPaymentInstanceForContractWithPaymentTimeOfInsurer() {
        paymentHandler.pay(masterVehicleContract, amount);

        Set<PaymentInstance> paymentInstances = paymentHandler.getPaymentHistory().get(masterVehicleContract);

        assertTrue(paymentInstances.stream()
                .anyMatch((instance) -> instance.getPaymentTime() == insurer.getCurrentTime())
        );
    }

    @Test
    public void givenTwoContracts_whenPayingOneAfterAnother_thenBothOfThemAreInPaymentHistoryOfContract() {
        paymentHandler.pay(contract, amount);
        paymentHandler.pay(contract2, amount);

        Set<AbstractContract> contracts = paymentHandler.getPaymentHistory().keySet();

        assertEquals(2, contracts.size());
    }

    @Test
    public void givenTwoContracts_whenPayingMasterVehicleContractOneAfterAnother_thenBothOfThemAreInPaymentHistoryOfContract() {
        paymentHandler.pay(masterVehicleContract, amount);
        paymentHandler.pay(masterVehicleContract2, amount);

        Set<AbstractContract> contracts = paymentHandler.getPaymentHistory().keySet();

        assertEquals(2, contracts.size());
    }

    @Test
    public void givenContract_whenPayingTwice_thenThereAreTwoPaymentsInTheHistoryOfTheContract() {
        paymentHandler.pay(contract, amount);
        insurer.setCurrentTime(insurer.getCurrentTime().plusDays(1));
        paymentHandler.pay(contract, amount);

        Set<PaymentInstance> paymentInstances = paymentHandler.getPaymentHistory().get(contract);

        assertEquals(2, paymentInstances.size());
    }

    @Test
    public void givenContract_whenPayingMasterVehicleContractTwice_thenThereAreTwoPaymentsInTheHistoryOfTheContract() {
        paymentHandler.pay(masterVehicleContract, amount);
        insurer.setCurrentTime(insurer.getCurrentTime().plusDays(1));
        paymentHandler.pay(masterVehicleContract, amount);

        Set<PaymentInstance> paymentInstances = paymentHandler.getPaymentHistory().get(masterVehicleContract);

        assertEquals(2, paymentInstances.size());
    }
}