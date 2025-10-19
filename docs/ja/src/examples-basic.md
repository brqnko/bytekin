# 例 - 基本的な使用方法

このセクションには、一般的なbytekinのユースケースの完全で動作する例が含まれています。

## 例1: ログの追加

### 問題
ソースコードを変更せずにメソッドにログを追加する。

### 解決策

**ターゲットクラス:**
```java
package com.example;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

**フッククラス:**
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

**使用方法:**
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
        // 出力:
        // Adding: 5 + 3
        // 8
    }
}
```

## 例2: パラメータ検証

### 問題
実行前にメソッドパラメータを検証する。

### 解決策

**フッククラス:**
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

## 例3: キャッシング

### 問題
キャッシングを実装するためにメソッド呼び出しをインターセプトする。

### 解決策

**フッククラス:**
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

## 例4: セキュリティ - 認証チェック

### 問題
すべての機密メソッドに認証が必要であることを確認する。

### 解決策

**フッククラス:**
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
        // 認証状態を確認
        return true;
    }
}
```

## 例5: 監視 - メソッド呼び出しカウンター

### 問題
特定のメソッドが何回呼び出されたかをカウントする。

### 解決策

**フッククラス:**
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

## 例6: 戻り値の変換

### 問題
メソッドの戻り値を変更する。

### 解決策

**フッククラス:**
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
        double discounted = originalPrice * 0.9;  // 10%割引
        ci.returnValue = discounted;
        return ci;
    }
}
```

## Invokeの例

### 例: メソッド呼び出しのインターセプト

**フッククラス:**
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

## 組み合わせ例: 包括的な変換

**完全なフッククラス:**
```java
@ModifyClass("com.example.UserRepository")
public class ComprehensiveHooks {

    @Inject(
        methodName = "save",
        methodDesc = "(Lcom/example/User;)V",
        at = At.HEAD
    )
    public static CallbackInfo validateBeforeSave(Object user, CallbackInfo ci) {
        // 入力を検証
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
        // 検証を強化
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

## 次のステップ

- [高度な例](./examples-advanced.md)を確認する
- [ベストプラクティス](./best-practices.md)を確認する
- さらなる[機能](./features.md)を探索する
