package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

class ProductCodeTest {

    @Test
    void Wで始まればWidgetCodeになる() {
        Either<String, ProductCode> result = ProductCode.create("ProductCode", "W1234");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isInstanceOf(WidgetCode.class);
        assertThat(result.get().value()).isEqualTo("W1234");
    }

    @Test
    void Gで始まればGizmoCodeになる() {
        Either<String, ProductCode> result = ProductCode.create("ProductCode", "G123");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isInstanceOf(GizmoCode.class);
        assertThat(result.get().value()).isEqualTo("G123");
    }

    @Test
    void nullはLeftを返す() {
        Either<String, ProductCode> result = ProductCode.create("ProductCode", null);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("ProductCode: Must not be null or empty");
    }

    @Test
    void 空文字列はLeftを返す() {
        Either<String, ProductCode> result = ProductCode.create("ProductCode", "");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void WでもGでも始まらないとLeftを返す() {
        Either<String, ProductCode> result = ProductCode.create("ProductCode", "X1234");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("ProductCode: Format not recognized 'X1234'");
    }

    @Test
    void Wで始まるが桁数が違うとWidgetCodeのバリデーションエラーになる() {
        Either<String, ProductCode> result = ProductCode.create("ProductCode", "W123");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("must match the pattern");
    }

    @Test
    void パターンマッチで網羅的に処理できる() {
        ProductCode widget = ProductCode.create("ProductCode", "W1234").get();
        ProductCode gizmo = ProductCode.create("ProductCode", "G123").get();

        String widgetLabel = switch (widget) {
            case WidgetCode wc -> "widget:" + wc.value();
            case GizmoCode gc -> "gizmo:" + gc.value();
        };
        String gizmoLabel = switch (gizmo) {
            case WidgetCode wc -> "widget:" + wc.value();
            case GizmoCode gc -> "gizmo:" + gc.value();
        };

        assertThat(widgetLabel).isEqualTo("widget:W1234");
        assertThat(gizmoLabel).isEqualTo("gizmo:G123");
    }
}
