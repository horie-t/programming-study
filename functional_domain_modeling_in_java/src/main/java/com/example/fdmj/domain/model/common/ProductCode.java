package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;

public sealed interface ProductCode permits WidgetCode, GizmoCode {

    String value();

    static Either<String, ProductCode> create(String fieldName, String code) {
        if (code == null || code.isEmpty()) {
            return Either.left(fieldName + ": Must not be null or empty");
        }
        if (code.startsWith("W")) {
            return Either.narrow(WidgetCode.create(fieldName, code));
        }
        if (code.startsWith("G")) {
            return Either.narrow(GizmoCode.create(fieldName, code));
        }
        return Either.left(fieldName + ": Format not recognized '" + code + "'");
    }
}
