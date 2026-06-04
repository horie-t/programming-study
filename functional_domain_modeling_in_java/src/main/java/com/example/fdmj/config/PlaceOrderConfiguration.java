package com.example.fdmj.config;

import com.example.fdmj.application.PlaceOrderService;
import com.example.fdmj.application.port.in.PlaceOrderUseCase;
import com.example.fdmj.application.port.out.CheckAddressExists;
import com.example.fdmj.application.port.out.CheckProductCodeExists;
import com.example.fdmj.application.port.out.CreateOrderAcknowledgmentLetter;
import com.example.fdmj.application.port.out.GetProductPrice;
import com.example.fdmj.application.port.out.SendOrderAcknowledgment;
import com.example.fdmj.domain.service.AcknowledgeOrder;
import com.example.fdmj.domain.service.CreateEvents;
import com.example.fdmj.domain.service.PriceOrder;
import com.example.fdmj.domain.service.ValidateOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// ドメインサービスとワークフローを組み立てる配線。
/// ドメイン/アプリケーション層は Spring 非依存に保ち、依存注入はここに集約する
/// (書籍 §9「依存性の注入」を Spring の DI コンテナで実現)。
@Configuration
public class PlaceOrderConfiguration {

    @Bean
    public ValidateOrder validateOrder(
            CheckProductCodeExists checkProductCodeExists,
            CheckAddressExists checkAddressExists) {
        return new ValidateOrder(checkProductCodeExists, checkAddressExists);
    }

    @Bean
    public PriceOrder priceOrder(GetProductPrice getProductPrice) {
        return new PriceOrder(getProductPrice);
    }

    @Bean
    public AcknowledgeOrder acknowledgeOrder(
            CreateOrderAcknowledgmentLetter createOrderAcknowledgmentLetter,
            SendOrderAcknowledgment sendOrderAcknowledgment) {
        return new AcknowledgeOrder(createOrderAcknowledgmentLetter, sendOrderAcknowledgment);
    }

    @Bean
    public CreateEvents createEvents() {
        return new CreateEvents();
    }

    @Bean
    public PlaceOrderUseCase placeOrderUseCase(
            ValidateOrder validateOrder,
            PriceOrder priceOrder,
            AcknowledgeOrder acknowledgeOrder,
            CreateEvents createEvents) {
        return new PlaceOrderService(validateOrder, priceOrder, acknowledgeOrder, createEvents);
    }
}
