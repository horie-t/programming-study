package com.example.fdmj.application.port.in;

public record UnvalidatedCustomerInfo(
        String firstName,
        String lastName,
        String emailAddress) {}
