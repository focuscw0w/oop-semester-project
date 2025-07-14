package objects;

import objects.Vehicle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleTest {

    @Test
    void testValidVehicleCreation() {
        // Valid vehicle with uppercase letters and numbers
        Vehicle vehicle1 = new Vehicle("ABC1234", 5000);
        assertEquals("ABC1234", vehicle1.getLicensePlate());
        assertEquals(5000, vehicle1.getOriginalValue());

        // Valid vehicle with only letters
        Vehicle vehicle2 = new Vehicle("ABCDEFG", 10000);
        assertEquals("ABCDEFG", vehicle2.getLicensePlate());
        assertEquals(10000, vehicle2.getOriginalValue());

        // Valid vehicle with only numbers
        Vehicle vehicle3 = new Vehicle("1234567", 7500);
        assertEquals("1234567", vehicle3.getLicensePlate());
        assertEquals(7500, vehicle3.getOriginalValue());

        // Valid vehicle with only numbers
        Vehicle vehicle4 = new Vehicle("PATAMAT", 3333);
        assertEquals("PATAMAT", vehicle4.getLicensePlate());
        assertEquals(3333, vehicle4.getOriginalValue());
    }

    @Test
    void testVehicleCreationWithInvalidLicensePlate() {
        // Null license plate
        assertThrows(IllegalArgumentException.class, () -> new Vehicle(null, 5000));

        // Empty license plate
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("", 5000));

        // License plate too short
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC123", 5000));

        // License plate too long
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC12345", 5000));

        // License plate with lowercase letters
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("abc1234", 5000));

        // License plate with special characters
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC-123", 5000));
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC 123", 5000));
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC@123", 5000));
    }

    @Test
    void testVehicleCreationWithInvalidValue() {
        // Zero value
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC1234", 0));

        // Negative value
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC1234", -1000));
        assertThrows(IllegalArgumentException.class, () -> new Vehicle("ABC1234", -1));
    }

/*
    @Test
    void testVehicleEquality() {
        Vehicle vehicle1 = new Vehicle("ABC1234", 5000);
        Vehicle vehicle2 = new Vehicle("ABC1234", 6000);
        Vehicle vehicle3 = new Vehicle("XYZ1234", 5000);

        // Vehicles with same license plate should be equal regardless of value
        assertEquals(vehicle1, vehicle2);

        // Vehicles with different license plates should not be equal
        assertNotEquals(vehicle1, vehicle3);

        // Equal vehicles should have equal hash codes
        assertEquals(vehicle1.hashCode(), vehicle2.hashCode());

        // Different vehicles should have different hash codes
        assertNotEquals(vehicle1.hashCode(), vehicle3.hashCode());
    }

     */

    @Test
    void testVehicleImmutability() {
        Vehicle vehicle = new Vehicle("ABC1234", 5000);

        // Store initial values
        String initialLicensePlate = vehicle.getLicensePlate();
        int initialValue = vehicle.getOriginalValue();

        // Verify initial values
        assertEquals("ABC1234", initialLicensePlate);
        assertEquals(5000, initialValue);

        // Verify that multiple getter calls return the same values
        for (int i = 0; i < 5; i++) {
            assertEquals(initialLicensePlate, vehicle.getLicensePlate());
            assertEquals(initialValue, vehicle.getOriginalValue());
        }

        // Verify reference equality of returned license plate
        // (This tests that we're consistently getting the same string object back)
        assertSame(initialLicensePlate, vehicle.getLicensePlate());
    }

    @Test
    void testBoundaryLicensePlates() {
        // Test with various combinations of valid characters at boundary positions
        Vehicle vehicle1 = new Vehicle("A000000", 1000);
        Vehicle vehicle2 = new Vehicle("0000000", 1000);
        Vehicle vehicle3 = new Vehicle("0000001", 1000);
        Vehicle vehicle4 = new Vehicle("ZZZZZZZ", 1000);
        Vehicle vehicle5 = new Vehicle("Z999999", 1000);

        assertEquals("A000000", vehicle1.getLicensePlate());
        assertEquals("0000000", vehicle2.getLicensePlate());
        assertEquals("0000001", vehicle3.getLicensePlate());
        assertEquals("ZZZZZZZ", vehicle4.getLicensePlate());
        assertEquals("Z999999", vehicle5.getLicensePlate());
    }

    @Test
    void testBoundaryValues() {
        // Test with boundary values for originalValue
        Vehicle vehicle1 = new Vehicle("ABC1234", 1); // Minimum valid value
        Vehicle vehicle2 = new Vehicle("ABC1234", Integer.MAX_VALUE); // Maximum possible value

        assertEquals(1, vehicle1.getOriginalValue());
        assertEquals(Integer.MAX_VALUE, vehicle2.getOriginalValue());
    }

    @Test
    void testDetailedInvalidOriginalValues() {
        // Test various invalid originalValue scenarios

        // Zero value
        Exception zeroException = assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("ABC1234", 0));

        // Various negative values
        Exception smallNegative = assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("ABC1234", -1));
        Exception largeNegative = assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("ABC1234", -10000));
        Exception extremeNegative = assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("ABC1234", Integer.MIN_VALUE));

        // We can optionally verify the exception messages if they contain specific text
        // This would require updating the Vehicle class to include descriptive messages
        // assertNotNull(zeroException.getMessage());
        // assertTrue(zeroException.getMessage().contains("positive"));
    }
}