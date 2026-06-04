package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.application.port.in.UnvalidatedOrder;
import io.vavr.collection.List;

public record OrderFormDto(
        String orderId,
        CustomerInfoDto customerInfo,
        AddressDto shippingAddress,
        AddressDto billingAddress,
        java.util.List<OrderFormLineDto> lines) {

    /// 検証なしの 1:1 コピー。外部からドメインへ取り込むときに使う。
    public UnvalidatedOrder toUnvalidatedOrder() {
        return new UnvalidatedOrder(
                orderId,
                customerInfo.toUnvalidatedCustomerInfo(),
                shippingAddress.toUnvalidatedAddress(),
                billingAddress.toUnvalidatedAddress(),
                List.ofAll(lines).map(OrderFormLineDto::toUnvalidatedOrderLine));
    }
}
