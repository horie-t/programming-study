package com.example.fdmj.domain.model.order.error;

public record RemoteServiceError(ServiceInfo service, Throwable exception) implements PlaceOrderError {}
