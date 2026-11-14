package com.fleettracker.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An immutable amount of money, stored as a {@link BigDecimal} with exactly
 * two decimal places.
 *
 * <p>Why not {@code double}? Binary floating point cannot represent most decimal
 * fractions exactly ({@code 0.1 + 0.2 == 0.30000000000000004}). {@code BigDecimal}
 * stores decimal digits exactly, so currency math is always correct.
 *
 * <p>Why immutable? A {@code Money} can be shared freely (between records,
 * vehicles, reports) without anyone being able to change it behind your back.
 * Every arithmetic operation returns a <em>new</em> {@code Money}.
 */
public final class Money {

    /** Cents precision: every amount is stored with exactly 2 decimal places. */
    private static final int SCALE = 2;

    /** Banker's rounding: ties round to the nearest even digit, avoiding upward bias. */
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    private static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    /**
     * Private: callers must use {@link #of(String)} or {@link #zero()}.
     * Every instance passes through here, so every amount is normalized to scale 2.
     */
    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, ROUNDING);
    }

    /**
     * Creates a {@code Money} from text such as {@code "19.99"}.
     * Taking a String (not a double) means the exact digits typed are preserved.
     *
     * @throws IllegalArgumentException if the text is null, blank, or not a number
     */
    public static Money of(String amount) {
        if (amount == null || amount.isBlank()) {
            throw new IllegalArgumentException("Amount must not be null or blank");
        }
        try {
            return new Money(new BigDecimal(amount.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid amount: \"" + amount + "\"", e);
        }
    }

    /** Returns a {@code Money} of 0.00. Safe to share because it is immutable. */
    public static Money zero() {
        return ZERO;
    }

    /** Returns a new {@code Money} equal to this + other. */
    public Money add(Money other) {
        requireNonNull(other, "other");
        return new Money(amount.add(other.amount));
    }

    /** Returns a new {@code Money} equal to this - other (may be negative). */
    public Money subtract(Money other) {
        requireNonNull(other, "other");
        return new Money(amount.subtract(other.amount));
    }

    /**
     * Returns a new {@code Money} equal to this * factor, rounded to cents.
     * Example: a 1.15 multiplier for a 15% surcharge.
     */
    public Money multiply(BigDecimal factor) {
        requireNonNull(factor, "factor");
        return new Money(amount.multiply(factor));
    }

    /**
     * Returns the underlying amount. Safe to expose because {@code BigDecimal}
     * is itself immutable. Needed later for comparisons (e.g. sorting by cost).
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Two {@code Money} objects are equal if their amounts are equal.
     *
     * <p>Trap: {@code BigDecimal.equals} compares scale too, so
     * {@code 2.0} and {@code 2.00} are NOT equal as BigDecimals. That is safe
     * here only because the constructor forces every amount to scale 2.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money)) {
            return false;
        }
        Money other = (Money) o;
        return amount.equals(other.amount);
    }

    /** Must be consistent with equals: equal objects must have equal hash codes. */
    @Override
    public int hashCode() {
        return amount.hashCode();
    }

    /** Formats as e.g. {@code $19.99} or {@code -$5.00}. */
    @Override
    public String toString() {
        if (amount.signum() < 0) {
            return "-$" + amount.negate().toPlainString();
        }
        return "$" + amount.toPlainString();
    }

    private static void requireNonNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }
}
