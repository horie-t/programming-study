package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import io.vavr.control.Option;

public record String50(String value) {

    private static final int MAX_LENGTH = 50;

    public static Either<String, String50> create(String fieldName, String value) {
        return ConstrainedType.createString(fieldName, String50::new, MAX_LENGTH, value);
    }

    public static Either<String, Option<String50>> createOption(String fieldName, String value) {
        return ConstrainedType.createStringOption(fieldName, String50::new, MAX_LENGTH, value);
    }
}
