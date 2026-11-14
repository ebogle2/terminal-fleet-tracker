package com.fleettracker.finance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    @DisplayName("0.10 + 0.20 is exactly 0.30 (unlike double)")
    void addsDecimalsExactly() {
        Money result = Money.of("0.10").add(Money.of("0.20"));

        assertEquals(Money.of("0.30"), result);
        // For contrast, the same math with double is wrong:
        assertNotEquals(0.3, 0.1 + 0.2);
    }

    @Test
    @DisplayName("add() returns a new object and leaves the original unchanged")
    void isImmutable() {
        Money original = Money.of("10.00");

        Money sum = original.add(Money.of("5.00"));

        assertEquals(Money.of("10.00"), original);
        assertEquals(Money.of("15.00"), sum);
    }

    @Test
    @DisplayName("subtract() can produce a negative amount")
    void subtracts() {
        assertEquals(Money.of("-2.50"), Money.of("5.00").subtract(Money.of("7.50")));
    }

    @Test
    @DisplayName("multiply() rounds to cents using banker's rounding (HALF_EVEN)")
    void multipliesWithBankersRounding() {
        // 0.25 * 0.5 = 0.125 -> tie -> rounds to even digit -> 0.12
        assertEquals(Money.of("0.12"), Money.of("0.25").multiply(new BigDecimal("0.5")));
        // 0.35 * 0.5 = 0.175 -> tie -> rounds to even digit -> 0.18
        assertEquals(Money.of("0.18"), Money.of("0.35").multiply(new BigDecimal("0.5")));
    }

    @Test
    @DisplayName("2.0 and 2.00 are equal Money values (the BigDecimal scale trap is avoided)")
    void equalityIgnoresInputScale() {
        Money a = Money.of("2.0");
        Money b = Money.of("2.00");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("zero() is 0.00")
    void zeroIsZero() {
        assertEquals(Money.of("0"), Money.zero());
    }

    @Test
    @DisplayName("toString formats as dollars with two decimals")
    void formatsAsDollars() {
        assertEquals("$19.99", Money.of("19.99").toString());
        assertEquals("$5.00", Money.of("5").toString());
        assertEquals("-$3.50", Money.of("-3.5").toString());
    }

    @Test
    @DisplayName("Invalid input is rejected with IllegalArgumentException")
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(null));
        assertThrows(IllegalArgumentException.class, () -> Money.of("   "));
        assertThrows(IllegalArgumentException.class, () -> Money.of("abc"));
        assertThrows(IllegalArgumentException.class, () -> Money.zero().add(null));
        assertThrows(IllegalArgumentException.class, () -> Money.zero().multiply(null));
    }
}
