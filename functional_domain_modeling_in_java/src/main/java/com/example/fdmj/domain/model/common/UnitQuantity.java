package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.math.BigDecimal;

public record UnitQuantity(int value) implements OrderQuantity {

    private static final int MIN = 1;
    private static final int MAX = 1000;

    public static Either<String, UnitQuantity> create(String fieldName, int value) {
        return ConstrainedType.createInt(fieldName, UnitQuantity::new, MIN, MAX, value);
    }

    @Override
    public BigDecimal amount() {
        return BigDecimal.valueOf(value);
    }
}
