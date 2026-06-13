import os
import sys

# common パッケージを import できるようにプロジェクトルートをパスに追加
sys.path.append(os.path.join(os.path.dirname(__file__), ".."))
from common.ids import UnicodeIDSProcessor

class IDSCharTokenizer:
    """UnicodeIDSProcessor で漢字を部品（IDS）に分解してからトークン化するトークナイザ。

    漢字をそのまま1トークンにすると語彙が膨大になるが、部首・部品レベルまで
    分解しておくと少ない語彙で表現でき、復元も IDS の構造記号からたどれる。
    """

    def __init__(self, processor):
        self.processor = processor

    def encode(self, text):
        # まず漢字を部品にフラット分解し、各文字を Unicode コードポイントに変換する
        components = self.processor.decompose_to_flat_list(text)
        return [ord(char) for char in components]

    def decode(self, ids):
        # ID -> 部品文字に戻し、IDS の構造記号をもとに元の文章を復元する
        components = [chr(i) for i in ids]
        return self.processor.restore_from_flat_list(components)


# ==========================================
# 🚀 IDS 分解つきトークナイザの実行
# ==========================================
processor = UnicodeIDSProcessor()
ids_file = os.path.join(os.path.dirname(__file__), "..", "ids_data", "ids.txt")
processor.load_from_ids_file(ids_file)

ids_tokenizer = IDSCharTokenizer(processor)
text = "hello世界😁"

# 分解の様子を確認（界 -> ⿱田介 のように部品へ分解される）
print(processor.decompose_to_flat_list(text))
# ['h', 'e', 'l', 'l', 'o', '世', '⿱', '田', '介', '😁']

# エンコード（部品に分解してからトークン化）
ids = ids_tokenizer.encode(text)
print(ids)

# デコード（部品から漢字へ復元）
decoded = ids_tokenizer.decode(ids)
print(decoded)  # hello世界😁
