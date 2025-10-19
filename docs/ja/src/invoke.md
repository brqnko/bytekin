# インボケーション変換

`@Invoke`アノテーションを使用すると、メソッド呼び出しをインターセプトし、実行前に引数をオプションで変更できます。

## 基本的な使用法

```java
@ModifyClass("com.example.DataProcessor")
public class ProcessorHooks {

    @Invoke(
        targetMethodName = "process",
        targetMethodDesc = "(Ljava/lang/String;)V",
        invokeMethodName = "validate",
        invokeMethodDesc = "(Ljava/lang/String;)V",
        shift = Shift.BEFORE
    )
    public static CallbackInfo validateBeforeProcess(String data) {
        if (data == null || data.isEmpty()) {
            return new CallbackInfo(true, null, new Object[]{"default"});
        }
        return CallbackInfo.empty();
    }
}
```

## アノテーションパラメータ

### targetMethodName (必須)

インターセプトする呼び出しを含むメソッド名。

```java
targetMethodName = "process"
```

### targetMethodDesc (必須)

呼び出しを含むメソッドのJVMディスクリプタ。

```java
targetMethodDesc = "(Ljava/lang/String;)V"
```

### invokeMethodName (必須)

呼び出されているメソッド(インターセプトしたいもの)の名前。

```java
invokeMethodName = "helper"
```

### invokeMethodDesc (必須)

呼び出されているメソッドのJVMディスクリプタ。

```java
invokeMethodDesc = "(I)Ljava/lang/String;"
```

### shift (必須)

メソッド呼び出しに対してフックを実行するタイミング。

## Shift列挙型 - タイミング

### Shift.BEFORE

メソッドが呼び出される**前**にフックを実行します。

```java
@Invoke(
    targetMethodName = "process",
    targetMethodDesc = "(Ljava/lang/String;)V",
    invokeMethodName = "validate",
    invokeMethodDesc = "(Ljava/lang/String;)V",
    shift = Shift.BEFORE
)
public static CallbackInfo beforeCall(String input) {
    System.out.println("Before calling validate");
    return CallbackInfo.empty();
}
```

**結果:**
```java
public void process(String input) {
    System.out.println("Before calling validate");  // インジェクション済み
    validate(input);
    // 残りのコード
}
```

### Shift.AFTER

メソッドが呼び出された**後**にフックを実行します。

```java
@Invoke(
    targetMethodName = "process",
    targetMethodDesc = "(Ljava/lang/String;)V",
    invokeMethodName = "save",
    invokeMethodDesc = "()V",
    shift = Shift.AFTER
)
public static CallbackInfo afterCall() {
    System.out.println("After calling save");
    return CallbackInfo.empty();
}
```

**結果:**
```java
public void process(String input) {
    // 何らかのコード
    save();
    System.out.println("After calling save");  // インジェクション済み
}
```

## 引数の変更

`CallbackInfo`を使用して、インターセプトされた呼び出しに渡される引数を変更します:

```java
@Invoke(
    targetMethodName = "processData",
    targetMethodDesc = "(Ljava/lang/String;I)V",
    invokeMethodName = "transform",
    invokeMethodDesc = "(Ljava/lang/String;I)V",
    shift = Shift.BEFORE
)
public static CallbackInfo sanitizeInput(String data, int count, CallbackInfo ci) {
    // 引数を変更
    String sanitized = data.trim().toLowerCase();
    int newCount = Math.max(0, count);

    ci.modifyArgs = new Object[]{sanitized, newCount};
    return ci;
}
```

**結果:**
```java
public void processData(String data, int count) {
    // 元: transform(data, count);
    // フック後: transform(data.trim().toLowerCase(), max(0, count));
    transform(data, count);
}
```

## メソッド呼び出しのキャンセル

メソッドが呼び出されないようにします:

```java
@Invoke(
    targetMethodName = "deleteFile",
    targetMethodDesc = "(Ljava/lang/String;)Z",
    invokeMethodName = "delete",
    invokeMethodDesc = "(Ljava/io/File;)Z",
    shift = Shift.BEFORE
)
public static CallbackInfo preventDeletion(File file, CallbackInfo ci) {
    if (isSystemFile(file)) {
        // delete()を呼び出さず、falseを返す
        ci.cancelled = true;
        ci.returnValue = false;
    }
    return ci;
}
```

## 戻り値の処理

戻り値にアクセスして変更します(`Shift.AFTER`の場合):

```java
@Invoke(
    targetMethodName = "getValue",
    targetMethodDesc = "()I",
    invokeMethodName = "compute",
    invokeMethodDesc = "()I",
    shift = Shift.AFTER
)
public static CallbackInfo modifyReturnValue(CallbackInfo ci) {
    // 戻り値にアクセス
    int original = (int) ci.returnValue;

    // 変更する
    ci.returnValue = original * 2;

    return ci;
}
```

## 複雑な例

```java
@ModifyClass("com.example.UserService")
public class UserServiceHooks {

    // ログイン試行をインターセプト
    @Invoke(
        targetMethodName = "authenticate",
        targetMethodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z",
        invokeMethodName = "validateCredentials",
        invokeMethodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z",
        shift = Shift.BEFORE
    )
    public static CallbackInfo logAuthAttempt(
        String username, String password, CallbackInfo ci
    ) {
        // 試行をログに記録
        System.out.println("Auth attempt for: " + username);

        // 特定のユーザー名をブロック
        if (username.equals("blocked")) {
            ci.cancelled = true;
            ci.returnValue = false;
        }

        return ci;
    }
}
```

## ベストプラクティス

1. **呼び出しフローを理解する**: メソッドがどこで呼び出されるかを知る
2. **タイミングを考慮する**: 入力検証には`BEFORE`、出力変換には`AFTER`
3. **引数の変更をテスト**: 型が一致することを確認
4. **キャンセルを慎重に処理**: 呼び出し元のコードがキャンセルされた呼び出しを処理することを確認
5. **パフォーマンスをプロファイル**: フックはすべての呼び出しで実行される

## 制限事項

- 明示的なメソッド呼び出しのみインターセプト可能、バイトコードからの仮想メソッド呼び出しは不可
- コンストラクタへの呼び出しを直接インターセプトできない
- すべての呼び出しでパフォーマンスへの影響が発生する

## 例

完全な動作例については、[例 - インボケーション](./examples-basic.md#invoke-examples)をご覧ください。

## 次のステップ

- [リダイレクト](./redirect.md)変換について学ぶ
- [高度な使用法](./advanced-usage.md)を探る
- [APIリファレンス](./api-reference.md)をチェック
