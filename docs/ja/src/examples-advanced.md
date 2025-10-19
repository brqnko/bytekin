# 高度な例

bytekinの高度なユースケースとパターン。

## 例1: カスタムClassLoader

変換を適用するカスタムClassLoaderを実装:

```java
public class TransformingClassLoader extends ClassLoader {
    private final BytekinTransformer transformer;
    private final ClassLoader parent;

    public TransformingClassLoader(BytekinTransformer transformer, ClassLoader parent) {
        super(parent);
        this.transformer = transformer;
        this.parent = parent;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] classBytes = loadBytesFromClasspath(name);
            byte[] transformed = transformer.transform(name, classBytes);
            return defineClass(name, transformed, 0, transformed.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Cannot find " + name, e);
        }
    }

    private byte[] loadBytesFromClasspath(String className) throws IOException {
        String path = className.replace('.', '/') + ".class";
        try (InputStream is = parent.getResourceAsStream(path)) {
            return is.readAllBytes();
        }
    }
}

// 使用方法
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class).build();
ClassLoader loader = new TransformingClassLoader(transformer, ClassLoader.getSystemClassLoader());
Class<?> clazz = loader.loadClass("com.example.MyClass");
```

## 例2: Javaエージェント

バイトコード変換のためのJavaエージェントを作成:

```java
public class BytekinAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class).build();
        inst.addTransformer((loader, className, klass, pd, bytecode) -> {
            return transformer.transform(className, bytecode);
        });
    }
}
```

起動: `java -javaagent:bytekin-agent.jar MyApplication`

## 例3: アスペクト指向プログラミング（AOP）

横断的関心事を実装:

```java
@ModifyClass("com.example.UserService")
public class AuditingAspect {

    @Inject(methodName = "save", methodDesc = "(Lcom/example/User;)V", at = At.HEAD)
    public static CallbackInfo auditBefore(Object user) {
        System.out.println("Audit: save() started");
        return CallbackInfo.empty();
    }

    @Inject(methodName = "delete", methodDesc = "(I)V", at = At.HEAD)
    public static CallbackInfo auditDelete(int id) {
        System.out.println("Audit: delete(" + id + ") started");
        return CallbackInfo.empty();
    }
}
```

## 例4: 遅延初期化

遅延ローディングパターンを実装:

```java
@ModifyClass("com.example.Repository")
public class LazyLoadingHooks {
    private static Object resource;

    @Inject(methodName = "initialize", methodDesc = "()V", at = At.HEAD)
    public static CallbackInfo lazyInit(CallbackInfo ci) {
        if (resource == null) {
            synchronized (LazyLoadingHooks.class) {
                if (resource == null) {
                    resource = loadExpensiveResource();
                }
            }
        }
        ci.cancelled = true;
        return ci;
    }

    private static Object loadExpensiveResource() {
        // 高コストな初期化
        return new Object();
    }
}
```

## 例5: 動的設定

設定に基づいて動作を変更:

```java
@ModifyClass("com.example.Service")
public class DynamicConfigHooks {
    private static final Properties config = new Properties();

    static {
        try {
            config.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
    public static CallbackInfo checkConfig(String input, CallbackInfo ci) {
        boolean enabled = Boolean.parseBoolean(config.getProperty("feature.enabled", "false"));
        if (!enabled) {
            ci.cancelled = true;
        }
        return ci;
    }
}
```

## 例6: 多層変換

複数のトランスフォーマーを順次適用:

```java
public class MultiLayerTransformation {
    public static void main(String[] args) {
        BytekinTransformer logging = new BytekinTransformer.Builder(LoggingHooks.class).build();
        BytekinTransformer security = new BytekinTransformer.Builder(SecurityHooks.class).build();
        BytekinTransformer caching = new BytekinTransformer.Builder(CachingHooks.class).build();

        byte[] original = getClassBytecode("com.example.Service");

        // 層ごとに適用
        byte[] withLogging = logging.transform("com.example.Service", original);
        byte[] withSecurity = security.transform("com.example.Service", withLogging);
        byte[] withCaching = caching.transform("com.example.Service", withSecurity);

        Class<?> transformed = loadClass(withCaching);
    }
}
```

## 例7: パフォーマンスプロファイリング

ソース変更なしでプロファイリングを追加:

```java
@ModifyClass("com.example.CriticalPath")
public class ProfilingHooks {
    private static final ThreadLocal<Long> timer = ThreadLocal.withInitial(() -> 0L);

    @Inject(methodName = "compute", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
    public static CallbackInfo startProfiling() {
        timer.set(System.nanoTime());
        return CallbackInfo.empty();
    }

    @Inject(methodName = "compute", methodDesc = "()Ljava/lang/Object;", at = At.RETURN)
    public static CallbackInfo endProfiling() {
        long duration = System.nanoTime() - timer.get();
        System.out.println("Duration: " + (duration / 1_000_000.0) + "ms");
        return CallbackInfo.empty();
    }
}
```

## 例8: レジリエンスパターン

リトライロジックを追加:

```java
@ModifyClass("com.example.HttpClient")
public class ResilienceHooks {
    private static final int MAX_RETRIES = 3;

    @Inject(methodName = "request", methodDesc = "(Ljava/lang/String;)Ljava/lang/String;",
            at = At.HEAD)
    public static CallbackInfo addRetry(String url, CallbackInfo ci) {
        String result = null;
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                result = executeRequest(url);
                break;
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) throw e;
            }
        }

        ci.cancelled = true;
        ci.returnValue = result;
        return ci;
    }

    private static String executeRequest(String url) throws Exception {
        // HTTPリクエストを実行
        return "";
    }
}
```

## 例9: 可観測性

メトリクスを収集:

```java
@ModifyClass("com.example.DataStore")
public class ObservabilityHooks {
    private static final AtomicLong callCount = new AtomicLong(0);
    private static final AtomicLong errorCount = new AtomicLong(0);

    @Inject(methodName = "query", methodDesc = "(Ljava/lang/String;)Ljava/util/List;",
            at = At.HEAD)
    public static CallbackInfo trackCall(String query) {
        callCount.incrementAndGet();
        return CallbackInfo.empty();
    }

    @Invoke(
        targetMethodName = "query",
        targetMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        invokeMethodName = "throwException",
        invokeMethodDesc = "()V",
        shift = Shift.BEFORE
    )
    public static CallbackInfo trackError() {
        errorCount.incrementAndGet();
        return CallbackInfo.empty();
    }

    public static void printMetrics() {
        System.out.println("Calls: " + callCount.get());
        System.out.println("Errors: " + errorCount.get());
    }
}
```

## 例10: 移行戦略

古いAPIから新しいAPIへ段階的に移行:

```java
@ModifyClass("com.example.Application")
public class MigrationHooks {
    private static final boolean USE_NEW_API = true;

    @Redirect(
        targetMethodName = "main",
        targetMethodDesc = "([Ljava/lang/String;)V",
        redirectMethodName = "oldSearch",
        redirectMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        from = "search",
        to = USE_NEW_API ? "newSearch" : "oldSearch"
    )
    public static void migrateAPI() {
        // 徐々に新しい実装にルーティング
    }
}
```

## 次のステップ

- [ベストプラクティス](./best-practices.md)を確認する
- [トラブルシューティング](./troubleshooting.md)を確認する
- [高度な使用方法](./advanced-usage.md)を探索する
