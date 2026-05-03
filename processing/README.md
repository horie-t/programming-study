# processing

[Processing](https://processing.org/) を Java から使うための学習用 Maven プロジェクトです。`main` メソッドを持つスケッチ（クラス）を `src/main/java/` 配下に複数置いて、個別に実行する想定です。

## 必要環境

- JDK 17 以上
- Maven 3.9+
- GUI 表示できる環境（WSL2 の場合は WSLg もしくは X サーバ）

## ディレクトリ構成

```
processing/
├── pom.xml
├── README.md
└── src/main/java/
    └── BouncingBall.java   ← main を持つスケッチをここに追加していく
```

## ビルド

```bash
mvn compile      # コンパイルだけ
mvn package      # JAR 作成（target/processing-1.0-SNAPSHOT.jar）
```

## 実行

`exec-maven-plugin` 経由で実行します。`main` メソッドを持つクラスを `-Dexec.mainClass` で **毎回明示** してください。

```bash
mvn exec:java -Dexec.mainClass=BouncingBall
```

別のスケッチを追加したら、そのクラス名を渡せば実行できます。

```bash
mvn exec:java -Dexec.mainClass=YourSketch
```

パッケージに分けた場合は完全修飾名で指定します。

```bash
mvn exec:java -Dexec.mainClass=sketch.demo.YourSketch
```

> `mvn exec:java` は依存ライブラリ（`processing-core`）を自動でクラスパスに含めるため、これだけで起動できます。`mvn package` で生成される JAR は依存を含まない薄い JAR なので `java -jar` では起動できません。

## 新しいスケッチの追加方法

1. `src/main/java/` に `Foo.java` などを作成し、`PApplet` を継承して `main` メソッドを書く。

    ```java
    import processing.core.PApplet;

    public class Foo extends PApplet {
        public static void main(String[] args) {
            PApplet.main("Foo");
        }

        @Override
        public void settings() { size(400, 400); }

        @Override
        public void draw() { background(0); }
    }
    ```

2. 実行する。

    ```bash
    mvn exec:java -Dexec.mainClass=Foo
    ```

## 依存ライブラリ

- `org.processing:core:4.5.3` — Processing の中核ライブラリ（`PApplet` など）
