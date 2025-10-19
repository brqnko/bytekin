# 高度な使用方法

このセクションでは、bytekinを効果的に使用するための高度なパターンとテクニックについて説明します。

## プログラマティックAPI(アノテーションベース以外)

アノテーションは便利ですが、プログラマティックAPIを使用することもできます:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder()
    .inject("com.example.Calculator", new Injection(
        "add",
        "(II)I",
        At.HEAD,
        Arrays.asList(Parameter.THIS, Parameter.INT, Parameter.INT)
    ))
    .build();
```

## 複数のフッククラス

フックを複数のクラスに整理し、すべてを渡します:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(
    LoggingHooks.class,
    AuthenticationHooks.class,
    PerformanceHooks.class,
    SecurityHooks.class
).build();
```

## クラスの再マッピング

マッピングを使用して難読化されたコードを処理します:

```java
class MyMappingProvider implements IMappingProvider {
    @Override
    public String getClassName(String name) {
        // a.classをcom.example.Calculatorにマップ
        if ("a".equals(name)) return "com.example.Calculator";
        return name;
    }

    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        // bをaddにマップ
        if ("com.example.Calculator".equals(className) && "b".equals(methodName)) {
            return "add";
        }
        return methodName;
    }

    @Override
    public String getFieldName(String className, String fieldName) {
        return fieldName;
    }
}

BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .mapping(new MyMappingProvider())
    .build();
```

## 変換のチェーン化

同じクラスに複数の変換を適用します:

```java
byte[] original = getClassBytecode("com.example.Service");

// 最初の変換
byte[] step1 = transformer1.transform("com.example.Service", original);

// 2番目の変換
byte[] step2 = transformer2.transform("com.example.Service", step1);

// 最終結果をロード
Class<?> clazz = loadFromBytecode(step2);
```

## 条件付きフックロジック

条件に基づいてフックを実行します:

```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo conditionalHook(String input, CallbackInfo ci) {
    // 特定の入力に対してのみインジェクト
    if (input.startsWith("test_")) {
        System.out.println("テストモード: " + input);
    }

    // 特定の環境に対してのみインジェクト
    String env = System.getProperty("app.env", "dev");
    if ("prod".equals(env)) {
        // 本番環境の場合の異なる動作
    }

    return ci;
}
```

## ステートフルフック

フックインボケーション間で状態を維持します:

```java
@ModifyClass("com.example.RequestHandler")
public class RequestHooks {
    private static final Map<String, Integer> callCounts = new ConcurrentHashMap<>();

    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
    public static CallbackInfo trackCalls(String id, CallbackInfo ci) {
        int count = callCounts.getOrDefault(id, 0);
        callCounts.put(id, count + 1);

        if (count > 100) {
            System.out.println("高い呼び出し回数: " + id);
        }

        return ci;
    }
}
```

## 複数の変換の組み合わせ

同じメソッドに異なる変換タイプを使用します:

```java
@ModifyClass("com.example.DataService")
public class ServiceHooks {

    // エントリをログ記録
    @Inject(methodName = "query", methodDesc = "(Ljava/lang/String;)Ljava/util/List;",
            at = At.HEAD)
    public static CallbackInfo logEntry(String sql) {
        System.out.println("クエリ: " + sql);
        return CallbackInfo.empty();
    }

    // データベース呼び出しをインターセプト
    @Invoke(
        targetMethodName = "query",
        targetMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        invokeMethodName = "execute",
        invokeMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        shift = Shift.BEFORE
    )
    public static CallbackInfo cacheLookup(String sql, CallbackInfo ci) {
        List<?> cached = getFromCache(sql);
        if (cached != null) {
            ci.cancelled = true;
            ci.returnValue = cached;
        }
        return ci;
    }

    // 定数データベースURLを変更
    @ModifyConstant(methodName = "getConnection", oldValue = "localhost",
                    newValue = "db.production.com")
    public static CallbackInfo updateDbLocation() {
        return CallbackInfo.empty();
    }
}
```

## パフォーマンス最適化パターン

効率的なパフォーマンス監視のためにフックを使用します:

