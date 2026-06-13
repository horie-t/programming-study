# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクトの目的

[ゼロから作るディープラーニング 6](https://github.com/oreilly-japan/deep-learning-from-scratch-6) を題材に、トークナイザなどを**独自実装**して学ぶための学習用プロジェクト。書籍の実装をそのまま写すのではなく、自分なりに作り直すことが目的。

現状はスキャフォルドのみ（`main.py` がプレースホルダ）。実装はこれから追加していく段階。

## 環境とコマンド

`uv` 管理の Python プロジェクト（`requires-python >=3.14`）。

```bash
uv run main.py        # 実行
uv add <package>      # 依存追加（pyproject.toml + uv.lock を更新）
uv sync               # ロックファイルから環境を同期
```

`uv add` で依存を追加すること（`pyproject.toml` の `dependencies` を手で編集しない）。テスト・リンタはまだ未導入。

## リポジトリ構成の注意

このディレクトリは monorepo `programming-study/` 配下の 1 サブプロジェクト。git のルートは親リポジトリ側にある。コミットメッセージは日本語で、`ゼロDL6: <内容>` のようにプロジェクト名のプレフィックスを付ける慣習（直近のコミット履歴参照）。
