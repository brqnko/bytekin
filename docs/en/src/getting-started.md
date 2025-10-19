# Getting Started with bytekin

This section will guide you through setting up bytekin and creating your first bytecode transformation.

## Prerequisites

- Java 8 or higher
- Basic understanding of Java
- Maven or Gradle (for dependency management)

## Installation

### Maven

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.brqnko.bytekin</groupId>
    <artifactId>bytekin</artifactId>
    <version>1.0</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.brqnko.bytekin:bytekin:1.0'
}
```

## Your First Transformation

### Step 1: Create a Hook Class

Create a class with the `@ModifyClass` annotation that defines how you want to transform a target class:

```java
import io.github.brqnko.bytekin.injection.ModifyClass;
import io.github.brqnko.bytekin.injection.Inject;
import io.github.brqnko.bytekin.injection.At;
import io.github.brqnko.bytekin.data.CallbackInfo;

@ModifyClass("com.example.Calculator")
public class CalculatorHooks {

    @Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
    public static CallbackInfo onAddHead(int a, int b) {
        System.out.println("Adding " + a + " + " + b);
        return CallbackInfo.empty();
    }
}
```

### Step 2: Create the Transformer

Instantiate a `BytekinTransformer` with your hook class:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(CalculatorHooks.class)
    .build();
```

### Step 3: Transform Your Classes

Apply the transformation to the target class bytecode:

```java
byte[] originalBytecode = loadClassBytecode("com.example.Calculator");
byte[] transformedBytecode = transformer.transform("com.example.Calculator", originalBytecode);
```

### Step 4: Use the Transformed Class

Load the transformed bytecode into your JVM using a custom `ClassLoader`:

```java
ClassLoader loader = new ByteArrayClassLoader(transformedBytecode);
Class<?> transformedClass = loader.loadClass("com.example.Calculator");
```

## Result

The transformed class will have logging added to the `add` method:

```java
// Original code
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}

// Transformed code
public class Calculator {
    public int add(int a, int b) {
        System.out.println("Adding " + a + " + " + b);  // Injected!
        return a + b;
    }
}
```

## Next Steps

- Learn about [Core Concepts](./core-concepts.md)
- Explore all [Features](./features.md)
- Check out [Examples](./examples.md)
