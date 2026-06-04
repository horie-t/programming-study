package com.example.fdmj.domain.model.order.event;

import com.example.fdmj.domain.model.common.EmailAddress;
import com.example.fdmj.domain.model.common.OrderId;

public record OrderAcknowledgmentSent(
        OrderId orderId,
        EmailAddress emailAddress) implements PlaceOrderEvent {}
