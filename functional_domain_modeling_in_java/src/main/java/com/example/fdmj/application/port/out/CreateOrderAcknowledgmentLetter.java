package com.example.fdmj.application.port.out;

import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.internal.HtmlString;

@FunctionalInterface
public interface CreateOrderAcknowledgmentLetter {

    HtmlString create(PricedOrder pricedOrder);
}
