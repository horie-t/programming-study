package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.math.BigDecimal;

public record Price(BigDecimal value) {

    private static final BigDecimal MIN = BigDecimal.ZERO;
    private static final BigDecimal MAX = new BigDecimal("1000");

    public static Either<String, Price> create(BigDecimal value) {
        return ConstrainedType.createDecimal("Price", Price::new, MIN, MAX, value);
    }

    public static Price unsafeCreate(BigDecimal value) {
        return create(value).getOrElseThrow(err ->
                new IllegalArgumentException("Not expecting Price to be out of bounds: " + err));
    }

    public Either<String, Price> multiply(BigDecimal qty) {
        return create(value.multiply(qty));
    }
}
