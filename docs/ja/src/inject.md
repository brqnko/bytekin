# インジェクション変換

`@Inject`アノテーションを使用すると、ターゲットメソッドの特定のポイントにカスタムコードを挿入できます。

## 基本的な使用法

```java
@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @Inject(
        methodName = "add",
        methodDesc = "(II)I",
        at = At.HEAD
    )
    public static CallbackInfo onAddStart(int a, int b) {
        System.out.println("Adding: " + a + " + " + b);
        return CallbackInfo.empty();
    }
}
```

## アノテーションパラメータ

### methodName (必須)
インジェクション対象のメソッド名。

```java
methodName = "add"
```

### methodDesc (必須)
ターゲットメソッドシグネチャのJVMディスクリプタ。

```java
methodDesc = "(II)I"  // int add(int a, int b)
```

詳細は[メソッドディスクリプタ](./bytecode-basics.md#method-descriptors-signatures)をご覧ください。

### at (必須)
メソッド内のどこにコードをインジェクションするか。

## At列挙型 - インジェクションポイント

### At.HEAD
メソッドの最初、既存のコードの前にインジェクションします。

```java
@Inject(methodName = "calculate", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo atMethodStart() {
    System.out.println("Method started");
    return CallbackInfo.empty();
}
```

**結果:**
```java
public int calculate() {
    System.out.println("Method started");  // インジェクション済み
    // 元のコードはここ
}
```

### At.RETURN
メソッド内のすべてのreturn文の前にインジェクションします。

```java
@Inject(methodName = "getValue", methodDesc = "()I", at = At.RETURN)
public static CallbackInfo beforeReturn(CallbackInfo ci) {
    System.out.println("Returning: " + ci.returnValue);
    return CallbackInfo.empty();
}
```

**結果:**
```java
public int getValue() {
    if (condition) {
        System.out.println("Returning: " + value);  // インジェクション済み
        return value;
    }

    System.out.println("Returning: " + defaultValue);  // インジェクション済み
    return defaultValue;
}
```

### At.TAIL
メソッドの最後、すべてのコードの後、暗黙的なreturnの前にインジェクションします。

```java
@Inject(methodName = "cleanup", methodDesc = "()V", at = At.TAIL)
public static CallbackInfo atMethodEnd() {
    System.out.println("Cleanup complete");
    return CallbackInfo.empty();
}
```

## フックメソッドのパラメータ

フックメソッドは、ターゲットメソッドと同じパラメータに加えて`CallbackInfo`を受け取ります:

```java
// ターゲットメソッド:
public String process(String input, int count) { ... }

// フックメソッド:
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;I)Ljava/lang/String;", at = At.HEAD)
public static CallbackInfo processHook(String input, int count, CallbackInfo ci) {
    // パラメータにアクセス可能
    return CallbackInfo.empty();
}
```

## CallbackInfo - 動作の制御

`CallbackInfo`オブジェクトを使用して、インジェクションの動作を制御できます:

```java
public class CallbackInfo {
    public boolean cancelled;      // 元のコードをスキップするか?
    public Object returnValue;     // カスタム値を返すか?
}
```

### 実行のキャンセル

元のメソッドをスキップして早期にreturnします:

```java
@Inject(methodName = "authenticate", methodDesc = "(Ljava/lang/String;)Z", at = At.HEAD)
public static CallbackInfo checkPermission(String user, CallbackInfo ci) {
    if (!user.equals("admin")) {
        ci.cancelled = true;
        ci.returnValue = false;  // 元のコードを実行せずfalseを返す
    }
    return ci;
}
```

### カスタム戻り値

元の結果の代わりにカスタム値を返します:

```java
@Inject(methodName = "getCached", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
public static CallbackInfo useCachedValue(CallbackInfo ci) {
    Object cached = getFromCache();
    if (cached != null) {
        ci.cancelled = true;
        ci.returnValue = cached;
    }
    return ci;
}
```

## 複数のインジェクション

同じメソッドに複数回インジェクションできます:

```java
@ModifyClass("com.example.Service")
public class ServiceHooks {

    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
    public static CallbackInfo logStart(String input) {
        System.out.println("Start: " + input);
        return CallbackInfo.empty();
    }

    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.RETURN)
    public static CallbackInfo logEnd(String input) {
        System.out.println("End: " + input);
        return CallbackInfo.empty();
    }
}
```

両方のインジェクションが適用されます。

## インスタンスメソッド vs 静的メソッド

インスタンスメソッドの場合、最初のパラメータは通常`this`(オブジェクトインスタンス)です:

```java
// ターゲットインスタンスメソッド:
public class Calculator {
    public int add(int a, int b) { return a + b; }
}

// フックは'this'を受け取れます:
@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
public static CallbackInfo onAdd(Calculator self, int a, int b) {
    System.out.println("Calculator instance: " + self);
    return CallbackInfo.empty();
}
```

## ベストプラクティス

1. **フックをシンプルに保つ**: 複雑なロジックは別のメソッドに
2. **例外を避ける**: フックメソッド内で例外を処理する
3. **ガードにAt.HEADを使用**: 早期に条件をチェック
4. **At.RETURNに注意**: 複数のreturnには処理が必要
5. **十分にテスト**: インジェクションが正しく動作することを検証

## 例

完全な動作例については、[例 - インジェクション](./examples-basic.md#inject-examples)をご覧ください。

## 次のステップ

- メソッドインターセプションのための[インボケーション](./invoke.md)について学ぶ
- [高度な使用法](./advanced-usage.md)を探る
- [APIリファレンス](./api-reference.md)をチェック
