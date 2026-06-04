package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.util.regex.Pattern;

public record ZipCode(String value) {

    private static final Pattern PATTERN = Pattern.compile("\\d{5}");

    public static Either<String, ZipCode> create(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return Either.left(fieldName + ": Must not be null or empty");
        }
        if (!PATTERN.matcher(value).matches()) {
            return Either.left(fieldName + ": '" + value + "' must match the pattern '" + PATTERN.pattern() + "'");
        }
        return Either.right(new ZipCode(value));
    }
}
