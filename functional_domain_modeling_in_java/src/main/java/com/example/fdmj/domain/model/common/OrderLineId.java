package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;

public record OrderLineId(String value) {

    private static final int MAX_LENGTH = 50;

    public static Either<String, OrderLineId> create(String fieldName, String value) {
        return ConstrainedType.createString(fieldName, OrderLineId::new, MAX_LENGTH, value);
    }
}
