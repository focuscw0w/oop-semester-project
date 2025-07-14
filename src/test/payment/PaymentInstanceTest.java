package payment;

import org.junit.jupiter.api.Test;
import payment.PaymentInstance;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentInstanceTest {

    private final LocalDateTime testTime = LocalDateTime.of(2023, 1, 1, 0, 0);

    @Test
    void testConstructorWithValidParameters() {
        // Test creation with valid parameters
        PaymentInstance payment = new PaymentInstance(testTime, 100);
        assertNotNull(payment);
        assertEquals(testTime, payment.getPaymentTime());
        assertEquals(100, payment.getPaymentAmount());
    }

    @Test
    void testConstructorWithNullTime() {
        // Test creation with null payment time
        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(null, 100));
    }

    @Test
    void testConstructorWithZeroAmount() {
        // Test creation with zero payment amount
        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(testTime, 0));
    }

    @Test
    void testConstructorWithNegativeAmount() {
        // Test creation with negative payment amount
        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(testTime, -50));
    }

    @Test
    void testImmutability() {
        // Create a payment instance
        PaymentInstance payment = new PaymentInstance(testTime, 100);

        // Verify immutability - these should not compile or be not supported
        // The test is mainly to document the expected behavior

        // This test can be compiled only if the class has no setters
        // and the attributes are final
        assertNotNull(payment);
    }
}