# カスタムトランスフォーマー

アノテーション以外にも、bytekinは高度なユースケースのためにカスタムトランスフォーマーを作成できます。

## カスタムトランスフォーマーの作成

カスタムロジックを実装して変換システムを拡張できます:

```java
public class CustomTransformer implements IBytekinMethodTransformer {
    @Override
    public byte[] transform(byte[] bytecode) {
        // カスタム変換ロジック
        return bytecode;
    }
}
```

## 高度なカスタマイゼーション

より複雑なシナリオの場合、ASMビジターパターンを直接使用します:

```java
public class AdvancedCustomTransformer extends ClassVisitor {
    public AdvancedCustomTransformer(ClassVisitor cv) {
        super(ASM9, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        // カスタムメソッドビジターを返す
        return new MethodVisitor(ASM9, mv) {
            @Override
            public void visitCode() {
                // カスタムメソッド計装
                super.visitCode();
            }
        };
    }
}
```

## カスタムと組み込み変換の組み合わせ

カスタムトランスフォーマーとbytekinの組み込み機能を混在させます:

```java
BytekinTransformer transformer = new BytekinTransformer.Builder(BuiltInHooks.class)
    .build();

// 後でカスタム変換を適用
byte[] original = getClassBytecode("com.example.MyClass");
byte[] withBuiltIn = transformer.transform("com.example.MyClass", original);
byte[] withCustom = applyCustom(withBuiltIn);
```

## パフォーマンスの考慮事項

- カスタムトランスフォーマーを効率的に保つ
- 可能な場合は変換結果をキャッシュ
- ホットスポットのためにカスタムコードをプロファイル

## 次のステップ

- [高度な使用方法](./advanced-usage.md)をレビュー
- [ベストプラクティス](./best-practices.md)をチェック
- [例](./examples.md)を探索
