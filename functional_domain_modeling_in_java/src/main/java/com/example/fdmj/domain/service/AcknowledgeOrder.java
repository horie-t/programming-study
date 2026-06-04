package com.example.fdmj.domain.service;

import com.example.fdmj.application.port.out.CreateOrderAcknowledgmentLetter;
import com.example.fdmj.application.port.out.SendOrderAcknowledgment;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.internal.HtmlString;
import com.example.fdmj.domain.model.order.internal.NotSent;
import com.example.fdmj.domain.model.order.internal.OrderAcknowledgment;
import com.example.fdmj.domain.model.order.internal.Sent;
import io.vavr.control.Option;

public class AcknowledgeOrder {

    private final CreateOrderAcknowledgmentLetter createLetter;
    private final SendOrderAcknowledgment sendAcknowledgment;

    public AcknowledgeOrder(
            CreateOrderAcknowledgmentLetter createLetter,
            SendOrderAcknowledgment sendAcknowledgment) {
        this.createLetter = createLetter;
        this.sendAcknowledgment = sendAcknowledgment;
    }

    public Option<OrderAcknowledgmentSent> acknowledge(PricedOrder pricedOrder) {
        HtmlString letter = createLetter.create(pricedOrder);
        OrderAcknowledgment ack = new OrderAcknowledgment(
                pricedOrder.customerInfo().emailAddress(), letter);
        return switch (sendAcknowledgment.send(ack)) {
            case Sent ignored -> Option.some(new OrderAcknowledgmentSent(
                    pricedOrder.orderId(), pricedOrder.customerInfo().emailAddress()));
            case NotSent ignored -> Option.none();
        };
    }
}
