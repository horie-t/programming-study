package com.example.fdmj.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.application.port.in.UnvalidatedAddress;
import com.example.fdmj.application.port.in.UnvalidatedCustomerInfo;
import com.example.fdmj.application.port.in.UnvalidatedOrder;
import com.example.fdmj.application.port.in.UnvalidatedOrderLine;
import com.example.fdmj.application.port.out.CheckAddressExists;
import com.example.fdmj.application.port.out.CheckProductCodeExists;
import com.example.fdmj.application.port.out.CreateOrderAcknowledgmentLetter;
import com.example.fdmj.application.port.out.GetProductPrice;
import com.example.fdmj.application.port.out.SendOrderAcknowledgment;
import com.example.fdmj.domain.model.common.Price;
import com.example.fdmj.domain.model.order.error.PlaceOrderError;
import com.example.fdmj.domain.model.order.error.PricingError;
import com.example.fdmj.domain.model.order.error.ValidationError;
import com.example.fdmj.domain.model.order.event.BillableOrderPlaced;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.event.OrderPlaced;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import com.example.fdmj.domain.model.order.internal.CheckedAddress;
import com.example.fdmj.domain.model.order.internal.HtmlString;
import com.example.fdmj.domain.model.order.internal.NotSent;
import com.example.fdmj.domain.model.order.internal.Sent;
import com.example.fdmj.domain.service.AcknowledgeOrder;
import com.example.fdmj.domain.service.CreateEvents;
import com.example.fdmj.domain.service.PriceOrder;
import com.example.fdmj.domain.service.ValidateOrder;
import io.vavr.collection.List;
import io.vavr.control.Either;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PlaceOrderServiceTest {

    private static final CheckProductCodeExists ALL_PRODUCTS_OK = pc -> true;
    private static final CheckAddressExists ADDRESS_OK = addr -> Either.right(new CheckedAddress(addr));
    private static final GetProductPrice UNIT_PRICE_100 = pc -> Price.unsafeCreate(new BigDecimal("100"));
    private static final CreateOrderAcknowledgmentLetter DUMMY_LETTER = order -> new HtmlString("letter");
    private static final SendOrderAcknowledgment SEND_OK = ack -> new Sent();
    private static final SendOrderAcknowledgment SEND_FAIL = ack -> new NotSent();

    @Test
    void 妥当な注文がイベント3件を返す() {
        // W1234 を 5 個 × 100 = 500, G123 を 2.5kg × 100 = 250、合計 750 (> 0 → Billable も出る)
        PlaceOrderService service = serviceWithSend(SEND_OK);

        Either<PlaceOrderError, List<PlaceOrderEvent>> result = service.place(sampleOrder());

        assertThat(result.isRight()).isTrue();
        List<PlaceOrderEvent> events = result.get();
        assertThat(events).hasSize(3);
        assertThat(events.exists(e -> e instanceof OrderAcknowledgmentSent)).isTrue();
        assertThat(events.exists(e -> e instanceof OrderPlaced)).isTrue();
        assertThat(events.exists(e -> e instanceof BillableOrderPlaced)).isTrue();
    }

    @Test
    void Acknowledgment送信失敗ならAckイベントが出ない() {
        PlaceOrderService service = serviceWithSend(SEND_FAIL);

        List<PlaceOrderEvent> events = service.place(sampleOrder()).get();

        assertThat(events).hasSize(2);
        assertThat(events.exists(e -> e instanceof OrderAcknowledgmentSent)).isFalse();
        assertThat(events.exists(e -> e instanceof OrderPlaced)).isTrue();
        assertThat(events.exists(e -> e instanceof BillableOrderPlaced)).isTrue();
    }

    @Test
    void Validate段階で失敗するとPlaceOrderErrorがValidationError() {
        UnvalidatedOrder bad = orderWithLines(List.of(
                new UnvalidatedOrderLine("L1", "X9999", new BigDecimal("5"))
        ));
        PlaceOrderService service = serviceWithSend(SEND_OK);

        Either<PlaceOrderError, List<PlaceOrderEvent>> result = service.place(bad);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(ValidationError.class);
    }

    @Test
    void Price段階で失敗するとPlaceOrderErrorがPricingError() {
        // 単価 600 × 5 = 3000 → Price 上限 (1000) 超過
        GetProductPrice highPrice = pc -> Price.unsafeCreate(new BigDecimal("600"));
        PlaceOrderService service = new PlaceOrderService(
                new ValidateOrder(ALL_PRODUCTS_OK, ADDRESS_OK),
                new PriceOrder(highPrice),
                new AcknowledgeOrder(DUMMY_LETTER, SEND_OK),
                new CreateEvents()
        );

        Either<PlaceOrderError, List<PlaceOrderEvent>> result = service.place(sampleOrder());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(PricingError.class);
    }

    // ---- fixtures ----

    private static PlaceOrderService serviceWithSend(SendOrderAcknowledgment send) {
        return new PlaceOrderService(
                new ValidateOrder(ALL_PRODUCTS_OK, ADDRESS_OK),
                new PriceOrder(UNIT_PRICE_100),
                new AcknowledgeOrder(DUMMY_LETTER, send),
                new CreateEvents()
        );
    }

    private static UnvalidatedOrder sampleOrder() {
        return orderWithLines(List.of(
                new UnvalidatedOrderLine("LINE-1", "W1234", new BigDecimal("5")),
                new UnvalidatedOrderLine("LINE-2", "G123", new BigDecimal("2.5"))
        ));
    }

    private static UnvalidatedOrder orderWithLines(List<UnvalidatedOrderLine> lines) {
        UnvalidatedAddress address = new UnvalidatedAddress("1-2-3", "", "", "", "Tokyo", "10001");
        UnvalidatedCustomerInfo customer = new UnvalidatedCustomerInfo("Taro", "Yamada", "taro@example.com");
        return new UnvalidatedOrder("ORD-001", customer, address, address, lines);
    }
}
