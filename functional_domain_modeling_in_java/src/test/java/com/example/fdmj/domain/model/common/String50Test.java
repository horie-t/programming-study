package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class String50Test {

    @Nested
    class Create {

        @Test
        void 有効な文字列の場合はRightで包んで返す() {
            Either<String, String50> result = String50.create("Name", "Alice");

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().value()).isEqualTo("Alice");
        }

        @Test
        void ちょうど50文字の文字列は有効() {
            String s = "a".repeat(50);

            Either<String, String50> result = String50.create("Name", s);

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().value()).isEqualTo(s);
        }

        @Test
        void nullはLeftを返す() {
            Either<String, String50> result = String50.create("Name", null);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo("Name must not be null or empty");
        }

        @Test
        void 空文字列はLeftを返す() {
            Either<String, String50> result = String50.create("Name", "");

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo("Name must not be null or empty");
        }

        @Test
        void 上限を超える文字列はLeftを返す() {
            String s = "a".repeat(51);

            Either<String, String50> result = String50.create("Name", s);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo("Name must not be more than 50 chars");
        }
    }

    @Nested
    class CreateOption {

        @Test
        void 有効な文字列の場合はRightでSomeを返す() {
            Either<String, Option<String50>> result = String50.createOption("Name", "Alice");

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().isDefined()).isTrue();
            assertThat(result.get().get().value()).isEqualTo("Alice");
        }

        @Test
        void nullの場合はRightでNoneを返す() {
            Either<String, Option<String50>> result = String50.createOption("Name", null);

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().isEmpty()).isTrue();
        }

        @Test
        void 空文字列の場合はRightでNoneを返す() {
            Either<String, Option<String50>> result = String50.createOption("Name", "");

            assertThat(result.isRight()).isTrue();
            assertThat(result.get().isEmpty()).isTrue();
        }

        @Test
        void 上限を超える文字列はLeftを返す() {
            String s = "a".repeat(51);

            Either<String, Option<String50>> result = String50.createOption("Name", s);

            assertThat(result.isLeft()).isTrue();
            assertThat(result.getLeft()).isEqualTo("Name must not be more than 50 chars");
        }
    }
}
