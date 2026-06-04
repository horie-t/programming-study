package com.example.fdmj.domain.model.order.error;

public sealed interface PlaceOrderError
        permits ValidationError, PricingError, RemoteServiceError {}
