package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class KilogramQuantityTest {

    @Test
    void 範囲内の小数は有効() {
        Either<String, KilogramQuantity> result =
                KilogramQuantity.create("Kg", new BigDecimal("2.5"));

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().value()).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    @Test
    void 下限の005は有効() {
        Either<String, KilogramQuantity> result =
                KilogramQuantity.create("Kg", new BigDecimal("0.05"));

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void 上限の100は有効() {
        Either<String, KilogramQuantity> result =
                KilogramQuantity.create("Kg", new BigDecimal("100"));

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void 下限未満はLeftを返す() {
        Either<String, KilogramQuantity> result =
                KilogramQuantity.create("Kg", new BigDecimal("0.04"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("Must not be less than");
    }

    @Test
    void 上限超過はLeftを返す() {
        Either<String, KilogramQuantity> result =
                KilogramQuantity.create("Kg", new BigDecimal("100.01"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("Must not be greater than");
    }

    @Test
    void amountは元の値を返す() {
        KilogramQuantity q = KilogramQuantity.create("Kg", new BigDecimal("2.5")).get();

        assertThat(q.amount()).isEqualByComparingTo(new BigDecimal("2.5"));
    }
}
