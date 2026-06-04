package com.example.fdmj.domain.model.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.BillingAmount;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.EmailAddress;
import com.example.fdmj.domain.model.common.OrderId;
import com.example.fdmj.domain.model.common.PersonalName;
import com.example.fdmj.domain.model.common.String50;
import com.example.fdmj.domain.model.common.ZipCode;
import com.example.fdmj.domain.model.order.error.PlaceOrderError;
import com.example.fdmj.domain.model.order.error.PricingError;
import com.example.fdmj.domain.model.order.error.RemoteServiceError;
import com.example.fdmj.domain.model.order.error.ServiceInfo;
import com.example.fdmj.domain.model.order.error.ValidationError;
import com.example.fdmj.domain.model.order.event.BillableOrderPlaced;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.event.OrderPlaced;
import com.example.fdmj.domain.model.order.event.PlaceOrderEvent;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.math.BigDecimal;
import java.net.URI;
import org.junit.jupiter.api.Test;

class SealedExhaustivenessTest {

    @Test
    void PlaceOrderEventが網羅的にパターンマッチできる() {
        PricedOrder pricedOrder = samplePricedOrder();
        PlaceOrderEvent placedEvent = new OrderPlaced(pricedOrder);
        PlaceOrderEvent billableEvent = new BillableOrderPlaced(
                pricedOrder.orderId(), pricedOrder.billingAddress(), pricedOrder.amountToBill());
        PlaceOrderEvent ackEvent = new OrderAcknowledgmentSent(
                pricedOrder.orderId(), pricedOrder.customerInfo().emailAddress());

        assertThat(label(placedEvent)).startsWith("placed");
        assertThat(label(billableEvent)).startsWith("billable");
        assertThat(label(ackEvent)).startsWith("ack");
    }

    @Test
    void PlaceOrderErrorが網羅的にパターンマッチできる() {
        PlaceOrderError validation = new ValidationError("bad input");
        PlaceOrderError pricing = new PricingError("over limit");
        PlaceOrderError remote = new RemoteServiceError(
                new ServiceInfo("AddressService", URI.create("https://example.com")),
                new RuntimeException("boom"));

        assertThat(errorLabel(validation)).isEqualTo("validation:bad input");
        assertThat(errorLabel(pricing)).isEqualTo("pricing:over limit");
        assertThat(errorLabel(remote)).startsWith("remote:");
    }

    private static String label(PlaceOrderEvent event) {
        return switch (event) {
            case OrderPlaced p -> "placed:" + p.pricedOrder().orderId().value();
            case BillableOrderPlaced b -> "billable:" + b.orderId().value();
            case OrderAcknowledgmentSent a -> "ack:" + a.orderId().value();
        };
    }

    private static String errorLabel(PlaceOrderError error) {
        return switch (error) {
            case ValidationError v -> "validation:" + v.message();
            case PricingError p -> "pricing:" + p.message();
            case RemoteServiceError r -> "remote:" + r.service().name();
        };
    }

    private static PricedOrder samplePricedOrder() {
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
                customerInfo,
                address,
                address,
                BillingAmount.create(new BigDecimal("100")).get(),
                List.empty());
    }
}
