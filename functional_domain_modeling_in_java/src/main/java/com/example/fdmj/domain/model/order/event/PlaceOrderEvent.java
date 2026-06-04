package com.example.fdmj.domain.model.order.event;

public sealed interface PlaceOrderEvent
        permits OrderPlaced, BillableOrderPlaced, OrderAcknowledgmentSent {}
