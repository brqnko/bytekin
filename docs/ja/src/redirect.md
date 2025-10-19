# リダイレクト変換

`@Redirect`アノテーションを使用すると、実行時に実際に呼び出されるメソッドを変更できます。

## 基本的な使用法

```java
@ModifyClass("com.example.LegacyService")
public class LegacyServiceHooks {

    @Redirect(
        targetMethodName = "oldMethod",
        targetMethodDesc = "(I)V",
        redirectMethodName = "newMethod",
        redirectMethodDesc = "(I)V"
    )
    public static void redirectCall(int value) {
        System.out.println("Redirecting call with value: " + value);
    }
}
```

## アノテーションパラメータ

### targetMethodName (必須)

リダイレクトする呼び出しを含むメソッドの名前。

```java
targetMethodName = "process"
```

### targetMethodDesc (必須)

ターゲットメソッドのJVMディスクリプタ。

```java
targetMethodDesc = "(Ljava/lang/String;)V"
```

### redirectMethodName (必須)

代わりに呼び出す新しいメソッドの名前。

```java
redirectMethodName = "newImplementation"
```

### redirectMethodDesc (必須)

リダイレクトメソッドのJVMディスクリプタ。

```java
redirectMethodDesc = "(Ljava/lang/String;)V"
```

## リダイレクトの仕組み

**前:**
```java
public class LegacyAPI {
    public void oldMethod(int value) {
        // 古い実装
    }
}

public class Client {
    public void use() {
        api.oldMethod(42);  // oldMethodを呼び出す
    }
}
```

**リダイレクト後:**
```java
public class Client {
    public void use() {
        api.newMethod(42);  // newMethodにリダイレクト!
    }
}
```

## 実用的な例

### 移行戦略

古いAPIから新しいAPIに段階的に移行:

```java
@ModifyClass("com.example.Application")
public class APIRedirection {

    @Redirect(
        targetMethodName = "main",
        targetMethodDesc = "([Ljava/lang/String;)V",
        redirectMethodName = "legacySearch",
        redirectMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        from = "oldSearch",
        to = "modernSearch"
    )
    public static void upgradeSearch() {
        // 検索呼び出しが最新の実装にルーティングされる
    }
}
```

### テストのためのモッキング

実際の実装をテストダブルで置き換え:

```java
@ModifyClass("com.example.DataAccess")
public class TestRedirection {

    @Redirect(
        targetMethodName = "query",
        targetMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        redirectMethodName = "fetchFromDatabase",
        redirectMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
        from = "realDB",
        to = "mockDB"
    )
    public static void useMockDatabase() {
        // すべてのデータベース呼び出しがモック実装を使用
    }
}
```

### パフォーマンス最適化

最適化された実装にルーティング:

```java
@ModifyClass("com.example.Processing")
public class PerformanceOptimization {

    @Redirect(
        targetMethodName = "processLargeList",
        targetMethodDesc = "(Ljava/util/List;)Ljava/util/List;",
        redirectMethodName = "slowImplementation",
        redirectMethodDesc = "(Ljava/util/List;)Ljava/util/List;",
        from = "bruteForce",
        to = "optimized"
    )
    public static void useOptimizedAlgorithm() {
        // 遅いアルゴリズムの代わりに高速アルゴリズムを使用
    }
}
```

## 他の変換との違い

| 機能 | インジェクション | インボケーション | リダイレクト |
|---------|--------|--------|----------|
| 何をするか | コードを挿入 | 呼び出しをインターセプト | ターゲットを変更 |
| 呼び出しは発生するか | はい | はい | はい、ただし異なるターゲット |
| 実行をスキップできるか | はい | はい | はい |
| ユースケース | ロギングの追加 | 動作を変更 | API移行 |

## 型の互換性

リダイレクトメソッドは互換性のあるシグネチャを持つ必要があります:

```java
// 元の呼び出し
search(String query);  // (Ljava/lang/String;)Ljava/util/List;

// 互換性のあるシグネチャにリダイレクトする必要がある
newSearch(String query);  // (Ljava/lang/String;)Ljava/util/List;
```

**型の不一致は問題を引き起こします:**
```java
// 間違い - パラメータの型が異なる
@Redirect(..., from = "process(int)", to = "process(String)")
```

## パフォーマンスの考慮事項

リダイレクトは通常のメソッド呼び出しと比較して最小限のオーバーヘッドです:

1. 直接的なバイトコード置換である
2. ラッパーやプロキシは作成されない
3. JVMは通常通りインライン化と最適化が可能

## 制限事項

- 両方のメソッドは互換性のあるシグネチャを持つ必要がある
- finalメソッドにリダイレクトできない
- コンストラクタ呼び出しをリダイレクトできない(代わりに`@Invoke`を使用)
- リダイレクトは静的 - すべての呼び出しで同じターゲット

## ベストプラクティス

1. **互換性を確保**: メソッドシグネチャが完全に一致することを確認
2. **リダイレクトを文書化**: なぜかを説明するコメントを残す
3. **リダイレクトをテスト**: リダイレクト後の動作を検証
4. **移行に使用**: 古いAPIから新しいAPIへの移行に最適
5. **注意する**: 混乱を避けるためにすべてのリダイレクトを追跡

## 高度なパターン: 条件付きリダイレクト

`@Redirect`は静的ですが、`@Invoke`と組み合わせて条件付き動作を実現できます:

```java
@Invoke(
    targetMethodName = "search",
    targetMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
    invokeMethodName = "getResults",
    invokeMethodDesc = "(Ljava/lang/String;)Ljava/util/List;",
    shift = Shift.BEFORE
)
public static CallbackInfo selectImplementation(String query, CallbackInfo ci) {
    if (query.length() > 100) {
        // 大きなクエリには最適化された検索を使用
        ci.returnValue = optimizedSearch(query);
        ci.cancelled = true;
    }
    return ci;
}
```

## 次のステップ

- [定数の変更](./constant-modification.md)について学ぶ
- [高度な使用法](./advanced-usage.md)を探る
- [例](./examples.md)をチェック
