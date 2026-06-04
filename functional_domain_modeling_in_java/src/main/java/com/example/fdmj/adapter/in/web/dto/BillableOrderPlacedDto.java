package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.domain.model.order.event.BillableOrderPlaced;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigDecimal;

/// 請求コンテキストへ送るイベント。
@JsonTypeName("BillableOrderPlaced")
public record BillableOrderPlacedDto(
        String orderId,
        AddressDto billingAddress,
        BigDecimal amountToBill) implements PlaceOrderEventDto {

    public static BillableOrderPlacedDto fromDomain(BillableOrderPlaced event) {
        return new BillableOrderPlacedDto(
                event.orderId().value(),
                AddressDto.fromDomain(event.billingAddress()),
                event.amountToBill().value());
    }
}
