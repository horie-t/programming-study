package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.math.BigDecimal;

public record KilogramQuantity(BigDecimal value) implements OrderQuantity {

    private static final BigDecimal MIN = new BigDecimal("0.05");
    private static final BigDecimal MAX = new BigDecimal("100");

    public static Either<String, KilogramQuantity> create(String fieldName, BigDecimal value) {
        return ConstrainedType.createDecimal(fieldName, KilogramQuantity::new, MIN, MAX, value);
    }

    @Override
    public BigDecimal amount() {
        return value;
    }
}
