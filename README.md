# bytekin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![CI](https://github.com/brqnko/bytekin/actions/workflows/gradle.yml/badge.svg)](https://github.com/brqnko/bytekin/actions)
[![JitPack](https://jitpack.io/v/brqnko/bytekin.svg)](https://jitpack.io/#brqnko/bytekin)

A lightweight Java bytecode manipulation framework built on ASM.

## Features

- **Simple API** - Annotation-based bytecode transformation
- **Type-safe** - No raw bytecode manipulation required
- **Flexible** - Inject, invoke, redirect, and modify methods
- **Zero dependencies** - Only requires ASM

## Installation

Add JitPack repository to your build file:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.brqnko:bytekin:1.0-SNAPSHOT'
}
```

## Quick Example

```java
@Inject(
    target = @MethodTarget(
        owner = "com/example/MyClass",
        name = "myMethod",
        descriptor = "()V"
    ),
    at = @At(value = At.Position.HEAD)
)
public static void beforeMyMethod(CallbackInfo ci) {
    System.out.println("Method called!");
}
```

## Documentation

[Full Documentation](https://brqnko.github.io/bytekin/en/) | [日本語ドキュメント](https://brqnko.github.io/bytekin/ja/)

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.


