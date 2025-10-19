# Annotations Reference

Complete reference for bytekin annotations.

## @ModifyClass

### Purpose
Marks a class as containing hook methods for bytecode transformation.

### Usage
```java
@ModifyClass("com.example.TargetClass")
public class MyHooks {
    // Hook methods here
}
```

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| className | String | Yes | Fully qualified name of the target class |

### Scope
Applied to class types only.

## @Inject

### Purpose
Inject code at specific points in methods.

### Usage
```java
@Inject(
    methodName = "myMethod",
    methodDesc = "(I)Ljava/lang/String;",
    at = At.HEAD
)
public static CallbackInfo hook(int param) { }
```

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| methodName | String | Yes | Target method name |
| methodDesc | String | Yes | Method descriptor (JVM format) |
| at | At | Yes | Where to inject code |

### Scope
Applied to methods only.

### Return Type
Must return `CallbackInfo`.

## @Invoke

### Purpose
Intercept method calls.

### Usage
```java
@Invoke(
    targetMethodName = "parentMethod",
    targetMethodDesc = "()V",
    invokeMethodName = "childMethod",
    invokeMethodDesc = "(I)V",
    shift = Shift.BEFORE
)
public static CallbackInfo hook() { }
```

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| targetMethodName | String | Yes | Method containing the call |
| targetMethodDesc | String | Yes | Descriptor of target method |
| invokeMethodName | String | Yes | Name of method being called |
| invokeMethodDesc | String | Yes | Descriptor of called method |
| shift | Shift | Yes | BEFORE or AFTER the call |

### Scope
Applied to methods only.

## @Redirect

### Purpose
Redirect method calls to different target.

### Usage
```java
@Redirect(
    targetMethodName = "oldMethod",
    targetMethodDesc = "()V",
    redirectMethodName = "newMethod",
    redirectMethodDesc = "()V"
)
public static void hook() { }
```

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| targetMethodName | String | Yes | Method containing the call |
| targetMethodDesc | String | Yes | Descriptor of target method |
| redirectMethodName | String | Yes | Name of redirect method |
| redirectMethodDesc | String | Yes | Descriptor of redirect method |

## @ModifyConstant

### Purpose
Modify constant values in bytecode.

### Usage
```java
@ModifyConstant(
    methodName = "getConfig",
    oldValue = "dev",
    newValue = "prod"
)
public static CallbackInfo hook() { }
```

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| methodName | String | Yes | Method containing the constant |
| oldValue | Object | Yes | Original constant value |
| newValue | Object | Yes | New constant value |

## @ModifyVariable

### Purpose
Modify local variable values.

### Usage
```java
@ModifyVariable(
    methodName = "process",
    variableIndex = 1
)
public static void hook(String param) { }
```

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| methodName | String | Yes | Target method name |
| variableIndex | int | Yes | Local variable slot index |

## Enum: At

### Values

| Value | Description |
|-------|-------------|
| HEAD | At start of method, before all code |
| RETURN | Before each return statement |
| TAIL | At end of method |

## Enum: Shift

### Values

| Value | Description |
|-------|-------------|
| BEFORE | Execute hook before method call |
| AFTER | Execute hook after method call |

## Next Steps

- Review [Classes and Interfaces](./classes-interfaces.md)
- Check [API Reference](./api-reference.md)
- Explore [Examples](./examples.md)
