# Examples - Basic Usage

This section contains complete, working examples for common bytekin use cases.

## Example 1: Adding Logging

### Problem
Add logging to a method without modifying source code.

### Solution

**Target Class:**
```java
package com.example;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

**Hook Class:**
```java
package com.example;

import io.github.brqnko.bytekin.injection.*;
import io.github.brqnko.bytekin.data.CallbackInfo;

@ModifyClass("com.example.Calculator")
public class CalculatorLoggingHooks {

    @Inject(
        methodName = "add",
        methodDesc = "(II)I",
        at = At.HEAD
    )
    public static CallbackInfo logAddition(int a, int b) {
        System.out.println("Adding: " + a + " + " + b);
        return CallbackInfo.empty();
    }
}
```

**Usage:**
```java
public class Main {
    public static void main(String[] args) {
        BytekinTransformer transformer = new BytekinTransformer.Builder(
            CalculatorLoggingHooks.class
        ).build();

        byte[] original = getClassBytecode("com.example.Calculator");
        byte[] transformed = transformer.transform("com.example.Calculator", original);
        
        Calculator calc = loadTransformed(transformed);
        int result = calc.add(5, 3);
        // Output:
        // Adding: 5 + 3
        // 8
    }
}
```

## Example 2: Parameter Validation

### Problem
Validate method parameters before execution.

### Solution

**Hook Class:**
```java
@ModifyClass("com.example.UserService")
public class UserValidationHooks {

    @Inject(
        methodName = "createUser",
        methodDesc = "(Ljava/lang/String;I)V",
        at = At.HEAD
    )
    public static CallbackInfo validateUser(String name, int age, CallbackInfo ci) {
        if (name == null || name.isEmpty()) {
            System.out.println("ERROR: Name cannot be empty");
            ci.cancelled = true;
            return ci;
        }
        
        if (age < 18) {
            System.out.println("ERROR: User must be 18 or older");
            ci.cancelled = true;
            return ci;
        }
        
        System.out.println("Valid user: " + name + ", age " + age);
        return CallbackInfo.empty();
    }
}
```

## Example 3: Caching

### Problem
Intercept method calls to implement caching.

### Solution

**Hook Class:**
```java
@ModifyClass("com.example.DataRepository")
public class CachingHooks {
    private static final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Invoke(
        targetMethodName = "fetch",
        targetMethodDesc = "(Ljava/lang/String;)Ljava/lang/Object;",
        invokeMethodName = "queryDatabase",
        invokeMethodDesc = "(Ljava/lang/String;)Ljava/lang/Object;",
        shift = Shift.BEFORE
    )
    public static CallbackInfo checkCache(String key, CallbackInfo ci) {
        Object cached = cache.get(key);
        if (cached != null) {
            System.out.println("Cache hit for: " + key);
            ci.cancelled = true;
            ci.returnValue = cached;
        } else {
            System.out.println("Cache miss for: " + key);
        }
        return ci;
    }
}
```

## Example 4: Security - Authentication Check

### Problem
Ensure all sensitive methods require authentication.

### Solution

**Hook Class:**
```java
@ModifyClass("com.example.PaymentService")
public class AuthenticationHooks {

    @Inject(
        methodName = "transfer",
        methodDesc = "(Ljava/lang/String;J)Z",
        at = At.HEAD
    )
    public static CallbackInfo checkAuthentication(String account, long amount, CallbackInfo ci) {
        if (!isUserAuthenticated()) {
            System.out.println("ERROR: Authentication required");
            ci.cancelled = true;
            ci.returnValue = false;
            return ci;
        }
        
        System.out.println("Authenticated transfer: " + amount);
        return CallbackInfo.empty();
    }

    private static boolean isUserAuthenticated() {
        // Check authentication status
        return true;
    }
}
```

## Example 5: Monitoring - Method Call Counter

### Problem
Count how many times specific methods are called.

### Solution

**Hook Class:**
```java
@ModifyClass("com.example.UserService")
public class MonitoringHooks {
    private static final AtomicInteger callCount = new AtomicInteger(0);

    @Inject(
        methodName = "getUser",
        methodDesc = "(I)Lcom/example/User;",
        at = At.HEAD
    )
    public static CallbackInfo countCalls(int userId) {
        int count = callCount.incrementAndGet();
        if (count % 100 == 0) {
            System.out.println("getUser() called " + count + " times");
        }
        return CallbackInfo.empty();
    }
}
```

## Example 6: Transforming Return Values

### Problem
Modify the return value of a method.

### Solution

**Hook Class:**
```java
@ModifyClass("com.example.PriceCalculator")
public class PriceHooks {

    @Inject(
        methodName = "getPrice",
        methodDesc = "()D",
        at = At.RETURN
    )
    public static CallbackInfo applyDiscount(CallbackInfo ci) {
        double originalPrice = (double) ci.returnValue;
        double discounted = originalPrice * 0.9;  // 10% discount
        ci.returnValue = discounted;
        return ci;
    }
}
```

## Invoke Examples

### Example: Method Call Interception

**Hook Class:**
```java
@ModifyClass("com.example.DataProcessor")
public class ProcessorHooks {

    @Invoke(
        targetMethodName = "process",
        targetMethodDesc = "(Ljava/lang/String;)Ljava/lang/String;",
        invokeMethodName = "validate",
        invokeMethodDesc = "(Ljava/lang/String;)Ljava/lang/String;",
        shift = Shift.BEFORE
    )
    public static CallbackInfo sanitizeBeforeValidation(String data, CallbackInfo ci) {
        String sanitized = data.trim().toLowerCase();
        ci.modifyArgs = new Object[]{sanitized};
        return ci;
    }
}
```

## Combined Example: Comprehensive Transformation

**Complete Hook Class:**
```java
@ModifyClass("com.example.UserRepository")
public class ComprehensiveHooks {

    @Inject(
        methodName = "save",
        methodDesc = "(Lcom/example/User;)V",
        at = At.HEAD
    )
    public static CallbackInfo validateBeforeSave(Object user, CallbackInfo ci) {
        // Validate input
        if (user == null) {
            System.out.println("ERROR: Cannot save null user");
            ci.cancelled = true;
        }
        return ci;
    }

    @Invoke(
        targetMethodName = "save",
        targetMethodDesc = "(Lcom/example/User;)V",
        invokeMethodName = "validateUser",
        invokeMethodDesc = "(Lcom/example/User;)Z",
        shift = Shift.BEFORE
    )
    public static CallbackInfo modifyValidation(Object user, CallbackInfo ci) {
        // Enhance validation
        System.out.println("Validating user...");
        return ci;
    }

    @Inject(
        methodName = "save",
        methodDesc = "(Lcom/example/User;)V",
        at = At.RETURN
    )
    public static CallbackInfo logSuccess(Object user) {
        System.out.println("User saved successfully");
        return CallbackInfo.empty();
    }
}
```

## Next Steps

- Review [Advanced Examples](./examples-advanced.md)
- Check [Best Practices](./best-practices.md)
- Explore more [Features](./features.md)
