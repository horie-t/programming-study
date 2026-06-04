package com.example.fdmj.adapter.out;

import com.example.fdmj.application.port.out.CheckProductCodeExists;
import com.example.fdmj.application.port.out.GetProductPrice;
import com.example.fdmj.domain.model.common.Price;
import com.example.fdmj.domain.model.common.ProductCode;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/// 製品カタログのダミー実装 (書籍 §9 の checkProductExists / getProductPrice 相当)。
/// すべての製品コードを有効とみなし、価格は一律 1 を返す。
@Component
public class DummyProductCatalog implements CheckProductCodeExists, GetProductPrice {

    @Override
    public boolean exists(ProductCode productCode) {
        return true;
    }

    @Override
    public Price getPrice(ProductCode productCode) {
        return Price.unsafeCreate(BigDecimal.ONE);
    }
}
