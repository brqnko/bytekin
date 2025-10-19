# Variable Modification

Modify local variable values within methods during bytecode transformation.

## Basic Usage

```java
@ModifyClass("com.example.Processor")
public class ProcessorHooks {

    @ModifyVariable(
        methodName = "process",
        variableIndex = 1
    )
    public static void sanitizeInput(String original) {
        // Transformation logic
    }
}
```

## Understanding Variable Indices

Local variables in a method are stored in slots, indexed starting from 0:

### Instance Methods

```java
public void process(String name, int count) {
    String result;
    // ...
}
```

Variable indices:
- `0`: `this` (implicit)
- `1`: `name` (first parameter)
- `2`: `count` (second parameter)
- `3`: `result` (local variable)

### Static Methods

```java
public static void process(String name, int count) {
    String result;
    // ...
}
```

Variable indices:
- `0`: `name` (first parameter)
- `1`: `count` (second parameter)
- `2`: `result` (local variable)

## Annotation Parameters

### methodName (required)

The name of the target method.

```java
methodName = "process"
```

### variableIndex (required)

The index of the local variable to modify.

```java
variableIndex = 1
```

## Modifying Parameters

Transform method parameters:

```java
@ModifyClass("com.example.UserService")
public class UserServiceHooks {

    @ModifyVariable(
        methodName = "createUser",
        variableIndex = 1  // First parameter: email
    )
    public static void normalizeEmail(String original) {
        // The original email will be normalized
        // e.g., "USER@EXAMPLE.COM" becomes "user@example.com"
    }
}
```

**Before:**
```java
public void createUser(String email) {
    // email = "USER@EXAMPLE.COM"
    // ...
}
```

**After:**
```java
public void createUser(String email) {
    // email = "user@example.com" (normalized)
    // ...
}
```

## Modifying Local Variables

Transform variables created inside methods:

```java
@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @ModifyVariable(
        methodName = "calculateTotal",
        variableIndex = 2  // Local variable: total
    )
    public static void applyTaxToTotal(int original) {
        // total will be multiplied by 1.1 to apply tax
    }
}
```

## Use Cases

### Input Sanitization

Clean up method inputs:

```java
@ModifyClass("com.example.WebService")
public class WebServiceHooks {

    @ModifyVariable(
        methodName = "handleRequest",
        variableIndex = 1  // request parameter
    )
    public static void sanitizeRequest(String original) {
        // Removes malicious characters
    }

    @ModifyVariable(
        methodName = "handleRequest",
        variableIndex = 2  // path parameter
    )
    public static void validatePath(String original) {
        // Ensures path doesn't escape root directory
    }
}
```

### Data Transformation

Convert data format:

```java
@ModifyClass("com.example.DateProcessor")
public class DateProcessorHooks {

    @ModifyVariable(
        methodName = "processDate",
        variableIndex = 1  // date parameter
    )
    public static void convertToUTC(String original) {
        // Converts from local time to UTC
    }
}
```

### Type Conversion

Change data types:

```java
@ModifyClass("com.example.Converter")
public class ConverterHooks {

    @ModifyVariable(
        methodName = "process",
        variableIndex = 1  // number parameter
    )
    public static void convertToPercentage(int original) {
        // Converts raw number to percentage
    }
}
```

## Advanced Patterns

### Multiple Variables

Modify multiple variables in same method:

```java
@ModifyClass("com.example.Transfer")
public class TransferHooks {

    @ModifyVariable(
        methodName = "transfer",
        variableIndex = 1  // from account
    )
    public static void validateFromAccount(String original) {
        // Validate source account
    }

    @ModifyVariable(
        methodName = "transfer",
        variableIndex = 2  // to account
    )
    public static void validateToAccount(String original) {
        // Validate destination account
    }

    @ModifyVariable(
        methodName = "transfer",
        variableIndex = 3  // amount
    )
    public static void validateAmount(long original) {
        // Ensure amount is positive
    }
}
```

All three modifications are applied to the same method.

## Type Preservation

Variable type is preserved during modification:

```java
@ModifyClass("com.example.Data")
public class DataHooks {

    // Modifying String parameter
    @ModifyVariable(methodName = "processName", variableIndex = 1)
    public static void transformName(String original) { }

    // Modifying int parameter
    @ModifyVariable(methodName = "processCount", variableIndex = 1)
    public static void transformCount(int original) { }

    // Modifying List parameter
    @ModifyVariable(methodName = "processItems", variableIndex = 1)
    public static void transformItems(List<?> original) { }
}
```

Each hook receives the correct type automatically.

## Limitations

### Cannot Modify

- Variables that are never used
- Variables whose values are optimized away by JVM
- Variables modified after initialization in complex ways

### Challenges

1. **Index calculation**: Must correctly identify variable indices
2. **Type safety**: Parameter types must match
3. **Scope**: Changes only within that method
4. **Debugging**: Can be hard to trace modifications

## Finding Correct Variable Indices

Use `javap` to inspect variable layout:

```bash
javap -c -private MyClass.class | grep -A 50 "methodName"
```

Look for LocalVariableTable which shows variable positions.

## Best Practices

1. **Document indices**: Clearly comment which variable at which index
2. **Keep transformations simple**: Complex logic should be separate
3. **Preserve semantics**: Ensure modified values make sense
4. **Test thoroughly**: Verify behavior with modified variables
5. **Use inspectors**: Verify indices are correct before applying

## Combining with Other Features

Use variable modification with injections:

```java
@ModifyClass("com.example.Service")
public class ServiceHooks {

    @Inject(
        methodName = "handle",
        methodDesc = "(Ljava/lang/String;)V",
        at = At.HEAD
    )
    public static CallbackInfo validateInput(String input) {
        if (input == null || input.isEmpty()) {
            return new CallbackInfo(true, null, null);
        }
        return CallbackInfo.empty();
    }

    @ModifyVariable(
        methodName = "handle",
        variableIndex = 1  // input parameter
    )
    public static void normalizeInput(String original) {
        // Also normalize the input
    }
}
```

## Next Steps

- Explore [Advanced Usage](./advanced-usage.md)
- Learn about [Mappings](./mappings.md)
- Check [Examples](./examples.md)
