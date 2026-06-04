package com.example.fdmj.domain.model.order.event;

import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.BillingAmount;
import com.example.fdmj.domain.model.common.OrderId;

public record BillableOrderPlaced(
        OrderId orderId,
        Address billingAddress,
        BillingAmount amountToBill) implements PlaceOrderEvent {}
