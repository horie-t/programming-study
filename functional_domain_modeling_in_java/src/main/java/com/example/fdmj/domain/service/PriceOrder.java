package com.example.fdmj.domain.service;

import com.example.fdmj.application.port.out.GetProductPrice;
import com.example.fdmj.domain.model.common.BillingAmount;
import com.example.fdmj.domain.model.common.Price;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.PricedOrderLine;
import com.example.fdmj.domain.model.order.error.PricingError;
import com.example.fdmj.domain.model.order.internal.ValidatedOrder;
import com.example.fdmj.domain.model.order.internal.ValidatedOrderLine;
import io.vavr.collection.List;
import io.vavr.control.Either;
import java.math.BigDecimal;

public class PriceOrder {

    private final GetProductPrice getProductPrice;

    public PriceOrder(GetProductPrice getProductPrice) {
        this.getProductPrice = getProductPrice;
    }

    public Either<PricingError, PricedOrder> price(ValidatedOrder validated) {
        return sequenceLines(validated.lines().map(this::toPricedOrderLine))
                .flatMap(lines -> BillingAmount
                        .sumPrices(lines.map(PricedOrderLine::linePrice))
                        .mapLeft(PricingError::new)
                        .map(amount -> new PricedOrder(
                                validated.orderId(),
                                validated.customerInfo(),
                                validated.shippingAddress(),
                                validated.billingAddress(),
                                amount,
                                lines)));
    }

    private Either<PricingError, PricedOrderLine> toPricedOrderLine(ValidatedOrderLine line) {
        Price unitPrice = getProductPrice.getPrice(line.productCode());
        BigDecimal qty = line.quantity().amount();
        return unitPrice.multiply(qty)
                .mapLeft(PricingError::new)
                .map(linePrice -> new PricedOrderLine(
                        line.orderLineId(),
                        line.productCode(),
                        line.quantity(),
                        linePrice));
    }

    private static <L, R> Either<L, List<R>> sequenceLines(List<Either<L, R>> eithers) {
        return eithers.foldRight(
                Either.<L, List<R>>right(List.empty()),
                (e, acc) -> e.flatMap(r -> acc.map(rs -> rs.prepend(r)))
        );
    }
}
