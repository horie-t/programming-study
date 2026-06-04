package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.event.OrderPlaced;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigDecimal;

/// 発送コンテキストへ送るイベント。
@JsonTypeName("OrderPlaced")
public record OrderPlacedDto(
        String orderId,
        CustomerInfoDto customerInfo,
        AddressDto shippingAddress,
        AddressDto billingAddress,
        BigDecimal amountToBill,
        java.util.List<PricedOrderLineDto> lines) implements PlaceOrderEventDto {

    public static OrderPlacedDto fromDomain(OrderPlaced event) {
        PricedOrder o = event.pricedOrder();
        return new OrderPlacedDto(
                o.orderId().value(),
                CustomerInfoDto.fromDomain(o.customerInfo()),
                AddressDto.fromDomain(o.shippingAddress()),
                AddressDto.fromDomain(o.billingAddress()),
                o.amountToBill().value(),
                o.lines().map(PricedOrderLineDto::fromDomain).toJavaList());
    }
}
