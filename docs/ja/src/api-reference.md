# APIリファレンス

このセクションではbytekinの詳細なAPIドキュメントを提供します。

## コアクラス

### BytekinTransformer

バイトコード変換のメインエントリーポイント。

```java
public class BytekinTransformer {
    public byte[] transform(String className, byte[] bytes, int api);

    public static class Builder {
        public Builder(Class<?>... classes);
        public Builder mapping(IMappingProvider mapping);
        public Builder inject(String className, Injection injection);
        public Builder invoke(String className, Invocation invocation);
        public Builder redirect(String className, RedirectData redirect);
        public Builder modifyConstant(String className, ConstantModification modification);
        public Builder modifyVariable(String className, VariableModification modification);
        public BytekinTransformer build();
    }
}
```

### CallbackInfo

フックメソッド内で変換動作を制御します。

```java
public class CallbackInfo {
    public boolean cancelled;
    public Object returnValue;
    public Object[] modifyArgs;

    public static CallbackInfo empty();
}
```

## アノテーション

### @ModifyClass

バイトコード変換のフックコンテナとしてクラスをマークします。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyClass {
    String className();
}
```

### @Inject

メソッドの特定の箇所にコードをインジェクトします。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    String methodName();
    String methodDesc();
    At at();
}
```

### @Invoke

メソッド呼び出しをインターセプトします。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Invoke {
    String targetMethodName();
    String targetMethodDesc();
    String invokeMethodName();
    String invokeMethodDesc();
    Shift shift();
}
```

### @Redirect

メソッド呼び出しを別のターゲットにリダイレクトします。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Redirect {
    String targetMethodName();
    String targetMethodDesc();
    String redirectMethodName();
    String redirectMethodDesc();
}
```

### @ModifyConstant

バイトコード内の定数値を変更します。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyConstant {
    String methodName();
    Object oldValue();
    Object newValue();
}
```

### @ModifyVariable

ローカル変数の値を変更します。

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModifyVariable {
    String methodName();
    int variableIndex();
}
```

## 列挙型

### At

コードをインジェクトする場所を指定します。

```java
public enum At {
    HEAD,      // メソッド本体の前
    RETURN,    // return文の前
    TAIL       // メソッドの終わり
}
```

### Shift

メソッド呼び出しに対する相対的なタイミングを指定します。

```java
public enum Shift {
    BEFORE,    // 呼び出しの前
    AFTER      // 呼び出しの後
}
```

## インターフェース

### IMappingProvider

クラス名とメソッド名をマッピングします。

```java
public interface IMappingProvider {
    String getClassName(String name);
    String getMethodName(String className, String methodName, String descriptor);
    String getFieldName(String className, String fieldName);
}
```

## データクラス

### Injection

インジェクション変換を表します。

```java
public class Injection {
    // コンストラクタとメソッド
}
```

### Invocation

インボケーション変換を表します。

```java
public class Invocation {
    // コンストラクタとメソッド
}
```

### RedirectData

リダイレクト変換を表します。

```java
public class RedirectData {
    // コンストラクタとメソッド
}
```

### ConstantModification

定数変更変換を表します。

```java
public class ConstantModification {
    // コンストラクタとメソッド
}
```

### VariableModification

変数変更変換を表します。

```java
public class VariableModification {
    // コンストラクタとメソッド
}
```

## 一般的な例外

### VerifyError

変換されたバイトコードが無効な場合にスローされます。

### ClassNotFoundException

ターゲットクラスが見つからない場合にスローされます。

### ClassFormatException

バイトコード形式が無効な場合にスローされます。

## ユーティリティクラス

### DescriptorParser

メソッドディスクリプタの解析と検証のためのユーティリティ。

### BytecodeManipulator

低レベルのバイトコード操作ユーティリティ。

## スレッディング

すべてのpublicメソッドは**初期化後はスレッドセーフ**です:

- `BytekinTransformer.transform()`は複数のスレッドから呼び出し可能
- `Builder`は設定中は**スレッドセーフではない**
- `CallbackInfo`は各フック呼び出しにローカル

## パフォーマンス特性

| 操作 | 複雑さ |
|-----------|-----------|
| Builder.build() | O(n) n = フックメソッド数 |
| transform() | O(m) m = バイトコードサイズ |
| フックの実行 | 平均O(1) |

## 次のステップ

- [アノテーション](./annotations.md)を詳しく確認する
- [クラスとインターフェース](./classes-interfaces.md)を確認する
- [例](./examples.md)を探索する
