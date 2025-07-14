import company.InsuranceCompany;
import contracts.SingleVehicleContract;
import contracts.TravelContract;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tento súbor obsahuje jednoduché testy pre triedu InsuranceCompany.
 * Každý test má komentáre, ktoré vysvetľujú, čo robí a čo testuje.
 */
public class InsuranceCompanyEasyTests {

    private InsuranceCompany insuranceCompany; // Objekt poisťovne
    private Person person;                     // Fyzická osoba (poistník)
    private Vehicle vehicle;                   // Vozidlo na poistenie

    @BeforeEach
    void setUp() {
        // Nastavenie pred každým testom
        insuranceCompany = new InsuranceCompany(LocalDateTime.of(2023, 11, 1, 10, 0)); // Vytvorí poisťovňu s dátumom 1.11.2023 10:00
        person = new Person("9010101243");       // Vytvorí osobu s rodným číslom
        vehicle = new Vehicle("XYZ6789", 10000);  // Vytvorí vozidlo s EČV a hodnotou 10 000
    }

    @Test
    public void testCreateVehicleContract() {
        // Test vytvorenia poistnej zmluvy na vozidlo
        String contractNumber = "V001";                     // Číslo zmluvy
        int premium = 200;                                  // Výška poistného
        PremiumPaymentFrequency frequency = PremiumPaymentFrequency.ANNUAL; // Ročná platba

        // Vytvorí zmluvu na vozidlo cez metódu insureVehicle
        SingleVehicleContract contract = insuranceCompany.insureVehicle(
                contractNumber, null, person, premium, frequency, vehicle);

        // Overí, či zmluva nie je null (či sa vytvorila)
        assertNotNull(contract);
        // Overí, či zmluva je v zozname zmlúv poisťovne
        assertTrue(insuranceCompany.getContracts().contains(contract));
        // Overí, či zmluva je v zozname zmlúv osoby
        assertTrue(person.getContracts().contains(contract));
        // Overí, či číslo zmluvy je správne
        assertEquals(contractNumber, contract.getContractNumber());
        // Overí, či poisťovateľ je správny
        assertEquals(insuranceCompany, contract.getInsurer());
        // Overí, či poistník je správny
        assertEquals(person, contract.getPolicyHolder());
        // Overí, či poistené vozidlo je správne
        assertEquals(vehicle, contract.getInsuredVehicle());
        // Overí, či poistné je správne nastavené
        assertEquals(premium, contract.getContractPaymentData().getPremium());
    }

    @Test
    public void testCreateTravelContract() {
        // Test vytvorenia cestovnej poistnej zmluvy
        String contractNumber = "T001";                     // Číslo zmluvy
        int premium = 50;                                   // Výška poistného
        PremiumPaymentFrequency frequency = PremiumPaymentFrequency.MONTHLY; // Mesačná platba
        Set<Person> insuredPersons = Collections.singleton(person); // Zoznam poistených osôb (iba jedna osoba)

        // Vytvorí cestovnú zmluvu cez metódu insurePersons
        TravelContract contract = insuranceCompany.insurePersons(
                contractNumber, person, premium, frequency, insuredPersons);

        // Overí, či zmluva nie je null (či sa vytvorila)
        assertNotNull(contract);
        // Overí, či zmluva je v zozname zmlúv poisťovne
        assertTrue(insuranceCompany.getContracts().contains(contract));
        // Overí, či zmluva je v zozname zmlúv osoby
        assertTrue(person.getContracts().contains(contract));
        // Overí, či číslo zmluvy je správne
        assertEquals(contractNumber, contract.getContractNumber());
        // Overí, či poistník je správny
        assertEquals(person, contract.getPolicyHolder());
        // Overí, či poistené osoby sú správne
        assertEquals(insuredPersons, contract.getInsuredPersons());
        // Overí, či poistné je správne nastavené
        assertEquals(premium, contract.getContractPaymentData().getPremium());
    }

    @Test
    public void testPaymentProcessing() {
        // Test spracovania platby pre zmluvu na vozidlo
        String contractNumber = "V002";                     // Číslo zmluvy
        int premium = 300;                                  // Výška poistného
        PremiumPaymentFrequency frequency = PremiumPaymentFrequency.QUARTERLY; // Štvrťročná platba

        // Vytvorí zmluvu na vozidlo
        SingleVehicleContract contract = insuranceCompany.insureVehicle(
                contractNumber, null, person, premium, frequency, vehicle);

        // Nastaví nedoplatok na 150
        contract.getContractPaymentData().setOutstandingBalance(150);
        // Zaplatí 100
        contract.pay(100);

        // Overí, či nedoplatok klesol na 50 (150 - 100)
        assertEquals(50, contract.getContractPaymentData().getOutstandingBalance());
        // Overí, či história platieb obsahuje jednu platbu
        assertEquals(1, insuranceCompany.getHandler().getPaymentHistory().get(contract).size());
    }

//    @Test
//    public void testClaimProcessing() {
//        // Test spracovania poistnej udalosti
//        String contractNumber = "V003";                     // Číslo zmluvy
//        int premium = 400;                                  // Výška poistného
//        PremiumPaymentFrequency frequency = PremiumPaymentFrequency.SEMI_ANNUAL; // Polročná platba
//
//        // Vytvorí zmluvu s beneficiary (príjemcom plnenia)
//        SingleVehicleContract contract = insuranceCompany.insureVehicle(
//                contractNumber, person, person, premium, frequency, vehicle);
//
//        // Nastaví krytie na 8000
//        contract.setCoverageAmount(8000);
//        // Spracuje poistnú udalosť so škodou 9000 (viac ako 70% hodnoty vozidla = totálna škoda)
//        insuranceCompany.processClaim(contract, 9000);
//
//        // Overí, či zmluva bola deaktivovaná
//        assertFalse(contract.isActive());
//        // Overí, či príjemca dostal plnenie 8000
//        assertEquals(8000, person.getPaidOutAmount());
//    }

    @Test
    public void testInvalidContractNumber() {
        // Test vytvorenia zmluvy s neplatným číslom
        String invalidContractNumber = "";                  // Prázdne číslo zmluvy
        int premium = 100;                                  // Výška poistného
        PremiumPaymentFrequency frequency = PremiumPaymentFrequency.ANNUAL; // Ročná platba

        // Overí, či vytvorenie zmluvy s prázdnym číslom vyhodí výnimku
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle(
                invalidContractNumber, null, person, premium, frequency, vehicle));
        // Overí, či žiadna zmluva nebola pridaná
        assertTrue(insuranceCompany.getContracts().isEmpty());
    }
}