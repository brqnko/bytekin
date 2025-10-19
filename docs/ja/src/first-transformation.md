# 最初の変換

このガイドでは、シンプルなバイトコード変換を実演する完全な例を作成します。

## 例: 計算機にロギングを追加する

### ステップ1: ターゲットクラスの作成

まず、変換する簡単な計算機クラスを作成しましょう:

```java
package com.example;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }
}
```

### ステップ2: フックメソッドの作成

インジェクションしたい内容を定義する`@ModifyClass`アノテーション付きのクラスを作成します:

```java
package com.example;

import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.At;
import io.github.brqnko.bytekin.data.CallbackInfo;

@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @Inject(
        methodName = "add",
        methodDesc = "(II)I",
        at = At.HEAD
    )
    public static CallbackInfo logAdd(int a, int b) {
        System.out.println("Computing: " + a + " + " + b);
        return CallbackInfo.empty();
    }

    @Inject(
        methodName = "multiply",
        methodDesc = "(II)I",
        at = At.HEAD
    )
    public static CallbackInfo logMultiply(int a, int b) {
        System.out.println("Computing: " + a + " * " + b);
        return CallbackInfo.empty();
    }
}
```

### ステップ3: トランスフォーマーの構築

```java
package com.example;

import io.github.brqnko.bytekin.transformer.BytekinTransformer;

public class TransformerSetup {
    public static BytekinTransformer createTransformer() {
        return new BytekinTransformer.Builder(CalculatorHooks.class)
            .build();
    }
}
```

### ステップ4: 変換の適用

```java
package com.example;

import io.github.brqnko.bytekin.transformer.BytekinTransformer;

public class Main {
    public static void main(String[] args) {
        // 元のバイトコードを取得
        byte[] originalBytecode = getClassBytecode("com.example.Calculator");

        // トランスフォーマーを作成
        BytekinTransformer transformer = TransformerSetup.createTransformer();

        // 変換を適用
        byte[] transformedBytecode = transformer.transform(
            "com.example.Calculator",
            originalBytecode
        );

        // 変換されたクラスをロード
        Calculator calc = loadTransformedClass(transformedBytecode);

        // 変換されたクラスを使用
        int result = calc.add(5, 3);
        // 出力: "Computing: 5 + 3" その後 "8"

        result = calc.multiply(4, 7);
        // 出力: "Computing: 4 * 7" その後 "28"
    }

    // クラスバイトコードを取得するヘルパー(疑似コード)
    static byte[] getClassBytecode(String className) {
        // 実装はクラスローダーのセットアップに依存します
        return new byte[]{};
    }

    // 変換されたクラスをロードするヘルパー(疑似コード)
    static Calculator loadTransformedClass(byte[] bytecode) {
        // カスタムClassLoaderを使用してロード
        return null;
    }
}
```

## 何が起こったのか?

変換プロセス:

1. **スキャン**: `@Inject`アノテーションを持つメソッドを`CalculatorHooks`でスキャンしました
2. **発見**: `com.example.Calculator`をターゲットとするインジェクションを見つけました
3. **変更**: フックメソッドを呼び出すようにCalculatorクラスのバイトコードを変更しました
4. **挿入**: 指定されたメソッドの先頭にロギングコードを挿入しました

## 変換前と変換後

**変換前:**

```java
public int add(int a, int b) {
    return a + b;
}
```

**変換後:**

```java
public int add(int a, int b) {
    // インジェクションされたコード
    com.example.CalculatorHooks.logAdd(a, b);
    // 元のコード
    return a + b;
}
```

## 次のステップ

- [At列挙型](./inject.md#at-enum)で他のインジェクションポイントを探る
- [インボケーション変換](./invoke.md)について学ぶ
- より多くの[例](./examples.md)をチェックする
