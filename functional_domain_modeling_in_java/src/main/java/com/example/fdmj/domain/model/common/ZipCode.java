package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.util.regex.Pattern;

public record ZipCode(String value) {

    private static final Pattern PATTERN = Pattern.compile("\\d{5}");

    public static Either<String, ZipCode> create(String fieldName, String value) {
        return ConstrainedType.createLike(fieldName, ZipCode::new, PATTERN, value);
    }
}
