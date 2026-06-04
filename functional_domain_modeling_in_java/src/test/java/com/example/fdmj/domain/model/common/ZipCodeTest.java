package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

class ZipCodeTest {

    @Test
    void ちょうど5桁の数字は有効() {
        Either<String, ZipCode> result = ZipCode.create("ZipCode", "12345");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().value()).isEqualTo("12345");
    }

    @Test
    void nullはLeftを返す() {
        Either<String, ZipCode> result = ZipCode.create("ZipCode", null);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("ZipCode: Must not be null or empty");
    }

    @Test
    void 空文字列はLeftを返す() {
        Either<String, ZipCode> result = ZipCode.create("ZipCode", "");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("ZipCode: Must not be null or empty");
    }

    @Test
    void 桁数が4桁ならLeftを返す() {
        Either<String, ZipCode> result = ZipCode.create("ZipCode", "1234");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("must match the pattern");
    }

    @Test
    void 桁数が6桁ならLeftを返す() {
        Either<String, ZipCode> result = ZipCode.create("ZipCode", "123456");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void 数字以外を含むとLeftを返す() {
        Either<String, ZipCode> result = ZipCode.create("ZipCode", "1234a");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void 数字に余分な文字が混じるとLeftを返す() {
        Either<String, ZipCode> result = ZipCode.create("ZipCode", "abc12345xyz");

        assertThat(result.isLeft()).isTrue();
    }
}
