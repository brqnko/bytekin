# ベストプラクティス

このガイドは、bytekinを効果的かつ安全に使用するためのベストプラクティスをカバーしています。

## 設計原則

### 1. フックをシンプルに保つ

フックメソッドは集中的でシンプルに保ちます:

**良い:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo log() {
    System.out.println("Starting process");
    return CallbackInfo.empty();
}
```

**避けるべき:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo complexLogic() {
    // 複数のデータベース呼び出し
    // 複雑な計算
    // ファイルI/O操作
    // これはフックには多すぎます!
    return CallbackInfo.empty();
}
```

### 2. 複雑なロジックを抽出

複雑なロジックは別のメソッドに移動:

```java
@Inject(methodName = "validate", methodDesc = "(Ljava/lang/String;)Z", at = At.HEAD)
public static CallbackInfo onValidate(String input, CallbackInfo ci) {
    if (!isValidInput(input)) {
        ci.cancelled = true;
        ci.returnValue = false;
    }
    return ci;
}

private static boolean isValidInput(String input) {
    // ここに複雑な検証ロジック
    return !input.isEmpty() && input.length() < 256;
}
```

## パフォーマンスガイドライン

### 1. フックオーバーヘッドを最小化

フックは頻繁に実行されます。高速に保ちます:

**良い:**
```java
@Inject(methodName = "getData", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
public static CallbackInfo checkCache() {
    if (cacheHit()) {
        // 高速なキャッシュルックアップ
        return new CallbackInfo(true, getFromCache(), null);
    }
    return CallbackInfo.empty();
}
```

**避けるべき:**
```java
@Inject(methodName = "getData", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
public static CallbackInfo expensiveCheck() {
    // データベース全体をスキャン
    List<Item> results = database.queryAll();
    // 結果を処理
    // ...これは遅すぎます!
    return CallbackInfo.empty();
}
```

### 2. Builderを再利用

トランスフォーマーを1回ビルドして再利用:

**良い:**
```java
// 初期化コード内
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .build();

// トランスフォーマーを複数回使用
byte[] transformed1 = transformer.transform("com.example.Class1", bytes1);
byte[] transformed2 = transformer.transform("com.example.Class2", bytes2);
```

**避けるべき:**
```java
// ループ内でこれをしないでください!
for (String className : classNames) {
    // 各クラスごとにトランスフォーマーを作成するのは無駄
    BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
        .build();
    byte[] transformed = transformer.transform(className, bytes);
}
```

## エラーハンドリング

### 1. フック内で例外を処理

フック内の例外は変換を壊す可能性があります:

**良い:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo safeLogging() {
    try {
        System.out.println("Processing started");
    } catch (Exception e) {
        // 優雅に処理し、伝播させない
        e.printStackTrace();
    }
    return CallbackInfo.empty();
}
```

**避けるべき:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo unsafeLogging() {
    // これがスローされると、変換が壊れます!
    Path path = Paths.get("/invalid/path");
    Files.writeString(path, "log");
    return CallbackInfo.empty();
}
```

### 2. 戻り値を検証

CallbackInfoを変更する場合、型が正しいことを確認:

**良い:**
```java
@Inject(methodName = "getValue", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo returnCustomValue() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = 42;  // Integerは戻り値型と一致
    return ci;
}
```

**避けるべき:**
```java
@Inject(methodName = "getValue", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo wrongType() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = "42";  // Stringはint戻り値型と一致しない!
    return ci;
}
```

## ドキュメント

### 1. 変換をドキュメント化

各フックが何をするかを明確にドキュメント化:

```java
/**
 * すべてのデータアクセスメソッドに認証チェックを追加。
 * ユーザーが認証されていない場合、メソッドをキャンセルしてfalseを返す。
 */
@ModifyClass("com.example.DataStore")
public class DataStoreHooks {

    /**
     * 読み取り操作の開始時に認証チェックをインジェクト。
     *
     * @param ci CallbackInfo - 認証されていない場合cancelled=trueを設定
     */
    @Inject(methodName = "read", methodDesc = "()Ljava/lang/Object;", at = At.HEAD)
    public static CallbackInfo ensureAuthenticated(CallbackInfo ci) {
        if (!isAuthenticated()) {
            ci.cancelled = true;
            ci.returnValue = null;
        }
        return ci;
    }
}
```

### 2. パラメータをドキュメント化

どのパラメータがメソッド引数に対応するかを明確に示す:

```java
/**
 * 処理前にユーザー入力をサニタイズ。
 *
 * @param userId ユーザーID（ターゲットメソッドの第1引数）
 * @param action 要求されたアクション（第2引数）
 */
@Inject(methodName = "execute", methodDesc = "(Ljava/lang/String;Ljava/lang/String;)V",
        at = At.HEAD)
public static CallbackInfo sanitizeInput(String userId, String action) {
    // userIdとactionはターゲットメソッドのパラメータから
    return CallbackInfo.empty();
}
```

## テスト

### 1. 変換をテスト

常に変換をテスト:

