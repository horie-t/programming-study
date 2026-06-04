package com.example.fdmj.domain.model.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;

class EmailAddressTest {

    @Test
    void 有効なメールアドレスはRightで包んで返す() {
        Either<String, EmailAddress> result = EmailAddress.create("Email", "alice@example.com");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().value()).isEqualTo("alice@example.com");
    }

    @Test
    void アットマークで区切られていれば何でも有効() {
        Either<String, EmailAddress> result = EmailAddress.create("Email", "a@b");

        assertThat(result.isRight()).isTrue();
    }

    @Test
    void nullはLeftを返す() {
        Either<String, EmailAddress> result = EmailAddress.create("Email", null);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("Email: Must not be null or empty");
    }

    @Test
    void 空文字列はLeftを返す() {
        Either<String, EmailAddress> result = EmailAddress.create("Email", "");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("Email: Must not be null or empty");
    }

    @Test
    void アットマークが無いとLeftを返す() {
        Either<String, EmailAddress> result = EmailAddress.create("Email", "alice.example.com");

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).contains("must match the pattern");
    }

    @Test
    void アットマークの前が空だとLeftを返す() {
        Either<String, EmailAddress> result = EmailAddress.create("Email", "@example.com");

        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void アットマークの後ろが空だとLeftを返す() {
        Either<String, EmailAddress> result = EmailAddress.create("Email", "alice@");

        assertThat(result.isLeft()).isTrue();
    }
}
