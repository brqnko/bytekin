# bytekinドキュメント

**bytekin**の包括的なドキュメントへようこそ - 軽量なJavaバイトコード変換フレームワークです。

## ドキュメント概要

このドキュメントは、bytekinを始めてマスターするためのいくつかのセクションに整理されています:

### はじめに
- [イントロダクション](./introduction.md) - bytekinとその機能の概要
- [インストール](./installation.md) - プロジェクトにbytekinを追加する方法
- [最初の変換](./first-transformation.md) - 最初のバイトコード変換へのステップバイステップガイド

### bytekinを理解する
- [コアコンセプト](./core-concepts.md) - バイトコード変換の背後にある基本的な概念
- [バイトコードの基礎](./bytecode-basics.md) - Javaバイトコードを理解する
- [bytekinの仕組み](./how-it-works.md) - 内部アーキテクチャとメカニズム

### 機能
- [機能概要](./features.md) - 利用可能なすべての変換機能
- [インジェクション変換](./inject.md) - メソッドのポイントにコードを挿入
- [インボケーション変換](./invoke.md) - メソッド呼び出しをインターセプト
- [リダイレクト変換](./redirect.md) - メソッド呼び出しのターゲットを変更
- [定数の変更](./constant-modification.md) - ハードコードされた値を変更
- [変数の変更](./variable-modification.md) - ローカル変数を変換

### 高度なトピック
- [高度な使用法](./advanced-usage.md) - 高度なパターンとテクニック
- [マッピング](./mappings.md) - 名前マッピングで難読化されたコードを扱う
- [ビルダーパターン](./builder-pattern.md) - 設定のための流暢なAPI
- [カスタムトランスフォーマー](./custom-transformers.md) - カスタムトランスフォーマーを作成

### APIリファレンス
- [APIリファレンス](./api-reference.md) - 完全なAPIドキュメント
- [アノテーション](./annotations.md) - アノテーションリファレンスガイド
- [クラスとインターフェース](./classes-interfaces.md) - クラスドキュメント

### 例とガイド
- [基本的な例](./examples-basic.md) - 実用的なコード例
- [高度な例](./examples-advanced.md) - 高度なユースケースとパターン
- [ベストプラクティス](./best-practices.md) - 推奨されるプラクティスとパターン
- [FAQ](./faq.md) - よくある質問
- [トラブルシューティング](./troubleshooting.md) - 一般的な問題の解決策

## クイックスタート

### 1. インストール

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.brqnko.bytekin</groupId>
    <artifactId>bytekin</artifactId>
    <version>1.0</version>
</dependency>
```

```gradle
// Gradle
implementation 'io.github.brqnko.bytekin:bytekin:1.0'
```

### 2. フッククラスの作成

```java
@ModifyClass("com.example.Calculator")
public class CalculatorHooks {
    @Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
    public static CallbackInfo logAdd(int a, int b) {
        System.out.println("Adding: " + a + " + " + b);
        return CallbackInfo.empty();
    }
}
```

### 3. トランスフォーマーの構築

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(CalculatorHooks.class)
    .build();
```

### 4. バイトコードの変換

```java
byte[] original = getClassBytecode("com.example.Calculator");
byte[] transformed = transformer.transform("com.example.Calculator", original);
```

## 読み方ガイド

### やりたいこと...

**bytekinが何であるかを理解したい**
→ [イントロダクション](./introduction.md)から始めてください

**すぐに始めたい**
→ [インストール](./installation.md)と[最初の変換](./first-transformation.md)に従ってください

**バイトコード変換の仕組みを学びたい**
→ [コアコンセプト](./core-concepts.md)と[bytekinの仕組み](./how-it-works.md)を読んでください

**メソッドにコードをインジェクションしたい**
→ [インジェクション変換](./inject.md)と[基本的な例](./examples-basic.md)を見てください

**メソッド呼び出しをインターセプトしたい**
→ [インボケーション変換](./invoke.md)をチェックしてください

**難読化されたコードを扱いたい**
→ [マッピング](./mappings.md)について学んでください

**特定の問題を解決したい**
→ [FAQ](./faq.md)と[トラブルシューティング](./troubleshooting.md)を参照してください

