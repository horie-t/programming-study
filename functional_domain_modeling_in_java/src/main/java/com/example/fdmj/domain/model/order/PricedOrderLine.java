package com.example.fdmj.domain.model.order;

import com.example.fdmj.domain.model.common.OrderLineId;
import com.example.fdmj.domain.model.common.OrderQuantity;
import com.example.fdmj.domain.model.common.Price;
import com.example.fdmj.domain.model.common.ProductCode;

public record PricedOrderLine(
        OrderLineId orderLineId,
        ProductCode productCode,
        OrderQuantity quantity,
        Price linePrice) {}
