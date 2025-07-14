import company.InsuranceCompany;
import contracts.SingleVehicleContract;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RequiredTestsFilip {

    InsuranceCompany insuranceCompany;
    Person policyHolder;
    Vehicle vehicle;
    ContractPaymentData paymentData;

    @BeforeEach
    void setUp() {
        // Vytvorenie poisťovne s aktuálnym časom
        insuranceCompany = new InsuranceCompany(LocalDateTime.of(2025, 4, 15, 12, 0));

        // Vytvorenie osoby a vozidla
        policyHolder = new Person("0351068080");
        vehicle = new Vehicle("AB123CD", 10_000);

        // Nastavenie údajov o platbe poistky
        paymentData = new ContractPaymentData(
                200,                               // výška poistky (premium)
                PremiumPaymentFrequency.ANNUAL,    // ročná frekvencia platby
                insuranceCompany.getCurrentTime(), // dátum prvej platby
                0                                   // zatiaľ žiadna dlžoba
        );
    }

    @Test
    void testSingleVehicleContractCreation() {
        // Vytvorenie novej poistnej zmluvy
        SingleVehicleContract contract = new SingleVehicleContract(
                "contract1",           // číslo zmluvy
                insuranceCompany,      // poisťovňa
                null,                  // beneficient (nevyplnený = null)
                policyHolder,          // držiteľ zmluvy
                paymentData,           // platobné údaje
                5000,                  // poistné krytie
                vehicle                // poistené vozidlo
        );

        // Overenie správneho uloženia údajov
        assertEquals("contract1", contract.getContractNumber());
        assertEquals(insuranceCompany, contract.getInsurer());
        assertEquals(policyHolder, contract.getPolicyHolder());
        assertEquals(5000, contract.getCoverageAmount());
        assertTrue(contract.isActive());
        assertEquals(vehicle, contract.getInsuredVehicle());
        assertEquals(paymentData, contract.getContractPaymentData());
    }

    @Test
    void testInsuranceCompanyInsureVehicle() {
        // Vytvorenie zmluvy cez metódu poisťovne
        SingleVehicleContract contract = insuranceCompany.insureVehicle(
                "contract2",           // číslo zmluvy
                null,                  // beneficient (nevyplnený)
                policyHolder,          // držiteľ zmluvy
                300,                   // poistka (premium)
                PremiumPaymentFrequency.QUARTERLY, // frekvencia platby
                vehicle                // poistené vozidlo
        );

        // Overenie, že zmluva bola správne pridaná poisťovňou a osobou
        assertTrue(insuranceCompany.getContracts().contains(contract));
        assertTrue(policyHolder.getContracts().contains(contract));

        // Overenie nastavení zmluvy
        assertEquals(300, contract.getContractPaymentData().getPremium());
        assertEquals(PremiumPaymentFrequency.QUARTERLY, contract.getContractPaymentData().getPremiumPaymentFrequency());
        assertEquals(insuranceCompany.getCurrentTime().plusMonths(3), contract.getContractPaymentData().getNextPaymentTime());
    }

    @Test
    void testInvalidSingleVehicleContract() {
        // Overenie, že neplatné vstupy vyhodia výnimky
        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract(null, insuranceCompany, null, policyHolder, paymentData, 1000, vehicle);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract("c", null, null, policyHolder, paymentData, 1000, vehicle);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract("c", insuranceCompany, null, null, paymentData, 1000, vehicle);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract("c", insuranceCompany, null, policyHolder, null, 1000, vehicle);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract("c", insuranceCompany, null, policyHolder, paymentData, 1000, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new SingleVehicleContract("c", insuranceCompany, policyHolder, policyHolder, paymentData, 1000, vehicle);
        });
    }
}
