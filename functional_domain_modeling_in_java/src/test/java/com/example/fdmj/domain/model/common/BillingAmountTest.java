package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.collection.List;
import io.vavr.control.Either;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BillingAmountTest {

    @Nested
    class Create {

        @Test
        void 範囲内なら有効() {
            Either<String, BillingAmount> result = BillingAmount.create(new BigDecimal("500"));

            assertThat(result.isRight()).isTrue();
        }

        @Test
        void 上限の10000は有効() {
            Either<String, BillingAmount> result = BillingAmount.create(new BigDecimal("10000"));

            assertThat(result.isRight()).isTrue();
        }

        @Test
        void 負の値はLeftを返す() {
            Either<String, BillingAmount> result = BillingAmount.create(new BigDecimal("-1"));

            assertThat(result.isLeft()).isTrue();
        }

        @Test
        void 上限超過はLeftを返す() {
            Either<String, BillingAmount> result = BillingAmount.create(new BigDecimal("10000.01"));

            assertThat(result.isLeft()).isTrue();
        }
    }

    @Nested
    class SumPrices {

        @Test
        void Priceのリストを合計してBillingAmountを作る() {
            List<Price> prices = List.of(
                    Price.unsafeCreate(new BigDecimal("100")),
                    Price.unsafeCreate(new BigDecimal("200")),
                    Price.unsafeCreate(new BigDecimal("50"))
            );

            Either<String, BillingAmount> result = BillingAmount.sumPrices(prices);

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().value()).isEqualByComparingTo(new BigDecimal("350"));
        }

        @Test
        void 空のリストは0になる() {
            Either<String, BillingAmount> result = BillingAmount.sumPrices(List.empty());

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().value()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void 合計が上限超過ならLeftを返す() {
            List<Price> prices = List.fill(20, () -> Price.unsafeCreate(new BigDecimal("1000")));

            Either<String, BillingAmount> result = BillingAmount.sumPrices(prices);

            assertThat(result.isLeft()).isTrue();
        }
    }
}
