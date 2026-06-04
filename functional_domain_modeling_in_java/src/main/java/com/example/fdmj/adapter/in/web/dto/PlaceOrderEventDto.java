package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.domain.model.order.event.BillableOrderPlaced;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.event.OrderPlaced;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/// PlaceOrderEvent の発信用 DTO。
/// JSON には判別子フィールド `type` が付与され、値はイベント種別名になる。
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public sealed interface PlaceOrderEventDto
        permits OrderPlacedDto, BillableOrderPlacedDto, OrderAcknowledgmentSentDto {

    /// ドメインのイベントを対応する DTO に変換する。
    static PlaceOrderEventDto fromDomain(PlaceOrderEvent event) {
        return switch (event) {
            case OrderPlaced e -> OrderPlacedDto.fromDomain(e);
            case BillableOrderPlaced e -> BillableOrderPlacedDto.fromDomain(e);
            case OrderAcknowledgmentSent e -> OrderAcknowledgmentSentDto.fromDomain(e);
        };
    }
}
