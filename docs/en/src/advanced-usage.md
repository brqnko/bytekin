# Advanced Usage

This section covers advanced patterns and techniques for using bytekin effectively.

## Programmatic API (Non-Annotation Based)

While annotations are convenient, you can also use the programmatic API:

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

## Multiple Hook Classes

Organize hooks into multiple classes and pass them all:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(
    LoggingHooks.class,
    AuthenticationHooks.class,
    PerformanceHooks.class,
    SecurityHooks.class
).build();
```

## Class Remapping

Handle obfuscated code using mappings:

```java
class MyMappingProvider implements IMappingProvider {
    @Override
    public String getClassName(String name) {
        // Map a.class to com.example.Calculator
        if ("a".equals(name)) return "com.example.Calculator";
        return name;
    }
    
    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        // Map b to add
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

## Chaining Transformations

Apply multiple transformations to the same class:

```java
byte[] original = getClassBytecode("com.example.Service");

// First transformation
byte[] step1 = transformer1.transform("com.example.Service", original);

// Second transformation
byte[] step2 = transformer2.transform("com.example.Service", step1);

// Load final result
Class<?> clazz = loadFromBytecode(step2);
```

## Conditional Hook Logic

Execute hooks based on conditions:

```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo conditionalHook(String input, CallbackInfo ci) {
    // Only inject for certain inputs
    if (input.startsWith("test_")) {
        System.out.println("Test mode: " + input);
    }
    
    // Only inject for certain environments
    String env = System.getProperty("app.env", "dev");
    if ("prod".equals(env)) {
        // Different behavior for production
    }
    
    return ci;
}
```

## Stateful Hooks

Maintain state across hook invocations:

```java
@ModifyClass("com.example.RequestHandler")
public class RequestHooks {
    private static final Map<String, Integer> callCounts = new ConcurrentHashMap<>();
    
    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
    public static CallbackInfo trackCalls(String id, CallbackInfo ci) {
        int count = callCounts.getOrDefault(id, 0);
        callCounts.put(id, count + 1);
        
        if (count > 100) {
            System.out.println("High call count for: " + id);
        }
        
        return ci;
    }
}
```

## Combining Multiple Transformations

Use different transformation types on the same method:

```java
@ModifyClass("com.example.DataService")
public class ServiceHooks {
    
    // Log entry
    @Inject(methodName = "query", methodDesc = "(Ljava/lang/String;)Ljava/util/List;", 
            at = At.HEAD)
    public static CallbackInfo logEntry(String sql) {
        System.out.println("Query: " + sql);
        return CallbackInfo.empty();
    }
    
    // Intercept database calls
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
    
    // Modify constant database URL
    @ModifyConstant(methodName = "getConnection", oldValue = "localhost", 
                    newValue = "db.production.com")
    public static CallbackInfo updateDbLocation() {
        return CallbackInfo.empty();
    }
}
```

## Performance Optimization Pattern

Use hooks for efficient performance monitoring:

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
            System.out.println("Slow operation: " + duration + "ms");
        }
        return CallbackInfo.empty();
    }
    
    private static final ThreadLocal<Long> TIMER = ThreadLocal.withInitial(() -> 0L);
}
```

## Security Pattern - Input Validation

Validate all inputs at entry points:

```java
@ModifyClass("com.example.WebController")
public class SecurityHooks {
    
    @Inject(methodName = "handleRequest", methodDesc = "(Ljava/lang/String;)V", 
            at = At.HEAD)
    public static CallbackInfo validateRequest(String request, CallbackInfo ci) {
        if (request == null || isMalicious(request)) {
            ci.cancelled = true;  // Block request
            return ci;
        }
        return ci;
    }
    
    private static boolean isMalicious(String request) {
        // Check for SQL injection, XSS, etc.
        return request.contains("DROP") || request.contains("<script>");
    }
}
```

## Testing Pattern - Mock Objects

Use hooks for dependency injection in tests:

```java
@ModifyClass("com.example.UserService")
public class TestHooks {
    private static UserRepository mockRepository = new MockUserRepository();
    
    @Inject(methodName = "getUserById", methodDesc = "(I)Lcom/example/User;", 
            at = At.HEAD)
    public static CallbackInfo useMockRepository() {
        // Inject mock repository
        return CallbackInfo.empty();
    }
}
```

## A/B Testing Pattern

Route to different implementations based on user:

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
        // Route to new or old implementation based on user
        if (useNewImplementation(data)) {
            ci.returnValue = computeNew(data);
            ci.cancelled = true;
        }
        return ci;
    }
    
    private static boolean useNewImplementation(Object data) {
        String userId = extractUserId(data);
        int hash = userId.hashCode();
        return hash % 2 == 0;  // 50/50 split
    }
}
```

## Feature Flag Pattern

Enable/disable features without deployment:

```java
@ModifyClass("com.example.Features")
public class FeatureFlagHooks {
    private static final Map<String, Boolean> flags = new ConcurrentHashMap<>();
    
    @Inject(methodName = "newFeature", methodDesc = "()V", at = At.HEAD)
    public static CallbackInfo checkFeatureFlag(CallbackInfo ci) {
        if (!isFeatureEnabled("newFeature")) {
            ci.cancelled = true;  // Skip this method
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

## Lazy Initialization Pattern

Defer expensive initialization:

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
                    config = loadConfiguration();  // Expensive operation
                }
            }
        }
        ci.cancelled = true;
        ci.returnValue = config;
        return ci;
    }
}
```

## Next Steps

- Explore [Mappings](./mappings.md) in detail
- Review [Best Practices](./best-practices.md)
- Check [Examples](./examples.md)
