# クラスとインターフェース

bytekinのクラスとインターフェースのリファレンスドキュメント。

## コアクラス

### BytekinTransformer

バイトコード操作のためのメイントランスフォーマークラス。

**メソッド:**
- `byte[] transform(String className, byte[] bytes, int api)` - クラスのバイトコードを変換
- `byte[] transform(String className, byte[] bytes)` - 変換（デフォルトAPI）

**ビルダー:**
- `new BytekinTransformer.Builder(Class<?>... classes)` - ビルダーを作成

### CallbackInfo

変換動作を制御するためのデータ構造。

**フィールド:**
- `boolean cancelled` - 元のコード実行をスキップ
- `Object returnValue` - カスタム戻り値
- `Object[] modifyArgs` - 変更されたメソッド引数

**メソッド:**
- `static CallbackInfo empty()` - 空のコールバックを作成
- `CallbackInfo(boolean cancelled, Object returnValue, Object[] modifyArgs)` - コンストラクタ

## ビルダークラス

### BytekinTransformer.Builder

トランスフォーマーを構築するための流暢なビルダー。

**コンストラクタ:**
- `Builder(Class<?>... classes)` - フッククラスで初期化

**メソッド:**
- `Builder mapping(IMappingProvider)` - マッピングプロバイダーを設定
- `Builder inject(String, Injection)` - インジェクションを追加
- `Builder invoke(String, Invocation)` - インボケーションを追加
- `Builder redirect(String, RedirectData)` - リダイレクトを追加
- `Builder modifyConstant(String, ConstantModification)` - 定数変更を追加
- `Builder modifyVariable(String, VariableModification)` - 変数変更を追加
- `BytekinTransformer build()` - トランスフォーマーをビルド

## データクラス

### Injection

インジェクションポイントを表します。

**目的:** インジェクション設定データを格納。

### Invocation

インボケーションポイントを表します。

**目的:** インボケーション設定データを格納。

### RedirectData

リダイレクトターゲットを表します。

**目的:** リダイレクト設定データを格納。

### ConstantModification

定数の変更を表します。

**目的:** 定数変更データを格納。

### VariableModification

変数の変更を表します。

**目的:** 変数変更データを格納。

## インターフェース

### IMappingProvider

名前マッピングのためのインターフェース。

**メソッド:**
- `String getClassName(String name)` - クラス名をマップ
- `String getMethodName(String className, String methodName, String descriptor)` - メソッド名をマップ
- `String getFieldName(String className, String fieldName)` - フィールド名をマップ

### 実装例

**EmptyMappingProvider** - 何もしないマッピング（変更されない名前を返す）

**カスタムマッピング:**
```java
public class CustomMapping implements IMappingProvider {
    @Override
    public String getClassName(String name) {
        // カスタムマッピングロジック
        return name;
    }

    @Override
    public String getMethodName(String className, String methodName, String descriptor) {
        // カスタムマッピングロジック
        return methodName;
    }

    @Override
    public String getFieldName(String className, String fieldName) {
        // カスタムマッピングロジック
        return fieldName;
    }
}
```

## ユーティリティクラス

### DescriptorParser

メソッドディスクリプタを解析して検証します。

**メソッド:**
- `static String parseDescriptor(String desc)` - ディスクリプタ形式を解析

### BytecodeManipulator

低レベルのバイトコードユーティリティ。

**目的:** バイトコード操作のための内部ユーティリティ。

## 継承階層

```
Object
├── BytekinTransformer
│   └── BytekinTransformer.Builder
├── CallbackInfo
├── Injection
├── Invocation
├── RedirectData
├── ConstantModification
└── VariableModification
```

## インターフェースの実装

```
IMappingProvider
├── EmptyMappingProvider
└── （カスタム実装）
```

## 次のステップ

- [APIリファレンス](./api-reference.md)を確認する
- [アノテーション](./annotations.md)を確認する
- [例](./examples.md)を探索する
