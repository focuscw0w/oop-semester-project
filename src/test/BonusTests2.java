import company.InsuranceCompany;
import contracts.*;
import objects.Person;
import objects.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.ContractPaymentData;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.*;

public class BonusTests2 {

    InsuranceCompany insuranceCompany;
    Person naturalPerson1;
    Person naturalPerson2;
    Person legalPerson1;
    Vehicle vehicle1;
    Vehicle vehicle2;
    Vehicle vehicle3;
    Vehicle vehicle4;

    @BeforeEach
    void setUp() {
        insuranceCompany = new InsuranceCompany(LocalDateTime.of(2025, 4, 15, 12, 0));
        naturalPerson1 = new Person("8351068242");
        naturalPerson2 = new Person("0402114911");
        legalPerson1 = new Person("12345678");
        vehicle1 = new Vehicle("AA111AA", 15_000);
        vehicle2 = new Vehicle("BANAN22", 22_000);
        vehicle3 = new Vehicle("SOMRYBA", 8_000);
        vehicle4 = new Vehicle("ICOOKED", 40_000);
    }

    @Test
    public void testContractsPackageStructure() throws NoSuchFieldException, NoSuchMethodException {
        // AbstractContract
        {
            // check the attributes of AbstractContract
            assertTrue(Modifier.isAbstract(AbstractContract.class.getModifiers()));
            String[] fields = {"contractNumber", "insurer", "policyHolder", "contractPaymentData", "coverageAmount", "isActive"};
            IntPredicate[] modifiers = {Modifier::isPrivate, Modifier::isProtected, Modifier::isProtected, Modifier::isProtected, Modifier::isProtected, Modifier::isProtected};
            boolean[] shouldBeFinal = {true, true, true, true, false, false};
            Class<?>[] classes = {String.class, InsuranceCompany.class, Person.class, ContractPaymentData.class, int.class, boolean.class};
            for (int i = 0; i < fields.length; i++) {
                Field f = AbstractContract.class.getDeclaredField(fields[i]);
                assertTrue(modifiers[i].test(f.getModifiers()));
                if (shouldBeFinal[i]) {
                    assertTrue(Modifier.isFinal(f.getModifiers()));
                }
                assertSame(classes[i], f.getType());
            }

            // constructor
            Class<?>[] expectedConstructorParams = {String.class, InsuranceCompany.class, Person.class, ContractPaymentData.class, int.class};
            assertTrue(Arrays.stream(AbstractContract.class.getDeclaredConstructors()).anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

            // methods
            String[] expectedMethodNames = {"getContractNumber", "getPolicyHolder", "getInsurer", "getCoverageAmount", "isActive", "setInactive", "getContractPaymentData", "updateBalance"};
            Class<?>[] returnTypes = {String.class, Person.class, InsuranceCompany.class, int.class, boolean.class, void.class, ContractPaymentData.class, void.class};
            for (int i = 0; i < expectedMethodNames.length; i++) {
                Method m = AbstractContract.class.getDeclaredMethod(expectedMethodNames[i]);
                assertSame(returnTypes[i], m.getReturnType());
                assertTrue(Modifier.isPublic(m.getModifiers()));
            }
            Method payMethod = AbstractContract.class.getDeclaredMethod("pay", int.class);
            assertSame(void.class, payMethod.getReturnType());
            assertTrue(Modifier.isPublic(payMethod.getModifiers()));

            Method coverageMethod = AbstractContract.class.getDeclaredMethod("setCoverageAmount", int.class);
            assertSame(void.class, coverageMethod.getReturnType());
            assertTrue(Modifier.isPublic(coverageMethod.getModifiers()));
        }

        // TravelContract
        {
            assertEquals(AbstractContract.class, TravelContract.class.getSuperclass());
            Field insuredPersons = TravelContract.class.getDeclaredField("insuredPersons");
            assertTrue(Modifier.isPrivate(insuredPersons.getModifiers()));
            assertSame(Set.class, insuredPersons.getType());
            assertSame(Person.class, ((ParameterizedType) insuredPersons.getGenericType()).getActualTypeArguments()[0]);

            // constructor
            Class<?>[] expectedConstructorParams = {String.class, InsuranceCompany.class, Person.class, ContractPaymentData.class, int.class, Set.class};
            assertTrue(Arrays.stream(TravelContract.class.getDeclaredConstructors()).anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

            // methods
            Method getInsuredPersons = TravelContract.class.getDeclaredMethod("getInsuredPersons");
            assertSame(Set.class, getInsuredPersons.getReturnType());
            assertSame(Person.class, ((ParameterizedType) getInsuredPersons.getGenericReturnType()).getActualTypeArguments()[0]);
        }

        // AbstractVehicleContract
        {
            assertTrue(Modifier.isAbstract(AbstractVehicleContract.class.getModifiers()));
            assertEquals(AbstractContract.class, AbstractVehicleContract.class.getSuperclass());
            Field beneficiary = AbstractVehicleContract.class.getDeclaredField("beneficiary");
            assertTrue(Modifier.isProtected(beneficiary.getModifiers()));

            // constructor
            Class<?>[] expectedConstructorParams = {String.class, InsuranceCompany.class, Person.class, Person.class, ContractPaymentData.class, int.class};
            assertTrue(Arrays.stream(AbstractVehicleContract.class.getDeclaredConstructors()).anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

            // methods
            Method m = AbstractVehicleContract.class.getDeclaredMethod("getBeneficiary");
            assertSame(Person.class, m.getReturnType());
            assertTrue(Modifier.isPublic(m.getModifiers()));

            Method setBeneficiaryMethod = AbstractVehicleContract.class.getDeclaredMethod("setBeneficiary", Person.class);
            assertSame(void.class, setBeneficiaryMethod.getReturnType());
            assertTrue(Modifier.isPublic(setBeneficiaryMethod.getModifiers()));
            assertEquals(1, setBeneficiaryMethod.getParameterTypes().length);
            assertEquals(Person.class, setBeneficiaryMethod.getParameterTypes()[0]);
        }

        // MasterVehicleContract
        {
            assertEquals(AbstractVehicleContract.class, MasterVehicleContract.class.getSuperclass());

            // fields
            Field childContracts = MasterVehicleContract.class.getDeclaredField("childContracts");
            assertTrue(Modifier.isPrivate(childContracts.getModifiers()));
            assertSame(Set.class, childContracts.getType());
            assertSame(SingleVehicleContract.class, ((ParameterizedType) childContracts.getGenericType()).getActualTypeArguments()[0]);

            // constructor
            Class<?>[] expectedConstructorParams = {String.class, InsuranceCompany.class, Person.class, Person.class};
            assertTrue(Arrays.stream(MasterVehicleContract.class.getDeclaredConstructors()).anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

            // methods
            Method getChildContracts = MasterVehicleContract.class.getDeclaredMethod("getChildContracts");
            assertSame(Set.class, getChildContracts.getReturnType());
            assertSame(SingleVehicleContract.class, ((ParameterizedType) getChildContracts.getGenericReturnType()).getActualTypeArguments()[0]);
            assertTrue(Modifier.isPublic(getChildContracts.getModifiers()));

            Method removeChildContractsMethod = MasterVehicleContract.class.getDeclaredMethod("requestAdditionOfChildContract", SingleVehicleContract.class);
            assertSame(void.class, removeChildContractsMethod.getReturnType());
            assertTrue(Modifier.isPublic(removeChildContractsMethod.getModifiers()));

            Method isActiveMethod = MasterVehicleContract.class.getDeclaredMethod("isActive");
            assertSame(boolean.class, isActiveMethod.getReturnType());
            assertTrue(Modifier.isPublic(isActiveMethod.getModifiers()));

            Method setInactiveMethod = MasterVehicleContract.class.getDeclaredMethod("setInactive");
            assertSame(void.class, setInactiveMethod.getReturnType());
            assertTrue(Modifier.isPublic(setInactiveMethod.getModifiers()));
        }

        // SingleVehicleContract
        {
            assertEquals(AbstractVehicleContract.class, SingleVehicleContract.class.getSuperclass());

            // fields
            Field insuredVehicle = SingleVehicleContract.class.getDeclaredField("insuredVehicle");
            assertTrue(Modifier.isPrivate(insuredVehicle.getModifiers()));
            assertSame(Vehicle.class, insuredVehicle.getType());

            // constructor
            Class<?>[] expectedConstructorParams = {String.class, InsuranceCompany.class, Person.class, Person.class, ContractPaymentData.class, int.class, Vehicle.class};
            assertTrue(Arrays.stream(SingleVehicleContract.class.getDeclaredConstructors()).anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

            // methods
            Method getInsuredVehicle = SingleVehicleContract.class.getDeclaredMethod("getInsuredVehicle");
            assertSame(Vehicle.class, getInsuredVehicle.getReturnType());
            assertTrue(Modifier.isPublic(getInsuredVehicle.getModifiers()));
        }

        // InvalidContractException
        {
            assertEquals(RuntimeException.class, InvalidContractException.class.getSuperclass());
        }
    }

    @Test
    public void testConstructSingleVehicleContract() {
        ContractPaymentData data = new ContractPaymentData(150, PremiumPaymentFrequency.QUARTERLY, insuranceCompany.getCurrentTime(), 0);
        // test exceptions thrown directly by SingleVehicleContract's constructor
        assertThrows(IllegalArgumentException.class, () -> {
            // vehicle is null
            new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, data, 5000, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // contractPaymentData is null
            new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, null, 5000, vehicle1);
        });
        // test transitive exceptions thrown by parents' constructors
        assertThrows(IllegalArgumentException.class, () -> {
            // policyHolder.equals(beneficiary)
            new SingleVehicleContract(null, insuranceCompany, naturalPerson1, naturalPerson1, data, 5000, vehicle1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // contractNumber is null
            new SingleVehicleContract(null, insuranceCompany, null, naturalPerson1, data, 5000, vehicle1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // contractNumber is empty
            new SingleVehicleContract("", insuranceCompany, null, naturalPerson1, data, 5000, vehicle1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // insurer is null
            new SingleVehicleContract("c1", null, null, naturalPerson1, data, 5000, vehicle1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // policyHolder is null
            new SingleVehicleContract("c1", insuranceCompany, null, null, data, 5000, vehicle1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // coverageAmount < 0
            new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, data, -5000, vehicle1);
        });
        SingleVehicleContract c1 = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1, data, 5000, vehicle1);
        assertEquals("c1", c1.getContractNumber());
        assertEquals(insuranceCompany, c1.getInsurer());
        assertNull(c1.getBeneficiary());
        assertEquals(naturalPerson1, c1.getPolicyHolder());
        assertEquals(data, c1.getContractPaymentData());
        assertEquals(5000, c1.getCoverageAmount());
        assertTrue(c1.isActive());
        SingleVehicleContract c2 = new SingleVehicleContract("c2", insuranceCompany, naturalPerson2, naturalPerson1, data, 5000, vehicle1);
        assertEquals(naturalPerson2, c2.getBeneficiary());
        assertEquals(naturalPerson1, c2.getPolicyHolder());
        assertTrue(c2.isActive());
        SingleVehicleContract c3 = new SingleVehicleContract("c3", insuranceCompany, legalPerson1, naturalPerson1, data, 5000, vehicle1);
        assertEquals(legalPerson1, c3.getBeneficiary());
        assertEquals(naturalPerson1, c3.getPolicyHolder());
        assertTrue(c3.isActive());
        // 'member kids, " " is not an empty string
        SingleVehicleContract c4 = new SingleVehicleContract(" ", insuranceCompany, naturalPerson1, legalPerson1, data, 5000, vehicle1);
        assertEquals(naturalPerson1, c4.getBeneficiary());
        assertEquals(legalPerson1, c4.getPolicyHolder());
        assertTrue(c4.isActive());
    }

    @Test
    public void testInsureVehicle() {
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle("number", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, null));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle("number", null, naturalPerson1, 300, null, vehicle1));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.insureVehicle("number", null, naturalPerson1, 250, PremiumPaymentFrequency.ANNUAL, vehicle1));
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("number", null, naturalPerson1, 150, PremiumPaymentFrequency.SEMI_ANNUAL, vehicle1);
        assertTrue(insuranceCompany.getContracts().contains(c1));
        assertTrue(naturalPerson1.getContracts().contains(c1));
        assertEquals(150, c1.getContractPaymentData().getPremium());
        assertEquals(PremiumPaymentFrequency.SEMI_ANNUAL, c1.getContractPaymentData().getPremiumPaymentFrequency());
        LocalDateTime expectedNextPayment = insuranceCompany.getCurrentTime().plusMonths(6);
        assertEquals(expectedNextPayment, c1.getContractPaymentData().getNextPaymentTime());
        assertEquals(150, c1.getContractPaymentData().getOutstandingBalance());
    }

    @Test
    public void testPayMasterContractSimple() {
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("c1", null, legalPerson1, 1500, PremiumPaymentFrequency.ANNUAL, new Vehicle("AA111AA", 15_000));
        SingleVehicleContract c2 = insuranceCompany.insureVehicle("c2", null, legalPerson1, 184, PremiumPaymentFrequency.MONTHLY, new Vehicle("BANAN22", 22_000));
        SingleVehicleContract c3 = insuranceCompany.insureVehicle("c3", null, legalPerson1, 400, PremiumPaymentFrequency.SEMI_ANNUAL, new Vehicle("SOMRYBA", 8_000));
        SingleVehicleContract c4 = insuranceCompany.insureVehicle("c4", null, legalPerson1, 1000, PremiumPaymentFrequency.QUARTERLY, new Vehicle("ICOOKED", 40_000));

        // only do this when setting up tests
        // it is intended for contracts to be created and managed by the insurer
        c1.getContractPaymentData().setPremium(30);
        c2.getContractPaymentData().setPremium(50);
        c3.getContractPaymentData().setPremium(75);
        c4.getContractPaymentData().setPremium(20);
        c1.getContractPaymentData().setOutstandingBalance(30);
        c2.getContractPaymentData().setOutstandingBalance(50);
        c3.getContractPaymentData().setOutstandingBalance(100);
        c4.getContractPaymentData().setOutstandingBalance(0);
        MasterVehicleContract m1 = new MasterVehicleContract("m1", insuranceCompany, null, legalPerson1);
        m1.getChildContracts().add(c1);
        m1.getChildContracts().add(c2);
        m1.getChildContracts().add(c3);
        m1.getChildContracts().add(c4);
        insuranceCompany.getContracts().add(m1);
        c4.setInactive();

        m1.pay(400);
        assertEquals(-60, c1.getContractPaymentData().getOutstandingBalance());
        assertEquals(-85, c2.getContractPaymentData().getOutstandingBalance());
        assertEquals(-75, c3.getContractPaymentData().getOutstandingBalance());
        assertEquals(0, c4.getContractPaymentData().getOutstandingBalance());

        assertEquals(1, insuranceCompany.getHandler().getPaymentHistory().get(m1).size());
        Optional<PaymentInstance> optionalInstance = insuranceCompany.getHandler().getPaymentHistory().get(m1).stream().findFirst();
        assertTrue(optionalInstance.isPresent());
        PaymentInstance paymentInstance = optionalInstance.get();
        assertEquals(400, paymentInstance.getPaymentAmount());
        assertTrue(paymentInstance.getPaymentTime().isEqual(insuranceCompany.getCurrentTime()));
    }

    @Test
    public void testPaySingleContract() {
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("payTest", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        assertEquals(300, c1.getContractPaymentData().getOutstandingBalance());

        c1.pay(100);
        assertEquals(200, c1.getContractPaymentData().getOutstandingBalance());

        c1.pay(200);
        assertEquals(0, c1.getContractPaymentData().getOutstandingBalance());

        c1.pay(100);
        assertEquals(-100, c1.getContractPaymentData().getOutstandingBalance());
    }

    @Test
    public void testUpdateBalance() {
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("updateBalanceTest", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);

        int initialBalance = c1.getContractPaymentData().getOutstandingBalance();
        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusYears(1));
        c1.updateBalance();

        assertEquals(initialBalance + 300, c1.getContractPaymentData().getOutstandingBalance());
    }

    @Test
    public void testProcessTravelClaim() {
        Set<Person> insured = Set.of(naturalPerson1, naturalPerson2);
        TravelContract tc = insuranceCompany.insurePersons("travelClaim", legalPerson1, 20, PremiumPaymentFrequency.ANNUAL, insured);

        assertTrue(tc.isActive());

        insuranceCompany.processClaim(tc, Set.of(naturalPerson1));

        assertEquals(20, naturalPerson1.getPaidOutAmount());
        assertEquals(0, naturalPerson2.getPaidOutAmount());
        assertFalse(tc.isActive());
    }

    @Test
    public void testProcessTravelClaimInvalidArguments() {
        Set<Person> insured = Set.of(naturalPerson1, naturalPerson2);
        TravelContract tc = insuranceCompany.insurePersons("travelClaimInvalid", legalPerson1, 20, PremiumPaymentFrequency.ANNUAL, insured);

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(tc, null));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(null, Set.of(naturalPerson1)));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(tc, Set.of()));
    }

    @Test
    public void testProcessSingleVehicleClaim() {
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("vehicleClaim", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);

        assertTrue(c1.isActive());

        insuranceCompany.processClaim(c1, (int) (vehicle1.getOriginalValue() * 0.8));

        assertFalse(c1.isActive());
        assertEquals(vehicle1.getOriginalValue() / 2, naturalPerson1.getPaidOutAmount());
    }

    @Test
    public void testProcessSingleVehicleClaimInvalidArguments() {
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("vehicleClaimInvalid", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(c1, -500));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.processClaim(null, 400));
    }

