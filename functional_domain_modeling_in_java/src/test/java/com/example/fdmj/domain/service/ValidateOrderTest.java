package com.example.fdmj.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.application.port.in.UnvalidatedAddress;
import com.example.fdmj.application.port.in.UnvalidatedCustomerInfo;
import com.example.fdmj.application.port.in.UnvalidatedOrder;
import com.example.fdmj.application.port.in.UnvalidatedOrderLine;
import com.example.fdmj.application.port.out.CheckAddressExists;
import com.example.fdmj.application.port.out.CheckProductCodeExists;
import com.example.fdmj.domain.model.common.GizmoCode;
import com.example.fdmj.domain.model.common.WidgetCode;
import com.example.fdmj.domain.model.order.error.ValidationError;
import com.example.fdmj.domain.model.order.internal.AddressNotFound;
import com.example.fdmj.domain.model.order.internal.CheckedAddress;
import com.example.fdmj.domain.model.order.internal.InvalidFormat;
import com.example.fdmj.domain.model.order.internal.ValidatedOrder;
import io.vavr.collection.List;
import io.vavr.control.Either;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ValidateOrderTest {

    private static final CheckProductCodeExists ALL_PRODUCTS_OK = pc -> true;
    private static final CheckAddressExists ADDRESS_OK = addr -> Either.right(new CheckedAddress(addr));

    private final ValidateOrder validator = new ValidateOrder(ALL_PRODUCTS_OK, ADDRESS_OK);

    @Test
    void 妥当な注文が検証を通る() {
        UnvalidatedOrder order = sampleOrder();

        Either<ValidationError, ValidatedOrder> result = validator.validate(order);

        assertThat(result.isRight()).isTrue();
        ValidatedOrder v = result.get();
        assertThat(v.orderId().value()).isEqualTo("ORD-001");
        assertThat(v.customerInfo().emailAddress().value()).isEqualTo("taro@example.com");
        assertThat(v.lines()).hasSize(2);
        assertThat(v.lines().get(0).productCode()).isInstanceOf(WidgetCode.class);
        assertThat(v.lines().get(1).productCode()).isInstanceOf(GizmoCode.class);
    }

    @Test
    void OrderIdが空ならValidationError() {
        UnvalidatedOrder order = sampleOrderBuilder().withOrderId("").build();

        Either<ValidationError, ValidatedOrder> result = validator.validate(order);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("OrderId");
    }

    @Test
    void メールアドレスが不正ならValidationError() {
        UnvalidatedOrder order = sampleOrderBuilder().withEmail("not-an-email").build();

        Either<ValidationError, ValidatedOrder> result = validator.validate(order);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("EmailAddress");
    }

    @Test
    void 商品コードのフォーマットが不正ならValidationError() {
        UnvalidatedOrderLine bad = new UnvalidatedOrderLine("LINE-1", "X9999", new BigDecimal("5"));
        UnvalidatedOrder order = sampleOrderBuilder().withLines(List.of(bad)).build();

        Either<ValidationError, ValidatedOrder> result = validator.validate(order);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("ProductCode");
    }

    @Test
    void 商品コードがカタログに存在しないならValidationError() {
        ValidateOrder rejectingValidator = new ValidateOrder(pc -> false, ADDRESS_OK);

        Either<ValidationError, ValidatedOrder> result = rejectingValidator.validate(sampleOrder());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).startsWith("Invalid: ");
    }

    @Test
    void Widgetへの数量が範囲外ならValidationError() {
        UnvalidatedOrderLine outOfRange = new UnvalidatedOrderLine("LINE-1", "W1234", new BigDecimal("2000"));
        UnvalidatedOrder order = sampleOrderBuilder().withLines(List.of(outOfRange)).build();

        Either<ValidationError, ValidatedOrder> result = validator.validate(order);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).contains("OrderQuantity");
    }

    @Test
    void 配送先住所がAddressNotFoundならValidationErrorに変換される() {
        CheckAddressExists notFound = addr -> Either.left(new AddressNotFound());
        ValidateOrder v = new ValidateOrder(ALL_PRODUCTS_OK, notFound);

        Either<ValidationError, ValidatedOrder> result = v.validate(sampleOrder());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).isEqualTo("Address not found");
    }

    @Test
    void 配送先住所がInvalidFormatならValidationErrorに変換される() {
        CheckAddressExists invalid = addr -> Either.left(new InvalidFormat());
        ValidateOrder v = new ValidateOrder(ALL_PRODUCTS_OK, invalid);

        Either<ValidationError, ValidatedOrder> result = v.validate(sampleOrder());

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft().message()).isEqualTo("Address has bad format");
    }

    @Test
    void 行の途中でエラーがあれば全体がLeftになる() {
        UnvalidatedOrderLine good = new UnvalidatedOrderLine("LINE-1", "W1234", new BigDecimal("5"));
        UnvalidatedOrderLine bad = new UnvalidatedOrderLine("LINE-2", "BAD", new BigDecimal("5"));
        UnvalidatedOrder order = sampleOrderBuilder().withLines(List.of(good, bad)).build();

        Either<ValidationError, ValidatedOrder> result = validator.validate(order);

        assertThat(result.isLeft()).isTrue();
    }

    // ---- test fixtures ----

    private static UnvalidatedOrder sampleOrder() {
        return sampleOrderBuilder().build();
    }

    private static OrderBuilder sampleOrderBuilder() {
        return new OrderBuilder();
    }

    private static class OrderBuilder {
        private String orderId = "ORD-001";
        private String email = "taro@example.com";
        private List<UnvalidatedOrderLine> lines = List.of(
                new UnvalidatedOrderLine("LINE-1", "W1234", new BigDecimal("5")),
                new UnvalidatedOrderLine("LINE-2", "G123", new BigDecimal("2.5"))
        );

        OrderBuilder withOrderId(String s) { this.orderId = s; return this; }
        OrderBuilder withEmail(String s) { this.email = s; return this; }
        OrderBuilder withLines(List<UnvalidatedOrderLine> l) { this.lines = l; return this; }

        UnvalidatedOrder build() {
            UnvalidatedAddress address = new UnvalidatedAddress(
                    "1-2-3", "", "", "", "Tokyo", "10001");
            UnvalidatedCustomerInfo customer = new UnvalidatedCustomerInfo("Taro", "Yamada", email);
            return new UnvalidatedOrder(orderId, customer, address, address, lines);
        }
    }
}
