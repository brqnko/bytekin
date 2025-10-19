# Constant Modification

Modify hardcoded constant values in bytecode without recompiling source code.

## Basic Usage

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

## What Constants Can Be Modified?

- String literals
- Numeric constants (int, long, float, double)
- Boolean constants
- Constants in the constant pool

## String Constants

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

**Before:**
```java
public String getAPIEndpoint() {
    return "http://localhost:8080";
}
```

**After:**
```java
public String getAPIEndpoint() {
    return "https://api.production.com";
}
```

## Numeric Constants

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

## Multiple Constants in Same Method

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

Both modifications will be applied to the same method.

## Use Cases

### Environment Configuration

Change environment-specific values:

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

### Feature Flags

Enable/disable features without recompilation:

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

### API Versioning

Update API endpoints:

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

## Performance Impact

Constant modification has **minimal runtime overhead**:

1. Changes occur during bytecode transformation (one-time)
2. Runtime performance is identical to recompiled code
3. JVM can optimize modified constants

## Limitations

### Cannot Modify

- Local variable initializations (in some cases)
- Constants created at runtime
- Constants already optimized by JVM

### Type Matching

The old value and new value must have compatible types:

```java
// CORRECT
@ModifyConstant(
    methodName = "getCount",
    oldValue = 10,        // int
    newValue = 20         // int - compatible
)

// WRONG
@ModifyConstant(
    methodName = "getCount",
    oldValue = 10,        // int
    newValue = "20"       // String - incompatible!
)
```

## Best Practices

1. **Use for configuration**: Not for logic changes
2. **Document clearly**: Explain why constants are modified
3. **Keep values consistent**: Use the exact old value
4. **Test all paths**: Verify code works with new values
5. **Avoid type changes**: Keep types compatible

## Advanced: Conditional Modification

Combine with other transformations for more control:

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
            // Additional setup for production
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

## Next Steps

- Learn about [Variable Modification](./variable-modification.md)
- Explore [Advanced Usage](./advanced-usage.md)
- Check [Examples](./examples.md)
