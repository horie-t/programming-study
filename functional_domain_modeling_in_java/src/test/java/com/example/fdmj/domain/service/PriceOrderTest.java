package com.example.fdmj.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.application.port.out.GetProductPrice;
import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.EmailAddress;
import com.example.fdmj.domain.model.common.OrderId;
import com.example.fdmj.domain.model.common.OrderLineId;
import com.example.fdmj.domain.model.common.OrderQuantity;
import com.example.fdmj.domain.model.common.PersonalName;
import com.example.fdmj.domain.model.common.Price;
import com.example.fdmj.domain.model.common.ProductCode;
import com.example.fdmj.domain.model.common.String50;
import com.example.fdmj.domain.model.common.WidgetCode;
import com.example.fdmj.domain.model.common.ZipCode;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.error.PricingError;
import com.example.fdmj.domain.model.order.internal.ValidatedOrder;
import com.example.fdmj.domain.model.order.internal.ValidatedOrderLine;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PriceOrderTest {

    @Test
    void 各行に単価を掛けて合計が算出される() {
        // W1234 を 5 個 × 100 = 500, G123 を 2.5kg × 200 = 500, 合計 1000
        GetProductPrice prices = pc -> switch (pc) {
            case WidgetCode w -> Price.unsafeCreate(new BigDecimal("100"));
            default -> Price.unsafeCreate(new BigDecimal("200"));
        };
        PriceOrder pricer = new PriceOrder(prices);

        Either<PricingError, PricedOrder> result = pricer.price(sampleOrder());

        assertThat(result.isRight()).isTrue();
        PricedOrder po = result.get();
        assertThat(po.amountToBill().value()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(po.lines()).hasSize(2);
        assertThat(po.lines().get(0).linePrice().value()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(po.lines().get(1).linePrice().value()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void OrderIdや顧客情報や住所は素通しされる() {
        GetProductPrice prices = pc -> Price.unsafeCreate(new BigDecimal("10"));
        PriceOrder pricer = new PriceOrder(prices);
        ValidatedOrder validated = sampleOrder();

        PricedOrder priced = pricer.price(validated).get();

        assertThat(priced.orderId()).isEqualTo(validated.orderId());
        assertThat(priced.customerInfo()).isEqualTo(validated.customerInfo());
        assertThat(priced.shippingAddress()).isEqualTo(validated.shippingAddress());
        assertThat(priced.billingAddress()).isEqualTo(validated.billingAddress());
    }

    @Test
    void 単一ラインの金額がPriceの上限を超えるとPricingError() {
        // 単価 600 × 数量 5 = 3000 (Price 上限 1000 を超える)
        GetProductPrice prices = pc -> Price.unsafeCreate(new BigDecimal("600"));
        PriceOrder pricer = new PriceOrder(prices);

        Either<PricingError, PricedOrder> result = pricer.price(sampleOrder());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("Price");
    }

    @Test
    void 合計がBillingAmountの上限を超えるとPricingError() {
        // 各行は Price 上限 (1000) 以内に収め、合計だけが BillingAmount 上限 (10000) を超えるようにする。
        // 単価 1000 × 数量 1 = 1000 (各行 Price 上限ジャスト)、これを 11 行で合計 11000 → BillingAmount 超過。
        List<ValidatedOrderLine> manyLines = List.range(0, 11)
                .map(i -> line("L" + i, "W1234", BigDecimal.ONE));
        ValidatedOrder bigOrder = orderWithLines(manyLines);
        GetProductPrice prices = pc -> Price.unsafeCreate(new BigDecimal("1000"));
        PriceOrder pricer = new PriceOrder(prices);

        Either<PricingError, PricedOrder> result = pricer.price(bigOrder);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("BillingAmount");
    }

    @Test
    void 行が空でも合計0で成立する() {
        ValidatedOrder emptyOrder = orderWithLines(List.empty());
        PriceOrder pricer = new PriceOrder(pc -> Price.unsafeCreate(BigDecimal.ONE));

        Either<PricingError, PricedOrder> result = pricer.price(emptyOrder);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().amountToBill().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get().lines()).isEmpty();
    }

    // ---- fixtures ----

    private static ValidatedOrder sampleOrder() {
        return orderWithLines(List.of(
                line("LINE-1", "W1234", new BigDecimal("5")),
                line("LINE-2", "G123", new BigDecimal("2.5"))
        ));
    }

    private static ValidatedOrderLine line(String lineId, String productCode, BigDecimal qty) {
        ProductCode pc = ProductCode.create("ProductCode", productCode).get();
        return new ValidatedOrderLine(
                OrderLineId.create("OrderLineId", lineId).get(),
                pc,
                OrderQuantity.create("Qty", pc, qty).get()
        );
    }

    private static ValidatedOrder orderWithLines(List<ValidatedOrderLine> lines) {
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
        return new ValidatedOrder(
                OrderId.create("OrderId", "ORD-001").get(),
                customerInfo, address, address, lines);
    }
}
