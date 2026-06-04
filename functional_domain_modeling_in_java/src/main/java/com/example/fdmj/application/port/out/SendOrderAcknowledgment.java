package com.example.fdmj.application.port.out;

import com.example.fdmj.domain.model.order.internal.OrderAcknowledgment;
import com.example.fdmj.domain.model.order.internal.SendResult;

@FunctionalInterface
public interface SendOrderAcknowledgment {

    SendResult send(OrderAcknowledgment acknowledgment);
}
