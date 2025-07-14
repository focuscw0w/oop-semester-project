import company.InsuranceCompany;
import contracts.*;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.PremiumPaymentFrequency;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BonusTests {
    InsuranceCompany insurer;
    Vehicle vehicle1;
    Vehicle vehicle2;
    Person person1;
    Person person2;
    Person person3;

    @BeforeEach
    void setUp() {
        insurer = new InsuranceCompany(LocalDateTime.now());
        person1 = new Person("0405144707");
        person2 = new Person("111111111");
        person3 = new Person("000000");
        vehicle1 = new Vehicle("LC068BD", 10_000);
        vehicle2 = new Vehicle("BA069BJ", 50_000);
    }

    @Test
    public void TestinsurePersons() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Proposed premium is <= 0
            insurer.insurePersons("1", person1, 0, PremiumPaymentFrequency.ANNUAL, Set.of(person2));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Proposed Payment Frequency is null
            insurer.insurePersons("1", person1, 1, null, Set.of(person2));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Persons to insure is null
            insurer.insurePersons("1", person1, 1, PremiumPaymentFrequency.ANNUAL, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Persons to insure is empty
            insurer.insurePersons("1", person1, 1, PremiumPaymentFrequency.ANNUAL, Set.of());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Yearly premium is low
            insurer.insurePersons("1", person1, 1, PremiumPaymentFrequency.ANNUAL, Set.of(person2));
        });
        TravelContract c1 = insurer.insurePersons("1", person1, 6, PremiumPaymentFrequency.ANNUAL, Set.of(person2));
        assertThrows(IllegalArgumentException.class, () -> {
            // Contract number is used
            insurer.insurePersons("1", person1, 7, PremiumPaymentFrequency.ANNUAL, Set.of(person2));
        });
        assertTrue(insurer.getContracts().contains(c1));
        assertTrue(person1.getContracts().contains(c1));
    }

    @Test
    public void TestSetCurrentTime() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Time cannot be null
            insurer.setCurrentTime(null);
        });
        LocalDateTime now = LocalDateTime.now();
        now = now.plusMonths(5);
        insurer.setCurrentTime(now);
        assertEquals(now, insurer.getCurrentTime());
    }

    @Test
    public void TestMasterVehicleContract() {
        MasterVehicleContract c1 = insurer.createMasterVehicleContract("1", person1, person3);
        assertThrows(IllegalArgumentException.class, () -> {
            // Unique contract numbers
            insurer.createMasterVehicleContract("1", person2, person3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Policy holder must be a legal entity
            insurer.createMasterVehicleContract("2", person2, person1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Policy holder cannot be null
            insurer.createMasterVehicleContract("2", person1, null);
        });
        assertTrue(insurer.getContracts().contains(c1));
        assertTrue(person3.getContracts().contains(c1));

        SingleVehicleContract c2 = insurer.insureVehicle("3", person1, person2, 200, PremiumPaymentFrequency.ANNUAL, vehicle1);
        SingleVehicleContract c3 = insurer.insureVehicle("4", person1, person3, 200, PremiumPaymentFrequency.ANNUAL, vehicle1);
        c2.setInactive();
        MasterVehicleContract temp_c = insurer.createMasterVehicleContract("5", person1, person3);
        temp_c.setInactive();
        assertThrows(IllegalArgumentException.class, () -> {
            // MVC is null
            insurer.moveSingleVehicleContractToMasterVehicleContract(null, c3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // SVC is null
            insurer.moveSingleVehicleContractToMasterVehicleContract(c1, null);
        });
        assertThrows(InvalidContractException.class, () -> {
            // SVC is inactive
            insurer.moveSingleVehicleContractToMasterVehicleContract(c1, c2);
        });
        assertThrows(InvalidContractException.class, () -> {
            // MVC is inactive
            insurer.moveSingleVehicleContractToMasterVehicleContract(temp_c, c2);
        });
        InsuranceCompany temp_insurer = new InsuranceCompany(LocalDateTime.now());
        MasterVehicleContract temp_c1 = temp_insurer.createMasterVehicleContract("1", person1, person3);
        SingleVehicleContract temp_c2 = temp_insurer.insureVehicle("4", person1, person3, 200, PremiumPaymentFrequency.ANNUAL, vehicle1);
        assertThrows(InvalidContractException.class, () -> {
            // insurer != insurer
            insurer.moveSingleVehicleContractToMasterVehicleContract(temp_c1, c2);
        });
        assertThrows(InvalidContractException.class, () -> {
            // insurer != insurer
            insurer.moveSingleVehicleContractToMasterVehicleContract(c1, temp_c2);
        });
        assertThrows(InvalidContractException.class, () -> {
            // insurer != insurer
            insurer.moveSingleVehicleContractToMasterVehicleContract(c1, c2);
        });
        insurer.moveSingleVehicleContractToMasterVehicleContract(c1, c3);
        assertTrue(c1.getChildContracts().contains(c3));
        assertFalse(insurer.getContracts().contains(c3));
        assertFalse(person3.getContracts().contains(c3));
    }

    @Test
    public void TestChargePremiumOnContracts() {
        TravelContract c1 = insurer.insurePersons("1", person1, 5, PremiumPaymentFrequency.ANNUAL, Set.of(person2));
        SingleVehicleContract c2 = insurer.insureVehicle("2", person1, person2, 200, PremiumPaymentFrequency.ANNUAL, vehicle1);
        insurer.setCurrentTime(LocalDateTime.now().plusMonths(12));

        insurer.chargePremiumsOnContracts();

        assertEquals(10, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(400, c2.getContractPaymentData().getOutstandingBalance());
    }

    @Test
    public void TestProcessClaim() {
        // Single vehicle contract
        SingleVehicleContract c1 = insurer.insureVehicle("1", person1, person2, 200, PremiumPaymentFrequency.ANNUAL, vehicle1);
        assertThrows(IllegalArgumentException.class, () -> {
            // Contract is null
            insurer.processClaim(null, 5000);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Expected damages must be > 0
            insurer.processClaim(c1, 0);
        });
        SingleVehicleContract temp_c1 = insurer.insureVehicle("2", person1, person2, 200, PremiumPaymentFrequency.ANNUAL, vehicle1);
        temp_c1.setInactive();
        assertThrows(InvalidContractException.class, () -> {
            // Contract is inactive
            insurer.processClaim(temp_c1, 5000);
        });
        SingleVehicleContract c2 = insurer.insureVehicle("3", null, person3, 200, PremiumPaymentFrequency.ANNUAL, vehicle1);
        insurer.processClaim(c1, 5000);
        insurer.processClaim(c2, 10000);
        assertTrue(c1.isActive());
        assertFalse(c2.isActive());
        assertEquals(5000, person1.getPaidOutAmount());
        assertEquals(5000, person3.getPaidOutAmount());
        assertEquals(0, person2.getPaidOutAmount());

        // Travel contract
        TravelContract t1 = insurer.insurePersons("01", person1, 10, PremiumPaymentFrequency.ANNUAL, Set.of(person2, person1));
        Person person4 = new Person("111111123");
        assertThrows(IllegalArgumentException.class, () -> {
            // Contract is null
            insurer.processClaim(null, Set.of(person2));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // Affected persons is not a subset
            insurer.processClaim(t1, Set.of(person2, person4));
        });
        TravelContract temp_t1 = insurer.insurePersons("02", person1, 5, PremiumPaymentFrequency.ANNUAL, Set.of(person2));
        temp_t1.setInactive();
        assertThrows(InvalidContractException.class, () -> {
            // Contract is inactive
            insurer.processClaim(temp_t1, Set.of(person2));
        });
        insurer.processClaim(t1, Set.of(person2));
        assertFalse(t1.isActive());
        assertEquals(20, person2.getPaidOutAmount());
        assertEquals(5000, person1.getPaidOutAmount());
    }
}
