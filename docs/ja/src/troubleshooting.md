# トラブルシューティングガイド

このガイドは、bytekin使用時の一般的な問題を解決するのに役立ちます。

## 変換が適用されない

### 症状

- フックメソッドが呼び出されない
- 元のコードが変更なしで実行される
- フック内のブレークポイントが到達されない

### 原因と解決策

#### 1. 不正なクラス名

`@ModifyClass`の値はバイトコードのクラス名と正確に一致する必要があります。

**問題:**
```java
@ModifyClass("Calculator")  // 誤り!
public class CalcHooks { }
```

**解決策:**
```java
@ModifyClass("com.example.Calculator")  // 正しい
public class CalcHooks { }
```

**確認方法:**
```bash
# JAR内のすべてのクラスをリスト
jar tf myapp.jar | grep -i calculator
```

#### 2. 間違ったメソッドディスクリプタ

`methodDesc`はバイトコード内のメソッドシグネチャと正確に一致する必要があります。

**問題:**
```java
// バイトコード内のメソッド: public int add(int a, int b)
@Inject(methodName = "add", methodDesc = "(int, int)int", at = At.HEAD)  // 誤り!
public static CallbackInfo hook() { }
```

**解決策:**
```java
@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)  // 正しい
public static CallbackInfo hook() { }
```

**正しいディスクリプタの見つけ方:**
```bash
# javapを使用してメソッドシグネチャを確認
javap -c com.example.Calculator | grep -A 5 "public int add"
```

#### 3. フッククラスがBuilderに渡されていない

フッククラスはBuilderに渡す必要があります。

**問題:**
```java
BytekinTransformer transformer = new BytekinTransformer.Builder()
    .build();  // フックはどこ?
```

**解決策:**
```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .build();  // フッククラスを渡す
```

#### 4. クラスがまだロードされていない

変換は、JVMがクラスをロードする前に適用する必要があります。

**問題:**
```java
// クラスがすでにロード済み
Class<?> clazz = Class.forName("com.example.MyClass");

// 今変換しようとしている - 遅すぎる!
byte[] transformed = transformer.transform("com.example.MyClass", bytecode);
```

**解決策:**
- ロード中に変換を適用するカスタム`ClassLoader`を使用
- またはクラスロードをインターセプトするJava instrumentation/agentsを使用

## 型不一致エラー

### 症状

- `java.lang.ClassCastException`
- メソッドから誤った値が返される
- 型の非互換性エラー

### 一般的な原因

#### 1. CallbackInfoの間違った戻り値型

**問題:**
```java
@Inject(methodName = "getCount", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo wrongReturn() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = "42";  // intの代わりにString!
    return ci;
}
```

**解決策:**
```java
@Inject(methodName = "getCount", methodDesc = "()I", at = At.HEAD)
public static CallbackInfo correctReturn() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = 42;  // 正しい: int
    return ci;
}
```

#### 2. フックメソッドの間違ったパラメータ型

**問題:**
```java
// ターゲットメソッド: void process(int count, String name)
@Inject(methodName = "process", methodDesc = "(ILjava/lang/String;)V", at = At.HEAD)
public static CallbackInfo wrongParams(String name, int count) {  // 逆!
    return CallbackInfo.empty();
}
```

**解決策:**
```java
@Inject(methodName = "process", methodDesc = "(ILjava/lang/String;)V", at = At.HEAD)
public static CallbackInfo correctParams(int count, String name) {  // 正しい順序
    return CallbackInfo.empty();
}
```

#### 3. 引数を間違った型に変更

**問題:**
```java
@Invoke(..., shift = Shift.BEFORE)
public static CallbackInfo wrongArgType() {
    CallbackInfo ci = new CallbackInfo();
    ci.modifyArgs = new Object[]{"100"};  // intの代わりにString
    return ci;
}
```

**解決策:**
```java
@Invoke(..., shift = Shift.BEFORE)
public static CallbackInfo correctArgType() {
    CallbackInfo ci = new CallbackInfo();
    ci.modifyArgs = new Object[]{100};  // 正しい: int
    return ci;
}
```

## ヌルポインタ例外

### 症状

- 変換中のNPE
- 変換されたメソッド呼び出し時のNPE
- バイトコードから発生するスタックトレース

### 原因と解決策

#### 1. インジェクションからnullを返す

**問題:**
```java
@Inject(methodName = "getValue", methodDesc = "()Ljava/lang/String;", at = At.HEAD)
public static CallbackInfo returnNull() {
    CallbackInfo ci = new CallbackInfo();
    ci.cancelled = true;
    ci.returnValue = null;  // オブジェクトには有効だが、期待されていない可能性
    return ci;
}
```

**解決策:**
- nullが返される可能性があることを文書化
- または代わりにデフォルト値を返す:
```java
ci.returnValue = "";  // nullの代わりに空文字列
```

#### 2. フック内でnullパラメータにアクセス

**問題:**
```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo unsafeAccess(String input) {
    System.out.println(input.length());  // inputがnullの場合NPE!
    return CallbackInfo.empty();
}
```

**解決策:**
```java
@Inject(methodName = "process", methodDesc = "(Ljava/lang/String;)V", at = At.HEAD)
public static CallbackInfo safeAccess(String input) {
    if (input != null) {
        System.out.println(input.length());
    }
    return CallbackInfo.empty();
}
```

## パフォーマンスの問題

### 症状

- アプリケーションの起動が遅い
- メモリ使用量が多い
- 応答時間が低下

### 原因と解決策

#### 1. 複雑なフックメソッド

**問題:**
```java
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo slowHook() {
    // データベースクエリ
    List<Item> items = database.queryAll();
    // ファイルI/O
    Files.write(Paths.get("log.txt"), data);
    // 高コストな計算
    // ...
    return CallbackInfo.empty();
}
```

