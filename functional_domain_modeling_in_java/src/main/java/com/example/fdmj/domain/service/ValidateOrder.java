package com.example.fdmj.domain.service;

import com.example.fdmj.application.port.in.UnvalidatedAddress;
import com.example.fdmj.application.port.in.UnvalidatedCustomerInfo;
import com.example.fdmj.application.port.in.UnvalidatedOrder;
import com.example.fdmj.application.port.in.UnvalidatedOrderLine;
import com.example.fdmj.application.port.out.CheckAddressExists;
import com.example.fdmj.application.port.out.CheckProductCodeExists;
import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.CustomerInfo;
import com.example.fdmj.domain.model.common.EmailAddress;
import com.example.fdmj.domain.model.common.OrderId;
import com.example.fdmj.domain.model.common.OrderLineId;
import com.example.fdmj.domain.model.common.OrderQuantity;
import com.example.fdmj.domain.model.common.PersonalName;
import com.example.fdmj.domain.model.common.ProductCode;
import com.example.fdmj.domain.model.common.String50;
import com.example.fdmj.domain.model.common.ZipCode;
import com.example.fdmj.domain.model.order.error.ValidationError;
import com.example.fdmj.domain.model.order.internal.AddressNotFound;
import com.example.fdmj.domain.model.order.internal.CheckedAddress;
import com.example.fdmj.domain.model.order.internal.InvalidFormat;
import com.example.fdmj.domain.model.order.internal.ValidatedOrder;
import com.example.fdmj.domain.model.order.internal.ValidatedOrderLine;
import io.vavr.collection.List;
import io.vavr.control.Either;
import java.math.BigDecimal;

public class ValidateOrder {

    private final CheckProductCodeExists checkProductCodeExists;
    private final CheckAddressExists checkAddressExists;

    public ValidateOrder(
            CheckProductCodeExists checkProductCodeExists,
            CheckAddressExists checkAddressExists) {
        this.checkProductCodeExists = checkProductCodeExists;
        this.checkAddressExists = checkAddressExists;
    }

    public Either<ValidationError, ValidatedOrder> validate(UnvalidatedOrder o) {
        return toOrderId(o.orderId()).flatMap(orderId ->
                toCustomerInfo(o.customerInfo()).flatMap(customerInfo ->
                toCheckedAddress(o.shippingAddress()).flatMap(checkedShipping ->
                toAddress(checkedShipping).flatMap(shippingAddress ->
                toCheckedAddress(o.billingAddress()).flatMap(checkedBilling ->
                toAddress(checkedBilling).flatMap(billingAddress ->
                sequenceLines(o.lines().map(this::toValidatedOrderLine)).map(lines ->
                        new ValidatedOrder(orderId, customerInfo, shippingAddress, billingAddress, lines)
                )))))));
    }

    private static Either<ValidationError, OrderId> toOrderId(String s) {
        return OrderId.create("OrderId", s).mapLeft(ValidationError::new);
    }

    private static Either<ValidationError, OrderLineId> toOrderLineId(String s) {
        return OrderLineId.create("OrderLineId", s).mapLeft(ValidationError::new);
    }

    private static Either<ValidationError, CustomerInfo> toCustomerInfo(UnvalidatedCustomerInfo c) {
        return String50.create("FirstName", c.firstName()).mapLeft(ValidationError::new).flatMap(firstName ->
                String50.create("LastName", c.lastName()).mapLeft(ValidationError::new).flatMap(lastName ->
                EmailAddress.create("EmailAddress", c.emailAddress()).mapLeft(ValidationError::new).map(email ->
                        new CustomerInfo(new PersonalName(firstName, lastName), email)
                )));
    }

    private Either<ValidationError, CheckedAddress> toCheckedAddress(UnvalidatedAddress address) {
        return checkAddressExists.check(address).mapLeft(err -> switch (err) {
            case AddressNotFound ignored -> new ValidationError("Address not found");
            case InvalidFormat ignored -> new ValidationError("Address has bad format");
        });
    }

    private static Either<ValidationError, Address> toAddress(CheckedAddress checked) {
        UnvalidatedAddress u = checked.address();
        return String50.create("AddressLine1", u.addressLine1()).mapLeft(ValidationError::new).flatMap(line1 ->
                String50.createOption("AddressLine2", u.addressLine2()).mapLeft(ValidationError::new).flatMap(line2 ->
                String50.createOption("AddressLine3", u.addressLine3()).mapLeft(ValidationError::new).flatMap(line3 ->
                String50.createOption("AddressLine4", u.addressLine4()).mapLeft(ValidationError::new).flatMap(line4 ->
                String50.create("City", u.city()).mapLeft(ValidationError::new).flatMap(city ->
                ZipCode.create("ZipCode", u.zipCode()).mapLeft(ValidationError::new).map(zip ->
                        new Address(line1, line2, line3, line4, city, zip)
                ))))));
    }

    private Either<ValidationError, ProductCode> toProductCode(String code) {
        return ProductCode.create("ProductCode", code)
                .mapLeft(ValidationError::new)
                .flatMap(pc -> checkProductCodeExists.exists(pc)
                        ? Either.right(pc)
                        : Either.left(new ValidationError("Invalid: " + pc.value())));
    }

    private static Either<ValidationError, OrderQuantity> toOrderQuantity(ProductCode pc, BigDecimal qty) {
        return OrderQuantity.create("OrderQuantity", pc, qty).mapLeft(ValidationError::new);
    }

    private Either<ValidationError, ValidatedOrderLine> toValidatedOrderLine(UnvalidatedOrderLine line) {
        return toOrderLineId(line.orderLineId()).flatMap(lineId ->
                toProductCode(line.productCode()).flatMap(productCode ->
                toOrderQuantity(productCode, line.quantity()).map(quantity ->
                        new ValidatedOrderLine(lineId, productCode, quantity)
                )));
    }

    private static <L, R> Either<L, List<R>> sequenceLines(List<Either<L, R>> eithers) {
        return eithers.foldRight(
                Either.<L, List<R>>right(List.empty()),
                (e, acc) -> e.flatMap(r -> acc.map(rs -> rs.prepend(r)))
        );
    }
}
