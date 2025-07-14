package tests.objects;

import contracts.AbstractContract;
import objects.LegalForm;
import objects.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tests.shared.TestContract;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PersonTests {
    @Test
    public void givenIdIsNull_whenCheckingBirthNumberValidity_thenReturnsFalse() {
        Assertions.assertFalse(Person.isValidBirthNumber(null));
    }

    @Test
    public void givenIdIsNull_whenCheckingRegistrationNumberValidity_thenReturnsFalse() {
        assertFalse(Person.isValidRegistrationNumber(null));
    }

    @Test
    public void givenIdIsNull_whenCreatingPerson_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Person(null));
    }

    @Test
    public void givenIdIsEmpty_whenCreatingPerson_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Person(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "1234567", "142a24", "11111111111", "535001421", "7201011236", "9933335566"})
    public void givenInvalidId_whenCreatingPerson_thenThrowsIllegalArgumentException(String invalidID) {
        assertTrue(!Person.isValidBirthNumber(invalidID) && !Person.isValidRegistrationNumber(invalidID));

        assertThrows(IllegalArgumentException.class, () -> new Person(invalidID));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "12345678", "7201011235", "535101421"})
    public void givenValidId_whenCreatingPerson_thenIdIsSet(String id) {
        Person person = new Person(id);

        assertEquals(id, person.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"525101234", "535101421", "1001011231", "7201011235"})
    public void givenValidBirthNumber_whenCreatingPerson_thenLegalFormIsNatural(String birthNumber) {
        assertTrue(Person.isValidBirthNumber(birthNumber));

        Person person = new Person(birthNumber);

        Assertions.assertEquals(LegalForm.NATURAL, person.getLegalForm());
    }

    @ParameterizedTest
    @ValueSource(strings = {"155241", "52241243"})
    public void givenValidRegistrationNumber_whenCreatingPerson_thenLegalFormIsLegal(String registrationNumber) {
        assertTrue(Person.isValidRegistrationNumber(registrationNumber));

        Person person = new Person(registrationNumber);

        assertEquals(LegalForm.LEGAL, person.getLegalForm());
    }

    @Test
    public void givenNewPerson_whenRetrievingPaidOutAmount_thenItEqualsToZero() {
        Person person = new Person("12345678");

        assertEquals(0, person.getPaidOutAmount());
    }

    @Test
    public void givenNewPerson_whenRetrievingContracts_thenItIsEmpty() {
        Person person = new Person("12345678");

        assertTrue(person.getContracts().isEmpty());
    }

    @Test
    public void givenNegativeInteger_whenPayingOut_thenThrowsIllegalArgumentException() {
        Person person = new Person("12345678");

        assertThrows(IllegalArgumentException.class, () -> person.payout(-100));
    }

    @Test
    public void givenZero_whenPayingOut_thenThrowsIllegalArgumentException() {
        Person person = new Person("12345678");

        assertThrows(IllegalArgumentException.class, () -> person.payout(0));
    }

    @Test
    public void givenPositiveInteger_whenPayingOut_thenPaidOutAmountIsIncreasedBygivenArgument() {
        Person person = new Person("12345678");

        assertEquals(0, person.getPaidOutAmount());

        person.payout(100);

        assertEquals(100, person.getPaidOutAmount());

        person.payout(150);

        assertEquals(250, person.getPaidOutAmount());
    }

    @Test
    public void givenContractIsNull_whenAddingContract_thenThrowsIllegalArgumentException() {
        Person person = new Person("12345678");

        assertThrows(IllegalArgumentException.class, () -> person.addContract(null));
    }

    @Test
    public void givenContracts_whenAddingContracts_thenOrderOfContractsIsPreserved() {
        Person person = new Person("12345678");
        TestContract contract1 = TestContract.Create("123");
        TestContract contract2 = TestContract.Create("234");
        TestContract contract3 = TestContract.Create("345");

        person.addContract(contract1);
        person.addContract(contract3);
        person.addContract(contract2);

        Set<AbstractContract> contracts = person.getContracts();

        int i = 0;
        for (AbstractContract contract : contracts) {
            i++;
            if (i == 1) assertEquals(contract1, contract);
            if (i == 2) assertEquals(contract3, contract);
            if (i == 3) assertEquals(contract2, contract);
        }

        assertEquals(3, i);
    }

    @Test
    public void givenTwoPersonHaveTheSameId_whenCheckingEquality_thenTheyEqual() {
        var person1 = new Person("123456");
        var person2 = new Person("123456");

        assertEquals(person1, person2);
    }
}