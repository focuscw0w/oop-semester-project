package objects;

public class Vehicle {
    private final String licensePlate;
    private final int originalValue;

    public Vehicle(String licensePlate, int originalValue) {
        if (licensePlate == null || licensePlate.length() != 7) {
            throw new IllegalArgumentException("Invalid license plate");
        }

        // Či všetky jeho znaky nie sú veľké písmená A-Z alebo číslice
        if (!licensePlate.matches("[A-Z0-9]+")) {
            throw new IllegalArgumentException("Invalid license plate");
        }

        // Či nie je originalValue pozitívny
        if (originalValue <= 0) {
            throw new IllegalArgumentException("Original value cannot be negative");
        }

        this.licensePlate = licensePlate;
        this.originalValue = originalValue;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public int getOriginalValue() {
        return originalValue;
    }
}