    @Test
    public void testMoveContractToMasterVehicleContract() {
        SingleVehicleContract single = insuranceCompany.insureVehicle("moveSingle", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        MasterVehicleContract master = insuranceCompany.createMasterVehicleContract("moveMaster", null, legalPerson1);

        insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(master, single);

        assertFalse(insuranceCompany.getContracts().contains(single));
        assertTrue(master.getChildContracts().contains(single));
    }

    @Test
    public void testMoveContractToMasterVehicleContractInvalidArguments() {
        InsuranceCompany anotherCompany = new InsuranceCompany(LocalDateTime.of(2025, 4, 15, 12, 0));
        SingleVehicleContract wrongContract = anotherCompany.insureVehicle("wrongSingle", null, legalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        MasterVehicleContract master = insuranceCompany.createMasterVehicleContract("invalidMaster", null, legalPerson1);

        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(master, null));
        assertThrows(IllegalArgumentException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(null, wrongContract));
        assertThrows(InvalidContractException.class, () -> insuranceCompany.moveSingleVehicleContractToMasterVehicleContract(master, wrongContract));
    }

    @Test
    public void testChargePremiumsOnContracts() {
        SingleVehicleContract c1 = insuranceCompany.insureVehicle("charge1", null, naturalPerson1, 300, PremiumPaymentFrequency.ANNUAL, vehicle1);
        SingleVehicleContract c2 = insuranceCompany.insureVehicle("charge2", null, naturalPerson2, 220, PremiumPaymentFrequency.SEMI_ANNUAL, vehicle2);

        insuranceCompany.setCurrentTime(insuranceCompany.getCurrentTime().plusMonths(13));

        insuranceCompany.chargePremiumsOnContracts();

        assertEquals(660, c2.getContractPaymentData().getOutstandingBalance());
        assertEquals(600, c1.getContractPaymentData().getOutstandingBalance());
    }
    @Test
    public void testTravelContractValidConstruction() {
        Set<Person> insured = Set.of(naturalPerson1, naturalPerson2);
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);

        TravelContract tc = new TravelContract("T1", insuranceCompany, legalPerson1, paymentData, 200, insured);

        assertEquals("T1", tc.getContractNumber());
        assertEquals(insuranceCompany, tc.getInsurer());
        assertEquals(legalPerson1, tc.getPolicyHolder());
        assertEquals(paymentData, tc.getContractPaymentData());
        assertEquals(200, tc.getCoverageAmount());
        assertTrue(tc.isActive());
        assertEquals(insured, tc.getInsuredPersons());
    }

