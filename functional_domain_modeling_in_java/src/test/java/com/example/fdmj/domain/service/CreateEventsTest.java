package com.example.fdmj.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.BillingAmount;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.EmailAddress;
import com.example.fdmj.domain.model.common.OrderId;
import com.example.fdmj.domain.model.common.PersonalName;
import com.example.fdmj.domain.model.common.String50;
import com.example.fdmj.domain.model.common.ZipCode;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.event.BillableOrderPlaced;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.event.OrderPlaced;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CreateEventsTest {

    private final CreateEvents creator = new CreateEvents();

    @Test
    void 金額有りでack有りなら3イベントを出力する() {
        PricedOrder order = pricedOrder(new BigDecimal("500"));
        Option<OrderAcknowledgmentSent> ack = Option.some(new OrderAcknowledgmentSent(
                order.orderId(), order.customerInfo().emailAddress()));

        List<PlaceOrderEvent> events = creator.create(order, ack);

        assertThat(events).hasSize(3);
        assertThat(events.get(0)).isInstanceOf(OrderAcknowledgmentSent.class);
        assertThat(events.get(1)).isInstanceOf(OrderPlaced.class);
        assertThat(events.get(2)).isInstanceOf(BillableOrderPlaced.class);
    }

    @Test
    void 金額有りでackが無ければOrderPlacedとBillableのみ() {
        PricedOrder order = pricedOrder(new BigDecimal("500"));

        List<PlaceOrderEvent> events = creator.create(order, Option.none());

        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isInstanceOf(OrderPlaced.class);
        assertThat(events.get(1)).isInstanceOf(BillableOrderPlaced.class);
    }

    @Test
    void 金額ゼロでack有りならOrderPlacedとack送信のみで請求は出ない() {
        PricedOrder order = pricedOrder(BigDecimal.ZERO);
        Option<OrderAcknowledgmentSent> ack = Option.some(new OrderAcknowledgmentSent(
                order.orderId(), order.customerInfo().emailAddress()));

        List<PlaceOrderEvent> events = creator.create(order, ack);

        assertThat(events).hasSize(2);
        assertThat(events.get(0)).isInstanceOf(OrderAcknowledgmentSent.class);
        assertThat(events.get(1)).isInstanceOf(OrderPlaced.class);
        assertThat(events.exists(e -> e instanceof BillableOrderPlaced)).isFalse();
    }

    @Test
    void 金額ゼロでackなしならOrderPlacedのみ() {
        PricedOrder order = pricedOrder(BigDecimal.ZERO);

        List<PlaceOrderEvent> events = creator.create(order, Option.none());

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderPlaced.class);
    }

    @Test
    void BillableOrderPlacedはOrderIdと請求先住所と金額を持つ() {
        PricedOrder order = pricedOrder(new BigDecimal("750"));

        List<PlaceOrderEvent> events = creator.create(order, Option.none());
        BillableOrderPlaced billable = (BillableOrderPlaced) events
                .filter(e -> e instanceof BillableOrderPlaced).get(0);

        assertThat(billable.orderId()).isEqualTo(order.orderId());
        assertThat(billable.billingAddress()).isEqualTo(order.billingAddress());
        assertThat(billable.amountToBill().value()).isEqualByComparingTo(new BigDecimal("750"));
    }

    // ---- fixtures ----

    private static PricedOrder pricedOrder(BigDecimal amount) {
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
        return new PricedOrder(
                OrderId.create("OrderId", "ORD-001").get(),
                customerInfo, address, address,
                BillingAmount.create(amount).get(),
                List.empty());
    }
}
