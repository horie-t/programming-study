package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.application.port.in.UnvalidatedAddress;
import com.example.fdmj.domain.model.common.Address;
import com.example.fdmj.domain.model.common.String50;

public record AddressDto(
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String addressLine4,
        String city,
        String zipCode) {

    /// 検証なしの 1:1 コピー。外部からドメインへ取り込むときに使う。
    public UnvalidatedAddress toUnvalidatedAddress() {
        return new UnvalidatedAddress(addressLine1, addressLine2, addressLine3, addressLine4, city, zipCode);
    }

    /// ドメインオブジェクトから DTO へ。任意項目 (Option) が None の場合は null になる。
    public static AddressDto fromDomain(Address domain) {
        return new AddressDto(
                domain.addressLine1().value(),
                domain.addressLine2().map(String50::value).getOrNull(),
                domain.addressLine3().map(String50::value).getOrNull(),
                domain.addressLine4().map(String50::value).getOrNull(),
                domain.city().value(),
                domain.zipCode().value());
    }
}