**高度なパターンを見たい**
→ [高度な使用法](./advanced-usage.md)と[高度な例](./examples-advanced.md)をレビューしてください

**より良いコードを書きたい**
→ [ベストプラクティス](./best-practices.md)に従ってください

## 主要な概念

### メソッドディスクリプタ
JVMメソッドシグネチャは特別な形式を使用します:
- `(II)I` - 2つのintを受け取り、intを返す
- `(Ljava/lang/String;)V` - Stringを受け取り、voidを返す

詳細は[バイトコードの基礎](./bytecode-basics.md)をご覧ください

### フッククラス
変換を定義する`@ModifyClass`アノテーション付きのクラス:
```java
@ModifyClass("target.ClassName")
public class MyHooks { }
```

### CallbackInfo
変換の動作を制御します:
```java
public class CallbackInfo {
    public boolean cancelled;      // 実行をスキップするか?
    public Object returnValue;     // カスタムの戻り値?
    public Object[] modifyArgs;    // 変更された引数?
}
```

### 変換タイプ
1. **インジェクション** - 特定のポイントにコードを挿入
2. **インボケーション** - メソッド呼び出しをインターセプト
3. **リダイレクト** - 呼び出しターゲットを変更
4. **定数の変更** - ハードコードされた値を変更
5. **変数の変更** - 変数を変換

## 一般的なタスク

### ロギングの追加
[インジェクション変換](./inject.md) → [基本的な例](./examples-basic.md#example-1-adding-logging)

### パラメータの検証
[インジェクション変換](./inject.md) → [基本的な例](./examples-basic.md#example-2-parameter-validation)

### キャッシングの実装
[インボケーション変換](./invoke.md) → [基本的な例](./examples-basic.md#example-3-caching)

### セキュリティチェックの追加
[インジェクション変換](./inject.md) → [ベストプラクティス](./best-practices.md#security)

### パフォーマンスプロファイリング
[インジェクション変換](./inject.md) → [高度な例](./examples-advanced.md#example-7-performance-profiling)

### 難読化されたコードの処理
[マッピング](./mappings.md)

### カスタムトランスフォーマーの作成
[カスタムトランスフォーマー](./custom-transformers.md)

## ヘルプが必要ですか?

1. **FAQをチェック** - [FAQ](./faq.md)
2. **トラブルシューティングを検索** - [トラブルシューティング](./troubleshooting.md)
3. **例をレビュー** - [基本](./examples-basic.md)と[高度](./examples-advanced.md)
4. **ベストプラクティスを読む** - [ベストプラクティス](./best-practices.md)
5. **APIリファレンスをチェック** - [APIリファレンス](./api-reference.md)

## ナビゲーション

- [イントロダクション](./introduction.md) - ここから始めてください
- [はじめに](./getting-started.md) - インストールとセットアップ
- [コアコンセプト](./core-concepts.md) - 基本的な理解
- [機能](./features.md) - すべての機能
- [高度な使用法](./advanced-usage.md) - パターンとテクニック
- [例](./examples-basic.md) - コードサンプル
- [ベストプラクティス](./best-practices.md) - 推奨事項
- [APIリファレンス](./api-reference.md) - 完全なAPI
- [FAQ](./faq.md) - 質問と回答
- [トラブルシューティング](./troubleshooting.md) - 問題解決

## ドキュメントの状態

このドキュメントはbytekinバージョン**1.0**をカバーし、以下を含んでいます:

- ✅ 完全な機能ドキュメント
- ✅ APIリファレンス
- ✅ 複数の例
- ✅ ベストプラクティスガイド
- ✅ トラブルシューティングガイド
- ✅ FAQセクション
- ✅ 高度なパターン

## 貢献

エラーを見つけたか、ドキュメントを改善したいですか?
- [GitHub](https://github.com/brqnko/bytekin)で問題を報告してください
- プルリクエストで改善を貢献してください

## ライセンス

bytekinはApache License 2.0の下でライセンスされています。
詳細は[LICENSE](LICENSE)をご覧ください。

## 始める準備はできましたか?

→ [インストールから始める](./installation.md)

→ [最初の変換を構築する](./first-transformation.md)

→ [すべての機能を探る](./features.md)
