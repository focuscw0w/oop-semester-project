package objects;

import contracts.AbstractContract;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

public class Person {
    private final String id;
    private final LegalForm legalForm;
    private int paidOutAmount;
    private final Set<AbstractContract> contracts;

    public Person(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }

        // Či je validné RČ
        if (isValidBirthNumber(id)) {
            this.id = id;
            this.legalForm = LegalForm.NATURAL;
        // Či je validné IČO
        } else if (isValidRegistrationNumber(id)) {
            this.id = id;
            this.legalForm = LegalForm.LEGAL;
        } else throw new IllegalArgumentException("ID must be either a valid birth number or registration number.");

        this.paidOutAmount = 0;
        this.contracts = new LinkedHashSet<>();
    }

    private static int getFullYear(int year, int length) {
        if (length == 9) {
            // Pre 9-miestne RČ - osoby narodené do roku 1953
            return 1900 + year;
        } else {
            // Pre 10-miestne RČ - osoby narodené od roku 1954
            if (year < 54) {
                return 2000 + year;
            } else {
                return 1900 + year;
            }
        }
    }

    private static boolean isValidChecksum(String birthNumber) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(birthNumber.charAt(i));
            if (i % 2 == 0) {
                sum += digit;
            } else {
                sum -= digit;
            }
        }
        return sum % 11 == 0;
    }

    public static boolean isValidBirthNumber(String birthNumber) {
        // Či je rodné číslo "null", a či nie sú všetky znaky číslice
        if (birthNumber == null || !birthNumber.matches("[0-9]+")) {
            return false;
        }

        // Či nemá dĺžku 10 alebo 9 znakov
        int length = birthNumber.length();
        if (length != 10 && length != 9) {
            return false;
        }

        int month = Integer.parseInt(birthNumber.substring(2, 4));

        // Či je mesiac v rozsahu 1-12 alebo 51-62
        if (!((month >= 1 && month <= 12) || (month >= 51 && month <= 62))) {
            return false;
        }

        int actualMonth = (month > 50) ? month - 50 : month;
        int year = Integer.parseInt(birthNumber.substring(0, 2));
        int day = Integer.parseInt(birthNumber.substring(4, 6));

        // Ak má RČ 9 znakov, tak rok (RR) musí byť menší alebo rovný 53
        if (length == 9 && year > 53) {
            return false;
        }

        // Kontrolná suma
        if (length == 10 && !isValidChecksum(birthNumber)) {
            return false;
        }

        // Určenie úplného roku
        int fullYear = getFullYear(year, length);

        // Či dátum existuje
        try {
            LocalDate date = LocalDate.of(fullYear, actualMonth, day);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    public static boolean isValidRegistrationNumber(String registrationNumber) {
        // Či IČO nie je null, a či skladá sa zo 6 alebo 8 znakov, ktoré sú číslice
        return registrationNumber != null && registrationNumber.matches("[0-9]+") && (registrationNumber.length() == 6 || registrationNumber.length() == 8);
    }

    public String getId() {
        return id;
    }

    public int getPaidOutAmount() {
        return paidOutAmount;
    }

    public LegalForm getLegalForm() {
        return legalForm;
    }

    public Set<AbstractContract> getContracts() {
        return contracts;
    }

    public void addContract(AbstractContract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Contracts cannot be null.");
        }
        contracts.add(contract);
    }

    public void payout(int paidOutAmount) {
        if (paidOutAmount <= 0) {
            throw new IllegalArgumentException("Paid out amount cannot be negative.");
        }
        this.paidOutAmount += paidOutAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;
        return id.equals(person.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
