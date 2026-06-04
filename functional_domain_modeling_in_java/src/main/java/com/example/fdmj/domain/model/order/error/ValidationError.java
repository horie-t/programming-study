package com.example.fdmj.domain.model.order.error;

public record ValidationError(String message) implements PlaceOrderError {}
