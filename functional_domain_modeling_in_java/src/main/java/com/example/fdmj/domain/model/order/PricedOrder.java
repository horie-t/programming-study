package com.example.fdmj.domain.model.order;

import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.BillingAmount;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.OrderId;
import io.vavr.collection.List;

public record PricedOrder(
        OrderId orderId,
        CustomerInfo customerInfo,
        Address shippingAddress,
        Address billingAddress,
        BillingAmount amountToBill,
        List<PricedOrderLine> lines) {}
