# Features Overview

bytekin provides several powerful transformation features to manipulate Java bytecode. This section provides an overview of each feature.

## Available Features

### 1. Inject - Code Insertion

Insert custom code at specific points in methods without modifying source code.

**Use Cases:**
- Add logging statements
- Implement cross-cutting concerns
- Add security checks
- Validate parameters

**Example:**
```java
@Inject(methodName = "calculate", methodDesc = "(II)I", at = At.HEAD)
public static CallbackInfo logStart(int a, int b) {
    System.out.println("Starting calculation");
    return CallbackInfo.empty();
}
```

**Learn More:** [Inject Transformation](./inject.md)

### 2. Invoke - Method Call Interception

Intercept method calls and optionally modify arguments or return values.

**Use Cases:**
- Intercept specific method calls
- Modify method arguments
- Mock or stub methods
- Add pre/post processing

**Example:**
```java
@Invoke(
    targetMethodName = "process",
    targetMethodDesc = "(Ljava/lang/String;)V",
    invokeMethodName = "validate",
    invokeMethodDesc = "(Ljava/lang/String;)V",
    shift = Shift.BEFORE
)
public static CallbackInfo validateBefore(String input) {
    return new CallbackInfo(false, null, new Object[]{input.trim()});
}
```

**Learn More:** [Invoke Transformation](./invoke.md)

### 3. Redirect - Method Call Redirection

Change which method is called at runtime.

**Use Cases:**
- Redirect calls to alternative implementations
- Mock method behavior
- Implement method forwarding
- Change behavior based on conditions

**Example:**
```java
@Redirect(
    targetMethodName = "oldMethod",
    targetMethodDesc = "(I)V",
    redirectMethodName = "newMethod",
    redirectMethodDesc = "(I)V"
)
public static void redirectCall(int value) {
    System.out.println("Redirected to new method: " + value);
}
```

**Learn More:** [Redirect Transformation](./redirect.md)

### 4. Constant Modification

Modify constant values embedded in bytecode.

**Use Cases:**
- Change hardcoded configuration values
- Modify string literals
- Change numeric constants
- Patch constants at runtime

**Example:**
```java
@ModifyConstant(
    methodName = "getVersion",
    oldValue = "1.0",
    newValue = "2.0"
)
public static CallbackInfo updateVersion() {
    return CallbackInfo.empty();
}
```

**Learn More:** [Constant Modification](./constant-modification.md)

### 5. Variable Modification

Modify local variable values within methods.

**Use Cases:**
- Sanitize inputs
- Transform data
- Debug variable values
- Implement custom logic

**Example:**
```java
@ModifyVariable(
    methodName = "process",
    variableIndex = 1
)
public static void transformVariable(int original) {
    // Transformation logic
}
```

**Learn More:** [Variable Modification](./variable-modification.md)

## Combining Features

You can use multiple features together for complex transformations:

```java
@ModifyClass("com.example.Service")
public class ServiceHooks {
    
    // Inject logging
    @Inject(methodName = "handle", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
    public static CallbackInfo logStart(String input) {
        System.out.println("Processing: " + input);
        return CallbackInfo.empty();
    }
    
    // Intercept internal calls
    @Invoke(
        targetMethodName = "handle",
        targetMethodDesc = "(Ljava/lang/String;)V",
        invokeMethodName = "validate",
        invokeMethodDesc = "(Ljava/lang/String;)V",
        shift = Shift.BEFORE
    )
    public static CallbackInfo validateInput(String input) {
        return new CallbackInfo(false, null, new Object[]{sanitize(input)});
    }
    
    private static String sanitize(String input) {
        return input.trim().toLowerCase();
    }
}
```

## Choosing the Right Feature

| Feature | Purpose | Complexity |
|---------|---------|-----------|
| Inject | Insert code at method points | Low |
| Invoke | Intercept specific calls | Medium |
| Redirect | Change call target | Medium |
| Constant Modification | Change hardcoded values | Low |
| Variable Modification | Transform local variables | High |

## Next Steps

- Learn about [Inject](./inject.md) transformation
- Explore [Invoke](./invoke.md) interception
- Check [Examples](./examples.md)
