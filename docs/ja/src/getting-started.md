# bytekinを始める

このセクションでは、bytekinのセットアップと最初のバイトコード変換の作成方法を説明します。

## 前提条件

- Java 8以上
- Javaの基本的な理解
- MavenまたはGradle(依存関係管理のため)

## インストール

### Maven

`pom.xml`に以下を追加してください:

```xml
<dependency>
    <groupId>io.github.brqnko.bytekin</groupId>
    <artifactId>bytekin</artifactId>
    <version>1.0</version>
</dependency>
```

### Gradle

`build.gradle`に以下を追加してください:

```gradle
dependencies {
    implementation 'io.github.brqnko.bytekin:bytekin:1.0'
}
```

## 最初の変換

### ステップ1: フッククラスの作成

ターゲットクラスをどのように変換したいかを定義する`@ModifyClass`アノテーションを持つクラスを作成します:

```java
import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.At;
import io.github.brqnko.bytekin.data.CallbackInfo;

@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
    public static CallbackInfo onAddHead(int a, int b) {
        System.out.println("Adding " + a + " + " + b);
        return CallbackInfo.empty();
    }
}
```

### ステップ2: トランスフォーマーの作成

フッククラスで`BytekinTransformer`をインスタンス化します:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(CalculatorHooks.class)
    .build();
```

### ステップ3: クラスの変換

ターゲットクラスのバイトコードに変換を適用します:

```java
byte[] originalBytecode = loadClassBytecode("com.example.Calculator");
byte[] transformedBytecode = transformer.transform("com.example.Calculator", originalBytecode);
```

### ステップ4: 変換されたクラスの使用

カスタム`ClassLoader`を使用して、変換されたバイトコードをJVMにロードします:

```java
ClassLoader loader = new ByteArrayClassLoader(transformedBytecode);
Class<?> transformedClass = loader.loadClass("com.example.Calculator");
```

## 結果

変換されたクラスには、`add`メソッドにロギングが追加されます:

```java
// 元のコード
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}

// 変換後のコード
public class Calculator {
    public int add(int a, int b) {
        System.out.println("Adding " + a + " + " + b);  // インジェクション済み!
        return a + b;
    }
}
```

## 次のステップ

- [コアコンセプト](./core-concepts.md)について学ぶ
- すべての[機能](./features.md)を探る
- [例](./examples.md)をチェックする
