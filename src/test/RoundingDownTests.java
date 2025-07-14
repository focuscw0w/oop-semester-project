import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import company.*;
import objects.*;
import contracts.*;
import payment.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests to verify the rounding down behavior in monetary calculations as specified:
 * - If the sum is 4, half of the sum is 2
 * - If the sum is 5, half of the sum is 2
 * - If the sum is 5, 70% of the sum is 3
 */
public class RoundingDownTests {

    @Test
    void testRoundingDownInVehicleInsurance() {
        // Setup test environment
        InsuranceCompany company = new InsuranceCompany(LocalDateTime.now());
        Person legalPerson = new Person("12345678"); // IČO for a legal person
        Person naturalPerson = new Person("9552017915"); // RČ for a natural person

        // Test case 1: Vehicle with value 400, coverage should be 200 (half)
        Vehicle vehicle1 = new Vehicle("ABC1234", 400);
        SingleVehicleContract contract1 = company.insureVehicle(
                "C001", naturalPerson, legalPerson, 10,
                PremiumPaymentFrequency.ANNUAL, vehicle1
        );

        assertEquals(200, contract1.getCoverageAmount(),
                "Coverage amount should be half of vehicle value (400), rounded down if needed");

        // Test case 2: Vehicle with value 500, coverage should be 250 (half)
        Vehicle vehicle2 = new Vehicle("DEF5678", 500);
        SingleVehicleContract contract2 = company.insureVehicle(
                "C002", naturalPerson, legalPerson, 10,
                PremiumPaymentFrequency.ANNUAL, vehicle2
        );

        assertEquals(250, contract2.getCoverageAmount(),
                "Coverage amount should be half of vehicle value (500)");

        // Test case 3: Vehicle with odd value 501, coverage should be 250 (half, rounded down)
        Vehicle vehicle3 = new Vehicle("GHI9012", 501);
        SingleVehicleContract contract3 = company.insureVehicle(
                "C003", naturalPerson, legalPerson, 11,
                PremiumPaymentFrequency.ANNUAL, vehicle3
        );

        assertEquals(250, contract3.getCoverageAmount(),
                "Coverage amount should be half of vehicle value (501), rounded down");
    }

    @Test
    void testRoundingDownInClaimProcessing() {
        // Setup test environment
        InsuranceCompany company = new InsuranceCompany(LocalDateTime.now());
        Person legalPerson = new Person("12345678"); // IČO for a legal person
        Person naturalPerson = new Person("1003045219"); // RČ for a natural person

        // Test case 1: Process claim with 70% of 500 = 350
        Vehicle vehicle = new Vehicle("JKL3456", 500);
        SingleVehicleContract contract = company.insureVehicle(
                "C004", naturalPerson, legalPerson, 10,
                PremiumPaymentFrequency.ANNUAL, vehicle
        );

        // Process claim with damage equal to 70% of vehicle value
        company.processClaim(contract, 350);

        // The contract should be marked as inactive due to total damage
        assertFalse(contract.isActive(),
                "Contract should be inactive when damage is >= 70% of vehicle value");

        // Test case 2: Process claim with 70% of 5 = 3.5, rounded down to 3
        Vehicle smallVehicle = new Vehicle("MNO7890", 5);
        SingleVehicleContract smallContract = company.insureVehicle(
                "C005", naturalPerson, legalPerson, 1,
                PremiumPaymentFrequency.ANNUAL, smallVehicle
        );

        // Process claim with damage that's less than 70% of vehicle value
        company.processClaim(smallContract, 2); // 2 is less than 70% of 5 (3)

        // The contract should remain active
        assertTrue(smallContract.isActive(),
                "Contract should remain active when damage is < 70% of vehicle value");
    }

    @Test
    void testRoundingDownInTravelInsurance() {
        // Setup test environment
        InsuranceCompany company = new InsuranceCompany(LocalDateTime.now());
        Person naturalPerson1 = new Person("9502264475"); // RČ for a natural person
        Person naturalPerson2 = new Person("1102064381"); // RČ for another natural person
        Person naturalPerson3 = new Person("1153233917"); // RČ for a third natural person

        Set<Person> insuredPersons = new HashSet<>();
        insuredPersons.add(naturalPerson1);
        insuredPersons.add(naturalPerson2);
        insuredPersons.add(naturalPerson3);

        // Create travel contract with 3 persons and coverage amount of 30 (10*3)
        TravelContract travelContract = company.insurePersons(
                "T001", naturalPerson1, 15,
                PremiumPaymentFrequency.ANNUAL, insuredPersons
        );

        assertEquals(30, travelContract.getCoverageAmount(),
                "Travel contract coverage should be 10 times the number of insured persons");

        // Test claim processing with just 2 affected persons
        Set<Person> affectedPersons = new HashSet<>();
        affectedPersons.add(naturalPerson1);
        affectedPersons.add(naturalPerson2);

        // Process claim - each person should get coverage/affectedPersons.size() = 30/2 = 15
        company.processClaim(travelContract, affectedPersons);

        // Each affected person should receive payment of 15
        assertEquals(15, naturalPerson1.getPaidOutAmount(),
                "First affected person should receive half of the coverage amount");
        assertEquals(15, naturalPerson2.getPaidOutAmount(),
                "Second affected person should receive half of the coverage amount");
        assertEquals(0, naturalPerson3.getPaidOutAmount(),
                "Non-affected person should not receive any payment");

        // Test with odd coverage amount and odd number of persons
        Set<Person> insuredPersons2 = new HashSet<>();
        insuredPersons2.add(naturalPerson1);
        insuredPersons2.add(naturalPerson2);
        insuredPersons2.add(naturalPerson3);

        // Override the coverage amount to 5 for testing purposes
        TravelContract oddCoverageContract = company.insurePersons(
                "T002", naturalPerson1, 15,
                PremiumPaymentFrequency.ANNUAL, insuredPersons2
        );
        oddCoverageContract.setCoverageAmount(5);

        // Process claim with all 3 persons - each should get 5/3 = 1.66... rounded down to 1
        company.processClaim(oddCoverageContract, insuredPersons2);

        // Previous payment was 15, new payment should be 1 more, total 16
        assertEquals(16, naturalPerson1.getPaidOutAmount(),
                "Payment should add 1 (5/3 rounded down) to previous amount");
    }

