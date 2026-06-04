package com.example.fdmj.domain.model.order.internal;

import com.example.fdmj.domain.model.common.EmailAddress;

public record OrderAcknowledgment(EmailAddress emailAddress, HtmlString letter) {}
