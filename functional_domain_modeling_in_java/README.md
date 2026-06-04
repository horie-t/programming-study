# Javaによる関数型ドメインモデリング

『[関数型ドメインモデリング](https://tatsu-zine.com/books/domain-modeling-made-functional)』をJavaで実装してみるプロジェクトです。

[オリジナルのコード](https://github.com/swlaschin/DomainModelingMadeFunctional)はF#で書かれていてライセンスは[Apache 2.0 license](https://github.com/swlaschin/DomainModelingMadeFunctional/blob/master/LICENSE)でライセンスされています。本ディレクトリ配下のコードをこれをJavaに移植したものとなります。

受注システム（注文を確定するワークフロー）をサンプルドメインとして、関数型アプローチによるドメインモデリングを Java 25 + Spring Boot + [Vavr](https://www.vavr.io/) で実践しています。

## 技術スタック

- Java 25
- Spring Boot 4
- Vavr 0.10.3（`Either` / `Option` / 永続コレクション）

## アーキテクチャ

ヘキサゴナルアーキテクチャ（Ports & Adapters）を採用しています。

```
com.example.fdmj/
├── domain/
│   ├── model/     # ドメインモデル（sealed interface・record による値オブジェクト／代数的データ型）
│   └── service/   # ドメインサービス（ワークフローの各ステップ。純粋関数）
├── adapter/
│   ├── in/web/    # 入力アダプタ（REST コントローラ・DTO）
│   └── out/       # 出力アダプタ（製品カタログ・住所確認などのダミー実装）
├── application/
│   ├── PlaceOrderService    # ユースケースの実装（各ステップを Either で合成）
│   └── port/
│       ├── in/    # 入力ポート（PlaceOrderUseCase・Unvalidated* コマンド）
│       └── out/   # 出力ポート（CheckProductCodeExists 等）
└── config/        # Spring の DI 配線（ドメイン層は Spring 非依存に保つ）
```

注文確定ワークフロー: `UnvalidatedOrder` → `ValidatedOrder` → `PricedOrder` → イベント列、という状態遷移を型で表現しています。

## ビルドと実行

```bash
# ビルド
./mvnw clean compile

# テスト実行
./mvnw test

# アプリケーションの起動（http://localhost:8080）
./mvnw spring-boot:run

# パッケージング
./mvnw package
```

## API

### 注文を確定する `POST /orders`

リクエストボディ（`OrderFormDto`）を JSON で受け取り、ワークフローを実行して結果イベントの配列を返します。

#### 正常系

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-100",
    "customerInfo": { "firstName": "Taro", "lastName": "Yamada", "emailAddress": "taro@example.com" },
    "shippingAddress": { "addressLine1": "1-2-3", "city": "Tokyo", "zipCode": "10001" },
    "billingAddress": { "addressLine1": "4-5-6", "city": "Osaka", "zipCode": "20002" },
    "lines": [
      { "orderLineId": "L1", "productCode": "W1234", "quantity": 5 },
      { "orderLineId": "L2", "productCode": "G123", "quantity": 2.5 }
    ]
  }'
```

レスポンス（`200 OK`）。`type` フィールドで識別される多態なイベント配列です。

```json
[
  { "type": "OrderAcknowledgmentSent", "orderId": "ORD-100", "emailAddress": "taro@example.com" },
  { "type": "OrderPlaced", "orderId": "ORD-100", "customerInfo": { ... }, "amountToBill": 7.5, "lines": [ ... ] },
  { "type": "BillableOrderPlaced", "orderId": "ORD-100", "billingAddress": { ... }, "amountToBill": 7.5 }
]
```

> 注: 出力アダプタはダミー実装です。製品価格は一律 `1`、住所確認は常に成功、確認通知は常に送信成功を返します。

#### 異常系（バリデーションエラー）

商品コードの形式が不正な例（`W` + 4桁 または `G` + 3桁 のみ有効）:

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-101",
    "customerInfo": { "firstName": "Taro", "lastName": "Yamada", "emailAddress": "taro@example.com" },
    "shippingAddress": { "addressLine1": "1-2-3", "city": "Tokyo", "zipCode": "10001" },
    "billingAddress": { "addressLine1": "4-5-6", "city": "Osaka", "zipCode": "20002" },
    "lines": [ { "orderLineId": "L1", "productCode": "X9999", "quantity": 5 } ]
  }'
```

レスポンス（`400 Bad Request`）:

```json
{ "code": "ValidationError", "message": "ProductCode: Format not recognized 'X9999'" }
```

エラー種別と HTTP ステータスの対応:

| エラー | HTTP ステータス |
| --- | --- |
| `ValidationError` | 400 Bad Request |
| `PricingError` | 422 Unprocessable Entity |
| `RemoteServiceError` | 502 Bad Gateway |

## ライセンス

本プロジェクトのコードは[Apache 2.0ライセンス](../LICENSE)でライセンスされています。

