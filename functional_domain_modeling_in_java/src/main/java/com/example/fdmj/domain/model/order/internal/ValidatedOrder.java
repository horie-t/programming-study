package com.example.fdmj.domain.model.order.internal;

import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.OrderId;
import io.vavr.collection.List;

public record ValidatedOrder(
        OrderId orderId,
        CustomerInfo customerInfo,
        Address shippingAddress,
        Address billingAddress,
        List<ValidatedOrderLine> lines) {}
