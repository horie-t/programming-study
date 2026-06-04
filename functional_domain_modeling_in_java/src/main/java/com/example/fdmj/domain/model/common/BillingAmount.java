package com.example.fdmj.domain.model.common;

import io.vavr.collection.List;
import io.vavr.control.Either;
import java.math.BigDecimal;

public record BillingAmount(BigDecimal value) {

    private static final BigDecimal MIN = BigDecimal.ZERO;
    private static final BigDecimal MAX = new BigDecimal("10000");

    public static Either<String, BillingAmount> create(BigDecimal value) {
        return ConstrainedType.createDecimal("BillingAmount", BillingAmount::new, MIN, MAX, value);
    }

    public static Either<String, BillingAmount> sumPrices(List<Price> prices) {
        BigDecimal total = prices.foldLeft(BigDecimal.ZERO, (acc, p) -> acc.add(p.value()));
        return create(total);
    }
}
