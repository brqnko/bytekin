# ビルダーパターン

bytekinは、トランスフォーマーをプログラム的に構築するための流暢なビルダーAPIを提供します。

## 基本的な使用方法

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .build();
```

## マッピングの使用

ビルダー構築時に名前マッピングを適用します:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(MyHooks.class)
    .mapping(new CustomMappingProvider())
    .build();
```

## プログラマティック変換の追加

アノテーションベースとプログラマティック構成を混在させます:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(AnnotationHooks.class)
    .inject("com.example.Extra", new Injection(...))
    .invoke("com.example.Another", new Invocation(...))
    .build();
```

## 複数のフッククラス

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(
    LoggingHooks.class,
    SecurityHooks.class,
    PerformanceHooks.class
)
.mapping(myMappings)
.build();
```

## ビルダーメソッド

### mapping(IMappingProvider)

クラス/メソッド名変換のためのマッピングプロバイダーを設定します。

### inject(String, Injection)

インジェクション変換をプログラム的に追加します。

### invoke(String, Invocation)

インボケーション変換をプログラム的に追加します。

### redirect(String, RedirectData)

リダイレクト変換をプログラム的に追加します。

### modifyConstant(String, ConstantModification)

定数変更をプログラム的に追加します。

### modifyVariable(String, VariableModification)

変数変更をプログラム的に追加します。

### build()

トランスフォーマーをビルドして返します。このメソッドは:
1. すべてのフッククラスをスキャン
2. アノテーションを抽出
3. プログラマティック変換を追加
4. 内部トランスフォーマーマップを作成
5. 使用可能なトランスフォーマーを返す

## ベストプラクティス

1. **一度だけビルド**: 初期化時にトランスフォーマーを作成
2. **再利用**: 複数の変換に同じトランスフォーマーを使用
3. **パターンを組み合わせる**: アノテーションとプログラマティックAPIを混在
4. **構成を文書化**: 特定の変換が適用される理由をコメント

## パフォーマンスのヒント

```java
// 良い: 一度だけビルド
BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class).build();

for (String className : classes) {
    byte[] transformed = transformer.transform(className, bytecode);
}

// 悪い: 複数回ビルド
for (String className : classes) {
    BytekinTransformer transformer = new BytekinTransformer.Builder(Hooks.class).build();
    byte[] transformed = transformer.transform(className, bytecode);
}
```

## 次のステップ

- [高度な使用方法](./advanced-usage.md)を学習
- [ベストプラクティス](./best-practices.md)をレビュー
