package com.example.fdmj.application.port.out;

import com.example.fdmj.application.port.in.UnvalidatedAddress;
import com.example.fdmj.domain.model.order.internal.AddressValidationError;
import com.example.fdmj.domain.model.order.internal.CheckedAddress;
import io.vavr.control.Either;

@FunctionalInterface
public interface CheckAddressExists {

    Either<AddressValidationError, CheckedAddress> check(UnvalidatedAddress address);
}
