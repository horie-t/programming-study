package com.example.fdmj.application.port.in;

import io.vavr.collection.List;

public record UnvalidatedOrder(
        String orderId,
        UnvalidatedCustomerInfo customerInfo,
        UnvalidatedAddress shippingAddress,
        UnvalidatedAddress billingAddress,
        List<UnvalidatedOrderLine> lines) {}
