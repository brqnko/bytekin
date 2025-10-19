# Redirect Transformation

The `@Redirect` annotation allows you to change which method is actually called at runtime.

## Basic Usage

```java
@ModifyClass("com.example.LegacyService")
public class LegacyServiceHooks {

    @Redirect(
        targetMethodName = "oldMethod",
        targetMethodDesc = "(I)V",
        redirectMethodName = "newMethod",
        redirectMethodDesc = "(I)V"
    )
    public static void redirectCall(int value) {
        System.out.println("Redirecting call with value: " + value);
    }
}
```

## Annotation Parameters

### targetMethodName (required)

The name of the method that contains the call to redirect.

```java
targetMethodName = "process"
```

### targetMethodDesc (required)

The JVM descriptor of the target method.

```java
targetMethodDesc = "(Ljava/lang/String;)V"
```

### redirectMethodName (required)

The name of the new method to call instead.

```java
redirectMethodName = "newImplementation"
```

### redirectMethodDesc (required)

The JVM descriptor of the redirect method.

```java
redirectMethodDesc = "(Ljava/lang/String;)V"
```

## How Redirect Works

**Before:**
```java
public class LegacyAPI {
    public void oldMethod(int value) {
        // Old implementation
    }
}

public class Client {
    public void use() {
        api.oldMethod(42);  // Calls oldMethod
    }
}
```

**After Redirect:**
```java
public class Client {
    public void use() {
        api.newMethod(42);  // Redirected to newMethod!
    }
}
```

## Practical Examples

### Migration Strategy

Gradually migrate from old API to new API:

```java
@ModifyClass("com.example.Application")
public class APIRedirection {

    @Redirect(
        targetMethodName = "main",
        targetMethodDesc = "([Ljava/lang/String;)V",
        redirectMethodName = "legacySearch",
        redirectMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        from = "oldSearch",
        to = "modernSearch"
    )
    public static void upgradeSearch() {
        // Search calls are now routed to the modern implementation
    }
}
```

### Mocking for Tests

Replace real implementations with test doubles:

```java
@ModifyClass("com.example.DataAccess")
public class TestRedirection {

    @Redirect(
        targetMethodName = "query",
        targetMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        redirectMethodName = "fetchFromDatabase",
        redirectMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        from = "realDB",
        to = "mockDB"
    )
    public static void useMockDatabase() {
        // All database calls use mocked implementation
    }
}
```

### Performance Optimization

Route to optimized implementations:

```java
@ModifyClass("com.example.Processing")
public class PerformanceOptimization {

    @Redirect(
        targetMethodName = "processLargeList",
        targetMethodDesc = "(Ljava/util/List;)Ljava/util/List;",
        redirectMethodName = "slowImplementation",
        redirectMethodDesc = "(Ljava/util/List;)Ljava/util/List;",
        from = "bruteForce",
        to = "optimized"
    )
    public static void useOptimizedAlgorithm() {
        // Uses fast algorithm instead of slow one
    }
}
```

## Differences from Other Transformations

| Feature | Inject | Invoke | Redirect |
|---------|--------|--------|----------|
| What it does | Insert code | Intercept calls | Change target |
| Call happens | Yes | Yes | Yes, but different target |
| Can skip execution | Yes | Yes | Yes |
| Use case | Add logging | Modify behavior | API migration |

## Type Compatibility

The redirect method must have compatible signature:

```java
// Original call
search(String query);  // (Ljava/lang/String;)Ljava/util/List;

// Must redirect to compatible signature
newSearch(String query);  // (Ljava/lang/String;)Ljava/util/List;
```

**Type mismatch will cause issues:**
```java
// WRONG - Different parameter types
@Redirect(..., from = "process(int)", to = "process(String)")
```

## Performance Considerations

Redirect has minimal overhead compared to normal method calls since:

1. It's a direct bytecode substitution
2. No wrapper or proxy is created
3. The JVM can inline and optimize as normal

## Limitations

- Both methods must have compatible signatures
- Cannot redirect to final methods
- Cannot redirect constructor calls (use `@Invoke` instead)
- Redirects are static - same target for all calls

## Best Practices

1. **Ensure compatibility**: Verify method signatures match exactly
2. **Document redirects**: Leave comments explaining why
3. **Test redirects**: Verify behavior after redirection
4. **Use for migration**: Great for moving from old to new APIs
5. **Be cautious**: Track all redirects to avoid confusion

## Advanced Pattern: Conditional Redirect

While `@Redirect` is static, you can combine it with `@Invoke` for conditional behavior:

```java
@Invoke(
    targetMethodName = "search",
    targetMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
    invokeMethodName = "getResults",
    invokeMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
    shift = Shift.BEFORE
)
public static CallbackInfo selectImplementation(String query, CallbackInfo ci) {
    if (query.length() > 100) {
        // Use optimized search for large queries
        ci.returnValue = optimizedSearch(query);
        ci.cancelled = true;
    }
    return ci;
}
```

## Next Steps

- Learn about [Constant Modification](./constant-modification.md)
- Explore [Advanced Usage](./advanced-usage.md)
- Check [Examples](./examples.md)
