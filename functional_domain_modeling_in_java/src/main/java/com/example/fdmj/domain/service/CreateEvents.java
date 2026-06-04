package com.example.fdmj.domain.service;

import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.event.BillableOrderPlaced;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.event.OrderPlaced;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.math.BigDecimal;

public class CreateEvents {

    public List<PlaceOrderEvent> create(
            PricedOrder pricedOrder,
            Option<OrderAcknowledgmentSent> acknowledgmentEvent) {
        return List.<PlaceOrderEvent>empty()
                .appendAll(Option.narrow(acknowledgmentEvent).toList())
                .append(new OrderPlaced(pricedOrder))
                .appendAll(Option.narrow(createBillableEvent(pricedOrder)).toList());
    }

    private static Option<BillableOrderPlaced> createBillableEvent(PricedOrder pricedOrder) {
        if (pricedOrder.amountToBill().value().compareTo(BigDecimal.ZERO) > 0) {
            return Option.some(new BillableOrderPlaced(
                    pricedOrder.orderId(),
                    pricedOrder.billingAddress(),
                    pricedOrder.amountToBill()));
        }
        return Option.none();
    }
}
