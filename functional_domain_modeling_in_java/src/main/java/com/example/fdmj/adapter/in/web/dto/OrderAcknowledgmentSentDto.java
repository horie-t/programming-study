package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.fasterxml.jackson.annotation.JsonTypeName;

/// 他の境界づけられたコンテキストへ送るイベント。
@JsonTypeName("OrderAcknowledgmentSent")
public record OrderAcknowledgmentSentDto(
        String orderId,
        String emailAddress) implements PlaceOrderEventDto {

    public static OrderAcknowledgmentSentDto fromDomain(OrderAcknowledgmentSent event) {
        return new OrderAcknowledgmentSentDto(
                event.orderId().value(),
                event.emailAddress().value());
    }
}
