package tests.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import payment.PaymentInstance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentInstanceTests {
    private LocalDateTime paymentTime;

    private int paymentAmount;

    @BeforeEach
    public void setUp() {
        paymentAmount = 100;
        paymentTime = LocalDateTime.parse("2025-11-11T10:12:11", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Test
    public void givenPaymentTimeIsNull_whenCreatingPaymentInstance_thenThrowsIllegalArgumentException() {
        paymentTime = null;

        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(paymentTime, paymentAmount));
    }

    @Test
    public void givenPaymentAmountIsZero_whenCreatingPaymentInstance_thenThrowsIllegalArgumentException() {
        paymentAmount = 0;

        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(paymentTime, paymentAmount));
    }

    @Test
    public void givenPaymentAmountIsNegative_whenCreatingPaymentInstance_thenThrowsIllegalArgumentException() {
        paymentAmount = -100;

        assertThrows(IllegalArgumentException.class, () -> new PaymentInstance(paymentTime, paymentAmount));
    }

    @Test
    public void givenValidData_whenCreatingPaymentInstance_thenAttributesAreSet() {
        var paymentInstance = new PaymentInstance(paymentTime, paymentAmount);

        assertEquals(paymentTime, paymentInstance.getPaymentTime());
        assertEquals(paymentAmount, paymentInstance.getPaymentAmount());
    }
}