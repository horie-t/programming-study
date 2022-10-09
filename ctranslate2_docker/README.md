# CTranslate2をDockerコンテナで実行

## 使い方

このリポジトリのディレクトリ上で以下を実行して、Dockerイメージをビルドします。

```bash
sudo docker build -t ctranslate2:latest .
```

OpenNMT-pyの英独のモデルを適当なディレクトリにダウンロードします。

```bash
mkdir /tmp/ctranslate2
cd !$
wget https://s3.amazonaws.com/opennmt-models/transformer-ende-wmt-pyOnmt.tar.gz
tar xf transformer-ende-wmt-pyOnmt.tar.gz
```

コンテナを起動して、コンテナ内でモデルをOpenNMT-pyのものからCTranslate2のものに変換します。

```bash
sudo docker run --rm -it --entrypoint bash -v $PWD:/var/opt/ctranslate2 ctranslate2:latest
# 以降、コンテナ内で実行
ct2-opennmt-py-converter --model_path averaged-10-epoch.pt --output_dir ctranslate2_model
exit
```

コンテナを実行し、翻訳します。

```bash
$ sudo docker run --rm -v $PWD:/var/opt/ctranslate2 ctranslate2:latest 'Hello world!'
Hallo Welt!
```
