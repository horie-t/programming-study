package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.application.port.in.UnvalidatedOrderLine;
import java.math.BigDecimal;

public record OrderFormLineDto(
        String orderLineId,
        String productCode,
        BigDecimal quantity) {

    /// 検証なしの 1:1 コピー。外部からドメインへ取り込むときに使う。
    public UnvalidatedOrderLine toUnvalidatedOrderLine() {
        return new UnvalidatedOrderLine(orderLineId, productCode, quantity);
    }
}
