# Mappings

bytekin supports class and method name mappings for working with obfuscated or renamed code.

## What Are Mappings?

Mappings translate between human-readable names and bytecode names. This is useful when:
- Working with obfuscated code
- Applying transformations to renamed classes
- Handling version differences
- Supporting multiple naming conventions

## Creating a Mapping Provider

Implement `IMappingProvider` interface:

```java
public class MyMappingProvider implements IMappingProvider {
    
    @Override
    public String getClassName(String name) {
        // Map class names
        if ("OriginalName".equals(name)) {
            return "MappedName";
        }
        return name;
    }
    
    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        // Map method names based on class and signature
        if ("MyClass".equals(className) && "oldMethod".equals(methodName)) {
            return "newMethod";
        }
        return methodName;
    }
    
    @Override
    public String getFieldName(String className, String fieldName) {
        // Map field names
        if ("MyClass".equals(className) && "oldField".equals(fieldName)) {
            return "newField";
        }
        return fieldName;
    }
}
```

## Using Mappings

Pass mapping provider to builder:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .mapping(new MyMappingProvider())
    .build();
```

## Common Mapping Patterns

### Simple Rename

```java
public String getClassName(String name) {
    return name.replace("OldPrefix", "NewPrefix");
}
```

### Lookup Table

```java
private static final Map<String, String> classMap = new HashMap<>();

static {
    classMap.put("a", "com.example.ClassA");
    classMap.put("b", "com.example.ClassB");
}

public String getClassName(String name) {
    return classMap.getOrDefault(name, name);
}
```

### File-Based Mappings

```java
public String getClassName(String name) {
    // Load from configuration file
    Properties props = loadMappings("mappings.properties");
    return props.getProperty(name, name);
}
```

## Mapping Obfuscated Code

When working with obfuscated code:

```java
public class ObfuscationMapping implements IMappingProvider {
    
    @Override
    public String getClassName(String name) {
        // a.class -> com.example.MyClass
        switch (name) {
            case "a": return "com.example.MyClass";
            case "b": return "com.example.OtherClass";
            default: return name;
        }
    }
    
    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        // a.b() -> MyClass.process()
        if ("com.example.MyClass".equals(className)) {
            switch (methodName) {
                case "b": return "process";
                case "c": return "validate";
                default: return methodName;
            }
        }
        return methodName;
    }
}
```

## Hook Configuration with Mappings

Write hooks using human-readable names:

```java
@ModifyClass("com.example.UserService")  // Use readable name
public class UserServiceHooks {
    @Inject(methodName = "getUser", methodDesc = "(I)Lcom/example/User;", at = At.HEAD)
    public static CallbackInfo hook() { }
}
```

The mapping provider will translate to actual class names in bytecode.

## Default (No-Op) Mapping

Use empty mapping for unchanged names:

```java
public class EmptyMappingProvider implements IMappingProvider {
    
    @Override
    public String getClassName(String name) {
        return name;  // No change
    }
    
    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        return methodName;  // No change
    }
    
    @Override
    public String getFieldName(String className, String fieldName) {
        return fieldName;  // No change
    }
}
```

## Advanced: Version-Specific Mappings

Support multiple versions:

```java
public class VersionAwareMappingProvider implements IMappingProvider {
    private final String version;
    
    public VersionAwareMappingProvider(String version) {
        this.version = version;
    }
    
    @Override
    public String getClassName(String name) {
        if ("1.0".equals(version)) {
            return mapToV1(name);
        } else if ("2.0".equals(version)) {
            return mapToV2(name);
        }
        return name;
    }
    
    private String mapToV1(String name) {
        // Version 1 mappings
        return name;
    }
    
    private String mapToV2(String name) {
        // Version 2 mappings
        return name;
    }
}
```

## Next Steps

- Review [Advanced Usage](./advanced-usage.md)
- Check [Best Practices](./best-practices.md)
- Explore [Examples](./examples.md)
