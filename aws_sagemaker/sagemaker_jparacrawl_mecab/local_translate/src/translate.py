#!/usr/bin/env python3

import ctranslate2
import sentencepiece as spm
import sys

translator = ctranslate2.Translator("ctranslate2_model", device="cpu")
sp = spm.SentencePieceProcessor("sentencepiece_en.model")

input_text = sys.argv[1]
input_tokens = sp.encode(input_text, out_type=str)

results = translator.translate_batch([input_tokens])

output_tokens = results[0].hypotheses[0]
output_text = "".join(output_tokens)

print(output_text)