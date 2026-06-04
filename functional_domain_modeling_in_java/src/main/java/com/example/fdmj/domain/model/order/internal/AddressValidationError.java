package com.example.fdmj.domain.model.order.internal;

public sealed interface AddressValidationError permits InvalidFormat, AddressNotFound {}
