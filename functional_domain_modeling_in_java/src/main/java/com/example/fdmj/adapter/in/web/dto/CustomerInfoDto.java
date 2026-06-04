package com.example.fdmj.adapter.in.web.dto;

import com.example.fdmj.application.port.in.UnvalidatedCustomerInfo;
import com.example.fdmj.domain.model.common.CustomerInfo;

public record CustomerInfoDto(
        String firstName,
        String lastName,
        String emailAddress) {

    /// 検証なしの 1:1 コピー。外部からドメインへ取り込むときに使う。
    public UnvalidatedCustomerInfo toUnvalidatedCustomerInfo() {
        return new UnvalidatedCustomerInfo(firstName, lastName, emailAddress);
    }

    /// ドメインオブジェクトから DTO へ。外部へ書き出すときに使う。
    public static CustomerInfoDto fromDomain(CustomerInfo domain) {
        return new CustomerInfoDto(
                domain.name().firstName().value(),
                domain.name().lastName().value(),
                domain.emailAddress().value());
    }
}
