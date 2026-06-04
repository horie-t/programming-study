package com.example.fdmj.application;

import com.example.fdmj.application.port.in.PlaceOrderUseCase;
import com.example.fdmj.application.port.in.UnvalidatedOrder;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.error.PlaceOrderError;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import com.example.fdmj.domain.model.order.internal.ValidatedOrder;
import com.example.fdmj.domain.service.AcknowledgeOrder;
import com.example.fdmj.domain.service.CreateEvents;
import com.example.fdmj.domain.service.PriceOrder;
import com.example.fdmj.domain.service.ValidateOrder;
import io.vavr.collection.List;
import io.vavr.control.Either;

public class PlaceOrderService implements PlaceOrderUseCase {

    private final ValidateOrder validateOrder;
    private final PriceOrder priceOrder;
    private final AcknowledgeOrder acknowledgeOrder;
    private final CreateEvents createEvents;

    public PlaceOrderService(
            ValidateOrder validateOrder,
            PriceOrder priceOrder,
            AcknowledgeOrder acknowledgeOrder,
            CreateEvents createEvents) {
        this.validateOrder = validateOrder;
        this.priceOrder = priceOrder;
        this.acknowledgeOrder = acknowledgeOrder;
        this.createEvents = createEvents;
    }

    @Override
    public Either<PlaceOrderError, List<PlaceOrderEvent>> place(UnvalidatedOrder order) {
        return Either.<PlaceOrderError, ValidatedOrder>narrow(validateOrder.validate(order))
                .flatMap(validated -> Either.<PlaceOrderError, PricedOrder>narrow(priceOrder.price(validated))
                        .map(priced -> createEvents.create(priced, acknowledgeOrder.acknowledge(priced))));
    }
}
