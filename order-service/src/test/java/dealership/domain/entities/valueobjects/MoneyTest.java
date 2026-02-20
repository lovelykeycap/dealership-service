package dealership.domain.entities.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void shouldRoundAmountToTwoDecimalPlaces() {
        Money money = new Money(new BigDecimal("123.456"));

        assertEquals(new BigDecimal("123.46"), money.amount());
    }

    @Test
    void shouldAddMoneyAmounts() {
        Money left = Money.of("100.10");
        Money right = Money.of("20.25");

        Money result = left.add(right);

        assertEquals(new BigDecimal("120.35"), result.amount());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("-0.01"));
    }
}
