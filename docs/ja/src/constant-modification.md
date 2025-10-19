# 定数の変更

ソースコードを再コンパイルすることなく、バイトコード内のハードコードされた定数値を変更します。

## 基本的な使用方法

```java
@ModifyClass("com.example.Config")
public class ConfigHooks {

    @ModifyConstant(
        methodName = "getVersion",
        oldValue = "1.0.0",
        newValue = "2.0.0"
    )
    public static CallbackInfo updateVersion() {
        return CallbackInfo.empty();
    }
}
```

## 変更可能な定数

- 文字列リテラル
- 数値定数（int、long、float、double）
- ブール定数
- 定数プール内の定数

## 文字列定数

```java
@ModifyClass("com.example.App")
public class AppHooks {

    @ModifyConstant(
        methodName = "getAPIEndpoint",
        oldValue = "http://localhost:8080",
        newValue = "https://api.production.com"
    )
    public static CallbackInfo updateEndpoint() {
        return CallbackInfo.empty();
    }
}
```

**変更前:**
```java
public String getAPIEndpoint() {
    return "http://localhost:8080";
}
```

**変更後:**
```java
public String getAPIEndpoint() {
    return "https://api.production.com";
}
```

## 数値定数

```java
@ModifyClass("com.example.Limits")
public class LimitsHooks {

    @ModifyConstant(
        methodName = "getMaxConnections",
        oldValue = 10,
        newValue = 100
    )
    public static CallbackInfo increaseLimit() {
        return CallbackInfo.empty();
    }
}
```

## 同じメソッド内の複数の定数

```java
@ModifyClass("com.example.Config")
public class ConfigHooks {

    @ModifyConstant(
        methodName = "initialize",
        oldValue = "DEBUG",
        newValue = "PRODUCTION"
    )
    public static CallbackInfo updateMode() {
        return CallbackInfo.empty();
    }

    @ModifyConstant(
        methodName = "initialize",
        oldValue = "localhost",
        newValue = "example.com"
    )
    public static CallbackInfo updateHost() {
        return CallbackInfo.empty();
    }
}
```

両方の変更が同じメソッドに適用されます。

## ユースケース

### 環境設定

環境固有の値を変更:

```java
@ModifyClass("com.example.Environment")
public class EnvironmentHooks {

    @ModifyConstant(
        methodName = "getDatabaseURL",
        oldValue = "jdbc:mysql://dev.local:3306/db",
        newValue = "jdbc:mysql://prod.remote:3306/db"
    )
    public static CallbackInfo updateDatabase() {
        return CallbackInfo.empty();
    }
}
```

### フィーチャーフラグ

再コンパイルなしで機能を有効/無効化:

```java
@ModifyClass("com.example.Features")
public class FeatureHooks {

    @ModifyConstant(
        methodName = "isNewFeatureEnabled",
        oldValue = false,
        newValue = true
    )
    public static CallbackInfo enableFeature() {
        return CallbackInfo.empty();
    }
}
```

### APIバージョニング

APIエンドポイントを更新:

```java
@ModifyClass("com.example.API")
public class APIHooks {

    @ModifyConstant(
        methodName = "getAPIVersion",
        oldValue = "v1",
        newValue = "v3"
    )
    public static CallbackInfo updateAPIVersion() {
        return CallbackInfo.empty();
    }
}
```

## パフォーマンスへの影響

定数の変更は**最小限のランタイムオーバーヘッド**です:

1. 変更はバイトコード変換時に行われる（1回のみ）
2. ランタイムパフォーマンスは再コンパイルされたコードと同一
3. JVMは変更された定数を最適化できる

## 制限事項

### 変更できないもの

- ローカル変数の初期化（一部のケース）
- 実行時に作成される定数
- JVMによってすでに最適化された定数

### 型の一致

古い値と新しい値は互換性のある型である必要があります:

```java
// 正しい
@ModifyConstant(
    methodName = "getCount",
    oldValue = 10,        // int
    newValue = 20         // int - 互換性あり
)

// 誤り
@ModifyConstant(
    methodName = "getCount",
    oldValue = 10,        // int
    newValue = "20"       // String - 互換性なし!
)
```

## ベストプラクティス

1. **設定に使用する**: ロジックの変更には使用しない
2. **明確に文書化する**: 定数が変更される理由を説明する
3. **値の一貫性を保つ**: 正確な古い値を使用する
4. **すべてのパスをテストする**: 新しい値でコードが動作することを確認する
5. **型の変更を避ける**: 型の互換性を保つ

## 応用: 条件付き変更

より詳細な制御のために、他の変換と組み合わせる:

```java
@ModifyClass("com.example.Service")
public class ServiceHooks {

    @Inject(
        methodName = "initialize",
        methodDesc = "()V",
        at = At.HEAD
    )
    public static CallbackInfo checkEnvironment() {
        String env = System.getProperty("environment");
        if ("production".equals(env)) {
            // プロダクション用の追加セットアップ
        }
        return CallbackInfo.empty();
    }

    @ModifyConstant(
        methodName = "getTimeout",
        oldValue = 5000,
        newValue = 30000
    )
    public static CallbackInfo productionTimeout() {
        return CallbackInfo.empty();
    }
}
```

## 次のステップ

- [変数の変更](./variable-modification.md)について学ぶ
- [高度な使用方法](./advanced-usage.md)を探索する
- [例](./examples.md)を確認する
