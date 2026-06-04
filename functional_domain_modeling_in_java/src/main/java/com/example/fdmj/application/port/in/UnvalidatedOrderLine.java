package com.example.fdmj.application.port.in;

import java.math.BigDecimal;

public record UnvalidatedOrderLine(
        String orderLineId,
        String productCode,
        BigDecimal quantity) {}
