# アノテーションリファレンス

bytekinアノテーションの完全なリファレンスです。

## @ModifyClass

### 目的
バイトコード変換のためのフックメソッドを含むクラスをマークします。

### 使用方法
```java
@ModifyClass("com.example.TargetClass")
public class MyHooks {
    // ここにフックメソッド
}
```

### パラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|------|----------|-------------|
| className | String | はい | ターゲットクラスの完全修飾名 |

### スコープ
クラス型にのみ適用されます。

## @Inject

### 目的
メソッドの特定の位置にコードをインジェクトします。

### 使用方法
```java
@Inject(
    methodName = "myMethod",
    methodDesc = "(I)Ljava/lang/String;",
    at = At.HEAD
)
public static CallbackInfo hook(int param) { }
```

### パラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|------|----------|-------------|
| methodName | String | はい | ターゲットメソッド名 |
| methodDesc | String | はい | メソッドディスクリプタ(JVM形式) |
| at | At | はい | コードをインジェクトする位置 |

### スコープ
メソッドにのみ適用されます。

### 戻り値の型
`CallbackInfo`を返す必要があります。

## @Invoke

### 目的
メソッド呼び出しをインターセプトします。

### 使用方法
```java
@Invoke(
    targetMethodName = "parentMethod",
    targetMethodDesc = "()V",
    invokeMethodName = "childMethod",
    invokeMethodDesc = "(I)V",
    shift = Shift.BEFORE
)
public static CallbackInfo hook() { }
```

### パラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|------|----------|-------------|
| targetMethodName | String | はい | 呼び出しを含むメソッド |
| targetMethodDesc | String | はい | ターゲットメソッドのディスクリプタ |
| invokeMethodName | String | はい | 呼び出されるメソッドの名前 |
| invokeMethodDesc | String | はい | 呼び出されるメソッドのディスクリプタ |
| shift | Shift | はい | 呼び出しの前か後か |

### スコープ
メソッドにのみ適用されます。

## @Redirect

### 目的
メソッド呼び出しを別のターゲットにリダイレクトします。

### 使用方法
```java
@Redirect(
    targetMethodName = "oldMethod",
    targetMethodDesc = "()V",
    redirectMethodName = "newMethod",
    redirectMethodDesc = "()V"
)
public static void hook() { }
```

### パラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|------|----------|-------------|
| targetMethodName | String | はい | 呼び出しを含むメソッド |
| targetMethodDesc | String | はい | ターゲットメソッドのディスクリプタ |
| redirectMethodName | String | はい | リダイレクトメソッドの名前 |
| redirectMethodDesc | String | はい | リダイレクトメソッドのディスクリプタ |

## @ModifyConstant

### 目的
バイトコード内の定数値を変更します。

### 使用方法
```java
@ModifyConstant(
    methodName = "getConfig",
    oldValue = "dev",
    newValue = "prod"
)
public static CallbackInfo hook() { }
```

### パラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|------|----------|-------------|
| methodName | String | はい | 定数を含むメソッド |
| oldValue | Object | はい | 元の定数値 |
| newValue | Object | はい | 新しい定数値 |

## @ModifyVariable

### 目的
ローカル変数の値を変更します。

### 使用方法
```java
@ModifyVariable(
    methodName = "process",
    variableIndex = 1
)
public static void hook(String param) { }
```

### パラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|------|----------|-------------|
| methodName | String | はい | ターゲットメソッド名 |
| variableIndex | int | はい | ローカル変数スロットのインデックス |

## 列挙型: At

### 値

| 値 | 説明 |
|-------|-------------|
| HEAD | メソッドの開始位置、すべてのコードの前 |
| RETURN | 各return文の前 |
| TAIL | メソッドの終了位置 |

## 列挙型: Shift

### 値

| 値 | 説明 |
|-------|-------------|
| BEFORE | メソッド呼び出しの前にフックを実行 |
| AFTER | メソッド呼び出しの後にフックを実行 |

## 次のステップ

- [クラスとインターフェース](./classes-interfaces.md)をレビュー
- [APIリファレンス](./api-reference.md)をチェック
- [例](./examples.md)を探索
