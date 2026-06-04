package com.example.fdmj.application.port.in;

import com.example.fdmj.domain.model.order.error.PlaceOrderError;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import io.vavr.collection.List;
import io.vavr.control.Either;

public interface PlaceOrderUseCase {

    Either<PlaceOrderError, List<PlaceOrderEvent>> place(UnvalidatedOrder order);
}
