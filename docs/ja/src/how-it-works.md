# How bytekin Works

This document explains the internal mechanisms of bytekin and how it transforms bytecode.

## The Transformation Process

### Step 1: Initialization

```
Define Hook Classes
        ↓
Create BytekinTransformer.Builder
        ↓
Pass hook classes to Builder
```

Example:
```java
BytekinTransformer transformer = new BytekinTransformer.Builder(
    CalculatorHooks.class,
    StringHooks.class
).build();
```

### Step 2: Analysis

When `build()` is called, bytekin:

1. Scans hook classes for annotations
2. Extracts transformation rules
3. Validates method signatures
4. Prepares transformation strategy
5. Creates BytekinClassTransformer instances

```
Builder.build()
    ↓
Scan @ModifyClass annotations
    ↓
Extract @Inject, @Invoke, etc.
    ↓
Create transformers map
    ↓
Return BytekinTransformer
```

### Step 3: Transformation

When `transform()` is called:

```java
byte[] transformed = transformer.transform("com.example.Calculator", bytecode);
```

bytekin:

1. Looks up the target class
2. Finds matching transformer
3. Uses ASM to parse bytecode
4. Applies all registered transformations
5. Returns modified bytecode

```
Target Bytecode
    ↓
ASM ClassReader
    ↓
BytekinClassVisitor
    ↓
Apply Injections
    ↓
Apply Invocations
    ↓
Apply Redirects
    ↓
Apply Constant Modifications
    ↓
Apply Variable Modifications
    ↓
ASM ClassWriter
    ↓
Modified Bytecode
```

## Architecture Overview

### Core Components

```
┌─────────────────────────────────────┐
│   BytekinTransformer (Main API)     │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        ↓             ↓
    Builder      transform()
        │             │
        └──────┬──────┘
               ↓
   ┌───────────────────────────┐
   │  BytekinClassTransformer  │
   └───────────┬───────────────┘
               ↓
   ┌───────────────────────────┐
   │   BytekinClassVisitor     │
   │  (ASM ClassVisitor)       │
   └───────────┬───────────────┘
               ↓
   ┌───────────────────────────┐
   │  BytekinMethodVisitor     │
   │  (ASM MethodVisitor)      │
   └───────────────────────────┘
```

### Visitor Pattern

bytekin uses the **Visitor Pattern** (from ASM):

```
┌─ ClassVisitor
│   └─ Method Visitor
│       └─ Code Visitor
│           └─ Instruction Handlers
```

As ASM parses bytecode, it calls methods on visitors to notify them of each element (class, method, field, instruction, etc.).

## Transformation Types

### 1. Injection (Code Insertion)

**Goal**: Insert code at specific method points

```
Method Bytecode
    ↓
Find injection point
    ↓
Insert call to hook method
    ↓
Continue with original code
```

Example locations:
- `At.HEAD`: Before method body
- `At.RETURN`: Before return statements
- `At.TAIL`: At method end

### 2. Invocation (Method Call Interception)

**Goal**: Intercept method calls within a method

```
Method Bytecode
    ↓
Find target method invocation
    ↓
Call hook method with same arguments
    ↓
Optionally modify arguments
    ↓
Make method call (or skip it)
```

### 3. Redirect (Call Target Change)

**Goal**: Change which method is called

```
Method Call to A()
    ↓
Intercept call
    ↓
Redirect to B()
```

### 4. Constant Modification

**Goal**: Change constant values

```
Load Constant X
    ↓
Replace with Constant Y
```

### 5. Variable Modification

**Goal**: Modify local variable values

```
Local Variable at index N
    ↓
Load from slot
    ↓
Apply modification
    ↓
Store back
```

## Key Data Structures

### Injection

Represents a code injection:
- **Target Method**: Which method to inject into
- **Hook Method**: Which hook method to call
- **Location**: Where to inject (HEAD, RETURN, etc.)
- **Parameters**: What parameters to pass

### Invocation

Represents a method call interception:
- **Target Method**: Which method calls the target
- **Target Call**: Which call to intercept
- **Hook Method**: Which hook to call
- **Shift**: Before or after the call

### CallbackInfo

Controls injection behavior:
```java
public class CallbackInfo {
    public boolean cancelled;      // Cancel execution?
    public Object returnValue;     // Custom return?
    public Object[] modifyArgs;    // Modified arguments?
}
```

## Mapping System

bytekin supports class/method name mappings for obfuscated code:

```
Original Name     Mapped Name
     ↓                  ↓
  a.class  ────→  com.example.Calculator
  b(II)I   ────→  add(II)I
```

Mappings are applied during transformation:

```
Hook class references "com.example.Calculator"
    ↓
Apply mapping
    ↓
Look for mapped name in bytecode
    ↓
Transform accordingly
```

## Thread Safety

- **BytekinTransformer**: Thread-safe after `build()`
- **Builder**: Not thread-safe during configuration
- **transform()**: Can be called concurrently from multiple threads

## Performance Considerations

### Efficiency

- **One-time cost**: Building transformers
- **Transform time**: Proportional to bytecode size
- **Runtime overhead**: Only injected code is executed

### Optimization Tips

1. Build transformers once, reuse them
2. Use transformation early in classloading
3. Minimize hook method complexity
4. Profile to identify bottlenecks

## Next Steps

- Explore [Features](./features.md)
- Learn about [Advanced Usage](./advanced-usage.md)
- Check out [Examples](./examples.md)
