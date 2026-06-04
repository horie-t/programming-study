package com.example.fdmj.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.application.port.out.CreateOrderAcknowledgmentLetter;
import com.example.fdmj.application.port.out.SendOrderAcknowledgment;
import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.BillingAmount;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.EmailAddress;
import com.example.fdmj.domain.model.common.OrderId;
import com.example.fdmj.domain.model.common.PersonalName;
import com.example.fdmj.domain.model.common.String50;
import com.example.fdmj.domain.model.common.ZipCode;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.event.OrderAcknowledgmentSent;
import com.example.fdmj.domain.model.order.internal.HtmlString;
import com.example.fdmj.domain.model.order.internal.NotSent;
import com.example.fdmj.domain.model.order.internal.OrderAcknowledgment;
import com.example.fdmj.domain.model.order.internal.Sent;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AcknowledgeOrderTest {

    private static final CreateOrderAcknowledgmentLetter LETTER =
            order -> new HtmlString("<html>order " + order.orderId().value() + "</html>");

    @Test
    void 送信成功ならOrderAcknowledgmentSentイベントを返す() {
        SendOrderAcknowledgment ok = ack -> new Sent();
        AcknowledgeOrder acker = new AcknowledgeOrder(LETTER, ok);

        Option<OrderAcknowledgmentSent> result = acker.acknowledge(samplePricedOrder());

        assertThat(result.isDefined()).isTrue();
        assertThat(result.get().orderId().value()).isEqualTo("ORD-001");
        assertThat(result.get().emailAddress().value()).isEqualTo("taro@example.com");
    }

    @Test
    void 送信失敗ならNoneを返す() {
        SendOrderAcknowledgment fail = ack -> new NotSent();
        AcknowledgeOrder acker = new AcknowledgeOrder(LETTER, fail);

        Option<OrderAcknowledgmentSent> result = acker.acknowledge(samplePricedOrder());

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void 送信時には注文者のメールアドレスとレターが渡される() {
        AtomicReference<OrderAcknowledgment> captured = new AtomicReference<>();
        SendOrderAcknowledgment capture = ack -> {
            captured.set(ack);
            return new Sent();
        };
        AcknowledgeOrder acker = new AcknowledgeOrder(LETTER, capture);

        acker.acknowledge(samplePricedOrder());

        assertThat(captured.get().emailAddress().value()).isEqualTo("taro@example.com");
        assertThat(captured.get().letter().value()).contains("ORD-001");
    }

    // ---- fixtures ----

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
                customerInfo, address, address,
                BillingAmount.create(new BigDecimal("100")).get(),
                List.empty());
    }
}
