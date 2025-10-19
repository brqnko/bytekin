# インストールガイド

## Mavenを使用する場合

### 1. 依存関係を追加

`pom.xml`ファイルを編集し、以下の依存関係を追加します:

```xml
<dependency>
    <groupId>io.github.brqnko.bytekin</groupId>
    <artifactId>bytekin</artifactId>
    <version>1.0</version>
</dependency>
```

### 2. プロジェクトを更新

Mavenを実行して依存関係をダウンロードします:

```bash
mvn clean install
```

## Gradleを使用する場合

### 1. 依存関係を追加

`build.gradle`ファイルを編集し、以下を追加します:

```gradle
dependencies {
    implementation 'io.github.brqnko.bytekin:bytekin:1.0'
}
```

### 2. プロジェクトを同期

Android StudioまたはIntelliJ IDEAの場合、Gradleを手動で同期できます。コマンドラインの場合:

```bash
./gradlew build
```

## 依存関係の要件

bytekinは最小限の依存関係を持っています:

| 依存関係 | バージョン | 目的 |
|-----------|---------|---------|
| ASM | 9.7.1+ | バイトコード操作ライブラリ |
| Java | 8+ | ランタイム環境 |

## 検証

インストール後、簡単なテストを実行してbytekinが正しくセットアップされていることを確認します:

```java
import io.github.brqnko.bytekin.transformer.BytekinTransformer;

public class BytekinVer {
    public static void main(String[] args) {
        BytekinTransformer transformer = new BytekinTransformer.Builder()
            .build();
        System.out.println("bytekinは使用する準備ができています!");
    }
}
```

## インストールのトラブルシューティング

### Maven: 依存関係が見つからない

- インターネットに接続されていることを確認
- `mvn clean`を実行してから`mvn install`を再度実行
- リポジトリにアクセスできるか確認

### Gradle: ビルドが失敗する

- 最初に`./gradlew clean`を実行
- Gradleラッパーのバージョンを確認
- Javaバージョンの互換性を確認

## 次のステップ

- [最初の変換](./first-transformation.md)
- [コアコンセプト](./core-concepts.md)
