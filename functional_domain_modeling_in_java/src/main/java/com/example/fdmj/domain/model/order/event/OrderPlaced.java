package com.example.fdmj.domain.model.order.event;

import com.example.fdmj.domain.model.order.PricedOrder;

public record OrderPlaced(PricedOrder pricedOrder) implements PlaceOrderEvent {}
