package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import io.vavr.control.Option;
import java.util.function.Function;

public final class ConstrainedType {

    private ConstrainedType() {}

    public static <T> Either<String, T> createString(
            String fieldName, Function<String, T> ctor, int maxLength, String value) {
        if (value == null || value.isEmpty()) {
            return Either.left(fieldName + " must not be null or empty");
        }
        if (value.length() > maxLength) {
            return Either.left(fieldName + " must not be more than " + maxLength + " chars");
        }
        return Either.right(ctor.apply(value));
    }

    public static <T> Either<String, Option<T>> createStringOption(
            String fieldName, Function<String, T> ctor, int maxLength, String value) {
        if (value == null || value.isEmpty()) {
            return Either.right(Option.none());
        }
        if (value.length() > maxLength) {
            return Either.left(fieldName + " must not be more than " + maxLength + " chars");
        }
        return Either.right(Option.some(ctor.apply(value)));
    }
}
