package com.example.fdmj.domain.model.common;

import io.vavr.control.Either;
import java.math.BigDecimal;

public sealed interface OrderQuantity permits UnitQuantity, KilogramQuantity {

    BigDecimal amount();

    static Either<String, OrderQuantity> create(String fieldName, ProductCode productCode, BigDecimal quantity) {
        return switch (productCode) {
            case WidgetCode ignored ->
                Either.narrow(UnitQuantity.create(fieldName, quantity.intValue()));
            case GizmoCode ignored ->
                Either.narrow(KilogramQuantity.create(fieldName, quantity));
        };
    }
}
