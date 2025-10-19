# 機能概要

bytekinはJavaバイトコードを操作するためのいくつかの強力な変換機能を提供します。このセクションでは各機能の概要を説明します。

## 利用可能な機能

### 1. Inject - コードの挿入

ソースコードを変更せずに、メソッドの特定の箇所にカスタムコードを挿入します。

**ユースケース:**
- ログステートメントの追加
- 横断的関心事の実装
- セキュリティチェックの追加
- パラメータの検証

**例:**
```java
@Inject(methodName = "calculate", methodDesc = "(II)I", at = At.HEAD)
public static CallbackInfo logStart(int a, int b) {
    System.out.println("Starting calculation");
    return CallbackInfo.empty();
}
```

**詳細:** [Inject変換](./inject.md)

### 2. Invoke - メソッド呼び出しのインターセプト

メソッド呼び出しをインターセプトし、必要に応じて引数や戻り値を変更します。

**ユースケース:**
- 特定のメソッド呼び出しのインターセプト
- メソッド引数の変更
- メソッドのモックやスタブ化
- 前処理/後処理の追加

**例:**
```java
@Invoke(
    targetMethodName = "process",
    targetMethodDesc = "(Ljava/lang/String;)V",
    invokeMethodName = "validate",
    invokeMethodDesc = "(Ljava/lang/String;)V",
    shift = Shift.BEFORE
)
public static CallbackInfo validateBefore(String input) {
    return new CallbackInfo(false, null, new Object[]{input.trim()});
}
```

**詳細:** [Invoke変換](./invoke.md)

### 3. Redirect - メソッド呼び出しのリダイレクト

実行時に呼び出されるメソッドを変更します。

**ユースケース:**
- 代替実装への呼び出しのリダイレクト
- メソッド動作のモック
- メソッド転送の実装
- 条件に基づく動作の変更

**例:**
```java
@Redirect(
    targetMethodName = "oldMethod",
    targetMethodDesc = "(I)V",
    redirectMethodName = "newMethod",
    redirectMethodDesc = "(I)V"
)
public static void redirectCall(int value) {
    System.out.println("Redirected to new method: " + value);
}
```

**詳細:** [Redirect変換](./redirect.md)

### 4. 定数の変更

バイトコードに埋め込まれた定数値を変更します。

**ユースケース:**
- ハードコードされた設定値の変更
- 文字列リテラルの変更
- 数値定数の変更
- 実行時の定数のパッチ

**例:**
```java
@ModifyConstant(
    methodName = "getVersion",
    oldValue = "1.0",
    newValue = "2.0"
)
public static CallbackInfo updateVersion() {
    return CallbackInfo.empty();
}
```

**詳細:** [定数の変更](./constant-modification.md)

### 5. 変数の変更

メソッド内のローカル変数の値を変更します。

**ユースケース:**
- 入力のサニタイゼーション
- データの変換
- 変数値のデバッグ
- カスタムロジックの実装

**例:**
```java
@ModifyVariable(
    methodName = "process",
    variableIndex = 1
)
public static void transformVariable(int original) {
    // 変換ロジック
}
```

**詳細:** [変数の変更](./variable-modification.md)

## 機能の組み合わせ

複雑な変換のために複数の機能を組み合わせて使用できます:

```java
@ModifyClass("com.example.Service")
public class ServiceHooks {

    // ロギングのインジェクション
    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
    public static CallbackInfo logStart(String input) {
        System.out.println("Processing: " + input);
        return CallbackInfo.empty();
    }

    // 内部呼び出しのインターセプト
    @Invoke(
        targetMethodName = "handle",
        targetMethodDesc = "(Ljava/lang/String;)V",
        invokeMethodName = "validate",
        invokeMethodDesc = "(Ljava/lang/String;)V",
        shift = Shift.BEFORE
    )
    public static CallbackInfo validateInput(String input) {
        return new CallbackInfo(false, null, new Object[]{sanitize(input)});
    }

    private static String sanitize(String input) {
        return input.trim().toLowerCase();
    }
}
```

## 適切な機能の選択

| 機能 | 目的 | 複雑さ |
|---------|---------|-----------|
| Inject | メソッドの特定箇所にコードを挿入 | 低 |
| Invoke | 特定の呼び出しをインターセプト | 中 |
| Redirect | 呼び出し先を変更 | 中 |
| 定数の変更 | ハードコードされた値を変更 | 低 |
| 変数の変更 | ローカル変数を変換 | 高 |

## 次のステップ

- [Inject](./inject.md)変換について学ぶ
- [Invoke](./invoke.md)インターセプトを探索する
- [例](./examples.md)を確認する
