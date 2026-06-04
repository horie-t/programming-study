package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

class OrderLineIdTest {

    @Test
    void 有効な文字列はRightで包んで返す() {
        Either<String, OrderLineId> result = OrderLineId.create("OrderLineId", "LINE-001");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().value()).isEqualTo("LINE-001");
    }

    @Test
    void ちょうど50文字の文字列は有効() {
        String s = "a".repeat(50);

        Either<String, OrderLineId> result = OrderLineId.create("OrderLineId", s);

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void nullはLeftを返す() {
        Either<String, OrderLineId> result = OrderLineId.create("OrderLineId", null);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("OrderLineId must not be null or empty");
    }

    @Test
    void 空文字列はLeftを返す() {
        Either<String, OrderLineId> result = OrderLineId.create("OrderLineId", "");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("OrderLineId must not be null or empty");
    }

    @Test
    void 上限を超える文字列はLeftを返す() {
        String s = "a".repeat(51);

        Either<String, OrderLineId> result = OrderLineId.create("OrderLineId", s);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("OrderLineId must not be more than 50 chars");
    }
}
