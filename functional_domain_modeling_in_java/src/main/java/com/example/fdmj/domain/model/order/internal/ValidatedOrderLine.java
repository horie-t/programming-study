package com.example.fdmj.domain.model.order.internal;

import com.example.fdmj.domain.model.common.OrderLineId;
import com.example.fdmj.domain.model.common.OrderQuantity;
import com.example.fdmj.domain.model.common.ProductCode;

public record ValidatedOrderLine(
        OrderLineId orderLineId,
        ProductCode productCode,
        OrderQuantity quantity) {}
