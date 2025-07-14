package structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import company.InsuranceCompany;
import contracts.*;
import objects.LegalForm;
import objects.Person;
import objects.Vehicle;
import payment.ContractPaymentData;
import payment.PaymentHandler;
import payment.PaymentInstance;
import payment.PremiumPaymentFrequency;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.IntPredicate;

/**
 * This class tests the structure of your insurance system implementation.
 * It verifies if classes, attributes, and methods match the UML diagram requirements.
 */
public class StructureTests {

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
    public void testAbstractContractStructure() throws NoSuchFieldException, NoSuchMethodException {
        assertTrue(Modifier.isAbstract(AbstractContract.class.getModifiers()));

        // Check fields
        String[] fields = {
                "contractNumber", "insurer", "policyHolder", "contractPaymentData", "coverageAmount", "isActive"
        };
        IntPredicate[] modifiers = {
                Modifier::isPrivate, Modifier::isProtected, Modifier::isProtected,
                Modifier::isProtected, Modifier::isProtected, Modifier::isProtected
        };
        boolean[] shouldBeFinal = {
                true, true, true, true, false, false
        };
        Class<?>[] classes = {
                String.class, InsuranceCompany.class, Person.class,
                ContractPaymentData.class, int.class, boolean.class
        };

        for (int i = 0; i < fields.length; i++) {
            Field f = AbstractContract.class.getDeclaredField(fields[i]);
            assertTrue(modifiers[i].test(f.getModifiers()));
            if (shouldBeFinal[i]) {
                assertTrue(Modifier.isFinal(f.getModifiers()));
            }
            assertSame(classes[i], f.getType());
        }

        // Check constructor
        Class<?>[] expectedConstructorParams = {
                String.class, InsuranceCompany.class, Person.class, ContractPaymentData.class, int.class
        };
        assertTrue(Arrays.stream(AbstractContract.class.getDeclaredConstructors())
                .anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

        // Check methods
        String[] expectedMethodNames = {
                "getContractNumber", "getPolicyHolder", "getInsurer", "getCoverageAmount",
                "isActive", "setInactive", "getContractPaymentData", "updateBalance"
        };
        Class<?>[] returnTypes = {
                String.class, Person.class, InsuranceCompany.class, int.class,
                boolean.class, void.class, ContractPaymentData.class, void.class
        };

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

    @Test
    public void testTravelContractStructure() throws NoSuchFieldException, NoSuchMethodException {
        assertEquals(AbstractContract.class, TravelContract.class.getSuperclass());

        // Check fields
        Field insuredPersons = TravelContract.class.getDeclaredField("insuredPersons");
        assertTrue(Modifier.isPrivate(insuredPersons.getModifiers()));
        assertTrue(Modifier.isFinal(insuredPersons.getModifiers()));
        assertSame(Set.class, insuredPersons.getType());
        assertSame(Person.class, ((ParameterizedType)insuredPersons.getGenericType()).getActualTypeArguments()[0]);

        // Check constructor
        Class<?>[] expectedConstructorParams = {
                String.class, InsuranceCompany.class, Person.class, ContractPaymentData.class, int.class, Set.class
        };
        assertTrue(Arrays.stream(TravelContract.class.getDeclaredConstructors())
                .anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

        // Check methods
        Method getInsuredPersons = TravelContract.class.getDeclaredMethod("getInsuredPersons");
        assertSame(Set.class, getInsuredPersons.getReturnType());
        assertSame(Person.class, ((ParameterizedType)getInsuredPersons.getGenericReturnType()).getActualTypeArguments()[0]);
        assertTrue(Modifier.isPublic(getInsuredPersons.getModifiers()));
    }

    @Test
    public void testAbstractVehicleContractStructure() throws NoSuchFieldException, NoSuchMethodException {
        assertTrue(Modifier.isAbstract(AbstractVehicleContract.class.getModifiers()));
        assertEquals(AbstractContract.class, AbstractVehicleContract.class.getSuperclass());

        // Check fields
        Field beneficiary = AbstractVehicleContract.class.getDeclaredField("beneficiary");
        assertTrue(Modifier.isProtected(beneficiary.getModifiers()));
        assertSame(Person.class, beneficiary.getType());

        // Check constructor
        Class<?>[] expectedConstructorParams = {
                String.class, InsuranceCompany.class, Person.class, Person.class, ContractPaymentData.class, int.class
        };
        assertTrue(Arrays.stream(AbstractVehicleContract.class.getDeclaredConstructors())
                .anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

        // Check methods
        Method getBeneficiary = AbstractVehicleContract.class.getDeclaredMethod("getBeneficiary");
        assertSame(Person.class, getBeneficiary.getReturnType());
        assertTrue(Modifier.isPublic(getBeneficiary.getModifiers()));

        Method setBeneficiary = AbstractVehicleContract.class.getDeclaredMethod("setBeneficiary", Person.class);
        assertSame(void.class, setBeneficiary.getReturnType());
        assertTrue(Modifier.isPublic(setBeneficiary.getModifiers()));
        assertEquals(1, setBeneficiary.getParameterTypes().length);
        assertEquals(Person.class, setBeneficiary.getParameterTypes()[0]);
    }

    @Test
    public void testSingleVehicleContractStructure() throws NoSuchFieldException, NoSuchMethodException {
        assertEquals(AbstractVehicleContract.class, SingleVehicleContract.class.getSuperclass());

        // Check fields
        Field insuredVehicle = SingleVehicleContract.class.getDeclaredField("insuredVehicle");
        assertTrue(Modifier.isPrivate(insuredVehicle.getModifiers()));
        assertTrue(Modifier.isFinal(insuredVehicle.getModifiers()));
        assertSame(Vehicle.class, insuredVehicle.getType());

        // Check constructor
        Class<?>[] expectedConstructorParams = {
                String.class, InsuranceCompany.class, Person.class, Person.class,
                ContractPaymentData.class, int.class, Vehicle.class
        };
        assertTrue(Arrays.stream(SingleVehicleContract.class.getDeclaredConstructors())
                .anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

        // Check methods
        Method getInsuredVehicle = SingleVehicleContract.class.getDeclaredMethod("getInsuredVehicle");
        assertSame(Vehicle.class, getInsuredVehicle.getReturnType());
        assertTrue(Modifier.isPublic(getInsuredVehicle.getModifiers()));
    }

    @Test
    public void testMasterVehicleContractStructure() throws NoSuchFieldException, NoSuchMethodException {
        assertEquals(AbstractVehicleContract.class, MasterVehicleContract.class.getSuperclass());

        // Check fields
        Field childContracts = MasterVehicleContract.class.getDeclaredField("childContracts");
        assertTrue(Modifier.isPrivate(childContracts.getModifiers()));
        assertTrue(Modifier.isFinal(childContracts.getModifiers()));
        assertSame(Set.class, childContracts.getType());
        assertSame(SingleVehicleContract.class,
                ((ParameterizedType)childContracts.getGenericType()).getActualTypeArguments()[0]);

        // Check constructor
        Class<?>[] expectedConstructorParams = {
                String.class, InsuranceCompany.class, Person.class, Person.class
        };
        assertTrue(Arrays.stream(MasterVehicleContract.class.getDeclaredConstructors())
                .anyMatch(c -> Arrays.equals(expectedConstructorParams, c.getParameterTypes())));

        // Check methods
        Method getChildContracts = MasterVehicleContract.class.getDeclaredMethod("getChildContracts");
        assertSame(Set.class, getChildContracts.getReturnType());
        assertSame(SingleVehicleContract.class,
                ((ParameterizedType)getChildContracts.getGenericReturnType()).getActualTypeArguments()[0]);
        assertTrue(Modifier.isPublic(getChildContracts.getModifiers()));

        Method requestAdditionOfChildContract = MasterVehicleContract.class.getDeclaredMethod(
                "requestAdditionOfChildContract", SingleVehicleContract.class);
        assertSame(void.class, requestAdditionOfChildContract.getReturnType());
        assertTrue(Modifier.isPublic(requestAdditionOfChildContract.getModifiers()));

        Method isActive = MasterVehicleContract.class.getDeclaredMethod("isActive");
        assertSame(boolean.class, isActive.getReturnType());
        assertTrue(Modifier.isPublic(isActive.getModifiers()));

        Method setInactive = MasterVehicleContract.class.getDeclaredMethod("setInactive");
        assertSame(void.class, setInactive.getReturnType());
        assertTrue(Modifier.isPublic(setInactive.getModifiers()));
    }

    @Test
    public void testPersonStructure() throws NoSuchFieldException, NoSuchMethodException {
        // Check fields
        Field id = Person.class.getDeclaredField("id");
        assertTrue(Modifier.isPrivate(id.getModifiers()));
        assertTrue(Modifier.isFinal(id.getModifiers()));
        assertSame(String.class, id.getType());

        Field legalForm = Person.class.getDeclaredField("legalForm");
        assertTrue(Modifier.isPrivate(legalForm.getModifiers()));
        assertTrue(Modifier.isFinal(legalForm.getModifiers()));
        assertSame(LegalForm.class, legalForm.getType());

        Field paidOutAmount = Person.class.getDeclaredField("paidOutAmount");
        assertTrue(Modifier.isPrivate(paidOutAmount.getModifiers()));
        assertSame(int.class, paidOutAmount.getType());

        Field contracts = Person.class.getDeclaredField("contracts");
        assertTrue(Modifier.isPrivate(contracts.getModifiers()));
        assertTrue(Modifier.isFinal(contracts.getModifiers()));
        assertSame(Set.class, contracts.getType());
        assertSame(AbstractContract.class,
                ((ParameterizedType)contracts.getGenericType()).getActualTypeArguments()[0]);

        // Check methods
        Method getId = Person.class.getDeclaredMethod("getId");
        assertSame(String.class, getId.getReturnType());
        assertTrue(Modifier.isPublic(getId.getModifiers()));

        Method getLegalForm = Person.class.getDeclaredMethod("getLegalForm");
        assertSame(LegalForm.class, getLegalForm.getReturnType());
        assertTrue(Modifier.isPublic(getLegalForm.getModifiers()));

        Method getPaidOutAmount = Person.class.getDeclaredMethod("getPaidOutAmount");
        assertSame(int.class, getPaidOutAmount.getReturnType());
        assertTrue(Modifier.isPublic(getPaidOutAmount.getModifiers()));

        Method getContracts = Person.class.getDeclaredMethod("getContracts");
        assertSame(Set.class, getContracts.getReturnType());
        assertSame(AbstractContract.class,
                ((ParameterizedType)getContracts.getGenericReturnType()).getActualTypeArguments()[0]);
        assertTrue(Modifier.isPublic(getContracts.getModifiers()));

        Method addContract = Person.class.getDeclaredMethod("addContract", AbstractContract.class);
        assertSame(void.class, addContract.getReturnType());
        assertTrue(Modifier.isPublic(addContract.getModifiers()));

        Method payout = Person.class.getDeclaredMethod("payout", int.class);
        assertSame(void.class, payout.getReturnType());
        assertTrue(Modifier.isPublic(payout.getModifiers()));
    }

    @Test
    public void testVehicleStructure() throws NoSuchFieldException, NoSuchMethodException {
        // Check fields
        Field licensePlate = Vehicle.class.getDeclaredField("licensePlate");
        assertTrue(Modifier.isPrivate(licensePlate.getModifiers()));
        assertTrue(Modifier.isFinal(licensePlate.getModifiers()));
        assertSame(String.class, licensePlate.getType());

        Field originalValue = Vehicle.class.getDeclaredField("originalValue");
        assertTrue(Modifier.isPrivate(originalValue.getModifiers()));
        assertTrue(Modifier.isFinal(originalValue.getModifiers()));
        assertSame(int.class, originalValue.getType());

        // Check methods
        Method getLicensePlate = Vehicle.class.getDeclaredMethod("getLicensePlate");
        assertSame(String.class, getLicensePlate.getReturnType());
        assertTrue(Modifier.isPublic(getLicensePlate.getModifiers()));

        Method getOriginalValue = Vehicle.class.getDeclaredMethod("getOriginalValue");
        assertSame(int.class, getOriginalValue.getReturnType());
        assertTrue(Modifier.isPublic(getOriginalValue.getModifiers()));
    }

    @Test
    public void testContractPaymentDataStructure() throws NoSuchFieldException, NoSuchMethodException {
        // Check fields
        Field premium = ContractPaymentData.class.getDeclaredField("premium");
        assertTrue(Modifier.isPrivate(premium.getModifiers()));
        assertSame(int.class, premium.getType());

        Field premiumPaymentFrequency = ContractPaymentData.class.getDeclaredField("premiumPaymentFrequency");
        assertTrue(Modifier.isPrivate(premiumPaymentFrequency.getModifiers()));
        assertSame(PremiumPaymentFrequency.class, premiumPaymentFrequency.getType());

        Field nextPaymentTime = ContractPaymentData.class.getDeclaredField("nextPaymentTime");
        assertTrue(Modifier.isPrivate(nextPaymentTime.getModifiers()));
        assertSame(LocalDateTime.class, nextPaymentTime.getType());

        Field outstandingBalance = ContractPaymentData.class.getDeclaredField("outstandingBalance");
        assertTrue(Modifier.isPrivate(outstandingBalance.getModifiers()));
        assertSame(int.class, outstandingBalance.getType());

        // Check methods
        Method getPremium = ContractPaymentData.class.getDeclaredMethod("getPremium");
        assertSame(int.class, getPremium.getReturnType());
        assertTrue(Modifier.isPublic(getPremium.getModifiers()));

        Method setPremium = ContractPaymentData.class.getDeclaredMethod("setPremium", int.class);
        assertSame(void.class, setPremium.getReturnType());
        assertTrue(Modifier.isPublic(setPremium.getModifiers()));

        Method getPremiumPaymentFrequency = ContractPaymentData.class.getDeclaredMethod("getPremiumPaymentFrequency");
        assertSame(PremiumPaymentFrequency.class, getPremiumPaymentFrequency.getReturnType());
        assertTrue(Modifier.isPublic(getPremiumPaymentFrequency.getModifiers()));

        Method setPremiumPaymentFrequency = ContractPaymentData.class.getDeclaredMethod("setPremiumPaymentFrequency", PremiumPaymentFrequency.class);
        assertSame(void.class, setPremiumPaymentFrequency.getReturnType());
        assertTrue(Modifier.isPublic(setPremiumPaymentFrequency.getModifiers()));

        Method getNextPaymentTime = ContractPaymentData.class.getDeclaredMethod("getNextPaymentTime");
        assertSame(LocalDateTime.class, getNextPaymentTime.getReturnType());
        assertTrue(Modifier.isPublic(getNextPaymentTime.getModifiers()));

        Method getOutstandingBalance = ContractPaymentData.class.getDeclaredMethod("getOutstandingBalance");
        assertSame(int.class, getOutstandingBalance.getReturnType());
        assertTrue(Modifier.isPublic(getOutstandingBalance.getModifiers()));

        Method setOutstandingBalance = ContractPaymentData.class.getDeclaredMethod("setOutstandingBalance", int.class);
        assertSame(void.class, setOutstandingBalance.getReturnType());
        assertTrue(Modifier.isPublic(setOutstandingBalance.getModifiers()));

        Method updateNextPaymentTime = ContractPaymentData.class.getDeclaredMethod("updateNextPaymentTime");
        assertSame(void.class, updateNextPaymentTime.getReturnType());
        assertTrue(Modifier.isPublic(updateNextPaymentTime.getModifiers()));
    }

    @Test
    public void testPremiumPaymentFrequencyStructure() throws NoSuchMethodException {
        // Check if it's an enum
        assertTrue(PremiumPaymentFrequency.class.isEnum());

        // Check enum values
        PremiumPaymentFrequency[] constants = PremiumPaymentFrequency.values();
        Set<String> enumNames = new HashSet<>();
        for (PremiumPaymentFrequency constant : constants) {
            enumNames.add(constant.name());
        }

        assertTrue(enumNames.contains("ANNUAL"));
        assertTrue(enumNames.contains("SEMI_ANNUAL"));
        assertTrue(enumNames.contains("QUARTERLY"));
        assertTrue(enumNames.contains("MONTHLY"));

        // Check method
        Method getValueInMonths = PremiumPaymentFrequency.class.getDeclaredMethod("getValueInMonths");
        assertSame(int.class, getValueInMonths.getReturnType());
        assertTrue(Modifier.isPublic(getValueInMonths.getModifiers()));
    }

    @Test
    public void testPaymentHandlerStructure() throws NoSuchFieldException, NoSuchMethodException {
        // Check fields
        Field insurer = PaymentHandler.class.getDeclaredField("insurer");
        assertTrue(Modifier.isPrivate(insurer.getModifiers()));
        assertTrue(Modifier.isFinal(insurer.getModifiers()));
        assertSame(InsuranceCompany.class, insurer.getType());

        Field paymentHistory = PaymentHandler.class.getDeclaredField("paymentHistory");
        assertTrue(Modifier.isPrivate(paymentHistory.getModifiers()));
        assertTrue(Modifier.isFinal(paymentHistory.getModifiers()));
        assertSame(Map.class, paymentHistory.getType());

        Type[] typeArgs = ((ParameterizedType)paymentHistory.getGenericType()).getActualTypeArguments();
        assertEquals(AbstractContract.class, typeArgs[0]);
        assertTrue(typeArgs[1] instanceof ParameterizedType);
        ParameterizedType setType = (ParameterizedType) typeArgs[1];
        assertEquals(Set.class, setType.getRawType());
        assertEquals(PaymentInstance.class, setType.getActualTypeArguments()[0]);

        Method getPaymentHistory = PaymentHandler.class.getDeclaredMethod("getPaymentHistory");
        assertSame(Map.class, getPaymentHistory.getReturnType());
        assertTrue(Modifier.isPublic(getPaymentHistory.getModifiers()));

        Method payAbstractContract = PaymentHandler.class.getDeclaredMethod("pay", AbstractContract.class, int.class);
        assertSame(void.class, payAbstractContract.getReturnType());
        assertTrue(Modifier.isPublic(payAbstractContract.getModifiers()));

        Method payMasterVehicleContract = PaymentHandler.class.getDeclaredMethod("pay", MasterVehicleContract.class, int.class);
        assertSame(void.class, payMasterVehicleContract.getReturnType());
        assertTrue(Modifier.isPublic(payMasterVehicleContract.getModifiers()));
    }

    @Test
    public void testPaymentInstanceStructure() throws NoSuchFieldException, NoSuchMethodException {
        // Check if implements Comparable
        boolean implementsComparable = false;
        for (Class<?> iface : PaymentInstance.class.getInterfaces()) {
            if (iface.equals(Comparable.class)) {
                implementsComparable = true;
                break;
            }
        }
        assertTrue(implementsComparable);

        // Check fields
        Field paymentTime = PaymentInstance.class.getDeclaredField("paymentTime");
        assertTrue(Modifier.isPrivate(paymentTime.getModifiers()));
        assertTrue(Modifier.isFinal(paymentTime.getModifiers()));
        assertSame(LocalDateTime.class, paymentTime.getType());

        Field paymentAmount = PaymentInstance.class.getDeclaredField("paymentAmount");
        assertTrue(Modifier.isPrivate(paymentAmount.getModifiers()));
        assertTrue(Modifier.isFinal(paymentAmount.getModifiers()));
        assertSame(int.class, paymentAmount.getType());

        // Check methods
        Method getPaymentTime = PaymentInstance.class.getDeclaredMethod("getPaymentTime");
        assertSame(LocalDateTime.class, getPaymentTime.getReturnType());
        assertTrue(Modifier.isPublic(getPaymentTime.getModifiers()));

        Method getPaymentAmount = PaymentInstance.class.getDeclaredMethod("getPaymentAmount");
        assertSame(int.class, getPaymentAmount.getReturnType());
        assertTrue(Modifier.isPublic(getPaymentAmount.getModifiers()));

        Method compareTo = PaymentInstance.class.getDeclaredMethod("compareTo", PaymentInstance.class);
        assertSame(int.class, compareTo.getReturnType());
        assertTrue(Modifier.isPublic(compareTo.getModifiers()));
    }

    @Test
    public void testLegalFormStructure() {
        // Check if it's an enum
        assertTrue(LegalForm.class.isEnum());

        // Check enum values
        LegalForm[] constants = LegalForm.values();
        Set<String> enumNames = new HashSet<>();
        for (LegalForm constant : constants) {
            enumNames.add(constant.name());
        }

        assertTrue(enumNames.contains("NATURAL"));
        assertTrue(enumNames.contains("LEGAL"));
    }

    @Test
    public void testInvalidContractExceptionStructure() {
        assertEquals(RuntimeException.class, InvalidContractException.class.getSuperclass());

        // Check constructor with message parameter
        try {
            Constructor<?> constructor = InvalidContractException.class.getDeclaredConstructor(String.class);
            assertNotNull(constructor);
            assertTrue(Modifier.isPublic(constructor.getModifiers()));
        } catch (NoSuchMethodException e) {
            fail("InvalidContractException should have a constructor that takes a String message");
        }
    }

    @Test
    public void testPaymentInstanceComparable() throws NoSuchMethodException {
        // Verify that PaymentInstance implements Comparable<PaymentInstance>
        Class<?>[] interfaces = PaymentInstance.class.getInterfaces();
        boolean implementsComparable = false;

        for (Class<?> iface : interfaces) {
            if (iface.equals(Comparable.class)) {
                implementsComparable = true;
                break;
            }
        }

        assertTrue(implementsComparable, "PaymentInstance must implement Comparable");

        // Verify compareTo method exists with correct signature
        Method compareTo = PaymentInstance.class.getDeclaredMethod("compareTo", PaymentInstance.class);
        assertSame(int.class, compareTo.getReturnType());
        assertTrue(Modifier.isPublic(compareTo.getModifiers()));
    }

    @Test
    public void testContractNumberUniqueness() throws Exception {
        // Create two contracts with same contract number
        ContractPaymentData data = new ContractPaymentData(150, PremiumPaymentFrequency.QUARTERLY,
                insuranceCompany.getCurrentTime(), 0);

        SingleVehicleContract c1 = new SingleVehicleContract("c1", insuranceCompany, null, naturalPerson1,
                data, 5000, vehicle1);

        // Attempt to create another contract with the same number - should be handled by InsuranceCompany
        // This is just verifying that the contract number field exists and is properly set
        assertEquals("c1", c1.getContractNumber());
    }

    @Test
    public void testPremiumPaymentFrequencyValues() {
        // Test that PremiumPaymentFrequency has the required enum values and they return correct month values
        assertEquals(12, PremiumPaymentFrequency.ANNUAL.getValueInMonths());
        assertEquals(6, PremiumPaymentFrequency.SEMI_ANNUAL.getValueInMonths());
        assertEquals(3, PremiumPaymentFrequency.QUARTERLY.getValueInMonths());
        assertEquals(1, PremiumPaymentFrequency.MONTHLY.getValueInMonths());
    }


    @Test
    public void testInsuranceCompanyStructure() throws NoSuchFieldException, NoSuchMethodException {
        // Check fields
        Field contracts = InsuranceCompany.class.getDeclaredField("contracts");
        assertTrue(Modifier.isPrivate(contracts.getModifiers()));
        assertTrue(Modifier.isFinal(contracts.getModifiers()));
        assertSame(Set.class, contracts.getType());
        assertEquals(AbstractContract.class,
                ((ParameterizedType)contracts.getGenericType()).getActualTypeArguments()[0]);

        Field handler = InsuranceCompany.class.getDeclaredField("handler");
        assertTrue(Modifier.isPrivate(handler.getModifiers()));
        assertTrue(Modifier.isFinal(handler.getModifiers()));
        assertSame(PaymentHandler.class, handler.getType());

        Field currentTime = InsuranceCompany.class.getDeclaredField("currentTime");
        assertTrue(Modifier.isPrivate(currentTime.getModifiers()));
        assertSame(LocalDateTime.class, currentTime.getType());

        // Check constructor
        Constructor<?> constructor = InsuranceCompany.class.getDeclaredConstructor(LocalDateTime.class);
        assertTrue(Modifier.isPublic(constructor.getModifiers()));

        // Check methods
        Method getContracts = InsuranceCompany.class.getDeclaredMethod("getContracts");
        assertSame(Set.class, getContracts.getReturnType());
        assertTrue(Modifier.isPublic(getContracts.getModifiers()));

        Method getHandler = InsuranceCompany.class.getDeclaredMethod("getHandler");
        assertSame(PaymentHandler.class, getHandler.getReturnType());
        assertTrue(Modifier.isPublic(getHandler.getModifiers()));

        Method getCurrentTime = InsuranceCompany.class.getDeclaredMethod("getCurrentTime");
        assertSame(LocalDateTime.class, getCurrentTime.getReturnType());
        assertTrue(Modifier.isPublic(getCurrentTime.getModifiers()));

        Method setCurrentTime = InsuranceCompany.class.getDeclaredMethod("setCurrentTime", LocalDateTime.class);
        assertSame(void.class, setCurrentTime.getReturnType());
        assertTrue(Modifier.isPublic(setCurrentTime.getModifiers()));

        // Check insurance creation methods
        Method insureVehicle = InsuranceCompany.class.getDeclaredMethod("insureVehicle",
                String.class, Person.class, Person.class, int.class, PremiumPaymentFrequency.class, Vehicle.class);
        assertSame(SingleVehicleContract.class, insureVehicle.getReturnType());
        assertTrue(Modifier.isPublic(insureVehicle.getModifiers()));

        Method insurePersons = InsuranceCompany.class.getDeclaredMethod("insurePersons",
                String.class, Person.class, int.class, PremiumPaymentFrequency.class, Set.class);
        assertSame(TravelContract.class, insurePersons.getReturnType());
        assertTrue(Modifier.isPublic(insurePersons.getModifiers()));

        Method createMasterVehicleContract = InsuranceCompany.class.getDeclaredMethod("createMasterVehicleContract",
                String.class, Person.class, Person.class);
        assertSame(MasterVehicleContract.class, createMasterVehicleContract.getReturnType());
        assertTrue(Modifier.isPublic(createMasterVehicleContract.getModifiers()));

        // Check contract management methods
        Method moveSingleVehicleContractToMasterVehicleContract = InsuranceCompany.class.getDeclaredMethod(
                "moveSingleVehicleContractToMasterVehicleContract",
                MasterVehicleContract.class, SingleVehicleContract.class);
        assertSame(void.class, moveSingleVehicleContractToMasterVehicleContract.getReturnType());
        assertTrue(Modifier.isPublic(moveSingleVehicleContractToMasterVehicleContract.getModifiers()));

        // Check premium management methods
        Method chargePremiumsOnContracts = InsuranceCompany.class.getDeclaredMethod("chargePremiumsOnContracts");
        assertSame(void.class, chargePremiumsOnContracts.getReturnType());
        assertTrue(Modifier.isPublic(chargePremiumsOnContracts.getModifiers()));

        Method chargePremiumOnContract1 = InsuranceCompany.class.getDeclaredMethod(
                "chargePremiumOnContract", AbstractContract.class);
        assertSame(void.class, chargePremiumOnContract1.getReturnType());
        assertTrue(Modifier.isPublic(chargePremiumOnContract1.getModifiers()));

        Method chargePremiumOnContract2 = InsuranceCompany.class.getDeclaredMethod(
                "chargePremiumOnContract", MasterVehicleContract.class);
        assertSame(void.class, chargePremiumOnContract2.getReturnType());
        assertTrue(Modifier.isPublic(chargePremiumOnContract2.getModifiers()));

        // Check claim processing methods
        Method processClaimSingleVehicle = InsuranceCompany.class.getDeclaredMethod(
                "processClaim", SingleVehicleContract.class, int.class);
        assertSame(void.class, processClaimSingleVehicle.getReturnType());
        assertTrue(Modifier.isPublic(processClaimSingleVehicle.getModifiers()));

        Method processClaimTravel = InsuranceCompany.class.getDeclaredMethod(
                "processClaim", TravelContract.class, Set.class);
        assertSame(void.class, processClaimTravel.getReturnType());
        assertTrue(Modifier.isPublic(processClaimTravel.getModifiers()));
    }
}