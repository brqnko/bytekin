# マッピング

bytekinは難読化または名前変更されたコードを扱うためのクラス名とメソッド名のマッピングをサポートしています。

## マッピングとは?

マッピングは人間が読める名前とバイトコード名の間を変換します。これは以下の場合に便利です:
- 難読化されたコードを扱う場合
- 名前変更されたクラスに変換を適用する場合
- バージョンの違いを処理する場合
- 複数の命名規則をサポートする場合

## マッピングプロバイダーの作成

`IMappingProvider`インターフェースを実装:

```java
public class MyMappingProvider implements IMappingProvider {

    @Override
    public String getClassName(String name) {
        // クラス名をマップ
        if ("OriginalName".equals(name)) {
            return "MappedName";
        }
        return name;
    }

    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        // クラスとシグネチャに基づいてメソッド名をマップ
        if ("MyClass".equals(className) && "oldMethod".equals(methodName)) {
            return "newMethod";
        }
        return methodName;
    }

    @Override
    public String getFieldName(String className, String fieldName) {
        // フィールド名をマップ
        if ("MyClass".equals(className) && "oldField".equals(fieldName)) {
            return "newField";
        }
        return fieldName;
    }
}
```

## マッピングの使用

ビルダーにマッピングプロバイダーを渡す:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .mapping(new MyMappingProvider())
    .build();
```

## 一般的なマッピングパターン

### シンプルな名前変更

```java
public String getClassName(String name) {
    return name.replace("OldPrefix", "NewPrefix");
}
```

### ルックアップテーブル

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

### ファイルベースのマッピング

```java
public String getClassName(String name) {
    // 設定ファイルから読み込み
    Properties props = loadMappings("mappings.properties");
    return props.getProperty(name, name);
}
```

## 難読化されたコードのマッピング

難読化されたコードを扱う場合:

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

## マッピングを使用したフック設定

人間が読める名前を使用してフックを書く:

```java
@ModifyClass("com.example.UserService")  // 読みやすい名前を使用
public class UserServiceHooks {
    @Inject(methodName = "getUser", methodDesc = "(I)Lcom/example/User;", at = At.HEAD)
    public static CallbackInfo hook() { }
}
```

マッピングプロバイダーがバイトコード内の実際のクラス名に変換します。

## デフォルト（何もしない）マッピング

変更されない名前のために空のマッピングを使用:

```java
public class EmptyMappingProvider implements IMappingProvider {

    @Override
    public String getClassName(String name) {
        return name;  // 変更なし
    }

    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        return methodName;  // 変更なし
    }

    @Override
    public String getFieldName(String className, String fieldName) {
        return fieldName;  // 変更なし
    }
}
```

## 応用: バージョン固有のマッピング

複数のバージョンをサポート:

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
        // バージョン1のマッピング
        return name;
    }

    private String mapToV2(String name) {
        // バージョン2のマッピング
        return name;
    }
}
```

## 次のステップ

- [高度な使用方法](./advanced-usage.md)を確認する
- [ベストプラクティス](./best-practices.md)を確認する
- [例](./examples.md)を探索する
