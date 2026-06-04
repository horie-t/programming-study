package com.example.fdmj.application.port.in;

public record UnvalidatedAddress(
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String addressLine4,
        String city,
        String zipCode) {}