**解決策:**
- フックをシンプルで高速に保つ
- 高コストな作業をバックグラウンドスレッドに延期
- リソースの遅延初期化を使用

#### 2. トランスフォーマーの繰り返しビルド

**問題:**
```java
for (String className : classNames) {
    // 各クラスごとに新しいトランスフォーマーを作成!
    BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class)
        .build();
    transformer.transform(className, bytecode);
}
```

**解決策:**
```java
// 1回ビルドして、何度も再利用
BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class)
    .build();

for (String className : classNames) {
    transformer.transform(className, bytecode);
}
```

#### 3. 不要なクラスの変換

**問題:**
```java
// 必要ない場合でも、すべてのクラスに変換を適用
for (String className : allClasses) {
    byte[] transformed = transformer.transform(className, bytecode);
}
```

**解決策:**
- 必要な特定のクラスのみを変換
- フィルタリング/命名パターンを使用
- ホットスポットを特定するためにプロファイル

## バイトコード検証エラー

### 症状

- クラスロード時の`java.lang.VerifyError`
- 「Illegal type at offset X」エラー
- スタックトレースが解釈困難

### 一般的な原因

#### 1. 無効なバイトコード変更

これは通常、変換が無効なバイトコードを作成したことを意味します。

**デバッグ方法:**
1. `javap`を使用して変換されたバイトコードを検査
2. 異常な命令シーケンスを探す
3. 戻り値型が一致することを確認

#### 2. 不正なメソッドディスクリプタ

不正なディスクリプタは検証失敗を引き起こす可能性があります。

**解決策:**
- すべてのメソッドディスクリプタを再確認
- 確認のためにオンラインディスクリプタコンバータを使用
- `javap`出力と比較

## メソッドが見つからない

### 症状

- 特定のメソッドが変換されていない
- オーバーロードされたメソッドが問題を引き起こす
- コンストラクタ変換が失敗

### 原因と解決策

#### 1. オーバーロードされたメソッド

オーバーロードされたメソッドは完全なディスクリプタで区別する必要があります。

**問題:**
```java
// クラスに複数のadd()メソッドがある
// add(int, int) と add(double, double)

@Inject(methodName = "add", methodDesc = "(II)I", at = At.HEAD)  // intバージョンのみに一致
public static CallbackInfo hook() { }
```

**解決策:**
- パラメータと戻り値型を含む完全なディスクリプタを使用
- ディスクリプタが自動的にオーバーロードを区別

#### 2. プライベートまたは内部メソッド

一部のプライベートメソッドはアクセスできない可能性があります。

**問題:**
```java
@Inject(methodName = "internalMethod", methodDesc = "()V", at = At.HEAD)  // プライベートメソッド
public static CallbackInfo hook() { }
```

**解決策:**
- メソッドが合成またはブリッジメソッドでないことを確認
- メソッド名とディスクリプタが正確に正しいことを確認

## 変換されたクラスをロードできない

### 症状

- 変換後のClassNotFoundException
- クラスが見つからないようだ
- カスタムClassLoaderの問題

### 原因と解決策

#### 1. 不正なClassLoaderセットアップ

**問題:**
```java
// デフォルトのクラスローダーで変換されたバイトコードを使用しようとしている
byte[] transformed = transformer.transform("com.example.MyClass", bytecode);
Class<?> clazz = Class.forName("com.example.MyClass");  // 変換されたバイトコードを使用しない!
```

**解決策:**
- 変換されたバイトコードを使用するカスタムClassLoaderを作成
- またはinstrumentation/agentsを使用してロードをインターセプト

#### 2. バイトコードの破損

変換が無効なバイトコードを生成した可能性があります。

**解決策:**
- 変換がバイトコードを破損していないことを確認
- バイトコードのサイズ/整合性を確認
- バイトコード検査ツールを使用

## デバッグのヒント

### 1. 詳細な出力を有効にする

```java
// フックにデバッグログを追加
@Inject(methodName = "process", methodDesc = "()V", at = At.HEAD)
public static CallbackInfo debug() {
    System.out.println("[DEBUG] Hook executed");
    System.out.println("[DEBUG] Stack: " + Arrays.toString(Thread.currentThread().getStackTrace()));
    return CallbackInfo.empty();
}
```

### 2. バイトコードを検査

```bash
# 変換されたバイトコードを表示
javap -c -private TransformedClass.class

# インジェクトされた呼び出しを探す
```

### 3. バイトコードビューアを使用

Bytecode ViewerやIDEAプラグインなどのツールがバイトコードの可視化に役立ちます。

### 4. パフォーマンスをプロファイル

```bash
# JProfilerまたはYourKitを使用してボトルネックを特定
# メモリ使用量とCPU時間を監視
```

## よくある質問

**Q: ブートストラップクラスを変換できますか?**
A: 標準のクラスローダーでは簡単ではありません。instrumentation APIでJavaエージェントを使用してください。

**Q: 変換はシリアライゼーションに影響しますか?**
A: 変換されたクラスは異なるバイトコードを持ちますが、フィールドを変更しなければ同じシリアライゼーション形式です。

**Q: bytekinをSpring Bootで使用できますか?**
A: はい、ただしカスタムクラスロードを設定するか、エージェントを使用する必要があります。

## ヘルプを得る

1. このトラブルシューティングガイドを確認
2. [ベストプラクティス](./best-practices.md)を確認
3. [例](./examples.md)を確認
4. [GitHub](https://github.com/brqnko/bytekin/issues)でissueを開く

## 次のステップ

- [ベストプラクティス](./best-practices.md)を確認する
- [例](./examples.md)を確認する
- GitHubでissueを報告する
