package com.example.fdmj.adapter.in.web;

import com.example.fdmj.adapter.in.web.dto.OrderFormDto;
import com.example.fdmj.adapter.in.web.dto.PlaceOrderErrorDto;
import com.example.fdmj.adapter.in.web.dto.PlaceOrderEventDto;
import com.example.fdmj.application.port.in.PlaceOrderUseCase;
import com.example.fdmj.domain.model.order.error.PlaceOrderError;
import com.example.fdmj.domain.model.order.error.PricingError;
import com.example.fdmj.domain.model.order.error.RemoteServiceError;
import com.example.fdmj.domain.model.order.error.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 注文確定ワークフローの JSON API (書籍 §11 の PlaceOrder.Api 相当)。
/// HttpRequest(JSON) → OrderFormDto → UnvalidatedOrder → ワークフロー → イベント DTO(JSON)。
@RestController
@RequestMapping("/orders")
public class PlaceOrderController {

    private final PlaceOrderUseCase placeOrder;

    public PlaceOrderController(PlaceOrderUseCase placeOrder) {
        this.placeOrder = placeOrder;
    }

    @PostMapping
    public ResponseEntity<Object> place(@RequestBody OrderFormDto orderForm) {
        return placeOrder.place(orderForm.toUnvalidatedOrder()).<ResponseEntity<Object>>fold(
                // 失敗: エラー DTO と対応する HTTP ステータス
                error -> ResponseEntity.status(httpStatus(error))
                        .body(PlaceOrderErrorDto.fromDomain(error)),
                // 成功: イベント配列 (配列の要素型は保持されるので @JsonTypeInfo の type が出力される)
                events -> ResponseEntity.ok(
                        events.map(PlaceOrderEventDto::fromDomain)
                                .toJavaArray(PlaceOrderEventDto[]::new)));
    }

    private static HttpStatus httpStatus(PlaceOrderError error) {
        return switch (error) {
            case ValidationError ignored -> HttpStatus.BAD_REQUEST;            // 400
            case PricingError ignored -> HttpStatus.UNPROCESSABLE_ENTITY;      // 422
            case RemoteServiceError ignored -> HttpStatus.BAD_GATEWAY;         // 502
        };
    }
}
