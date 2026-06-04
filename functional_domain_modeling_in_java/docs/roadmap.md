# Java + Spring Boot 再実装ロードマップ

『関数型ドメインモデリング』のサンプルプロジェクト (F#)
`./DomainModelingMadeFunctional/src/OrderTaking` を Java + Spring Boot で再実装するためのロードマップ。

実装順は F# プロジェクトの `OrderTaking.fsproj` のコンパイル順 (依存関係順) に倣う。

## 全体方針

- F# の `Result<T, E>` / `AsyncResult` は **Vavr の `Either<L, R>` / `Try<T>`** で代替する (自前実装しない)。
- 値オブジェクトは Java の `record` + 静的ファクトリ (`Either<String, T> create(...)`) で表現する。
- 和型 (Discriminated Union) は `sealed interface` + `record` で表現する。
- ワークフローの各ステップは `Function<Input, Either<Error, Output>>` として宣言し、依存はコンストラクタ/関数引数で注入する。
- パッケージ構成は `CLAUDE.md` のヘキサゴナルアーキテクチャに従う。

## マッピング表

| F# ファイル | Java の配置先 | 備考 |
| --- | --- | --- |
| `Result.fs` | (skip) | Vavr の `Either` / `Try` で代替 |
| `Common.SimpleTypes.fs` | `domain/model/common/` | 制約付き値オブジェクト |
| `Common.CompoundTypes.fs` | `domain/model/common/` | レコードの合成 |
| `PlaceOrder.PublicTypes.fs` | `application/port/in/` + `domain/model/order/` | 公開境界の型 |
| `PlaceOrder.Implementation.fs` | `domain/service/` + `application/` | ワークフロー本体 |
| `PlaceOrder.Dto.fs` | `adapter/in/web/dto/` | DTO ⇄ ドメイン変換 |
| `PlaceOrder.Api.fs` | `adapter/in/web/` + `adapter/out/` | REST エントリと依存実装 |

---

## 進捗チェックリスト

### Phase 0: 基盤
- [x] Vavr (`vavr`, `vavr-jackson`, `vavr-test`) を `pom.xml` に追加

### Phase 1: 共通の単純型 (`Common.SimpleTypes.fs` → `domain/model/common/`)

制約付き値オブジェクト。各型に対し `record` + 静的 `create` (Vavr `Either<String, T>` を返す) を実装する。

- [x] `String50` (50 文字以下、非 null)
- [x] `EmailAddress` (`.+@.+` を満たす)
- [x] `ZipCode` (5 桁の数字)
- [x] `OrderId` (50 文字以下、非空)
- [x] `OrderLineId` (50 文字以下、非空)
- [ ] `WidgetCode` (`W` + 4 桁)
- [ ] `GizmoCode` (`G` + 3 桁)
- [ ] `ProductCode` (sealed: `WidgetCode | GizmoCode`)
- [ ] `UnitQuantity` (Integer, 1 〜 1000)
- [ ] `KilogramQuantity` (BigDecimal, 0.05 〜 100.00)
- [ ] `OrderQuantity` (sealed: `UnitQuantity | KilogramQuantity`)
- [ ] `Price` (BigDecimal, 0.0 〜 1000.00, `multiply` 付き)
- [ ] `BillingAmount` (BigDecimal, 0.0 〜 10000.00, `sumPrices` 付き)
- [ ] `PdfAttachment` (`record(String name, byte[] bytes)`)
- [ ] 各型のユニットテスト (境界値・異常系)

### Phase 2: 共通の複合型 (`Common.CompoundTypes.fs` → `domain/model/common/`)

Phase 1 の値オブジェクトを組み合わせたレコード。

- [ ] `PersonalName` (`FirstName`, `LastName`)
- [ ] `CustomerInfo` (`Name`, `EmailAddress`)
- [ ] `Address` (`AddressLine1` 〜 `AddressLine4`, `City`, `ZipCode`)

### Phase 3: ワークフロー公開型 (`PlaceOrder.PublicTypes.fs`)

境界づけられたコンテキストの公開境界。

- [ ] 入力 (`application/port/in/` 配下):
  - [ ] `UnvalidatedCustomerInfo`
  - [ ] `UnvalidatedAddress`
  - [ ] `UnvalidatedOrderLine`
  - [ ] `UnvalidatedOrder`
- [ ] 出力イベント (`domain/model/order/event/` 配下):
  - [ ] `OrderAcknowledgmentSent`
  - [ ] `OrderPlaced` (= `PricedOrder`)
  - [ ] `BillableOrderPlaced`
  - [ ] `PlaceOrderEvent` (sealed interface)
- [ ] 価格付き状態 (`domain/model/order/` 配下):
  - [ ] `PricedOrderLine`
  - [ ] `PricedOrder`
- [ ] エラー (`domain/model/order/error/` 配下):
  - [ ] `ValidationError`
  - [ ] `PricingError`
  - [ ] `RemoteServiceError` (`ServiceInfo` を含む)
  - [ ] `PlaceOrderError` (sealed interface)
- [ ] ユースケースインタフェース `PlaceOrderUseCase`
  - シグネチャ: `Either<PlaceOrderError, List<PlaceOrderEvent>> place(UnvalidatedOrder)`

### Phase 4: ワークフロー実装 (`PlaceOrder.Implementation.fs`)

- [ ] 出力ポート (`application/port/out/` 配下):
  - [ ] `CheckProductCodeExists` (`ProductCode -> boolean`)
  - [ ] `CheckAddressExists` (`UnvalidatedAddress -> Either<AddressValidationError, CheckedAddress>`)
  - [ ] `GetProductPrice` (`ProductCode -> Price`)
  - [ ] `CreateOrderAcknowledgmentLetter` (`PricedOrder -> HtmlString`)
  - [ ] `SendOrderAcknowledgment` (`OrderAcknowledgment -> SendResult`)
- [ ] 内部型 (`domain/model/order/internal/` 配下):
  - [ ] `CheckedAddress`
  - [ ] `ValidatedOrderLine`, `ValidatedOrder`
  - [ ] `HtmlString`
  - [ ] `OrderAcknowledgment`
  - [ ] `AddressValidationError` (sealed: `InvalidFormat | AddressNotFound`)
  - [ ] `SendResult` (sealed: `Sent | NotSent`)
- [ ] ステップ実装 (`domain/service/` 配下):
  - [ ] `ValidateOrder` (依存: `CheckProductCodeExists`, `CheckAddressExists`)
  - [ ] `PriceOrder` (依存: `GetProductPrice`)
  - [ ] `AcknowledgeOrder` (依存: `CreateOrderAcknowledgmentLetter`, `SendOrderAcknowledgment`)
  - [ ] `CreateEvents`
- [ ] 全体ワークフロー (`application/` 配下):
  - [ ] `PlaceOrderService` (`PlaceOrderUseCase` の実装、各ステップを合成)
- [ ] 各ステップのユニットテスト

### Phase 5: DTO 層 (`PlaceOrder.Dto.fs` → `adapter/in/web/dto/`)

各 DTO に `toDomain` / `fromDomain` (or `toUnvalidatedXxx`) を実装。

- [ ] `CustomerInfoDto`
- [ ] `AddressDto`
- [ ] `OrderFormLineDto`
- [ ] `OrderFormDto` (受信用ルート)
- [ ] `PricedOrderLineDto`
- [ ] `PricedOrderDto`
- [ ] `OrderPlacedDto`
- [ ] `BillableOrderPlacedDto`
- [ ] `OrderAcknowledgmentSentDto`
- [ ] `PlaceOrderEventDto` (発信用ルート、polymorphic)
- [ ] `PlaceOrderErrorDto`
- [ ] Jackson 設定で `VavrModule` を登録 (`Option`, `Either`, `List` のシリアライズ)

### Phase 6: API/アダプタ層 (`PlaceOrder.Api.fs`)

- [ ] 入力アダプタ (`adapter/in/web/`):
  - [ ] `PlaceOrderController` (`POST /orders`)
  - [ ] `PlaceOrderError` → HTTP ステータス変換
- [ ] 出力アダプタ (`adapter/out/`, ダミー実装):
  - [ ] `DummyProductCatalog` (`CheckProductCodeExists` + `GetProductPrice`)
  - [ ] `DummyAddressChecker` (`CheckAddressExists`)
  - [ ] `DummyAcknowledgmentSender` (`CreateOrderAcknowledgmentLetter` + `SendOrderAcknowledgment`)
- [ ] Spring の `@Configuration` で依存を組み立て、`PlaceOrderService` を Bean 化
- [ ] エンドツーエンドの WebMvcTest / 統合テスト

### Phase 7: 仕上げ (任意)
- [ ] README の整備 (起動方法、サンプル curl)
- [ ] 9 章版 (`PlaceOrder.Implementation(without effects).fs`) を別パッケージで再現するか検討
- [ ] 13 章以降 (`OrderTakingEvolved`) の取り込み検討

---

## 完了の定義 (Phase 6 まで)

- `./mvnw clean test` がパス
- `./mvnw spring-boot:run` でアプリ起動後、`POST /orders` に妥当な JSON を送ると 200 と `PlaceOrderEvent[]` (JSON) が返る
- 不正な入力に対しては 4xx と `PlaceOrderErrorDto` が返る