```java
public class TransformationTest {
    @Test
    public void testInjectionWorks() {
        BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
            .build();

        byte[] original = getClassBytecode("com.example.Target");
        byte[] transformed = transformer.transform("com.example.Target", original);

        // 変換されたクラスをロードしてテスト
        Class<?> clazz = loadFromBytecode(transformed);
        Object instance = clazz.newInstance();

        // 変換が適用されたことを確認
        assertNotNull(instance);
    }
}
```

### 2. リグレッションがないことを確認

元の動作が保持されていることを確認:

```java
@Test
public void testOriginalBehaviorPreserved() {
    // 変換なしでテスト
    Calculator calc1 = new Calculator();
    int result1 = calc1.add(3, 4);

    // 変換ありでテスト
    byte[] transformed = applyTransformation(Calculator.class);
    Calculator calc2 = loadTransformed(transformed);
    int result2 = calc2.add(3, 4);

    // 結果は同じであるべき
    assertEquals(result1, result2);
}
```

## 互換性

### 1. バージョン互換性

サポートされているJavaバージョンをドキュメント化:

```java
/**
 * これらのフックはJava 8+で動作
 * バージョン間で互換性のある標準メソッドディスクリプタを使用
 */
@ModifyClass("com.example.Service")
public class CompatibleHooks {
    // ...
}
```

### 2. ライブラリ互換性

他のバイトコードツールとの非互換性を確認:

```java
// 他のバイトコード操作との競合をドキュメント化
// 例: Spring、Mockito、AspectJなど
```

## セキュリティ

### 1. 入力検証

フック内で常に入力を検証:

```java
@Inject(methodName = "processFile", methodDesc = "(Ljava/lang/String;)V",
        at = At.HEAD)
public static CallbackInfo validatePath(String path, CallbackInfo ci) {
    if (path != null && isPathTraversal(path)) {
        // ディレクトリトラバーサル攻撃を防ぐ
        ci.cancelled = true;
    }
    return ci;
}

private static boolean isPathTraversal(String path) {
    return path.contains("..") || path.startsWith("/");
}
```

### 2. 機密データの露出を避ける

機密情報をログに記録したり露出させたりしない:

**良い:**
```java
@Inject(methodName = "login", methodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z",
        at = At.HEAD)
public static CallbackInfo logAttempt(String user) {
    System.out.println("Login attempt by: " + user);
    return CallbackInfo.empty();
}
```

**避けるべき:**
```java
@Inject(methodName = "login", methodDesc = "(Ljava/lang/String;Ljava/lang/String;)Z",
        at = At.HEAD)
public static CallbackInfo logAttempt(String user, String password) {
    // パスワードをログに記録しないでください!
    System.out.println("Login attempt: " + user + " / " + password);
    return CallbackInfo.empty();
}
```

## デバッグのヒント

### 1. バイトコード検査

生成されたバイトコードを検査して変換を確認:

```bash
# javapを使用して変換されたクラスを検査
javap -c TransformedClass.class

# インジェクトされたメソッド呼び出しを探す
```

### 2. ログを追加

ログを使用して変換の実行を追跡:

```java
@Inject(methodName = "critical", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo logEntry() {
    System.out.println("[DEBUG] Entering critical method");
    System.out.println("[DEBUG] Stack trace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
    return CallbackInfo.empty();
}
```

## メンテナンス

### 1. フックのバージョン管理

フックのバージョンを追跡:

```java
/**
 * バージョン2.0の変換フック
 *
 * v1.0からの変更点:
 * - 認証チェックを追加
 * - キャッシング戦略を最適化
 * - レガシーコードのnullポインタ問題を修正
 */
@ModifyClass("com.example.Service")
public class ServiceHooksV2 {
    // ...
}
```

### 2. 記録を保持

各変換が存在する理由をドキュメント化:

```
変換: Calculator.add()のログ記録
作成日: 2025-01-15
理由: デバッグビルドのパフォーマンス監視
ステータス: アクティブ
注記: プロファイリングフェーズ後に削除可能
```

## よくある落とし穴

### 1. 間違ったメソッドディスクリプタ

❌ **誤り:**
```java
@Inject(methodName = "add", methodDesc = "(I I)I", at = At.HEAD)  // ディスクリプタ内のスペース!
```

✅ **正しい:**
```java
@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)
```

### 2. 型の不一致

❌ **誤り:**
```java
@Invoke(..., invokeMethodDesc = "(I)V", shift = Shift.BEFORE)
public static CallbackInfo hook(String param) {  // 型の不一致!
}
```

✅ **正しい:**
```java
@Invoke(..., invokeMethodDesc = "(I)V", shift = Shift.BEFORE)
public static CallbackInfo hook(int param) {  // 正しい型
}
```

### 3. 不変データの変更

❌ **誤り:**
```java
@ModifyVariable(methodName = "process", variableIndex = 1)
public static void modify(String str) {
    str = str.toUpperCase();  // Stringは不変、機能しない!
}
```

✅ **正しい:**
```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo modifyByReplacing(String str, CallbackInfo ci) {
    ci.modifyArgs = new Object[]{str.toUpperCase()};
    return ci;
}
```

## 次のステップ

- [APIリファレンス](./api-reference.md)を確認する
- [例](./examples.md)を確認する
- コミュニティに参加してパターンを共有する
