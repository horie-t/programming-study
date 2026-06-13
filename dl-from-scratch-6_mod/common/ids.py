import re

class UnicodeIDSProcessor:
    def __init__(self):
        # 高速に検索するための双方向の辞書
        self.kanji_to_ids = {}  # 漢字 -> [部品のリスト]
        self.ids_to_kanji = {}  # "構造文字列" -> 漢字
        self.flat_to_kanji = {} # "構造記号なし文字列" -> 漢字 (構造を捨てた場合の復元用)

    def load_mock_data(self):
        """動作確認用のサンプルデータを登録する関数"""
        # 実際のids.txtのフォーマットに準拠したデータ構造
        sample_data = {
            "語": "Base\t語\t⿰言⿱五口",
            "雲": "Base\t雲\t⿱雨⿱二厶",
            "細": "Base\t細\t⿰糸⿱田十",
        }
        for line in sample_data.values():
            self._parse_and_add_line(line)

    def load_from_ids_file(self, file_path):
        """本家 cjkvi/cjkvi-ids の ids.txt を一括で読み込む関数"""
        with open(file_path, "r", encoding="utf-8") as f:
            for line in f:
                # コメント行や空行を除外
                if line.startswith("U+") and not line.startswith("#"):
                    self._parse_and_add_line(line)

    def _parse_and_add_line(self, line):
        """1行のテキストを解析してマッピングに登録する内部関数"""
        parts = line.strip().split("\t")
        if len(parts) >= 3:
            kanji = parts[1]
            ids_str = parts[2]
            
            # 特殊なバリアント表記（[J]など）を除外して1文字の漢字のみ対象にする
            if len(kanji) == 1:
                components = list(ids_str)
                self.kanji_to_ids[kanji] = components
                
                # 1. 構造記号を含んだ状態での復元用（完全一意）
                self.ids_to_kanji[ids_str] = kanji
                
                # 2. 構造記号（⿰, ⿱など）を除去した状態での復元用（フラット用）
                clean_str = re.sub(r'[\u2FF0-\u2FFB]', '', ids_str)
                self.flat_to_kanji[clean_str] = kanji

    def decompose_text(self, text, keep_structure=True):
        """
        文字列を分解する。漢字以外（ひらがな、カタカナなど）はそのまま残す。
        keep_structure=True : 構造記号（⿰, ⿱など）を残して分解（LLM推奨）
        keep_structure=False: 純粋なパーツだけのリストにする
        """
        decomposed_result = []
        for char in text:
            if char in self.kanji_to_ids:
                components = self.kanji_to_ids[char]
                if not keep_structure:
                    # 構造記号（Unicodeの U+2FF0 〜 U+2FFB）を取り除く
                    components = [c for c in components if not re.match(r'[\u2FF0-\u2FFB]', c)]
                decomposed_result.append(components)
            else:
                # 漢字以外は1文字のリストとして保持
                decomposed_result.append([char])
        return decomposed_result

    def restore_text(self, decomposed_result):
        """
        分解されたネストリストから、元の文字列（文章）を完全に復元する
        """
        restored_chars = []
        for item in decomposed_result:
            # パーツを結合して文字列に戻す
            joined_str = "".join(item)
            
            # 1. 構造記号つきの辞書から検索
            if joined_str in self.ids_to_kanji:
                restored_chars.append(self.ids_to_kanji[joined_str])
            # 2. ない場合は構造記号なし（フラット）の辞書から検索
            elif joined_str in self.flat_to_kanji:
                restored_chars.append(self.flat_to_kanji[joined_str])
            else:
                # 辞書にない場合（ひらがなや、復元できなかったパーツ）はそのまま結合
                restored_chars.append(joined_str)
                
        return "".join(restored_chars)


# ==========================================
# 🚀 実行テスト
# ==========================================
if __name__ == "__main__":
    processor = UnicodeIDSProcessor()
    processor.load_mock_data()  # サンプルデータの読み込み
    
    original_text = "雲の下で詳細を語る"  # 「詳細」の「詳」はデータがないのでスキップされるテスト
    print(f"元テキスト: {original_text}\n")

    # --------------------------------------------------
    # パターン1: 構造記号を「残す」場合（LLMのコンテキスト理解に最適）
    # --------------------------------------------------
    print("--- パターン1: 構造（レイアウト）を維持して分解 ---")
    decomposed_with_struct = processor.decompose_text(original_text, keep_structure=True)
    print(f"分解結果: {decomposed_with_struct}")
    
    # 復元
    restored_with_struct = processor.restore_text(decomposed_with_struct)
    print(f"復元結果: {restored_with_struct}\n")

    # --------------------------------------------------
    # パターン2: 構造記号を「捨てる」場合（純粋な部品リスト）
    # --------------------------------------------------
    print("--- パターン2: 構造を捨てて純粋な部品のみに分解 ---")
    decomposed_flat = processor.decompose_text(original_text, keep_structure=False)
    print(f"分解結果: {decomposed_flat}")
    
    # 復元
    restored_flat = processor.restore_text(decomposed_flat)
    print(f"復元結果: {restored_flat}")