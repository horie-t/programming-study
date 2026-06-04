package com.example.fdmj.domain.model.order.internal;

import com.example.fdmj.application.port.in.UnvalidatedAddress;

public record CheckedAddress(UnvalidatedAddress address) {}
