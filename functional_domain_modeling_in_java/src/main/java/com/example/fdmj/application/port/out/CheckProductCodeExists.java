package com.example.fdmj.application.port.out;

import com.example.fdmj.domain.model.common.ProductCode;

@FunctionalInterface
public interface CheckProductCodeExists {

    boolean exists(ProductCode productCode);
}
