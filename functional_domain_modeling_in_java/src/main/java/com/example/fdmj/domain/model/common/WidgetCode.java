package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.util.regex.Pattern;

public record WidgetCode(String value) implements ProductCode {

    private static final Pattern PATTERN = Pattern.compile("W\\d{4}");

    public static Either<String, WidgetCode> create(String fieldName, String value) {
        return ConstrainedType.createLike(fieldName, WidgetCode::new, PATTERN, value);
    }
}
