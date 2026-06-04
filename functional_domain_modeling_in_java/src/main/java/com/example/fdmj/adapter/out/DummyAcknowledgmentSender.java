package com.example.fdmj.adapter.out;

import com.example.fdmj.application.port.out.CreateOrderAcknowledgmentLetter;
import com.example.fdmj.application.port.out.SendOrderAcknowledgment;
import com.example.fdmj.domain.model.order.PricedOrder;
import com.example.fdmj.domain.model.order.internal.HtmlString;
import com.example.fdmj.domain.model.order.internal.OrderAcknowledgment;
import com.example.fdmj.domain.model.order.internal.SendResult;
import com.example.fdmj.domain.model.order.internal.Sent;
import org.springframework.stereotype.Component;

/// 確認通知サービスのダミー実装 (書籍 §9 の createOrderAcknowledgmentLetter / sendOrderAcknowledgment 相当)。
/// 固定のレターを生成し、常に送信成功 (Sent) を返す。
@Component
public class DummyAcknowledgmentSender
        implements CreateOrderAcknowledgmentLetter, SendOrderAcknowledgment {

    @Override
    public HtmlString create(PricedOrder pricedOrder) {
        return new HtmlString("<html>Thank you for your order " + pricedOrder.orderId().value() + "</html>");
    }

    @Override
    public SendResult send(OrderAcknowledgment acknowledgment) {
        return new Sent();
    }
}
