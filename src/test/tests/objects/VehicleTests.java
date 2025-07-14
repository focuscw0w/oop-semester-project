package tests.objects;

import objects.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleTests {
    @Test
    public void givenValidLicencePlateAndOriginalValue_whenCreatingVehicle_thenPropertiesAreSet() {
        String licencePlate = "BA123CD";
        int originalValue = 1000;

        var vehicle = new Vehicle(licencePlate, originalValue);

        assertEquals(licencePlate, vehicle.getLicensePlate());
        assertEquals(originalValue, vehicle.getOriginalValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "va12445", " fa31fe", "FAWIOFNFWO"})
    public void givenInvalidLicencePlate_whenCreatingVehicle_thenThrowsIllegalArgumentException(String licencePlate) {
        int originalValue = 1000;

        assertThrows(IllegalArgumentException.class, () -> new Vehicle(licencePlate, originalValue));
    }

    @Test
    public void givenLicencePlateIsNull_whenCreatingVehicle_thenThrowsIllegalArgumentException() {
        String licencePlate = null;
        int originalValue = 1000;

        assertThrows(IllegalArgumentException.class, () -> new Vehicle(licencePlate, originalValue));
    }

    @Test
    public void givenOriginalValueIsZero_whenCreatingVehicle_thenThrowsIllegalArgumentException() {
        String licencePlate = "BA123CD";
        int originalValue = 0;

        assertThrows(IllegalArgumentException.class, () -> new Vehicle(licencePlate, originalValue));
    }

    @Test
    public void givenOriginalValueIsNegative_whenCreatingVehicle_thenThrowsIllegalArgumentException() {
        String licencePlate = "BA123CD";
        int originalValue = -1000;

        assertThrows(IllegalArgumentException.class, () -> new Vehicle(licencePlate, originalValue));
    }

    @Test
    public void givenTwoDifferentVehicles_whenCheckingEquality_thenTheyAreNotEqual() {
        var vehicle1 = new Vehicle("BA123CD", 1000);
        var vehicle2 = new Vehicle("BA125CD", 1000);
        var vehicle3 = new Vehicle("BA123CD", 1010);

        assertNotEquals(vehicle1, vehicle2);
        assertNotEquals(vehicle1, vehicle3);
    }
}