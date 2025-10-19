# 変数の変更

バイトコード変換中にメソッド内のローカル変数の値を変更します。

## 基本的な使用方法

```java
@ModifyClass("com.example.Processor")
public class ProcessorHooks {

    @ModifyVariable(
        methodName = "process",
        variableIndex = 1
    )
    public static void sanitizeInput(String original) {
        // 変換ロジック
    }
}
```

## 変数インデックスの理解

メソッド内のローカル変数はスロットに格納され、0から始まるインデックスが付けられます:

### インスタンスメソッド

```java
public void process(String name, int count) {
    String result;
    // ...
}
```

変数インデックス:
- `0`: `this`（暗黙的）
- `1`: `name`（第1引数）
- `2`: `count`（第2引数）
- `3`: `result`（ローカル変数）

### 静的メソッド

```java
public static void process(String name, int count) {
    String result;
    // ...
}
```

変数インデックス:
- `0`: `name`（第1引数）
- `1`: `count`（第2引数）
- `2`: `result`（ローカル変数）

## アノテーションパラメータ

### methodName（必須）

対象メソッドの名前。

```java
methodName = "process"
```

### variableIndex（必須）

変更するローカル変数のインデックス。

```java
variableIndex = 1
```

## パラメータの変更

メソッドパラメータを変換:

```java
@ModifyClass("com.example.UserService")
public class UserServiceHooks {

    @ModifyVariable(
        methodName = "createUser",
        variableIndex = 1  // 第1引数: email
    )
    public static void normalizeEmail(String original) {
        // 元のメールアドレスが正規化される
        // 例: "USER@EXAMPLE.COM" が "user@example.com" になる
    }
}
```

**変更前:**
```java
public void createUser(String email) {
    // email = "USER@EXAMPLE.COM"
    // ...
}
```

**変更後:**
```java
public void createUser(String email) {
    // email = "user@example.com" （正規化済み）
    // ...
}
```

## ローカル変数の変更

メソッド内で作成された変数を変換:

```java
@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @ModifyVariable(
        methodName = "calculateTotal",
        variableIndex = 2  // ローカル変数: total
    )
    public static void applyTaxToTotal(int original) {
        // total に1.1を掛けて税を適用
    }
}
```

## ユースケース

### 入力のサニタイゼーション

メソッド入力をクリーンアップ:

```java
@ModifyClass("com.example.WebService")
public class WebServiceHooks {

    @ModifyVariable(
        methodName = "handleRequest",
        variableIndex = 1  // request パラメータ
    )
    public static void sanitizeRequest(String original) {
        // 悪意のある文字を削除
    }

    @ModifyVariable(
        methodName = "handleRequest",
        variableIndex = 2  // path パラメータ
    )
    public static void validatePath(String original) {
        // パスがルートディレクトリから逸脱しないことを確認
    }
}
```

### データ変換

データ形式を変換:

```java
@ModifyClass("com.example.DateProcessor")
public class DateProcessorHooks {

    @ModifyVariable(
        methodName = "processDate",
        variableIndex = 1  // date パラメータ
    )
    public static void convertToUTC(String original) {
        // ローカル時間からUTCに変換
    }
}
```

### 型変換

データ型を変更:

```java
@ModifyClass("com.example.Converter")
public class ConverterHooks {

    @ModifyVariable(
        methodName = "process",
        variableIndex = 1  // number パラメータ
    )
    public static void convertToPercentage(int original) {
        // 生の数値をパーセンテージに変換
    }
}
```

## 高度なパターン

### 複数の変数

同じメソッド内の複数の変数を変更:

```java
@ModifyClass("com.example.Transfer")
public class TransferHooks {

    @ModifyVariable(
        methodName = "transfer",
        variableIndex = 1  // 送信元アカウント
    )
    public static void validateFromAccount(String original) {
        // 送信元アカウントを検証
    }

    @ModifyVariable(
        methodName = "transfer",
        variableIndex = 2  // 送信先アカウント
    )
    public static void validateToAccount(String original) {
        // 送信先アカウントを検証
    }

    @ModifyVariable(
        methodName = "transfer",
        variableIndex = 3  // 金額
    )
    public static void validateAmount(long original) {
        // 金額が正であることを確認
    }
}
```

3つすべての変更が同じメソッドに適用されます。

## 型の保持

変更中に変数の型は保持されます:

```java
@ModifyClass("com.example.Data")
public class DataHooks {

    // String パラメータの変更
    @ModifyVariable(methodName = "processName", variableIndex = 1)
    public static void transformName(String original) { }

    // int パラメータの変更
    @ModifyVariable(methodName = "processCount", variableIndex = 1)
    public static void transformCount(int original) { }

    // List パラメータの変更
    @ModifyVariable(methodName = "processItems", variableIndex = 1)
    public static void transformItems(List<?> original) { }
}
```

各フックは自動的に正しい型を受け取ります。

## 制限事項

### 変更できないもの

- 使用されない変数
- JVMによって最適化されて削除された値を持つ変数
- 初期化後に複雑な方法で変更される変数

### 課題

1. **インデックスの計算**: 変数インデックスを正しく識別する必要がある
2. **型安全性**: パラメータの型が一致する必要がある
3. **スコープ**: 変更はそのメソッド内でのみ有効
4. **デバッグ**: 変更の追跡が困難な場合がある

## 正しい変数インデックスの特定

`javap`を使用して変数レイアウトを検査:

```bash
javap -c -private MyClass.class | grep -A 50 "methodName"
```

変数の位置を示すLocalVariableTableを探してください。

## ベストプラクティス

1. **インデックスを文書化する**: どの変数がどのインデックスかを明確にコメントする
2. **変換をシンプルに保つ**: 複雑なロジックは別にすべき
3. **セマンティクスを保持する**: 変更された値が意味をなすことを確認する
4. **徹底的にテストする**: 変更された変数での動作を検証する
5. **インスペクタを使用する**: 適用前にインデックスが正しいことを確認する

## 他の機能との組み合わせ

変数の変更をインジェクションと併用:

```java
@ModifyClass("com.example.Service")
public class ServiceHooks {

    @Inject(
        methodName = "handle",
        methodDesc = "(Ljava/lang/String;)V",
        at = At.HEAD
    )
    public static CallbackInfo validateInput(String input) {
        if (input == null || input.isEmpty()) {
            return new CallbackInfo(true, null, null);
        }
        return CallbackInfo.empty();
    }

    @ModifyVariable(
        methodName = "handle",
        variableIndex = 1  // input パラメータ
    )
    public static void normalizeInput(String original) {
        // 入力も正規化
    }
}
```

## 次のステップ

- [高度な使用方法](./advanced-usage.md)を探索する
- [マッピング](./mappings.md)について学ぶ
- [例](./examples.md)を確認する
