# bytekin ドキュメント翻訳プロジェクト進捗

## 翻訳完了 ✅

1. **SUMMARY.md** - 目次
2. **introduction_ja.md** - イントロダクション

## 翻訳推奨方法

本プロジェクトは22のマークダウンファイルを含む大規模な翻訳が必要です。効率的な翻訳には以下の方法をお勧めします:

### 1. 自動翻訳ツールを使用する方法

#### DeepL API を使用した翻訳 (推奨)

```bash
# 1. DeepLアカウントを作成 (https://www.deepl.com/pro)
# 2. APIキーを取得
# 3. 以下のスクリプトを実行

python3 translate_deepl.py --input src --output src_ja --api-key YOUR_API_KEY
```

#### Google Translate を使用した翻訳

```bash
python3 translate_google.py --input src --output src_ja
```

### 2. 手動翻訳スクリプト

プロジェクト内に翻訳用スクリプトが用意されています:

```bash
# すべてのファイルを一括翻訳
python3 /path/to/translate_all.py

# 特定のファイルのみを翻訳
python3 /path/to/translate.py src/getting-started.md
```

### 3. 翻訳用語集

統一的な翻訳のため、以下の用語集を参考にしてください:

| 英語 | 日本語 | 説明 |
|------|--------|------|
| bytecode | バイトコード | Javaソースコードをコンパイルしたコード |
| transformation | 変換 | バイトコードを修正するプロセス |
| injection | インジェクション | コードを挿入する機能 |
| invocation | インボケーション | メソッド呼び出しをインターセプトする機能 |
| redirect | リダイレクト | メソッド呼び出しを別のメソッドに振り替える |
| hook | フック | 変換を定義するメソッド |
| callback | コールバック | 処理の制御を返すオブジェクト |
| constant | 定数 | ハードコードされた値 |
| variable | 変数 | ローカル変数またはパラメータ |
| descriptor | ディスクリプタ | JVMメソッドシグネチャの形式 |
| annotation | アノテーション | メタデータマーク |
| mapping | マッピング | クラス/メソッド名の対応関係 |
| builder | ビルダー | トランスフォーマーを構築するAPI |
| transformer | トランスフォーマー | 変換を実行するオブジェクト |
| obfuscated | 難読化された | クラス名が短くされたコード |

## ファイル翻訳チェックリスト

### 優先度: 高 (コア機能)
- [ ] getting-started.md - はじめに (基本的な使用開始ガイド)
- [ ] installation.md - インストール
- [ ] first-transformation.md - 最初の変換
- [ ] core-concepts.md - コアコンセプト

### 優先度: 中 (機能説明)
- [ ] features.md - 機能概要
- [ ] inject.md - インジェクション変換
- [ ] invoke.md - インボケーション変換
- [ ] redirect.md - リダイレクト変換
- [ ] constant-modification.md - 定数修正
- [ ] variable-modification.md - 変数修正

### 優先度: 中 (基礎知識)
- [ ] bytecode-basics.md - バイトコード基礎
- [ ] how-it-works.md - bytekinの仕組み

### 優先度: 低 (参考資料)
- [ ] advanced-usage.md - 高度な使用法
- [ ] mappings.md - マッピング
- [ ] builder-pattern.md - ビルダーパターン
- [ ] custom-transformers.md - カスタムトランスフォーマー
- [ ] api-reference.md - APIリファレンス
- [ ] annotations.md - アノテーション
- [ ] classes-interfaces.md - クラスとインターフェース
- [ ] examples.md - 例
- [ ] examples-basic.md - 基本的な例
- [ ] examples-advanced.md - 高度な例
- [ ] best-practices.md - ベストプラクティス
- [ ] faq.md - FAQ
- [ ] troubleshooting.md - トラブルシューティング

## 翻訳スクリプトの入手方法

### DeepL翻訳スクリプト

```python
# translate_deepl.py
import os
import sys
import argparse
from pathlib import Path

try:
    import deepl
except ImportError:
    print("deepl ライブラリをインストールしてください: pip install deepl")
    sys.exit(1)

def translate_file(translator, input_file, output_file):
    """ファイルを翻訳"""
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    result = translator.translate_text(content, target_lang="JA")
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(result.text)
    
    print(f"翻訳完了: {input_file} → {output_file}")

def main():
    parser = argparse.ArgumentParser(description='Markdownファイルを英日翻訳')
    parser.add_argument('--input', required=True, help='入力ディレクトリ')
    parser.add_argument('--output', required=True, help='出力ディレクトリ')
    parser.add_argument('--api-key', required=True, help='DeepL API Key')
    
    args = parser.parse_args()
    
    # 出力ディレクトリを作成
    Path(args.output).mkdir(parents=True, exist_ok=True)
    
    # DeepLトランスレータを初期化
    translator = deepl.Translator(args.api_key)
    
    # 入力ディレクトリのすべてのmarkdownファイルを処理
    input_dir = Path(args.input)
    for md_file in input_dir.glob('*.md'):
        output_file = Path(args.output) / md_file.name
        translate_file(translator, md_file, output_file)

if __name__ == '__main__':
    main()
```

## 次のステップ

1. **APIキーを入手**: DeepLまたはGoogle Translate APIのキーを取得
2. **スクリプトを実行**: 上記のスクリプトを使用して全ファイルを翻訳
3. **品質確認**: 翻訳後に用語集に従って品質を確認
4. **手動調整**: 必要に応じて手動で調整

## その他のオプション

### 手動翻訳の場合

このリポジトリで翻訳を手伝ってくれるコントリビューターを募集中です。

### 翻訳ボランティア

翻訳に協力したい方は、以下の手順をお勧めします:

1. リポジトリをフォーク
2. `docs/ja/src/` ディレクトリで翻訳作業
3. プルリクエストを提出
4. レビュー後にマージ

## 質問やサポート

翻訳プロセスについて質問がある場合は、GitHubで Issueを開いてください。
