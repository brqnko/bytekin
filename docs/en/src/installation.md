# Installation Guide

## Using Maven

### 1. Add Dependency

Edit your `pom.xml` file and add the following dependency:

```xml
<dependency>
    <groupId>io.github.brqnko.bytekin</groupId>
    <artifactId>bytekin</artifactId>
    <version>1.0</version>
</dependency>
```

### 2. Update Your Project

Run Maven to download dependencies:

```bash
mvn clean install
```

## Using Gradle

### 1. Add Dependency

Edit your `build.gradle` file and add:

```gradle
dependencies {
    implementation 'io.github.brqnko.bytekin:bytekin:1.0'
}
```

### 2. Sync Your Project

For Android Studio or IntelliJ IDEA, you can sync Gradle manually. For command line:

```bash
./gradlew build
```

## Dependency Requirements

bytekin has minimal dependencies:

| Dependency | Version | Purpose |
|-----------|---------|---------|
| ASM | 9.7.1+ | Bytecode manipulation library |
| Java | 8+ | Runtime environment |

## Verification

After installation, verify that bytekin is correctly set up by running a simple test:

```java
import io.github.brqnko.bytekin.transformer.BytekinTransformer;

public class ByitekVer {
    public static void main(String[] args) {
        BytekinTransformer transformer = new BytekinTransformer.Builder()
            .build();
        System.out.println("bytekin is ready to use!");
    }
}
```

## Troubleshooting Installation

### Maven: Dependency not found

- Ensure you're connected to the internet
- Try running `mvn clean` and then `mvn install` again
- Check if the repository is accessible

### Gradle: Build fails

- Run `./gradlew clean` first
- Check your Gradle wrapper version
- Verify Java version compatibility

## Next Steps

- [Your First Transformation](./first-transformation.md)
- [Core Concepts](./core-concepts.md)
