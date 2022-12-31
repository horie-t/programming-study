# This is the file that implements a flask server to do inferences. It's the file that you will modify to
# implement the scoring for your own algorithm.

from __future__ import print_function

import os
import ctranslate2
import sentencepiece as spm

import flask

prefix = "/opt/ml/"
model_path = os.path.join(prefix, "model")

# A singleton for holding the model. This simply loads the model and holds it.
# It has a predict function that does a prediction based on the model and the input data.

class TranslateService(object):
    translator = None
    sp = None

    @classmethod
    def get_model(cls):
        if cls.translator == None:
            cls.translator = ctranslate2.Translator(f"{model_path}/ctranslate2_model", device="cpu")
        if cls.sp == None:
            cls.sp = spm.SentencePieceProcessor(f"{model_path}/sentencepiece.model")
        
        return (cls.translator, cls.sp)

    @classmethod
    def tranlate(cls, original_text):
        (translator, sp) = cls.get_model()
        input_tokens = sp.encode(original_text, out_type=str)

        results = translator.translate_batch([input_tokens])

        output_tokens = results[0].hypotheses[0]
        translated_text = sp.decode(output_tokens)

        return translated_text

# The flask app for serving predictions
app = flask.Flask(__name__)


@app.route("/ping", methods=["GET"])
def ping():
    """Determine if the container is working and healthy. In this sample container, we declare
    it healthy if we can load the model successfully."""
    (translator, sp) = TranslateService.get_model()
    health = translator is not None and sp is not None

    status = 200 if health else 404
    return flask.Response(response="\n", status=status, mimetype="application/json")


@app.route("/invocations", methods=["POST"])
def translate():
    data = None

    data = flask.request.data.decode("utf-8")
    # 翻訳する
    translated_text = TranslateService.tranlate(data)

    return flask.Response(response=translated_text, status=200, mimetype="text/plain")
