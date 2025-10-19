# Your First Transformation

In this guide, we'll create a complete example demonstrating a simple bytecode transformation.

## Example: Adding Logging to a Calculator

### Step 1: Create the Target Class

First, let's create a simple calculator class that we'll transform:

```java
package com.example;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }
}
```

### Step 2: Create Hook Methods

Create a class annotated with `@ModifyClass` that defines what you want to inject:

```java
package com.example;

import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.At;
import io.github.brqnko.bytekin.data.CallbackInfo;

@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @Inject(
        methodName = "add",
        methodDesc = "(II)I",
        at = At.HEAD
    )
    public static CallbackInfo logAdd(int a, int b) {
        System.out.println("Computing: " + a + " + " + b);
        return CallbackInfo.empty();
    }

    @Inject(
        methodName = "multiply",
        methodDesc = "(II)I",
        at = At.HEAD
    )
    public static CallbackInfo logMultiply(int a, int b) {
        System.out.println("Computing: " + a + " * " + b);
        return CallbackInfo.empty();
    }
}
```

### Step 3: Build the Transformer

```java
package com.example;

import io.github.brqnko.bytekin.transformer.BytekinTransformer;

public class TransformerSetup {
    public static BytekinTransformer createTransformer() {
        return new BytekinTransformer.Builder(CalculatorHooks.class)
            .build();
    }
}
```

### Step 4: Apply the Transformation

```java
package com.example;

import io.github.brqnko.bytekin.transformer.BytekinTransformer;

public class Main {
    public static void main(String[] args) {
        // Get original bytecode
        byte[] originalBytecode = getClassBytecode("com.example.Calculator");

        // Create transformer
        BytekinTransformer transformer = TransformerSetup.createTransformer();

        // Apply transformation
        byte[] transformedBytecode = transformer.transform(
            "com.example.Calculator",
            originalBytecode
        );

        // Load transformed class
        Calculator calc = loadTransformedClass(transformedBytecode);

        // Use the transformed class
        int result = calc.add(5, 3);
        // Output: "Computing: 5 + 3" then "8"

        result = calc.multiply(4, 7);
        // Output: "Computing: 4 * 7" then "28"
    }

    // Helper to get class bytecode (pseudo code)
    static byte[] getClassBytecode(String className) {
        // Implementation depends on your classloader setup
        return new byte[]{};
    }

    // Helper to load transformed class (pseudo code)
    static Calculator loadTransformedClass(byte[] bytecode) {
        // Load using custom ClassLoader
        return null;
    }
}
```

## What Happened?

The transformation process:

1. **Scanned** `CalculatorHooks` for methods with `@Inject` annotation
2. **Found** injections targeting `com.example.Calculator`
3. **Modified** the Calculator class bytecode to call our hook methods
4. **Inserted** the logging code at the head of specified methods

## Before and After

**Before Transformation:**

```java
public int add(int a, int b) {
    return a + b;
}
```

**After Transformation:**

```java
public int add(int a, int b) {
    // Injected code
    com.example.CalculatorHooks.logAdd(a, b);
    // Original code
    return a + b;
}
```

## Next Steps

- Explore other injection points with [At enum](./inject.md#at-enum)
- Learn about [Invoke transformations](./invoke.md)
- Check out more [Examples](./examples.md)
