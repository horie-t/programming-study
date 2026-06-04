package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern PATTERN = Pattern.compile(".+@.+");

    public static Either<String, EmailAddress> create(String fieldName, String value) {
        return ConstrainedType.createLike(fieldName, EmailAddress::new, PATTERN, value);
    }
}
