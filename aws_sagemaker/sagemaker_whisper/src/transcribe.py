from __future__ import print_function
import imp

import os, tempfile
from urllib import request

import whisper

import flask

prefix = "/opt/ml/"
model_path = os.path.join(prefix, "model")

# TODO: TranscribeService
class TranslateService(object):
    model = None

    @classmethod
    def get_model(cls):
        if cls.model == None:
            cls.model = whisper.load_model("large", download_root=model_path)

        return cls.model

    @classmethod
    def transcribe(cls, voice_file):
        model = cls.get_model()
        res = model.transcribe(voice_file)

        return res["text"]


app = flask.Flask(__name__)

@app.route("/ping", methods=["GET"])
def ping():
    health = TranslateService.get_model() is not None

    status = 200 if health else 404
    return flask.Response(response="\n", status=status, mimetype="application/json")

@app.route("/invocations", methods=["POST"])
def transcribe():
    res = None
    with tempfile.NamedTemporaryFile() as f:
        f.write(flask.request.get_data())
        res = TranslateService.transcribe(f.name)
        f.close()

    return flask.Response(response=res, status=200, mimetype="text/plain")

