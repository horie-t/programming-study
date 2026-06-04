package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderQuantityTest {

    @Test
    void WidgetCodeに対してはUnitQuantityになる() {
        ProductCode widget = ProductCode.create("PC", "W1234").get();

        Either<String, OrderQuantity> result =
                OrderQuantity.create("Qty", widget, new BigDecimal("5"));

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isInstanceOf(UnitQuantity.class);
        assertThat(result.get().amount()).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void GizmoCodeに対してはKilogramQuantityになる() {
        ProductCode gizmo = ProductCode.create("PC", "G123").get();

        Either<String, OrderQuantity> result =
                OrderQuantity.create("Qty", gizmo, new BigDecimal("2.5"));

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isInstanceOf(KilogramQuantity.class);
        assertThat(result.get().amount()).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    @Test
    void Widgetに対する範囲外の数量はLeftを返す() {
        ProductCode widget = ProductCode.create("PC", "W1234").get();

        Either<String, OrderQuantity> result =
                OrderQuantity.create("Qty", widget, new BigDecimal("1001"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("Must not be greater than 1000");
    }

    @Test
    void Gizmoに対する範囲外の数量はLeftを返す() {
        ProductCode gizmo = ProductCode.create("PC", "G123").get();

        Either<String, OrderQuantity> result =
                OrderQuantity.create("Qty", gizmo, new BigDecimal("0.01"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("Must not be less than");
    }
}
