# Invoke Transformation

The `@Invoke` annotation allows you to intercept method calls and optionally modify their arguments before execution.

## Basic Usage

```java
@ModifyClass("com.example.DataProcessor")
public class ProcessorHooks {

    @Invoke(
        targetMethodName = "process",
        targetMethodDesc = "(Ljava/lang/String;)V",
        invokeMethodName = "validate",
        invokeMethodDesc = "(Ljava/lang/String;)V",
        shift = Shift.BEFORE
    )
    public static CallbackInfo validateBeforeProcess(String data) {
        if (data == null || data.isEmpty()) {
            return new CallbackInfo(true, null, new Object[]{"default"});
        }
        return CallbackInfo.empty();
    }
}
```

## Annotation Parameters

### targetMethodName (required)

The method name that contains the call to intercept.

```java
targetMethodName = "process"
```

### targetMethodDesc (required)

The JVM descriptor of the method that contains the call.

```java
targetMethodDesc = "(Ljava/lang/String;)V"
```

### invokeMethodName (required)

The name of the method being called (the one you want to intercept).

```java
invokeMethodName = "helper"
```

### invokeMethodDesc (required)

The JVM descriptor of the method being called.

```java
invokeMethodDesc = "(I)Ljava/lang/String;"
```

### shift (required)

When to execute the hook relative to the method call.

## Shift Enum - Timing

### Shift.BEFORE

Execute the hook **before** the method is called.

```java
@Invoke(
    targetMethodName = "process",
    targetMethodDesc = "(Ljava/lang/String;)V",
    invokeMethodName = "validate",
    invokeMethodDesc = "(Ljava/lang/String;)V",
    shift = Shift.BEFORE
)
public static CallbackInfo beforeCall(String input) {
    System.out.println("Before calling validate");
    return CallbackInfo.empty();
}
```

**Result:**
```java
public void process(String input) {
    System.out.println("Before calling validate");  // Injected
    validate(input);
    // Rest of code
}
```

### Shift.AFTER

Execute the hook **after** the method is called.

```java
@Invoke(
    targetMethodName = "process",
    targetMethodDesc = "(Ljava/lang/String;)V",
    invokeMethodName = "save",
    invokeMethodDesc = "()V",
    shift = Shift.AFTER
)
public static CallbackInfo afterCall() {
    System.out.println("After calling save");
    return CallbackInfo.empty();
}
```

**Result:**
```java
public void process(String input) {
    // Some code
    save();
    System.out.println("After calling save");  // Injected
}
```

## Modifying Arguments

Use `CallbackInfo` to change the arguments passed to the intercepted call:

```java
@Invoke(
    targetMethodName = "processData",
    targetMethodDesc = "(Ljava/lang/String;I)V",
    invokeMethodName = "transform",
    invokeMethodDesc = "(Ljava/lang/String;I)V",
    shift = Shift.BEFORE
)
public static CallbackInfo sanitizeInput(String data, int count, CallbackInfo ci) {
    // Modify arguments
    String sanitized = data.trim().toLowerCase();
    int newCount = Math.max(0, count);
    
    ci.modifyArgs = new Object[]{sanitized, newCount};
    return ci;
}
```

**Result:**
```java
public void processData(String data, int count) {
    // Original: transform(data, count);
    // After hook: transform(data.trim().toLowerCase(), max(0, count));
    transform(data, count);
}
```

## Cancelling Method Calls

Prevent the method from being called:

```java
@Invoke(
    targetMethodName = "deleteFile",
    targetMethodDesc = "(Ljava/lang/String;)Z",
    invokeMethodName = "delete",
    invokeMethodDesc = "(Ljava/io/File;)Z",
    shift = Shift.BEFORE
)
public static CallbackInfo preventDeletion(File file, CallbackInfo ci) {
    if (isSystemFile(file)) {
        // Don't call delete(), return false
        ci.cancelled = true;
        ci.returnValue = false;
    }
    return ci;
}
```

## Handling Return Values

Access and modify return values (with `Shift.AFTER`):

```java
@Invoke(
    targetMethodName = "getValue",
    targetMethodDesc = "()I",
    invokeMethodName = "compute",
    invokeMethodDesc = "()I",
    shift = Shift.AFTER
)
public static CallbackInfo modifyReturnValue(CallbackInfo ci) {
    // Access the return value
    int original = (int) ci.returnValue;
    
    // Modify it
    ci.returnValue = original * 2;
    
    return ci;
}
```

## Complex Example

```java
@ModifyClass("com.example.UserService")
public class UserServiceHooks {

    // Intercept login attempts
    @Invoke(
        targetMethodName = "authenticate",
        targetMethodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z",
        invokeMethodName = "validateCredentials",
        invokeMethodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z",
        shift = Shift.BEFORE
    )
    public static CallbackInfo logAuthAttempt(
        String username, String password, CallbackInfo ci
    ) {
        // Log the attempt
        System.out.println("Auth attempt for: " + username);
        
        // Block certain usernames
        if (username.equals("blocked")) {
            ci.cancelled = true;
            ci.returnValue = false;
        }
        
        return ci;
    }
}
```

## Best Practices

1. **Understand call flow**: Know where the method is called
2. **Consider timing**: `BEFORE` for input validation, `AFTER` for output transformation
3. **Test argument modification**: Ensure types match
4. **Handle cancellation carefully**: Ensure calling code handles cancelled calls
5. **Profile performance**: Hooks are executed on every call

## Limitations

- Can only intercept explicit method calls, not virtual method invocations from bytecode
- Cannot intercept calls to constructors directly
- Performance impact is incurred for every call

## Examples

See [Examples - Invoke](./examples-basic.md#invoke-examples) for complete working examples.

## Next Steps

- Learn about [Redirect](./redirect.md) transformation
- Explore [Advanced Usage](./advanced-usage.md)
- Check [API Reference](./api-reference.md)
