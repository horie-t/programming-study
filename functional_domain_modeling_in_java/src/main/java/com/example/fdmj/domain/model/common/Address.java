package com.example.fdmj.domain.model.common;

import io.vavr.control.Option;

public record Address(
        String50 addressLine1,
        Option<String50> addressLine2,
        Option<String50> addressLine3,
        Option<String50> addressLine4,
        String50 city,
        ZipCode zipCode) {}
