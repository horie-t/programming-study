package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.domain.model.order.error.PlaceOrderError;
import com.example.fdmj.domain.model.order.error.PricingError;
import com.example.fdmj.domain.model.order.error.RemoteServiceError;
import com.example.fdmj.domain.model.order.error.ValidationError;

public record PlaceOrderErrorDto(String code, String message) {

    public static PlaceOrderErrorDto fromDomain(PlaceOrderError error) {
        return switch (error) {
            case ValidationError e -> new PlaceOrderErrorDto("ValidationError", e.message());
            case PricingError e -> new PlaceOrderErrorDto("PricingError", e.message());
            case RemoteServiceError e -> new PlaceOrderErrorDto(
                    "RemoteServiceError",
                    e.service().name() + ": " + e.exception().getMessage());
        };
    }
}