```java
@ModifyClass("com.example.CriticalPath")
public class PerformanceHooks {
    private static final int SLOW_THRESHOLD = 1000; // ms

    @Inject(methodName = "criticalOperation", methodDesc = "()V", at = At.HEAD)
    public static CallbackInfo startTimer() {
        TIMER.set(System.currentTimeMillis());
        return CallbackInfo.empty();
    }

    @Inject(methodName = "criticalOperation", methodDesc = "()V", at = At.RETURN)
    public static CallbackInfo checkTimer() {
        long duration = System.currentTimeMillis() - TIMER.get();
        if (duration > SLOW_THRESHOLD) {
            System.out.println("遅い操作: " + duration + "ms");
        }
        return CallbackInfo.empty();
    }

    private static final ThreadLocal<Long> TIMER = ThreadLocal.withInitial(() -> 0L);
}
```

## セキュリティパターン - 入力検証

エントリポイントですべての入力を検証します:

```java
@ModifyClass("com.example.WebController")
public class SecurityHooks {

    @Inject(methodName = "handleRequest", methodDesc = "(Ljava/lang/String;)V",
            at = At.HEAD)
    public static CallbackInfo validateRequest(String request, CallbackInfo ci) {
        if (request == null || isMalicious(request)) {
            ci.cancelled = true;  // リクエストをブロック
            return ci;
        }
        return ci;
    }

    private static boolean isMalicious(String request) {
        // SQLインジェクション、XSSなどをチェック
        return request.contains("DROP") || request.contains("<script>");
    }
}
```

## テストパターン - モックオブジェクト

テストで依存性インジェクションのためにフックを使用します:

```java
@ModifyClass("com.example.UserService")
public class TestHooks {
    private static UserRepository mockRepository = new MockUserRepository();

    @Inject(methodName = "getUserById", methodDesc = "(I)Lcom/example/User;",
            at = At.HEAD)
    public static CallbackInfo useMockRepository() {
        // モックリポジトリをインジェクト
        return CallbackInfo.empty();
    }
}
```

## A/Bテストパターン

ユーザーに基づいて異なる実装にルーティングします:

```java
@ModifyClass("com.example.Algorithm")
public class ABTestingHooks {

    @Invoke(
        targetMethodName = "process",
        targetMethodDesc = "(Ljava/lang/Object;)Ljava/lang/Object;",
        invokeMethodName = "compute",
        invokeMethodDesc = "(Ljava/lang/Object;)Ljava/lang/Object;",
        shift = Shift.BEFORE
    )
    public static CallbackInfo selectImplementation(Object data, CallbackInfo ci) {
        // ユーザーに基づいて新しいまたは古い実装にルーティング
        if (useNewImplementation(data)) {
            ci.returnValue = computeNew(data);
            ci.cancelled = true;
        }
        return ci;
    }

    private static boolean useNewImplementation(Object data) {
        String userId = extractUserId(data);
        int hash = userId.hashCode();
        return hash % 2 == 0;  // 50/50分割
    }
}
```

## フィーチャーフラグパターン

デプロイなしで機能を有効/無効にします:

```java
@ModifyClass("com.example.Features")
public class FeatureFlagHooks {
    private static final Map<String, Boolean> flags = new ConcurrentHashMap<>();

    @Inject(methodName = "newFeature", methodDesc = "()V", at = At.HEAD)
    public static CallbackInfo checkFeatureFlag(CallbackInfo ci) {
        if (!isFeatureEnabled("newFeature")) {
            ci.cancelled = true;  // このメソッドをスキップ
        }
        return ci;
    }

    private static boolean isFeatureEnabled(String feature) {
        return flags.getOrDefault(feature, false);
    }

    public static void setFeatureFlag(String feature, boolean enabled) {
        flags.put(feature, enabled);
    }
}
```

## 遅延初期化パターン

高コストな初期化を延期します:

```java
@ModifyClass("com.example.Config")
public class ConfigHooks {
    private static volatile Configuration config;

    @Inject(methodName = "getConfig", methodDesc = "()Lcom/example/Configuration;",
            at = At.HEAD)
    public static CallbackInfo lazyInitialize(CallbackInfo ci) {
        if (config == null) {
            synchronized (ConfigHooks.class) {
                if (config == null) {
                    config = loadConfiguration();  // 高コストな操作
                }
            }
        }
        ci.cancelled = true;
        ci.returnValue = config;
        return ci;
    }
}
```

## 次のステップ

- 詳細な[マッピング](./mappings.md)を探索
- [ベストプラクティス](./best-practices.md)をレビュー
- [例](./examples.md)をチェック
