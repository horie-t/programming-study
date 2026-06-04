package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UnitQuantityTest {

    @Test
    void 範囲内の整数は有効() {
        Either<String, UnitQuantity> result = UnitQuantity.create("Qty", 100);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().value()).isEqualTo(100);
    }

    @Test
    void 下限の1は有効() {
        Either<String, UnitQuantity> result = UnitQuantity.create("Qty", 1);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void 上限の1000は有効() {
        Either<String, UnitQuantity> result = UnitQuantity.create("Qty", 1000);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void 下限未満はLeftを返す() {
        Either<String, UnitQuantity> result = UnitQuantity.create("Qty", 0);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("Qty: Must not be less than 1");
    }

    @Test
    void 上限超過はLeftを返す() {
        Either<String, UnitQuantity> result = UnitQuantity.create("Qty", 1001);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("Qty: Must not be greater than 1000");
    }

    @Test
    void amountはBigDecimalを返す() {
        UnitQuantity q = UnitQuantity.create("Qty", 5).get();

        assertThat(q.amount()).isEqualByComparingTo(new BigDecimal("5"));
    }
}
