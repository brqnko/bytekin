# Advanced Examples

Advanced use cases and patterns for bytekin.

## Example 1: Custom ClassLoader

Implement a custom ClassLoader that applies transformations:

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

// Usage
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class).build();
ClassLoader loader = new TransformingClassLoader(transformer, ClassLoader.getSystemClassLoader());
Class<?> clazz = loader.loadClass("com.example.MyClass");
```

## Example 2: Java Agent

Create a Java agent for bytecode transformation:

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

Launch with: `java -javaagent:bytekin-agent.jar MyApplication`

## Example 3: Aspect-Oriented Programming (AOP)

Implement cross-cutting concerns:

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

## Example 4: Lazy Initialization

Implement lazy loading pattern:

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
        // Expensive initialization
        return new Object();
    }
}
```

## Example 5: Dynamic Configuration

Change behavior based on configuration:

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

## Example 6: Multi-Layer Transformation

Apply multiple transformers sequentially:

```java
public class MultiLayerTransformation {
    public static void main(String[] args) {
        BytekinTransformer logging = new BytekinTransformer.Builder(LoggingHooks.class).build();
        BytekinTransformer security = new BytekinTransformer.Builder(SecurityHooks.class).build();
        BytekinTransformer caching = new BytekinTransformer.Builder(CachingHooks.class).build();
        
        byte[] original = getClassBytecode("com.example.Service");
        
        // Apply layer by layer
        byte[] withLogging = logging.transform("com.example.Service", original);
        byte[] withSecurity = security.transform("com.example.Service", withLogging);
        byte[] withCaching = caching.transform("com.example.Service", withSecurity);
        
        Class<?> transformed = loadClass(withCaching);
    }
}
```

## Example 7: Performance Profiling

Add profiling without source changes:

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

## Example 8: Resilience Pattern

Add retry logic:

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
        // Make HTTP request
        return "";
    }
}
```

## Example 9: Observability

Collect metrics:

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

## Example 10: Migration Strategy

Gradually migrate from old to new API:

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
        // Gradually route to new implementation
    }
}
```

## Next Steps

- Review [Best Practices](./best-practices.md)
- Check [Troubleshooting](./troubleshooting.md)
- Explore [Advanced Usage](./advanced-usage.md)
