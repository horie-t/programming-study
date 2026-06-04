package com.example.fdmj.adapter.in.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fdmj.application.port.in.UnvalidatedOrder;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class OrderFormDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void JSONをOrderFormDtoにデシリアライズできる() {
        String json = """
                {
                  "orderId": "ORD-001",
                  "customerInfo": {
                    "firstName": "Taro",
                    "lastName": "Yamada",
                    "emailAddress": "taro@example.com"
                  },
                  "shippingAddress": {
                    "addressLine1": "1-2-3",
                    "addressLine2": null,
                    "addressLine3": null,
                    "addressLine4": null,
                    "city": "Tokyo",
                    "zipCode": "10001"
                  },
                  "billingAddress": {
                    "addressLine1": "4-5-6",
                    "addressLine2": "Apt 7",
                    "addressLine3": null,
                    "addressLine4": null,
                    "city": "Osaka",
                    "zipCode": "20002"
                  },
                  "lines": [
                    { "orderLineId": "L1", "productCode": "W1234", "quantity": 5 },
                    { "orderLineId": "L2", "productCode": "G123", "quantity": 2.5 }
                  ]
                }
                """;

        OrderFormDto dto = mapper.readValue(json, OrderFormDto.class);

        assertThat(dto.orderId()).isEqualTo("ORD-001");
        assertThat(dto.customerInfo().emailAddress()).isEqualTo("taro@example.com");
        assertThat(dto.shippingAddress().city()).isEqualTo("Tokyo");
        assertThat(dto.billingAddress().addressLine2()).isEqualTo("Apt 7");
        assertThat(dto.lines()).hasSize(2);
        assertThat(dto.lines().get(1).quantity()).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    @Test
    void OrderFormDtoをUnvalidatedOrderに変換できる() {
        OrderFormDto dto = new OrderFormDto(
                "ORD-001",
                new CustomerInfoDto("Taro", "Yamada", "taro@example.com"),
                new AddressDto("1-2-3", null, null, null, "Tokyo", "10001"),
                new AddressDto("4-5-6", "Apt 7", null, null, "Osaka", "20002"),
                java.util.List.of(
                        new OrderFormLineDto("L1", "W1234", new BigDecimal("5")),
                        new OrderFormLineDto("L2", "G123", new BigDecimal("2.5"))));

        UnvalidatedOrder order = dto.toUnvalidatedOrder();

        assertThat(order.orderId()).isEqualTo("ORD-001");
        assertThat(order.customerInfo().emailAddress()).isEqualTo("taro@example.com");
        assertThat(order.shippingAddress().city()).isEqualTo("Tokyo");
        assertThat(order.billingAddress().addressLine2()).isEqualTo("Apt 7");
        assertThat(order.lines()).hasSize(2);
        assertThat(order.lines().get(0).productCode()).isEqualTo("W1234");
        assertThat(order.lines().get(1).quantity()).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    @Test
    void JSONからUnvalidatedOrderまで一気通貫で変換できる() throws Exception {
        String json = """
                {
                  "orderId": "ORD-XYZ",
                  "customerInfo": { "firstName": "Hanako", "lastName": "Suzuki", "emailAddress": "hanako@example.com" },
                  "shippingAddress": { "addressLine1": "A", "city": "Kyoto", "zipCode": "30003" },
                  "billingAddress": { "addressLine1": "B", "city": "Kobe", "zipCode": "40004" },
                  "lines": [ { "orderLineId": "L9", "productCode": "W9999", "quantity": 10 } ]
                }
                """;

        UnvalidatedOrder order = mapper.readValue(json, OrderFormDto.class).toUnvalidatedOrder();

        assertThat(order.orderId()).isEqualTo("ORD-XYZ");
        assertThat(order.lines()).hasSize(1);
        assertThat(order.lines().get(0).orderLineId()).isEqualTo("L9");
        // JSON で省略された addressLine2 などは null として渡る (検証は ValidateOrder の責務)
        assertThat(order.shippingAddress().addressLine2()).isNull();
    }
}
