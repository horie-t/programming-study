package com.example.fdmj.application.port.out;

import com.example.fdmj.domain.model.common.Price;
import com.example.fdmj.domain.model.common.ProductCode;

@FunctionalInterface
public interface GetProductPrice {

    Price getPrice(ProductCode productCode);
}
