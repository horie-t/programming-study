package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import io.vavr.control.Option;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

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

    public static <T> Either<String, T> createLike(
            String fieldName, Function<String, T> ctor, Pattern pattern, String value) {
        if (value == null || value.isEmpty()) {
            return Either.left(fieldName + ": Must not be null or empty");
        }
        if (!pattern.matcher(value).matches()) {
            return Either.left(fieldName + ": '" + value + "' must match the pattern '" + pattern.pattern() + "'");
        }
        return Either.right(ctor.apply(value));
    }

    public static <T> Either<String, T> createInt(
            String fieldName, IntFunction<T> ctor, int minVal, int maxVal, int value) {
        if (value < minVal) {
            return Either.left(fieldName + ": Must not be less than " + minVal);
        }
        if (value > maxVal) {
            return Either.left(fieldName + ": Must not be greater than " + maxVal);
        }
        return Either.right(ctor.apply(value));
    }

    public static <T> Either<String, T> createDecimal(
            String fieldName, Function<BigDecimal, T> ctor, BigDecimal minVal, BigDecimal maxVal, BigDecimal value) {
        if (value.compareTo(minVal) < 0) {
            return Either.left(fieldName + ": Must not be less than " + minVal);
        }
        if (value.compareTo(maxVal) > 0) {
            return Either.left(fieldName + ": Must not be greater than " + maxVal);
        }
        return Either.right(ctor.apply(value));
    }
}
