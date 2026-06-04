package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

class WidgetCodeTest {

    @Test
    void Wと4桁の数字なら有効() {
        Either<String, WidgetCode> result = WidgetCode.create("WidgetCode", "W1234");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().value()).isEqualTo("W1234");
    }

    @Test
    void nullはLeftを返す() {
        Either<String, WidgetCode> result = WidgetCode.create("WidgetCode", null);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("WidgetCode: Must not be null or empty");
    }

    @Test
    void 空文字列はLeftを返す() {
        Either<String, WidgetCode> result = WidgetCode.create("WidgetCode", "");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void プレフィックスがWでなければLeftを返す() {
        Either<String, WidgetCode> result = WidgetCode.create("WidgetCode", "G1234");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("must match the pattern");
    }

    @Test
    void 数字が3桁ならLeftを返す() {
        Either<String, WidgetCode> result = WidgetCode.create("WidgetCode", "W123");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void 数字が5桁ならLeftを返す() {
        Either<String, WidgetCode> result = WidgetCode.create("WidgetCode", "W12345");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void Wの後ろが数字でなければLeftを返す() {
        Either<String, WidgetCode> result = WidgetCode.create("WidgetCode", "Wabcd");

        assertThat(result.isLeft()).isTrue();
    }
}
