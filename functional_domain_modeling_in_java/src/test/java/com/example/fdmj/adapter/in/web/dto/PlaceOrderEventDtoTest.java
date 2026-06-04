package com.example.fdmj.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.BillingAmount;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.EmailAddress;
import com.example.fdmj.domain.model.common.OrderId;
import com.example.fdmj.domain.model.common.OrderLineId;
import com.example.fdmj.domain.model.common.OrderQuantity;
import com.example.fdmj.domain.model.common.PersonalName;
import com.example.fdmj.domain.model.common.Price;
import com.example.fdmj.domain.model.common.ProductCode;
import com.example.fdmj.domain.model.common.String50;
import com.example.fdmj.domain.model.common.ZipCode;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.PricedOrderLine;
import com.example.fdmj.domain.model.order.event.BillableOrderPlaced;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.event.OrderPlaced;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class PlaceOrderEventDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void OrderPlacedをDTOに変換しJSONにシリアライズできる() {
        PlaceOrderEvent event = new OrderPlaced(samplePricedOrder(new BigDecimal("750")));

        PlaceOrderEventDto dto = PlaceOrderEventDto.fromDomain(event);
        String json = mapper.writeValueAsString(dto);

        assertThat(dto).isInstanceOf(OrderPlacedDto.class);
        assertThat(json).contains("\"type\":\"OrderPlaced\"");
        assertThat(json).contains("\"orderId\":\"ORD-001\"");
        assertThat(json).contains("\"amountToBill\":750");
        assertThat(json).contains("\"productCode\":\"W1234\"");
    }

    @Test
    void BillableOrderPlacedをDTOに変換しJSONにシリアライズできる() {
        PricedOrder o = samplePricedOrder(new BigDecimal("750"));
        PlaceOrderEvent event = new BillableOrderPlaced(o.orderId(), o.billingAddress(), o.amountToBill());

        PlaceOrderEventDto dto = PlaceOrderEventDto.fromDomain(event);
        String json = mapper.writeValueAsString(dto);

        assertThat(dto).isInstanceOf(BillableOrderPlacedDto.class);
        assertThat(json).contains("\"type\":\"BillableOrderPlaced\"");
        assertThat(json).contains("\"amountToBill\":750");
    }

    @Test
    void OrderAcknowledgmentSentをDTOに変換しJSONにシリアライズできる() {
        PricedOrder o = samplePricedOrder(new BigDecimal("100"));
        PlaceOrderEvent event = new OrderAcknowledgmentSent(o.orderId(), o.customerInfo().emailAddress());

        PlaceOrderEventDto dto = PlaceOrderEventDto.fromDomain(event);
        String json = mapper.writeValueAsString(dto);

        assertThat(dto).isInstanceOf(OrderAcknowledgmentSentDto.class);
        assertThat(json).contains("\"type\":\"OrderAcknowledgmentSent\"");
        assertThat(json).contains("\"emailAddress\":\"taro@example.com\"");
    }

    @Test
    void イベントのリストを混在させてJSON配列にできる() {
        PricedOrder o = samplePricedOrder(new BigDecimal("750"));
        java.util.List<PlaceOrderEventDto> dtos = java.util.List.of(
                PlaceOrderEventDto.fromDomain(new OrderAcknowledgmentSent(o.orderId(), o.customerInfo().emailAddress())),
                PlaceOrderEventDto.fromDomain(new OrderPlaced(o)),
                PlaceOrderEventDto.fromDomain(new BillableOrderPlaced(o.orderId(), o.billingAddress(), o.amountToBill())));

        // 要素型を明示してシリアライズ (Spring の @RestController は戻り値型のジェネリクスを保持するため
        // 実際の API でも判別子フィールドが出力される)。
        String json = mapper.writerFor(new TypeReference<java.util.List<PlaceOrderEventDto>>() {})
                .writeValueAsString(dtos);

        assertThat(json).contains("\"type\":\"OrderAcknowledgmentSent\"");
        assertThat(json).contains("\"type\":\"OrderPlaced\"");
        assertThat(json).contains("\"type\":\"BillableOrderPlaced\"");
    }

    @Test
    void 任意項目がNoneの住所はJSONでnullになる() {
        PlaceOrderEvent event = new OrderPlaced(samplePricedOrder(new BigDecimal("100")));

        String json = mapper.writeValueAsString(PlaceOrderEventDto.fromDomain(event));

        // shippingAddress の addressLine2 は None → null
        assertThat(json).contains("\"addressLine2\":null");
    }

    // ---- fixtures ----

    private static PricedOrder samplePricedOrder(BigDecimal amount) {
        Address address = new Address(
                String50.create("AddressLine1", "1-2-3").get(),
                Option.none(), Option.none(), Option.none(),
                String50.create("City", "Tokyo").get(),
                ZipCode.create("ZipCode", "10001").get());
        CustomerInfo customerInfo = new CustomerInfo(
                new PersonalName(
                        String50.create("FirstName", "Taro").get(),
                        String50.create("LastName", "Yamada").get()),
                EmailAddress.create("Email", "taro@example.com").get());
        ProductCode pc = ProductCode.create("ProductCode", "W1234").get();
        PricedOrderLine line = new PricedOrderLine(
                OrderLineId.create("OrderLineId", "L1").get(),
                pc,
                OrderQuantity.create("Qty", pc, new BigDecimal("5")).get(),
                Price.unsafeCreate(new BigDecimal("150")));
        return new PricedOrder(
                OrderId.create("OrderId", "ORD-001").get(),
                customerInfo, address, address,
                BillingAmount.create(amount).get(),
                List.of(line));
    }
}
