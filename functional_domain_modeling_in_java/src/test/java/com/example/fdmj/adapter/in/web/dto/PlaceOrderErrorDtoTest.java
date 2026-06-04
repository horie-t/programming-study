package com.example.fdmj.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.domain.model.order.error.PlaceOrderError;
import com.example.fdmj.domain.model.order.error.PricingError;
import com.example.fdmj.domain.model.order.error.RemoteServiceError;
import com.example.fdmj.domain.model.order.error.ServiceInfo;
import com.example.fdmj.domain.model.order.error.ValidationError;
import java.net.URI;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class PlaceOrderErrorDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void ValidationErrorはcodeとmessageに変換される() {
        PlaceOrderError error = new ValidationError("bad input");

        PlaceOrderErrorDto dto = PlaceOrderErrorDto.fromDomain(error);

        assertThat(dto.code()).isEqualTo("ValidationError");
        assertThat(dto.message()).isEqualTo("bad input");
    }

    @Test
    void PricingErrorはcodeとmessageに変換される() {
        PlaceOrderError error = new PricingError("over limit");

        PlaceOrderErrorDto dto = PlaceOrderErrorDto.fromDomain(error);

        assertThat(dto.code()).isEqualTo("PricingError");
        assertThat(dto.message()).isEqualTo("over limit");
    }

    @Test
    void RemoteServiceErrorはサービス名と例外メッセージを結合する() {
        PlaceOrderError error = new RemoteServiceError(
                new ServiceInfo("AddressService", URI.create("https://example.com")),
                new RuntimeException("timeout"));

        PlaceOrderErrorDto dto = PlaceOrderErrorDto.fromDomain(error);

        assertThat(dto.code()).isEqualTo("RemoteServiceError");
        assertThat(dto.message()).isEqualTo("AddressService: timeout");
    }

    @Test
    void JSONにシリアライズできる() {
        PlaceOrderErrorDto dto = PlaceOrderErrorDto.fromDomain(new ValidationError("bad input"));

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"code\":\"ValidationError\"");
        assertThat(json).contains("\"message\":\"bad input\"");
    }
}
