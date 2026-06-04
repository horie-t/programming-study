package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;

public record OrderId(String value) {

    private static final int MAX_LENGTH = 50;

    public static Either<String, OrderId> create(String fieldName, String value) {
        return ConstrainedType.createString(fieldName, OrderId::new, MAX_LENGTH, value);
    }
}
