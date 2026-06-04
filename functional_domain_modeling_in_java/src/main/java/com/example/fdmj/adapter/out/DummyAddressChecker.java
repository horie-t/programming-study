package com.example.fdmj.adapter.out;

import com.example.fdmj.application.port.in.UnvalidatedAddress;
import com.example.fdmj.application.port.out.CheckAddressExists;
import com.example.fdmj.domain.model.order.internal.AddressValidationError;
import com.example.fdmj.domain.model.order.internal.CheckedAddress;
import io.vavr.control.Either;
import org.springframework.stereotype.Component;

/// 住所確認サービスのダミー実装 (書籍 §9 の checkAddressExists 相当)。
/// どんな住所も存在するものとして CheckedAddress を返す。
@Component
public class DummyAddressChecker implements CheckAddressExists {

    @Override
    public Either<AddressValidationError, CheckedAddress> check(UnvalidatedAddress address) {
        return Either.right(new CheckedAddress(address));
    }
}
