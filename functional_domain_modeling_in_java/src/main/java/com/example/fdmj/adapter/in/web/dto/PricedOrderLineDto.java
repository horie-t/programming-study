package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.domain.model.order.PricedOrderLine;
import java.math.BigDecimal;

public record PricedOrderLineDto(
        String orderLineId,
        String productCode,
        BigDecimal quantity,
        BigDecimal linePrice) {

    /// ドメインオブジェクトから DTO へ。外部へ書き出すときに使う。
    public static PricedOrderLineDto fromDomain(PricedOrderLine domain) {
        return new PricedOrderLineDto(
                domain.orderLineId().value(),
                domain.productCode().value(),
                domain.quantity().amount(),
                domain.linePrice().value());
    }
}
