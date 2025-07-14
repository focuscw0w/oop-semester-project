import company.InsuranceCompany;
import contracts.InvalidContractException;
import contracts.MasterVehicleContract;
import contracts.SingleVehicleContract;
import contracts.TravelContract;
import objects.Person;
import objects.Vehicle;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomTest {

    private InsuranceCompany insuranceCompany;
    private InsuranceCompany insuranceCompany2;
    private Person naturalPerson1;
    private Person naturalPerson2;
    private Person legalPerson1;
    private Person legalPerson2;
    private Vehicle vehicle1;
    private Vehicle vehicle2;
    private Vehicle vehicle3;
    private Vehicle vehicle4;
    private LocalDateTime time;

    @BeforeEach
    void setUp() {
        time = LocalDateTime.of(2025, 5, 4, 12, 0);
        insuranceCompany = new InsuranceCompany(time);
        insuranceCompany2 = new InsuranceCompany(time);
        naturalPerson1 = new Person("8351068242");
        naturalPerson2 = new Person("0402114911");
        legalPerson1 = new Person("12345678");
        legalPerson2 = new Person("12345679");
        vehicle1 = new Vehicle("AA111AA", 15_000);
        vehicle2 = new Vehicle("BANAN22", 22_000);
        vehicle3 = new Vehicle("SOMRYBA", 8_000);
        vehicle4 = new Vehicle("ICOOKED", 40_000);
    }

    @Test
    void testSingleVehicleContract() {
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("c1", naturalPerson2, legalPerson1, 1500, PremiumPaymentFrequency.ANNUAL, vehicle1);
        assertNotNull(c1, "Contract should not be null");
        assertEquals("c1", c1.getContractNumber());
        assertEquals(legalPerson1, c1.getPolicyHolder());
        assertEquals(naturalPerson2, c1.getBeneficiary());
        int coverageAmount = vehicle1.getOriginalValue() / 2;
        assertEquals(coverageAmount, c1.getCoverageAmount());
        assertEquals(vehicle1, c1.getInsuredVehicle());
        assertTrue(c1.isActive(), "Contract should be active");
        assertNotNull(c1.getContractPaymentData(), "Payment data should not be null");
        assertEquals(PremiumPaymentFrequency.ANNUAL, c1.getContractPaymentData().getPremiumPaymentFrequency());
        assertEquals(1500, c1.getContractPaymentData().getOutstandingBalance());
        insuranceCompany.setCurrentTime(LocalDateTime.of(2026, 5, 4, 12, 0));
        c1.updateBalance();
        assertEquals(3000, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(LocalDateTime.of(2027, 5, 4, 12, 0), c1.getContractPaymentData().getNextPaymentTime());
        c1.pay(10_000);
        assertEquals(-7000, c1.getContractPaymentData().getOutstandingBalance());
        insuranceCompany.setCurrentTime(LocalDateTime.of(2030, 5, 4, 12, 0));
        c1.updateBalance();
        assertEquals(-1_000, c1.getContractPaymentData().getOutstandingBalance());
        c1.pay(1_000);
        insuranceCompany.processClaim(c1, 15_000);
        assertEquals(vehicle1.getOriginalValue() / 2, naturalPerson2.getPaidOutAmount());
        assertFalse(c1.isActive());
        assertEquals(7500, c1.getBeneficiary().getPaidOutAmount());
        assertEquals(0, c1.getPolicyHolder().getPaidOutAmount());
        c1.setInactive();
        assertThrows(InvalidContractException.class, () -> insuranceCompany.processClaim(c1, 50), "contract must be active");
        assertTrue(true, String.valueOf(insuranceCompany.getHandler().getPaymentHistory().containsKey(c1)));
        Set<PaymentInstance> payments = insuranceCompany.getHandler().getPaymentHistory().get(c1);
        assertEquals(2, payments.size());
        Iterator<PaymentInstance> iterator = payments.iterator();
        PaymentInstance first = iterator.next();
        PaymentInstance second = iterator.next();
        assertTrue(first.getPaymentTime().isBefore(second.getPaymentTime()));
        assertEquals(10_000, first.getPaymentAmount());
        assertEquals(1_000, second.getPaymentAmount());
    }

    @Test
    void testMasterVehicleContract() {
        MasterVehicleContract m1 = insuranceCompany.createMasterVehicleContract("m1", null, legalPerson1);
        MasterVehicleContract m2 = insuranceCompany2.createMasterVehicleContract("m2", null, legalPerson2);
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("c1", naturalPerson2, legalPerson1, 1000, PremiumPaymentFrequency.ANNUAL, vehicle2); //22_000 / 2 = 11_000
        SingleVehicleContract c2 = insuranceCompany2.insureVehicle("c2", naturalPerson1, legalPerson1, 2000, PremiumPaymentFrequency.MONTHLY, vehicle3); //8_000 / 2 = 4_000
        SingleVehicleContract c3 = insuranceCompany.insureVehicle("c3", null, legalPerson1, 3000, PremiumPaymentFrequency.SEMI_ANNUAL, vehicle4); //40_000 / 2 = 20_000
        assertThrows(InvalidContractException.class, () -> insuranceCompany2.moveSingleVehicleContractToMasterVehicleContract(m2, c1));
        assertThrows(InvalidContractException.class, () -> m1.requestAdditionOfChildContract(c2));
        assertTrue(legalPerson1.getContracts().contains(c1));
        assertTrue(legalPerson1.getContracts().contains(c3));
        assertTrue(insuranceCompany.getContracts().contains(c1));
        assertTrue(insuranceCompany.getContracts().contains(c3));
        m1.requestAdditionOfChildContract(c1);
        m1.requestAdditionOfChildContract(c3);
        assertFalse(insuranceCompany.getContracts().contains(c1));
        assertFalse(insuranceCompany.getContracts().contains(c3));
        insuranceCompany.setCurrentTime(LocalDateTime.of(2026, 6, 4, 12, 0));
        assertNull(m1.getContractPaymentData());
        assertEquals(0, m2.getCoverageAmount());
        //m1.updateBalance();
        insuranceCompany.chargePremiumsOnContracts(); //update outstanding balance of all contracts in insuranceCompany
        assertEquals(2_000, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(9_000, c3.getContractPaymentData().getOutstandingBalance());
        m1.pay(15_000);
        assertEquals(-1_000, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(-3_000, c3.getContractPaymentData().getOutstandingBalance());
        //c1.setInactive(); //try if singleVehicle is inactive
        insuranceCompany.setCurrentTime(LocalDateTime.of(2026, 12, 4, 12, 0));
        insuranceCompany.chargePremiumsOnContracts();
        m1.pay(1_000);
        assertEquals(-2_000, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(0, c3.getContractPaymentData().getOutstandingBalance());
        insuranceCompany.processClaim(c3, 35_000);
        assertFalse(c3.isActive());
        insuranceCompany.setCurrentTime(LocalDateTime.of(2027, 12, 4, 12, 0));
        insuranceCompany.chargePremiumsOnContracts();
        assertEquals(-1_000, c1.getContractPaymentData().getOutstandingBalance());
        m1.pay(2_000);
        assertEquals(-3_000, c1.getContractPaymentData().getOutstandingBalance());
        Set<PaymentInstance> paymentsContract = insuranceCompany.getHandler().getPaymentHistory().get(m1);
        assertEquals(3, paymentsContract.size());
        Iterator<PaymentInstance> iterator3M = paymentsContract.iterator();
        PaymentInstance first = iterator3M.next();
        PaymentInstance second = iterator3M.next();
        PaymentInstance third = iterator3M.next();
        assertTrue(first.getPaymentTime().isBefore(second.getPaymentTime()));
        assertTrue(second.getPaymentTime().isBefore(third.getPaymentTime()));
        assertEquals(15_000, first.getPaymentAmount());
        assertEquals(1_000, second.getPaymentAmount());
        assertEquals(2_000, third.getPaymentAmount());
    }

    @Test
    void testMasterVehicleContract2() {
        insuranceCompany.setCurrentTime(LocalDateTime.of(2024, 6, 4, 12, 0));
        MasterVehicleContract m1 = insuranceCompany.createMasterVehicleContract("m1", null, legalPerson1);
        MasterVehicleContract m2 = insuranceCompany2.createMasterVehicleContract("m2", null, legalPerson2);
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("c1", naturalPerson2, legalPerson1, 1000, PremiumPaymentFrequency.ANNUAL, vehicle2); // 22_000 / 2 = 11_000
        SingleVehicleContract c2 = insuranceCompany2.insureVehicle("c2", naturalPerson1, legalPerson1, 2000, PremiumPaymentFrequency.MONTHLY, vehicle3); // 8_000 / 2 = 4_000
        SingleVehicleContract c3 = insuranceCompany.insureVehicle("c3", naturalPerson2, legalPerson1, 3000, PremiumPaymentFrequency.SEMI_ANNUAL, vehicle4); // 40_000 / 2 = 20_000
        assertThrows(InvalidContractException.class, () -> insuranceCompany2.moveSingleVehicleContractToMasterVehicleContract(m2, c1));
        assertThrows(InvalidContractException.class, () -> m1.requestAdditionOfChildContract(c2));
        assertTrue(legalPerson1.getContracts().contains(c1));
        assertTrue(legalPerson1.getContracts().contains(c3));
        assertTrue(insuranceCompany.getContracts().contains(c1));
        assertTrue(insuranceCompany.getContracts().contains(c3));
        c1.pay(300);
        c3.pay(500);
        Set<PaymentInstance> paymentsContract1 = insuranceCompany.getHandler().getPaymentHistory().get(c1);
        Set<PaymentInstance> paymentsContract3 = insuranceCompany.getHandler().getPaymentHistory().get(c3);
        Iterator<PaymentInstance> iterator1S = paymentsContract1.iterator();
        Iterator<PaymentInstance> iterator3S = paymentsContract3.iterator();
        PaymentInstance first1 = iterator1S.next();
        PaymentInstance first3 = iterator3S.next();
        assertEquals(700, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(2_500, c3.getContractPaymentData().getOutstandingBalance());
        assertEquals(300, first1.getPaymentAmount());
        assertEquals(500, first3.getPaymentAmount());
        insuranceCompany.setCurrentTime(LocalDateTime.of(2025, 6, 4, 12, 0));
        insuranceCompany.chargePremiumsOnContracts();
        assertEquals(1700, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(8_500, c3.getContractPaymentData().getOutstandingBalance());
        c1.pay(600);
        c3.pay(1000);
        paymentsContract1 = insuranceCompany.getHandler().getPaymentHistory().get(c1);
        paymentsContract3 = insuranceCompany.getHandler().getPaymentHistory().get(c3);
        iterator1S = paymentsContract1.iterator();
        iterator3S = paymentsContract3.iterator();
        PaymentInstance _skipFirst1 = iterator1S.next();
        PaymentInstance _skipFirst3 = iterator3S.next();
        PaymentInstance second1 = iterator1S.next();
        PaymentInstance second3 = iterator3S.next();
        assertTrue(first1.getPaymentTime().isBefore(second1.getPaymentTime()));
        assertTrue(first3.getPaymentTime().isBefore(second3.getPaymentTime()));
        assertEquals(300, first1.getPaymentAmount());
        assertEquals(600, second1.getPaymentAmount());
        assertEquals(500, first3.getPaymentAmount());
        assertEquals(1_000, second3.getPaymentAmount());
        insuranceCompany.processClaim(c1, 16_000);
        insuranceCompany.processClaim(c3, 20_000);
        assertEquals(31_000, c1.getBeneficiary().getPaidOutAmount());
        assertFalse(c1.isActive());
    }

    @Test
    void testTravelContract() {
        Set<Person> validInsured = new LinkedHashSet<>();
        Person naturalPerson3 = new Person("2457218291");
        validInsured.add(naturalPerson1);
        validInsured.add(naturalPerson2);
        validInsured.add(naturalPerson3);

        Set<Person> invalidInsured = new LinkedHashSet<>();
        invalidInsured.add(naturalPerson2);
        invalidInsured.add(legalPerson1); // legal form not allowed

        // === Valid case ===
        int monthlyPremium = 5; // 5 * 12 = 60 >= 5 * 3 = 15
        TravelContract t1 = insuranceCompany.insurePersons(
                "t1",
                naturalPerson1,
                monthlyPremium,
                PremiumPaymentFrequency.MONTHLY,
                validInsured
        );

        assertNotNull(t1);
        assertEquals("t1", t1.getContractNumber());
        assertEquals(3, t1.getInsuredPersons().size());
        assertEquals(10 * 3, t1.getCoverageAmount());
        assertEquals(monthlyPremium, t1.getContractPaymentData().getPremium());
        assertEquals(PremiumPaymentFrequency.MONTHLY, t1.getContractPaymentData().getPremiumPaymentFrequency());

        // === Duplicate contract number ===
        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.insurePersons(
                    "t1", // already used
                    naturalPerson1,
                    5,
                    PremiumPaymentFrequency.MONTHLY,
                    validInsured
            );
        });

        // === Not enough annual premium ===
        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.insurePersons(
                    "t2",
                    naturalPerson1,
                    1, // annual = 12 >= 15
                    PremiumPaymentFrequency.MONTHLY,
                    validInsured
            );
        });

        // === Non-NATURAL person in insured set ===
        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.insurePersons(
                    "t3",
                    naturalPerson2,
                    100,
                    PremiumPaymentFrequency.MONTHLY,
                    invalidInsured
            );
        });

        // === Null parameter ===
        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.insurePersons(
                    "t4",
                    null,
                    100,
                    PremiumPaymentFrequency.MONTHLY,
                    validInsured
            );
        });
    }

    @Test
    void testProcessClaim() {
        // Setup
        Set<Person> personsToInsure = new LinkedHashSet<>();
        Person p1 = new Person("371231660");
        Person p2 = new Person("6812217291");
        personsToInsure.add(p1);
        personsToInsure.add(p2);

        TravelContract t = insuranceCompany.insurePersons(
                "tc1",
                p1,
                10,
                PremiumPaymentFrequency.MONTHLY,
                personsToInsure
        );

        assertDoesNotThrow(() -> insuranceCompany.processClaim(t, t.getInsuredPersons()));
        assertFalse(t.isActive()); // Contract should now be inactive

        // === Null contract ===
        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.processClaim(null, t.getInsuredPersons());
        });

        // === Null affectedPersons ===
        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.processClaim(t, null);
        });

        // === Empty affectedPersons ===
        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.processClaim(t, new HashSet<>());
        });

        // === affectedPersons not subset ===
        Set<Person> invalidAffected = new HashSet<>();
        invalidAffected.add(new Person("7206165450")); // Not insured

        assertThrows(IllegalArgumentException.class, () -> {
            insuranceCompany.processClaim(t, invalidAffected);
        });

        // === Contract already inactive ===
        assertThrows(InvalidContractException.class, () -> {
            insuranceCompany.processClaim(t, t.getInsuredPersons());
        });
    }
}