# バイトコードの基礎

## Javaバイトコードとは?

Javaソースコード（`.java`ファイル）はJavaバイトコード（`.class`ファイルに含まれる）にコンパイルされます。このバイトコードはJava仮想マシン（JVM）が理解するプラットフォーム非依存の中間言語です。

### ソースコードとバイトコード

**Javaソースコード:**
```java
public class Example {
    public void greet(String name) {
        System.out.println("Hello, " + name);
    }
}
```

**コンパイルされたバイトコード（概念的）:**
```
aload_0
aload_1
invokedynamic <concat>
getstatic System.out
swap
invokevirtual println
return
```

バイトコードバージョンはより冗長で、オペレーティングシステムに依存しません。

## バイトコードが重要な理由

1. **プラットフォーム非依存**: JVMがあればどのシステムでも動作
2. **実行時の柔軟性**: ロード前にバイトコードを変換可能
3. **セキュリティ**: JVMはバイトコードの正しさを検証可能
4. **最適化**: JVMはネイティブコードにJITコンパイル可能
5. **イントロスペクション**: ツールはソースコードなしでバイトコードを分析可能

## バイトコードの読み方

### 検査ツール

- **javap**: 組み込みのJava逆アセンブラ
- **Bytecode Viewer**: バイトコード検査用のGUIツール
- **ASM Tree View**: IDE可視化プラグイン

### 例: javapの使用

```bash
javap -c Example.class
```

出力は各メソッドのバイトコード命令を表示します。

## 一般的なバイトコード命令

頻繁に遭遇するバイトコード命令:

| 命令 | 目的 |
|-----------|---------|
| `aload` | オブジェクト参照をロード |
| `iload` | 整数をロード |
| `invoke*` | メソッドを呼び出す（静的、仮想など） |
| `return` | メソッドから戻る |
| `getstatic` | 静的フィールドを読む |
| `putstatic` | 静的フィールドに書き込む |
| `new` | 新しいオブジェクトを作成 |

## メソッドディスクリプタ（シグネチャ）

バイトコードはメソッドシグネチャに特別な記法を使用します:

```
(パラメータ型)戻り値型
```

### プリミティブ型

- `Z` - boolean
- `B` - byte
- `C` - char
- `S` - short
- `I` - int
- `J` - long
- `F` - float
- `D` - double
- `V` - void

### オブジェクト型

- `Ljava/lang/String;` - Stringクラス
- `L...classname...;` - 任意のクラス

### 配列型

- `[I` - int配列
- `[Ljava/lang/String;` - String配列

### 例

- `(II)I` - 2つの整数を加算: `int method(int a, int b) { return ...; }`
- `(Ljava/lang/String;)V` - 文字列を受け取り、何も返さない: `void method(String s) { ... }`
- `()Ljava/lang/String;` - パラメータなし、文字列を返す: `String method() { ... }`
- `([Ljava/lang/String;)V` - 文字列配列を受け取る: `void method(String[] args) { ... }`

## クラス参照

クラスは完全修飾名を`/`区切りで参照されます:

- `java/lang/String`
- `java/util/ArrayList`
- `com/mycompany/MyClass`

bytekinでは、通常、ドット付きの標準Java記法を使用します:

- `java.lang.String`
- `java.util.ArrayList`
- `com.mycompany.MyClass`

## クラスファイル形式

コンパイルされた`.class`ファイルには以下が含まれます:

1. **マジックナンバー**: クラスファイルとして識別（`0xCAFEBABE`）
2. **バージョン**: Javaバージョン情報
3. **定数プール**: 文字列、メソッド名、フィールド名、型情報
4. **アクセスフラグ**: public、final、abstractなど
5. **このクラス**: クラス名
6. **スーパークラス**: 親クラス
7. **インターフェース**: 実装されたインターフェース
8. **フィールド**: クラスメンバー変数
9. **メソッド**: バイトコード付きメソッド
10. **属性**: 追加のメタデータ

## 重要な注意事項

- バイトコードは**人間が読めるものではない**が、**体系的で分析可能**
- **すべてのJavaソースの構造**がバイトコードにマッピングされる
- **バイトコードは検証可能** - JVMは実行前に正しさをチェック
- **バイトコードは操作可能** - ソースコードなしでプログラム的に操作できる

## 次のステップ

- [bytekinの動作](./how-it-works.md)を学ぶ
- [コア概念](./core-concepts.md)を理解する
