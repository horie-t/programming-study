package com.example.fdmj.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class PlaceOrderControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static final String VALID_ORDER = """
            {
              "orderId": "ORD-001",
              "customerInfo": { "firstName": "Taro", "lastName": "Yamada", "emailAddress": "taro@example.com" },
              "shippingAddress": { "addressLine1": "1-2-3", "city": "Tokyo", "zipCode": "10001" },
              "billingAddress": { "addressLine1": "4-5-6", "city": "Osaka", "zipCode": "20002" },
              "lines": [
                { "orderLineId": "L1", "productCode": "W1234", "quantity": 5 },
                { "orderLineId": "L2", "productCode": "G123", "quantity": 2.5 }
              ]
            }
            """;

    @Test
    void 妥当な注文はステータス200とイベント配列を返す() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"type\":\"OrderPlaced\"")))
                .andExpect(content().string(containsString("\"type\":\"BillableOrderPlaced\"")))
                .andExpect(content().string(containsString("\"type\":\"OrderAcknowledgmentSent\"")))
                .andExpect(content().string(containsString("\"orderId\":\"ORD-001\"")));
    }

    @Test
    void 商品コードが不正な注文はステータス400とエラーDTOを返す() throws Exception {
        String invalidOrder = """
                {
                  "orderId": "ORD-002",
                  "customerInfo": { "firstName": "Taro", "lastName": "Yamada", "emailAddress": "taro@example.com" },
                  "shippingAddress": { "addressLine1": "1-2-3", "city": "Tokyo", "zipCode": "10001" },
                  "billingAddress": { "addressLine1": "4-5-6", "city": "Osaka", "zipCode": "20002" },
                  "lines": [ { "orderLineId": "L1", "productCode": "X9999", "quantity": 5 } ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidOrder))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"ValidationError\"")))
                .andExpect(content().string(containsString("ProductCode")));
    }

    @Test
    void メールアドレスが不正な注文はステータス400を返す() throws Exception {
        String invalidOrder = """
                {
                  "orderId": "ORD-003",
                  "customerInfo": { "firstName": "Taro", "lastName": "Yamada", "emailAddress": "not-an-email" },
                  "shippingAddress": { "addressLine1": "1-2-3", "city": "Tokyo", "zipCode": "10001" },
                  "billingAddress": { "addressLine1": "4-5-6", "city": "Osaka", "zipCode": "20002" },
                  "lines": [ { "orderLineId": "L1", "productCode": "W1234", "quantity": 5 } ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidOrder))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"ValidationError\"")))
                .andExpect(content().string(containsString("EmailAddress")));
    }
}