    @Test
    public void testTravelContractNullInsuredPersons() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);

        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract("T2", insuranceCompany, naturalPerson1, paymentData, 200, null);
        });
    }

    @Test
    public void testTravelContractEmptyInsuredPersons() {
        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);

        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract("T3", insuranceCompany, naturalPerson1, paymentData, 200, Set.of());
        });
    }

    @Test
    public void testTravelContractNullPaymentData() {
        Set<Person> insured = Set.of(naturalPerson1, naturalPerson2);

        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract("T4", insuranceCompany, naturalPerson1, null, 200, insured);
        });
    }

    @Test
    public void testTravelContractWithNonNaturalPerson() {
        Set<Person> insured = Set.of(legalPerson1); // právnická osoba v insuredPersons

        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);

        assertThrows(IllegalArgumentException.class, () -> {
            new TravelContract("T5", insuranceCompany, naturalPerson1, paymentData, 200, insured);
        });
    }

    @Test
    public void testTravelContractPolicyHolderMayNotBeInInsuredPersons() {
        Set<Person> insured = Set.of(naturalPerson2); // policyHolder nie je medzi insured

        ContractPaymentData paymentData = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);

        TravelContract tc = new TravelContract("T6", insuranceCompany, naturalPerson1, paymentData, 200, insured);

        assertTrue(tc.getInsuredPersons().contains(naturalPerson2));
        assertFalse(tc.getInsuredPersons().contains(naturalPerson1));
    }
    @Test
    public void testContractsEquality() {
        ContractPaymentData data = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insuranceCompany.getCurrentTime(), 0);
        TravelContract t1 = new TravelContract("C123", insuranceCompany, naturalPerson1, data, 200, Set.of(naturalPerson1));
        TravelContract t2 = new TravelContract("C123", insuranceCompany, naturalPerson1, data, 200, Set.of(naturalPerson1));

        assertEquals(t1, t2); // funguje vďaka equals
        assertEquals(t1.hashCode(), t2.hashCode());
    }
    @Test
    public void testEqualsAndHashCodeInSet() {
        // Príprava spoločných údajov
        InsuranceCompany insurer = new InsuranceCompany(LocalDateTime.of(2024, 1, 1, 0, 0));
        Person policyHolder = new Person("8351068242");
        Vehicle vehicle = new Vehicle("AB123CD", 10000);
        ContractPaymentData paymentData = new ContractPaymentData(150, PremiumPaymentFrequency.SEMI_ANNUAL, insurer.getCurrentTime(), 0);

        // Prvý kontrakt (reálny, pridáme do množiny)
        SingleVehicleContract realContract = new SingleVehicleContract(
                "XYZ-001", insurer, null, policyHolder, paymentData, 5000, vehicle
        );

        // Množina zmlúv
        Set<SingleVehicleContract> contracts = new HashSet<>();
        contracts.add(realContract);

        // Druhý kontrakt (rovnaké contractNumber a insurer, iné objekty)
        SingleVehicleContract fakeContract = new SingleVehicleContract(
                "XYZ-001", insurer, null, policyHolder, paymentData, 5000, vehicle
        );

        // Tento objekt nie je priamo ten istý (==), ale equals() vráti true
        assertNotSame(realContract, fakeContract);
        assertEquals(realContract, fakeContract); // uses equals()

        // A teraz najdôležitejšie:
        assertTrue(contracts.contains(fakeContract), "Set.contains() nefunguje bez správneho equals/hashCode");
    }
    @Test
    public void testContractsWithSameNumberDifferentInsurersAreNotEqual() {
        // Dve rôzne poisťovne s rovnakým časom
        InsuranceCompany insurer1 = new InsuranceCompany(LocalDateTime.of(2025, 4, 15, 12, 0));
        InsuranceCompany insurer2 = new InsuranceCompany(LocalDateTime.of(2025, 4, 15, 12, 0));

        ContractPaymentData paymentData1 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insurer1.getCurrentTime(), 0);
        ContractPaymentData paymentData2 = new ContractPaymentData(100, PremiumPaymentFrequency.ANNUAL, insurer2.getCurrentTime(), 0);

        Set<Person> insured = Set.of(naturalPerson1);

        // Rovnaký contractNumber, rôzni insurer
        TravelContract c1 = new TravelContract("XYZ123", insurer1, naturalPerson1, paymentData1, 100, insured);
        TravelContract c2 = new TravelContract("XYZ123", insurer2, naturalPerson1, paymentData2, 100, insured);

        // Overenie
        assertNotEquals(c1, c2); // nesmú byť rovnaké
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }
}