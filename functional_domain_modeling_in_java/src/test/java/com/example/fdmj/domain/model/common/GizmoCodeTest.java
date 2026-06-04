package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

class GizmoCodeTest {

    @Test
    void Gと3桁の数字なら有効() {
        Either<String, GizmoCode> result = GizmoCode.create("GizmoCode", "G123");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().value()).isEqualTo("G123");
    }

    @Test
    void nullはLeftを返す() {
        Either<String, GizmoCode> result = GizmoCode.create("GizmoCode", null);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("GizmoCode: Must not be null or empty");
    }

    @Test
    void 空文字列はLeftを返す() {
        Either<String, GizmoCode> result = GizmoCode.create("GizmoCode", "");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void プレフィックスがGでなければLeftを返す() {
        Either<String, GizmoCode> result = GizmoCode.create("GizmoCode", "W123");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void 数字が2桁ならLeftを返す() {
        Either<String, GizmoCode> result = GizmoCode.create("GizmoCode", "G12");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void 数字が4桁ならLeftを返す() {
        Either<String, GizmoCode> result = GizmoCode.create("GizmoCode", "G1234");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void Gの後ろが数字でなければLeftを返す() {
        Either<String, GizmoCode> result = GizmoCode.create("GizmoCode", "Gabc");

        assertThat(result.isLeft()).isTrue();
    }
}
