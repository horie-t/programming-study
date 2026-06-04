package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.util.regex.Pattern;

public record GizmoCode(String value) implements ProductCode {

    private static final Pattern PATTERN = Pattern.compile("G\\d{3}");

    public static Either<String, GizmoCode> create(String fieldName, String value) {
        return ConstrainedType.createLike(fieldName, GizmoCode::new, PATTERN, value);
    }
}