    @Test
    void testPremiumValidation() {
        // Setup test environment
        InsuranceCompany company = new InsuranceCompany(LocalDateTime.now());
        Person naturalPerson = new Person("7503129766");
        Person legalPerson = new Person("12345678");

        // Test case 1: Vehicle insurance with premium less than 2% of vehicle value
        Vehicle expensiveVehicle = new Vehicle("ABC1234", 10000);
        // Premium 100 with ANNUAL frequency = 100 per year
        // 2% of 10000 = 200, so 100 is insufficient
        assertThrows(IllegalArgumentException.class, () -> {
            company.insureVehicle(
                    "V001", naturalPerson, legalPerson, 100,
                    PremiumPaymentFrequency.ANNUAL, expensiveVehicle
            );
        }, "Should throw exception when premium is less than 2% of vehicle value");

        // Test case 2: Vehicle insurance with premium at boundary (just below 2%)
        Vehicle mediumVehicle = new Vehicle("DEF5678", 1000);
        // 2% of 1000 = 20, so 19.9 (19) is insufficient
        assertThrows(IllegalArgumentException.class, () -> {
            company.insureVehicle(
                    "V002", naturalPerson, legalPerson, 19,
                    PremiumPaymentFrequency.ANNUAL, mediumVehicle
            );
        }, "Should throw exception when premium is just below 2% of vehicle value");

        // Test case 3: Vehicle insurance with quarterly premium that sums to less than 2% annually
        Vehicle anotherVehicle = new Vehicle("GHI9012", 2000);
        // Premium 8 with QUARTERLY frequency = 8*4 = 32 per year
        // 2% of 2000 = 40, so 32 is insufficient
        assertThrows(IllegalArgumentException.class, () -> {
            company.insureVehicle(
                    "V003", naturalPerson, legalPerson, 8,
                    PremiumPaymentFrequency.QUARTERLY, anotherVehicle
            );
        }, "Should throw exception when annual equivalent of quarterly premium is less than 2% of vehicle value");

        // Test case 4: Travel insurance with premium less than 5 times number of persons
        Set<Person> threePersons = new HashSet<>();
        threePersons.add(new Person("7551032412"));
        threePersons.add(new Person("55315755"));
        threePersons.add(new Person("50108575"));
        // 3 persons * 5 = 15 minimum premium required
        assertThrows(IllegalArgumentException.class, () -> {
            company.insurePersons(
                    "T001", naturalPerson, 14,
                    PremiumPaymentFrequency.ANNUAL, threePersons
            );
        }, "Should throw exception when premium is less than 5 times the number of insured persons");

        // Test case 5: Travel insurance with premium at boundary (just below 5*persons)
        Set<Person> twoPersons = new HashSet<>();
        twoPersons.add(new Person("50120741"));
        twoPersons.add(new Person("55215314"));
        // 2 persons * 5 = 10 minimum premium required
        assertThrows(IllegalArgumentException.class, () -> {
            company.insurePersons(
                    "T002", naturalPerson, 9,
                    PremiumPaymentFrequency.ANNUAL, twoPersons
            );
        }, "Should throw exception when premium is just below 5 times the number of insured persons");

        // Test case 6: Travel insurance with quarterly premium that sums to less than 5*persons annually
        Set<Person> fourPersons = new HashSet<>();
        fourPersons.add(new Person("2451125820"));
        fourPersons.add(new Person("2403218587"));
        fourPersons.add(new Person("9502138943"));
        fourPersons.add(new Person("9552062069"));
        // 4 persons * 5 = 20 minimum annual premium required
        // Quarterly premium of 4 = 4*4 = 16 annually, which is less than 20
        assertThrows(IllegalArgumentException.class, () -> {
            company.insurePersons(
                    "T003", naturalPerson, 4,
                    PremiumPaymentFrequency.QUARTERLY, fourPersons
            );
        }, "Should throw exception when annual equivalent of quarterly premium is less than 5 times the number of insured persons");
    }
}