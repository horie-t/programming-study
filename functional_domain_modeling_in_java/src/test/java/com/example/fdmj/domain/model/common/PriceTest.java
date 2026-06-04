package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.vavr.control.Either;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PriceTest {

    @Nested
    class Create {

        @Test
        void 範囲内なら有効() {
            Either<String, Price> result = Price.create(new BigDecimal("100"));

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().value()).isEqualByComparingTo(new BigDecimal("100"));
        }

        @Test
        void 下限のゼロは有効() {
            Either<String, Price> result = Price.create(BigDecimal.ZERO);

            assertThat(result.isRight()).isTrue();
        }

        @Test
        void 上限の1000は有効() {
            Either<String, Price> result = Price.create(new BigDecimal("1000"));

            assertThat(result.isRight()).isTrue();
        }

        @Test
        void 負の値はLeftを返す() {
            Either<String, Price> result = Price.create(new BigDecimal("-0.01"));

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).contains("Must not be less than");
        }

        @Test
        void 上限超過はLeftを返す() {
            Either<String, Price> result = Price.create(new BigDecimal("1000.01"));

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).contains("Must not be greater than");
        }
    }

    @Nested
    class UnsafeCreate {

        @Test
        void 有効な値ならPriceを返す() {
            Price p = Price.unsafeCreate(new BigDecimal("50"));

            assertThat(p.value()).isEqualByComparingTo(new BigDecimal("50"));
        }

        @Test
        void 範囲外なら例外を投げる() {
            assertThatThrownBy(() -> Price.unsafeCreate(new BigDecimal("9999")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not expecting Price to be out of bounds");
        }
    }

    @Nested
    class Multiply {

        @Test
        void 数量を掛けた結果が範囲内ならRight() {
            Price p = Price.unsafeCreate(new BigDecimal("10"));

            Either<String, Price> result = p.multiply(new BigDecimal("3"));

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().value()).isEqualByComparingTo(new BigDecimal("30"));
        }

        @Test
        void 掛けた結果が範囲外ならLeft() {
            Price p = Price.unsafeCreate(new BigDecimal("500"));

            Either<String, Price> result = p.multiply(new BigDecimal("3"));

            assertThat(result.isLeft()).isTrue();
        }
    }
}
