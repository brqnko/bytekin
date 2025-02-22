# bytekin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![CI](https://github.com/brqnko/bytekin/actions/workflows/gradle.yml/badge.svg)](https://github.com/brqnko/bytekin/actions)

bytekin is a small Java bytecode manipulation framework built in ASM

## Usage in 3 steps

### 1. Define your hooks:

```java
@ModifyClass("com.example.Example")
public class Example {

    @Inject(methodName = "example", methodDesc = "(I)Ljava/lang/String;", at = At.HEAD)
    public CallbackInfo exampleMethod(Example self, int i) {
        return CallbackInfo.empty();
    }
}
```

### 2. Instantiate BytekinTransformer:

You can use mappings or multiple transformers if you want

```java
BytekinTransformer transformer = new BytekinTransformer(Example.class);
```

### 3. Transform your classes:

Simply call the transform method with the class name and the original bytecode, then transform function will return the transformed bytecode

```java
byte[] transformed = transformer.transform("com.example.Example", original);
```

## Features

### Inject

Transform class Example:

```java
@ModifyClass("com.example.Example")
public class Example {
    
    @Inject(methodName = "example", methodDesc = "(I)Ljava/lang/String;", at = At.HEAD)
    public CallbackInfo exampleMethod(Example self, int i) {
        return CallbackInfo.empty();
    }
}
```

Transformed class will look like this:

```java
public class Example {
    
    public String example(int i) {
        CallbackInfo ci = ExampleHooks.exampleMethod(this, i);
        if (ci.cancelled) {
            return ci.returnValue;
        }
        
        // existing code
    }
}
```

### Invoke

Transform class Example:

```java
@ModifyClass("com.example.Example")
public class Example {
    
    @Invoke(
            targetMethodName = "example",
            targetMethodDesc = "(I)Ljava/lang/String;",
            invokeMethodOwner = "com.example.Example",
            invokeMethodName = "example2",
            invokeMethodDesc = "(I)Ljava/lang/String;",
            shift = Shift.BEFORE
    )
    public static CallbackInfo invokeBefore(Example self, int i, int j) {
        return new CallbackInfo(false, null, new Object[]{ 100 });
    }
    
}
```

Transformed class will look like this:

```java
public class Example {
    
    public String example(int i) {
        // existing code
        
        CallbackInfo ci = ExampleHooks.invokeBefore(this, i, something);
        if (ci.cancelled) {
            return ci.returnValue;
        }
        example2((int) ci.modifyArgs[0]);
        
        // existing code
    }
    
    public String example2(int i) {
        // existing code
    }
}
```

## License

bytekin is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for more information.
