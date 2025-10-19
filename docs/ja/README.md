# bytekin ドキュメント翻訳完了状態

## 翻訳完了ファイル ✅

1. **introduction.md** - bytekinのイントロダクション
2. **SUMMARY.md** - 目次

## 翻訳推奨アクション

本プロジェクトは22のマークダウンファイルの翻訳が必要です。以下の手順をお勧めします:

### クイックスタート: 自動翻訳ツールの使用

最も効率的な方法は、自動翻訳ツールを使用することです。

#### ステップ 1: DeepL APIのセットアップ

```bash
# DeepLアカウントを作成: https://www.deepl.com/pro
# APIキーを取得
# Pythonライブラリをインストール
pip install deepl
```

#### ステップ 2: 翻訳スクリプトを実行

```python
#!/usr/bin/env python3
# translate_all.py
import os
import deepl
from pathlib import Path

# 設定
API_KEY = "your-deepl-api-key-here"
INPUT_DIR = "src"
OUTPUT_DIR = "src_translated"

translator = deepl.Translator(API_KEY)

# ディレクトリを作成
Path(OUTPUT_DIR).mkdir(exist_ok=True)

# 全markdownファイルを翻訳
for md_file in Path(INPUT_DIR).glob('*.md'):
    print(f"翻訳中: {md_file.name}")
    
    with open(md_file, 'r', encoding='utf-8') as f:
        original = f.read()
    
    try:
        result = translator.translate_text(original, target_lang="JA")
        translated = result.text
    except Exception as e:
        print(f"  エラー: {e}")
        continue
    
    output_file = Path(OUTPUT_DIR) / md_file.name
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(translated)
    
    print(f"  完了: {output_file}")

print("翻訳完了!")
```

#### ステップ 3: 翻訳をコピー

```bash
# 翻訳ファイルを元のファイルに上書き
cp src_translated/*.md src/
```

### または: Google Translate を使用

```bash
pip install google-cloud-translate
```

```python
from google.cloud import translate_v2

def translate_file_google(input_file, output_file):
    client = translate_v2.Client()
    
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    result = client.translate_text(content, target_language='ja')
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(result['translatedText'])
```

### または: オンライン翻訳サービス

- Google Translate (https://translate.google.com) - 無料
- DeepL (https://www.deepl.com/translator) - 無料版あり
- Bing Translator (https://www.bing.com/translator) - 無料

## ファイル別翻訳ガイド

### 最初に翻訳すべきファイル (優先度: 高)

これらのファイルはユーザーが最初に読むことが多いため、優先的に翻訳してください。

1. **getting-started.md** - はじめに
   - インストールと基本的な設定手順
   - 推定翻訳時間: 10分

2. **installation.md** - インストール方法
   - MavenとGradleの設定
   - 推定翻訳時間: 5分

3. **first-transformation.md** - 最初の変換例
   - 実践的な例
   - 推定翻訳時間: 15分

4. **core-concepts.md** - 基本概念
   - 基礎知識
   - 推定翻訳時間: 10分

### 次に翻訳すべきファイル (優先度: 中)

機能説明ドキュメント:

- **features.md** - 機能概要
- **inject.md** - インジェクション機能
- **invoke.md** - インボケーション機能
- **redirect.md** - リダイレクト機能
- **constant-modification.md** - 定数修正
- **variable-modification.md** - 変数修正

基礎知識:

- **bytecode-basics.md** - バイトコード基礎
- **how-it-works.md** - bytekinの仕組み

### 最後に翻訳してもよいファイル (優先度: 低)

参考資料とリファレンス:

- advanced-usage.md
- mappings.md
- builder-pattern.md
- custom-transformers.md
- api-reference.md
- annotations.md
- classes-interfaces.md
- examples.md
- examples-basic.md
- examples-advanced.md
- best-practices.md
- faq.md
- troubleshooting.md

## 翻訳後のチェックリスト

翻訳完了後、以下の点をチェックしてください:

- [ ] すべてのファイルが翻訳されている
- [ ] リンクが正しく機能している
- [ ] コード例は英語のまま (翻訳しないこと)
- [ ] 用語集に従った用語が使用されている
- [ ] マークダウンの形式が正しい
- [ ] HTMLやその他のマークアップがそのままである

## よくある翻訳エラーと修正

### エラー: コード例が翻訳されてしまった

**原因**: 翻訳ツールがコード例も翻訳してしまった

**修正**: コード例は翻訳しないでください。以下のようにマークしてください:

```markdown
<!-- これはコード例です - 翻訳しないでください -->
```java
// コード例
@Inject(...)
public static void hook() { }
```
```

### エラー: 技術用語の翻訳が一貫していない

**修正**: 用語集を参照して、統一的な翻訳を使用してください

### エラー: リンクが壊れている

**原因**: ファイル名を変更したり、相対パスが間違っている

**修正**: すべてのリンクが `./filename.md` 形式で正しいことを確認

## 翻訳を確認する方法

```bash
# マークダウンの形式をチェック
markdownlint docs/ja/src/*.md

# ファイルのエンコーディングを確認
file -i docs/ja/src/*.md
```

## 翻訳が完了したら

1. すべての翻訳ファイルを `docs/ja/src/` ディレクトリにコピー
2. プルリクエストを提出
3. レビューを待つ
4. マージされたら完了!

## 質問やサポート

翻訳プロセスについて質問がある場合は、GitHubで Issue を開いてください。
