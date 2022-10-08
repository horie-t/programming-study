## コーパスのダウンロード

[JParaCrawl](https://www.kecl.ntt.co.jp/icl/lirg/jparacrawl/)のサイトからコーパスデータをダウンロードします。


```python
!mkdir data
```


```python
%cd data
```


```python
!wget https://www.kecl.ntt.co.jp/icl/lirg/jparacrawl/release/3.0/bitext/en-ja.tar.gz
```

## コーパスの前処理

OpenNMT-pyで扱いやすいようにデータを前処理します。
まずは、データを展開します。


```python
!tar xvf en-ja.tar.gz
```


```python
%cd en-ja/
```

データの中身を確認してみましょう。タブ区切りで5つのフィールドがあり、4、5番目にそれぞれ英文、和文がありました。

文数は25,740,835でした。


```python
!head en-ja.bicleaner05.txt
```

    0001vip.cocolog-nifty.com	0001vip.cocolog-nifty.com	0.535	And everyone will not care that it is not you.	鼻・口のところはあらかじめ少し切っておくといいですね。
    0001vip.cocolog-nifty.com	0001vip.cocolog-nifty.com	0.557	And everyone will not care that it is not you.	アドレス置いとくので、消されないうちにメールくれたら嬉しいです。
    000-lhr.web.wox.cc	000-lhr.web.wox.cc	0.743	Sponsored link This advertisement is displayed when there is no update for a certain period of time.	スポンサードリンク この広告は一定期間更新がない場合に表示されます。
    000-lhr.web.wox.cc	000-lhr.web.wox.cc	0.750	Also, it will always be hidden when becoming a premium user .	また、 プレミアムユーザー になると常に非表示になります。
    000-lhr.web.wox.cc	000-lhr.web.wox.cc	0.751	It will return to non-display when content update is done.	コンテンツの更新が行われると非表示に戻ります。
    000-lhr.web.wox.cc	kapuri21.web.wox.cc	0.743	Sponsored link This advertisement is displayed when there is no update for a certain period of time.	スポンサードリンク この広告は一定期間更新がない場合に表示されます。
    000-lhr.web.wox.cc	kapuri21.web.wox.cc	0.750	Also, it will always be hidden when becoming a premium user .	また、 プレミアムユーザー になると常に非表示になります。
    000-lhr.web.wox.cc	kapuri21.web.wox.cc	0.751	It will return to non-display when content update is done.	コンテンツの更新が行われると非表示に戻ります。
    0017.org	0017.org	0.500	It’s like you can enrich it and save money as a result. I’ve watched a lot of videos, mainly on Youtube, of people who say they’re minimalists, and I agree.	Youtubeを中心にミニマリストと言っている方の動画をたくさんみましたが、納得いくもののも多く、不況と少子化を呼ばれる”今の”日本には合っている考え方なのかな、と感じています。
    0017.org	0017.org	0.502	Go to the original video hierarchy of the conversion source, copy and paste the following is fine. ffmpeg -i sample.mp4 -strict -2 video.webm summary I’ve been using the upload and embed method to Youtube to set up videos on the web.	ffmpeg -i sample.mp4 -strict -2 video.webm まとめ Web上で動画を設置するときは、Youtubeにアップして埋め込む方法ばかり使っていましたが、これで複数の動画形式を作ることができるので、自分のサーバに設定することも可能になりますね。



```python
!wc -l en-ja.bicleaner05.txt
```

    25740835 en-ja.bicleaner05.txt


英文と和文だけを取り出します。


```python
!cat en-ja.bicleaner05.txt | cut -f4 > en.txt
!cat en-ja.bicleaner05.txt | cut -f5 > ja.txt
```

## 文をトークン化

学習用に文章をトークン化します。

トークン化には[MeCab](https://taku910.github.io/mecab/)等が用いられることも多いのですが、ここでは、[SentencePiece](https://github.com/google/sentencepiece)を使ってトークン化してみます。

まず、Pythonのパッケージをインストールします。


```python
!pip install sentencepiece
```

全部の文章から単語分割モデルを学習しようとすると、筆者のPCのメモリ(8GB)では足りなかったので、一部の文(100,000文)から学習しました。

PCの環境によっては文数を増やしてもよいかもしれません。

まず最初に、英文、和文の中身の一部をシャッフルして取り出します。


```python
!shuf -n 100000 en.txt -o vocab_train.en
```


```python
!shuf -n 100000 ja.txt -o vocab_train.ja
```

取り出した文で学習します。

character_coverageは、英語の場合は公式サイトで例示されている値、日本語の場合は一般的によいと言われている値です。


```python
import sentencepiece as spm

spm.SentencePieceTrainer.Train(
   '--input=vocab_train.en --model_prefix=sentencepiece_en --vocab_size=32000 --character_coverage=0.98'
)
```


```python
spm.SentencePieceTrainer.Train(
   '--input=vocab_train.ja --model_prefix=sentencepiece_ja --vocab_size=32000 --character_coverage=0.9995'
)
```

学習したモデルを使って、試しにトークン化してみます。

英語の場合は空白で区切っただけのように見えますが、日本語の場合は文法通りではなく独自に学習しているようです。


```python
sp_en = spm.SentencePieceProcessor("sentencepiece_en.model")
```


```python
print(sp_en.encode("It will return to non-display when content update is done.", out_type=str))
```

    ['▁It', '▁will', '▁return', '▁to', '▁non', '-', 'display', '▁when', '▁content', '▁update', '▁is', '▁done', '.']



```python
sp_ja = spm.SentencePieceProcessor("sentencepiece_ja.model")
```


```python
print(sp_ja.encode("スポンサードリンク この広告は一定期間更新がない場合に表示されます。", out_type=str))
```

    ['▁', 'スポンサー', 'ド', 'リンク', '▁この', '広告', 'は', '一定', '期間', '更新', 'がない場合', 'に表示されます', '。']



```python
print(' '.join(sp_ja.encode("スポンサードリンク この広告は一定期間更新がない場合に表示されます。", out_type=str)))
```

    ▁ スポンサー ド リンク ▁この 広告 は 一定 期間 更新 がない場合 に表示されます 。


コーパスのデータとトークン化したファイルを作成します。

このファイルを使って、翻訳モデルを作成します。


```python
with open("./en.txt") as in_f:
    with open("en_tokenized.txt", mode='w') as out_f:
        for line in in_f:
            out_f.write(' '.join(sp_en.encode(line, out_type=str)) + "\n")
```


```python
with open("./ja.txt") as in_f:
    with open("ja_tokenized.txt", mode='w') as out_f:
        for line in in_f:
            out_f.write(' '.join(sp_ja.encode(line, out_type=str)) + "\n")
```

トークン化したら、そのファイルの一部(25730000文)を訓練用、5000文を検証用、5000文をテスト用に分けます。

コーパスファイルの頭から何万文と言うように分けると、取得元のドメインが偏るのでシャッフルしてデータを分けるようにします。

まず最初にコーパスの文数までの数値がランダムにならんだファイルを作成します。


```python
!seq `wc -l en-ja.bicleaner05.txt | cut -f1 -d' '` | shuf -o line_nums.txt -
```


```python
訓練用、検証用、テスト用の文に使うデータの行番号が入ったファイルを作成します。
```


```python
!head --lines=25730000 line_nums.txt | sort --numeric-sort > train_line_nums.txt
!head --lines=25735000 line_nums.txt | tail --lines=5000 | sort --numeric-sort > val_line_nums.txt
!head --lines=25740000 line_nums.txt | tail --lines=5000 | sort --numeric-sort > test_line_nums.txt
```

文のファイル名と、行番号のファイル名から、行番号の文を取り出す関数を定義して、訓練用、検証用、テスト用のファイルを作成します。


```python
# input_file: 取り出し元ファイル
# num_fileの行番号の行だけ取り出します。
# output_file: 取り出した結果ファイル
def extract_lines(input_file, num_file, output_file):
    text_line_num = 1
    with open(num_file) as line_f:
        line_num = int(line_f.readline())
        with open(input_file) as in_f:
            with open(output_file, mode='w') as out_f:
                for line in in_f:
                    if text_line_num == line_num:
                        out_f.write(line)
                        line_num = line_f.readline()
                        if line_num == '':
                            break
                        else:
                            line_num = int(line_num)
                    text_line_num += 1
```


```python
extract_lines("en_tokenized.txt", "train_line_nums.txt", "train.en")
extract_lines("ja_tokenized.txt", "train_line_nums.txt", "train.ja")
extract_lines("en_tokenized.txt", "val_line_nums.txt", "val.en")
extract_lines("ja_tokenized.txt", "val_line_nums.txt", "val.ja")
extract_lines("en_tokenized.txt", "test_line_nums.txt", "test.en")
extract_lines("ja_tokenized.txt", "test_line_nums.txt", "test.ja")
```

試しに日本語の訓練データを見てみましょう。


```python
!head train.ja
```

    ▁ 鼻 ・ 口 の ところ は あらかじめ 少し 切 って お く と いい ですね 。
    ▁ アドレス 置 い と く ので 、 消 されない うち に メール く れた ら 嬉しい です 。
    ▁ スポンサー ド リンク ▁この 広告 は 一定 期間 更新 がない場合 に表示されます 。
    ▁また 、 ▁ プレミアム ユーザー ▁ になると 常に 非表示 になります 。
    ▁ コンテンツ の 更新 が 行われる と 非表示 に戻ります 。
    ▁ スポンサー ド リンク ▁この 広告 は 一定 期間 更新 がない場合 に表示されます 。
    ▁また 、 ▁ プレミアム ユーザー ▁ になると 常に 非表示 になります 。
    ▁ コンテンツ の 更新 が 行われる と 非表示 に戻ります 。
    ▁You tu be を中心に ミニ マ リスト と言って いる 方 の 動画 を たくさん み ました が 、 納 得 いく ものの も 多く 、 不 況 と 少 子 化 を 呼ばれ る ” 今 の ” 日本 には 合 っている 考え方 なのか な 、 と 感じ ています 。
    ▁f f mp e g ▁- i ▁ sa mp le . mp 4 ▁- str ic t ▁ -2 ▁ v ide o . web m ▁ まとめ ▁Web 上で 動画 を 設置 するとき は 、 Y out ub e に アップ して 埋め 込む 方法 ばかり 使 っていました が 、 これ で 複数の 動画 形式 を作る ことができる ので 、 自分の サーバ に 設定 することも 可能 になります ね 。





```python
%cd ../..
```

## OpenNMT-pyで学習

データの準備ができたので、OpenNMT-pyで学習させます。

まずはOpenNMT-pyをインストールします。


```python
!pip install OpenNMT-py
```

以下のようなYamlファイルを作成します。

ボキャブラリー(語彙)ファイルを作成します。


```python
!onmt_build_vocab -config src/train_en_ja.yml -n_sample 10000
```

以下のようなシェルスクリプトを作成します。

以下のようなDockerファイルを作成します。

以下のようなスクリプトを使ってDockerイメージを作成し、Amazon Elastic Container Registry(ECR)にイメージをpushします。


```python
!./build_and_push.sh jparacrawl-train
```

前に作成したIAMロールを取得します。


```python
import boto3

role_name = "SageMaker-local"

iam = boto3.client("iam")
role = iam.get_role(RoleName=role_name)["Role"]["Arn"]
```

SageMakerのセッションを開始します。


```python
import sagemaker as sage

sess = sage.Session()
```


```python
prefix = 'jparacrawl/training'
WORK_DIRECTORY = 'data_train'
data_location = sess.upload_data(WORK_DIRECTORY, key_prefix=prefix)
print("data uploaded.")
```

    data uploaded.



```python
account = sess.boto_session.client('sts').get_caller_identity()['Account']
region = sess.boto_session.region_name
image = '{}.dkr.ecr.{}.amazonaws.com/jparacrawl-train:latest'.format(account, region)

estimator = sage.estimator.Estimator(image,
                       role, 1, 'ml.g4dn.12xlarge',
                       output_path="s3://{}/jparacrawl/output".format(sess.default_bucket()),
                       sagemaker_session=sess)

print("estimation starting.")
estimator.fit({"training": data_location})
```

    estimation starting.
    2022-09-27 12:26:48 Starting - Starting the training job...
    2022-09-27 12:27:11 Starting - Preparing the instances for trainingProfilerReport-1664281607: InProgress
    ......
    2022-09-27 12:28:21 Downloading - Downloading input data.................[34mTue Sep 27 12:31:18 UTC 2022[0m
    [34m/opt/ml/input/:[0m
    [34mtotal 8[0m
    [34mdrw-r--r-- 2 root root 4096 Sep 27 12:28 config[0m
    [34mdrwxr-xr-x 3 root root 4096 Sep 27 12:28 data[0m
    [34m/opt/ml/input/config:[0m
    [34mtotal 28[0m
    [34m-rw-r--r-- 1 root root 217 Sep 27 12:28 debughookconfig.json[0m
    [34m-rw-r--r-- 1 root root   2 Sep 27 12:28 hyperparameters.json[0m
    [34m-rw-r--r-- 1 root root 502 Sep 27 12:28 init-config.json[0m
    [34m-rw-r--r-- 1 root root 107 Sep 27 12:28 inputdataconfig.json[0m
    [34m-rw-r--r-- 1 root root   2 Sep 27 12:28 metric-definition-regex.json[0m
    [34m-rw-r--r-- 1 root root 220 Sep 27 12:28 profilerconfig.json[0m
    [34m-rw-r--r-- 1 root root 280 Sep 27 12:28 resourceconfig.json[0m
    [34m/opt/ml/input/data:[0m
    [34mtotal 8[0m
    [34mdrwxr-xr-x 3 root root 4096 Sep 27 12:28 training[0m
    [34m-rw-r--r-- 1 root root  478 Sep 27 12:28 training-manifest[0m
    [34m/opt/ml/input/data/training:[0m
    [34mtotal 4[0m
    [34mdrwxr-xr-x 2 root root 4096 Sep 27 12:28 en-ja[0m
    [34m/opt/ml/input/data/training/en-ja:[0m
    [34mtotal 11129040[0m
    [34m-rw-r--r-- 1 root root      91684 Sep 27 12:28 jparacrawl.vocab.src[0m
    [34m-rw-r--r-- 1 root root      81821 Sep 27 12:28 jparacrawl.vocab.tgt[0m
    [34m-rw-r--r-- 1 root root 5786479045 Sep 27 12:31 train.en[0m
    [34m-rw-r--r-- 1 root root 5607241295 Sep 27 12:31 train.ja[0m
    [34m-rw-r--r-- 1 root root    1130859 Sep 27 12:28 val.en[0m
    [34m-rw-r--r-- 1 root root    1091648 Sep 27 12:28 val.ja[0m
    [34m[2022-09-27 12:31:21,899 INFO] Missing transforms field for corpus_1 data, set to default: [].[0m
    [34m[2022-09-27 12:31:21,900 WARNING] Corpus corpus_1's weight should be given. We default it to 1 for you.[0m
    [34m[2022-09-27 12:31:21,900 INFO] Missing transforms field for valid data, set to default: [].[0m
    [34m[2022-09-27 12:31:21,900 INFO] Parsed 2 corpora from -data.[0m
    [34m[2022-09-27 12:31:21,900 INFO] Get special vocabs from Transforms: {'src': set(), 'tgt': set()}.[0m
    [34m[2022-09-27 12:31:21,900 INFO] Loading vocab from text file...[0m
    [34m[2022-09-27 12:31:21,900 INFO] Loading src vocabulary from /opt/ml/input/data/training/en-ja/jparacrawl.vocab.src[0m
    [34m[2022-09-27 12:31:21,915 INFO] Loaded src vocab has 7674 tokens.[0m
    [34m[2022-09-27 12:31:21,919 INFO] Loading tgt vocabulary from /opt/ml/input/data/training/en-ja/jparacrawl.vocab.tgt[0m
    [34m[2022-09-27 12:31:21,932 INFO] Loaded tgt vocab has 7838 tokens.[0m
    [34m[2022-09-27 12:31:21,936 INFO] Building fields with vocab in counters...[0m
    [34m[2022-09-27 12:31:21,945 INFO]  * tgt vocab size: 7842.[0m
    [34m[2022-09-27 12:31:21,954 INFO]  * src vocab size: 7676.[0m
    [34m[2022-09-27 12:31:21,954 INFO]  * src vocab size = 7676[0m
    [34m[2022-09-27 12:31:21,954 INFO]  * tgt vocab size = 7842[0m
    [34m[2022-09-27 12:31:22,699 INFO]  Starting process pid: 62  [0m
    [34m[2022-09-27 12:31:23,435 INFO]  Starting process pid: 112  [0m
    [34m[2022-09-27 12:31:24,170 INFO]  Starting process pid: 162  [0m
    [34m[2022-09-27 12:31:24,910 INFO]  Starting process pid: 214  [0m
    [34m[2022-09-27 12:31:24,951 INFO] Building model...[0m
    [34m[2022-09-27 12:31:25,740 INFO]  Starting producer process pid: 266  [0m
    [34m[2022-09-27 12:31:25,744 INFO] corpus_1's transforms: TransformPipe()[0m
    [34m[2022-09-27 12:31:25,744 INFO] Weighted corpora loaded so far:[0m
    [34m#011#011#011* corpus_1: 1[0m
    [34m[2022-09-27 12:31:26,523 INFO]  Starting producer process pid: 412  [0m
    [34m[2022-09-27 12:31:27,319 INFO]  Starting producer process pid: 490  [0m
    [34m[2022-09-27 12:31:28,139 INFO]  Starting producer process pid: 564  [0m
    
    2022-09-27 12:31:32 Training - Training image download completed. Training in progress.[34m[2022-09-27 12:31:29,839 INFO] NMTModel(
      (encoder): TransformerEncoder(
        (embeddings): Embeddings(
          (make_embedding): Sequential(
            (emb_luts): Elementwise(
              (0): Embedding(7676, 512, padding_idx=1)
            )
            (pe): PositionalEncoding(
              (dropout): Dropout(p=0.1, inplace=False)
            )
          )
        )
        (transformer): ModuleList(
          (0): TransformerEncoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (dropout): Dropout(p=0.1, inplace=False)
          )
          (1): TransformerEncoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (dropout): Dropout(p=0.1, inplace=False)
          )
          (2): TransformerEncoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (dropout): Dropout(p=0.1, inplace=False)
          )
          (3): TransformerEncoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (dropout): Dropout(p=0.1, inplace=False)
          )
          (4): TransformerEncoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (dropout): Dropout(p=0.1, inplace=False)
          )
          (5): TransformerEncoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (dropout): Dropout(p=0.1, inplace=False)
          )
        )
        (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
      )
      (decoder): TransformerDecoder(
        (embeddings): Embeddings(
          (make_embedding): Sequential(
            (emb_luts): Elementwise(
              (0): Embedding(7842, 512, padding_idx=1)
            )
            (pe): PositionalEncoding(
              (dropout): Dropout(p=0.1, inplace=False)
            )
          )
        )
        (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
        (transformer_layers): ModuleList(
          (0): TransformerDecoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm_1): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (drop): Dropout(p=0.1, inplace=False)
            (context_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (layer_norm_2): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
          )
          (1): TransformerDecoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm_1): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (drop): Dropout(p=0.1, inplace=False)
            (context_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (layer_norm_2): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
          )
          (2): TransformerDecoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm_1): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (drop): Dropout(p=0.1, inplace=False)
            (context_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (layer_norm_2): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
          )
          (3): TransformerDecoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm_1): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (drop): Dropout(p=0.1, inplace=False)
            (context_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (layer_norm_2): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
          )
          (4): TransformerDecoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm_1): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (drop): Dropout(p=0.1, inplace=False)
            (context_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (layer_norm_2): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
          )
          (5): TransformerDecoderLayer(
            (self_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (feed_forward): PositionwiseFeedForward(
              (w_1): Linear(in_features=512, out_features=2048, bias=True)
              (w_2): Linear(in_features=2048, out_features=512, bias=True)
              (layer_norm): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
              (dropout_1): Dropout(p=0.1, inplace=False)
              (dropout_2): Dropout(p=0.1, inplace=False)
            )
            (layer_norm_1): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
            (drop): Dropout(p=0.1, inplace=False)
            (context_attn): MultiHeadedAttention(
              (linear_keys): Linear(in_features=512, out_features=512, bias=True)
              (linear_values): Linear(in_features=512, out_features=512, bias=True)
              (linear_query): Linear(in_features=512, out_features=512, bias=True)
              (softmax): Softmax(dim=-1)
              (dropout): Dropout(p=0.1, inplace=False)
              (final_linear): Linear(in_features=512, out_features=512, bias=True)
            )
            (layer_norm_2): LayerNorm((512,), eps=1e-06, elementwise_affine=True)
          )
        )
      )
      (generator): Sequential(
        (0): Linear(in_features=512, out_features=7842, bias=True)
        (1): Cast()
        (2): LogSoftmax(dim=-1)
      )[0m
    [34m)[0m
    [34m[2022-09-27 12:31:29,841 INFO] encoder: 22845440[0m
    [34m[2022-09-27 12:31:29,841 INFO] decoder: 33263266[0m
    [34m[2022-09-27 12:31:29,841 INFO] * number of parameters: 56108706[0m
    [34m[2022-09-27 12:31:29,844 INFO] Starting training on GPU: [0, 1, 2, 3][0m
    [34m[2022-09-27 12:31:29,844 INFO] Start training loop and validate every 10000 steps...[0m
    [34m[2022-09-27 12:32:51,247 INFO] Step 100/40000; acc:   4.41; ppl: 1654.45; xent: 7.41; lr: 0.00001; 21889/22994 tok/s;     81 sec;[0m
    [34m[2022-09-27 12:34:09,895 INFO] Step 200/40000; acc:   7.15; ppl: 829.36; xent: 6.72; lr: 0.00002; 19791/18806 tok/s;    160 sec;[0m
    [34m[2022-09-27 12:35:31,677 INFO] Step 300/40000; acc:  15.58; ppl: 315.70; xent: 5.75; lr: 0.00004; 28579/25610 tok/s;    242 sec;[0m
    [34m[2022-09-27 12:36:52,611 INFO] Step 400/40000; acc:  21.60; ppl: 178.83; xent: 5.19; lr: 0.00005; 25194/24128 tok/s;    323 sec;[0m
    [34m[2022-09-27 12:38:15,294 INFO] Step 500/40000; acc:  17.59; ppl: 250.71; xent: 5.52; lr: 0.00006; 22008/21581 tok/s;    405 sec;[0m
    [34m[2022-09-27 12:39:39,193 INFO] Step 600/40000; acc:  17.11; ppl: 230.95; xent: 5.44; lr: 0.00007; 18142/18881 tok/s;    489 sec;[0m
    [34m[2022-09-27 12:41:04,063 INFO] Step 700/40000; acc:  17.02; ppl: 196.88; xent: 5.28; lr: 0.00009; 20403/20298 tok/s;    574 sec;[0m
    [34m[2022-09-27 12:42:32,359 INFO] Step 800/40000; acc:  24.06; ppl: 95.54; xent: 4.56; lr: 0.00010; 19664/20169 tok/s;    663 sec;[0m
    [34m[2022-09-27 12:43:55,668 INFO] Step 900/40000; acc:  19.96; ppl: 146.06; xent: 4.98; lr: 0.00011; 19448/19386 tok/s;    746 sec;[0m
    [34m[2022-09-27 12:45:22,799 INFO] Step 1000/40000; acc:  22.51; ppl: 105.58; xent: 4.66; lr: 0.00012; 24404/21630 tok/s;    833 sec;[0m
    [34m[2022-09-27 12:46:48,813 INFO] Step 1100/40000; acc:  26.80; ppl: 67.62; xent: 4.21; lr: 0.00014; 26062/24756 tok/s;    919 sec;[0m
    [34m[2022-09-27 12:48:13,709 INFO] Step 1200/40000; acc:  28.49; ppl: 54.84; xent: 4.00; lr: 0.00015; 24373/23205 tok/s;   1004 sec;[0m
    [34m[2022-09-27 12:49:40,687 INFO] Step 1300/40000; acc:  26.53; ppl: 68.21; xent: 4.22; lr: 0.00016; 22108/22230 tok/s;   1091 sec;[0m
    [34m[2022-09-27 12:51:07,140 INFO] Step 1400/40000; acc:  21.27; ppl: 94.57; xent: 4.55; lr: 0.00017; 18050/18962 tok/s;   1177 sec;[0m
    [34m[2022-09-27 12:52:32,108 INFO] Step 1500/40000; acc:  22.31; ppl: 90.30; xent: 4.50; lr: 0.00019; 19816/19677 tok/s;   1262 sec;[0m
    [34m[2022-09-27 12:53:57,565 INFO] Step 1600/40000; acc:  20.98; ppl: 90.58; xent: 4.51; lr: 0.00020; 19434/19296 tok/s;   1348 sec;[0m
    [34m[2022-09-27 12:55:23,053 INFO] Step 1700/40000; acc:  27.33; ppl: 55.29; xent: 4.01; lr: 0.00021; 23010/21488 tok/s;   1433 sec;[0m
    [34m[2022-09-27 12:56:47,921 INFO] Step 1800/40000; acc:  33.22; ppl: 35.70; xent: 3.58; lr: 0.00022; 23290/22345 tok/s;   1518 sec;[0m
    [34m[2022-09-27 12:58:12,214 INFO] Step 1900/40000; acc:  23.90; ppl: 65.52; xent: 4.18; lr: 0.00023; 19514/19447 tok/s;   1602 sec;[0m
    [34m[2022-09-27 12:59:35,796 INFO] Step 2000/40000; acc:  23.09; ppl: 68.26; xent: 4.22; lr: 0.00025; 19137/19027 tok/s;   1686 sec;[0m
    [34m[2022-09-27 13:00:59,073 INFO] Step 2100/40000; acc:  24.75; ppl: 58.72; xent: 4.07; lr: 0.00026; 19146/18682 tok/s;   1769 sec;[0m
    [34m[2022-09-27 13:02:24,819 INFO] Step 2200/40000; acc:  33.01; ppl: 33.40; xent: 3.51; lr: 0.00027; 21882/20982 tok/s;   1855 sec;[0m
    [34m[2022-09-27 13:03:50,034 INFO] Step 2300/40000; acc:  47.12; ppl: 13.17; xent: 2.58; lr: 0.00028; 25044/23911 tok/s;   1940 sec;[0m
    [34m[2022-09-27 13:05:13,158 INFO] Step 2400/40000; acc:  34.09; ppl: 30.63; xent: 3.42; lr: 0.00030; 19294/20368 tok/s;   2023 sec;[0m
    [34m[2022-09-27 13:06:37,590 INFO] Step 2500/40000; acc:  32.06; ppl: 34.23; xent: 3.53; lr: 0.00031; 19858/19773 tok/s;   2108 sec;[0m
    [34m[2022-09-27 13:07:58,814 INFO] Step 2600/40000; acc:  29.30; ppl: 39.66; xent: 3.68; lr: 0.00032; 19455/19681 tok/s;   2189 sec;[0m
    [34m[2022-09-27 13:09:17,047 INFO] Step 2700/40000; acc:  37.21; ppl: 24.38; xent: 3.19; lr: 0.00033; 21172/21224 tok/s;   2267 sec;[0m
    [34m[2022-09-27 13:10:34,773 INFO] Step 2800/40000; acc:  35.12; ppl: 26.67; xent: 3.28; lr: 0.00035; 21599/21829 tok/s;   2345 sec;[0m
    [34m[2022-09-27 13:11:51,488 INFO] Step 2900/40000; acc:  33.02; ppl: 30.15; xent: 3.41; lr: 0.00036; 21198/21441 tok/s;   2422 sec;[0m
    [34m[2022-09-27 13:13:11,701 INFO] Step 3000/40000; acc:  34.73; ppl: 26.81; xent: 3.29; lr: 0.00037; 19673/20647 tok/s;   2502 sec;[0m
    [34m[2022-09-27 13:14:38,547 INFO] Step 3100/40000; acc:  41.52; ppl: 17.34; xent: 2.85; lr: 0.00038; 19823/21489 tok/s;   2589 sec;[0m
    [34m[2022-09-27 13:16:02,804 INFO] Step 3200/40000; acc:  38.61; ppl: 20.73; xent: 3.03; lr: 0.00040; 19089/19803 tok/s;   2673 sec;[0m
    [34m[2022-09-27 13:17:26,448 INFO] Step 3300/40000; acc:  38.33; ppl: 21.46; xent: 3.07; lr: 0.00041; 18831/19276 tok/s;   2757 sec;[0m
    [34m[2022-09-27 13:18:50,383 INFO] Step 3400/40000; acc:  40.30; ppl: 18.93; xent: 2.94; lr: 0.00042; 19302/19033 tok/s;   2841 sec;[0m
    [34m[2022-09-27 13:20:17,111 INFO] Step 3500/40000; acc:  41.81; ppl: 17.17; xent: 2.84; lr: 0.00043; 21860/20146 tok/s;   2927 sec;[0m
    [34m[2022-09-27 13:21:42,876 INFO] Step 3600/40000; acc:  60.58; ppl:  5.66; xent: 1.73; lr: 0.00044; 26170/24733 tok/s;   3013 sec;[0m
    [34m[2022-09-27 13:23:06,782 INFO] Step 3700/40000; acc:  63.25; ppl:  5.01; xent: 1.61; lr: 0.00046; 23856/22840 tok/s;   3097 sec;[0m
    [34m[2022-09-27 13:24:25,587 INFO] Step 3800/40000; acc:  42.95; ppl: 16.36; xent: 2.79; lr: 0.00047; 20953/20610 tok/s;   3176 sec;[0m
    [34m[2022-09-27 13:25:45,209 INFO] Step 3900/40000; acc:  41.23; ppl: 17.12; xent: 2.84; lr: 0.00048; 20741/21112 tok/s;   3255 sec;[0m
    [34m[2022-09-27 13:27:04,012 INFO] Step 4000/40000; acc:  36.92; ppl: 22.75; xent: 3.12; lr: 0.00049; 19536/19891 tok/s;   3334 sec;[0m
    [34m[2022-09-27 13:28:23,229 INFO] Step 4100/40000; acc:  38.63; ppl: 20.54; xent: 3.02; lr: 0.00051; 20264/20355 tok/s;   3413 sec;[0m
    [34m[2022-09-27 13:29:47,909 INFO] Step 4200/40000; acc:  37.91; ppl: 20.53; xent: 3.02; lr: 0.00052; 20127/20009 tok/s;   3498 sec;[0m
    [34m[2022-09-27 13:31:12,050 INFO] Step 4300/40000; acc:  52.25; ppl: 10.64; xent: 2.36; lr: 0.00053; 20666/21351 tok/s;   3582 sec;[0m
    [34m[2022-09-27 13:32:35,651 INFO] Step 4400/40000; acc:  45.49; ppl: 13.59; xent: 2.61; lr: 0.00054; 19222/18806 tok/s;   3666 sec;[0m
    [34m[2022-09-27 13:33:57,460 INFO] Step 4500/40000; acc:  53.78; ppl:  8.28; xent: 2.11; lr: 0.00056; 22105/22169 tok/s;   3748 sec;[0m
    [34m[2022-09-27 13:35:15,260 INFO] Step 4600/40000; acc:  45.11; ppl: 13.60; xent: 2.61; lr: 0.00057; 19634/20447 tok/s;   3825 sec;[0m
    [34m[2022-09-27 13:36:35,381 INFO] Step 4700/40000; acc:  40.86; ppl: 16.64; xent: 2.81; lr: 0.00058; 21879/20993 tok/s;   3906 sec;[0m
    [34m[2022-09-27 13:37:53,298 INFO] Step 4800/40000; acc:  40.20; ppl: 17.82; xent: 2.88; lr: 0.00059; 20109/20701 tok/s;   3983 sec;[0m
    [34m[2022-09-27 13:39:12,214 INFO] Step 4900/40000; acc:  40.90; ppl: 17.37; xent: 2.85; lr: 0.00061; 19698/19704 tok/s;   4062 sec;[0m
    [34m[2022-09-27 13:40:30,745 INFO] Step 5000/40000; acc:  40.19; ppl: 18.44; xent: 2.91; lr: 0.00062; 19359/19527 tok/s;   4141 sec;[0m
    [34m[2022-09-27 13:41:51,102 INFO] Step 5100/40000; acc:  46.32; ppl: 12.94; xent: 2.56; lr: 0.00063; 18584/19229 tok/s;   4221 sec;[0m
    [34m[2022-09-27 13:43:14,753 INFO] Step 5200/40000; acc:  61.91; ppl:  5.88; xent: 1.77; lr: 0.00064; 21325/22093 tok/s;   4305 sec;[0m
    [34m[2022-09-27 13:44:39,594 INFO] Step 5300/40000; acc:  53.90; ppl:  8.42; xent: 2.13; lr: 0.00065; 19825/20444 tok/s;   4390 sec;[0m
    [34m[2022-09-27 13:46:13,226 INFO] Step 5400/40000; acc:  63.03; ppl:  6.09; xent: 1.81; lr: 0.00067; 19284/19243 tok/s;   4483 sec;[0m
    [34m[2022-09-27 13:47:37,092 INFO] Step 5500/40000; acc:  62.30; ppl:  5.36; xent: 1.68; lr: 0.00068; 19857/20942 tok/s;   4567 sec;[0m
    [34m[2022-09-27 13:49:03,634 INFO] Step 5600/40000; acc:  65.85; ppl:  4.27; xent: 1.45; lr: 0.00069; 20647/21877 tok/s;   4654 sec;[0m
    [34m[2022-09-27 13:50:26,155 INFO] Step 5700/40000; acc:  71.48; ppl:  3.16; xent: 1.15; lr: 0.00070; 22728/24517 tok/s;   4736 sec;[0m
    [34m[2022-09-27 13:51:49,822 INFO] Step 5800/40000; acc:  64.28; ppl:  4.82; xent: 1.57; lr: 0.00072; 19758/21518 tok/s;   4820 sec;[0m
    [34m[2022-09-27 13:53:08,217 INFO] Step 5900/40000; acc:  64.30; ppl:  4.68; xent: 1.54; lr: 0.00073; 21414/22807 tok/s;   4898 sec;[0m
    [34m[2022-09-27 13:54:27,928 INFO] Step 6000/40000; acc:  67.25; ppl:  4.17; xent: 1.43; lr: 0.00074; 22413/23239 tok/s;   4978 sec;[0m
    [34m[2022-09-27 13:55:47,872 INFO] Step 6100/40000; acc:  55.59; ppl:  7.92; xent: 2.07; lr: 0.00075; 21525/21208 tok/s;   5058 sec;[0m
    [34m[2022-09-27 13:57:08,266 INFO] Step 6200/40000; acc:  63.53; ppl:  5.56; xent: 1.71; lr: 0.00077; 20959/21111 tok/s;   5138 sec;[0m
    [34m[2022-09-27 13:58:27,987 INFO] Step 6300/40000; acc:  58.95; ppl:  6.93; xent: 1.94; lr: 0.00078; 22348/22466 tok/s;   5218 sec;[0m
    [34m[2022-09-27 13:59:49,769 INFO] Step 6400/40000; acc:  50.71; ppl: 10.56; xent: 2.36; lr: 0.00079; 20102/20430 tok/s;   5300 sec;[0m
    [34m[2022-09-27 14:01:14,010 INFO] Step 6500/40000; acc:  54.96; ppl:  8.86; xent: 2.18; lr: 0.00080; 18749/18803 tok/s;   5384 sec;[0m
    [34m[2022-09-27 14:02:39,706 INFO] Step 6600/40000; acc:  46.02; ppl: 12.52; xent: 2.53; lr: 0.00082; 19841/19632 tok/s;   5470 sec;[0m
    [34m[2022-09-27 14:04:03,092 INFO] Step 6700/40000; acc:  46.60; ppl: 13.47; xent: 2.60; lr: 0.00083; 17624/19035 tok/s;   5553 sec;[0m
    [34m[2022-09-27 14:05:21,576 INFO] Step 6800/40000; acc:  50.06; ppl: 10.27; xent: 2.33; lr: 0.00084; 20531/21226 tok/s;   5632 sec;[0m
    [34m[2022-09-27 14:06:38,789 INFO] Step 6900/40000; acc:  50.11; ppl: 10.12; xent: 2.31; lr: 0.00085; 20676/21034 tok/s;   5709 sec;[0m
    [34m[2022-09-27 14:07:58,943 INFO] Step 7000/40000; acc:  59.97; ppl:  6.11; xent: 1.81; lr: 0.00086; 23454/21824 tok/s;   5789 sec;[0m
    [34m[2022-09-27 14:09:20,668 INFO] Step 7100/40000; acc:  68.00; ppl:  3.72; xent: 1.31; lr: 0.00088; 28225/26126 tok/s;   5871 sec;[0m
    [34m[2022-09-27 14:10:39,029 INFO] Step 7200/40000; acc:  72.97; ppl:  2.97; xent: 1.09; lr: 0.00089; 26508/25385 tok/s;   5949 sec;[0m
    [34m[2022-09-27 14:11:58,746 INFO] Step 7300/40000; acc:  54.00; ppl:  8.10; xent: 2.09; lr: 0.00090; 20150/20396 tok/s;   6029 sec;[0m
    [34m[2022-09-27 14:13:18,725 INFO] Step 7400/40000; acc:  53.49; ppl:  8.63; xent: 2.16; lr: 0.00091; 18013/19861 tok/s;   6109 sec;[0m
    [34m[2022-09-27 14:14:38,139 INFO] Step 7500/40000; acc:  51.67; ppl:  9.42; xent: 2.24; lr: 0.00093; 20061/20389 tok/s;   6188 sec;[0m
    [34m[2022-09-27 14:16:02,177 INFO] Step 7600/40000; acc:  61.82; ppl:  6.09; xent: 1.81; lr: 0.00094; 19948/20734 tok/s;   6272 sec;[0m
    [34m[2022-09-27 14:17:24,937 INFO] Step 7700/40000; acc:  51.68; ppl:  9.23; xent: 2.22; lr: 0.00095; 21073/21778 tok/s;   6355 sec;[0m
    [34m[2022-09-27 14:18:51,382 INFO] Step 7800/40000; acc:  58.28; ppl:  6.38; xent: 1.85; lr: 0.00096; 22176/20855 tok/s;   6442 sec;[0m
    [34m[2022-09-27 14:20:16,222 INFO] Step 7900/40000; acc:  72.54; ppl:  2.98; xent: 1.09; lr: 0.00098; 26218/24691 tok/s;   6526 sec;[0m
    [34m[2022-09-27 14:21:41,964 INFO] Step 8000/40000; acc:  68.39; ppl:  3.85; xent: 1.35; lr: 0.00099; 23642/22359 tok/s;   6612 sec;[0m
    [34m[2022-09-27 14:23:06,923 INFO] Step 8100/40000; acc:  46.59; ppl: 12.12; xent: 2.49; lr: 0.00098; 17635/18118 tok/s;   6697 sec;[0m
    [34m[2022-09-27 14:24:29,243 INFO] Step 8200/40000; acc:  42.30; ppl: 15.62; xent: 2.75; lr: 0.00098; 14375/16951 tok/s;   6779 sec;[0m
    [34m[2022-09-27 14:25:53,242 INFO] Step 8300/40000; acc:  46.40; ppl: 12.38; xent: 2.52; lr: 0.00097; 19783/19086 tok/s;   6863 sec;[0m
    [34m[2022-09-27 14:27:18,857 INFO] Step 8400/40000; acc:  71.45; ppl:  3.16; xent: 1.15; lr: 0.00096; 25474/23633 tok/s;   6949 sec;[0m
    [34m[2022-09-27 14:28:43,417 INFO] Step 8500/40000; acc:  61.05; ppl:  5.76; xent: 1.75; lr: 0.00096; 21547/20911 tok/s;   7034 sec;[0m
    [34m[2022-09-27 14:30:06,080 INFO] Step 8600/40000; acc:  51.48; ppl:  9.21; xent: 2.22; lr: 0.00095; 19319/19179 tok/s;   7116 sec;[0m
    [34m[2022-09-27 14:31:28,447 INFO] Step 8700/40000; acc:  51.68; ppl:  9.35; xent: 2.24; lr: 0.00095; 18952/19102 tok/s;   7199 sec;[0m
    [34m[2022-09-27 14:32:55,235 INFO] Step 8800/40000; acc:  47.35; ppl: 11.34; xent: 2.43; lr: 0.00094; 19768/18941 tok/s;   7285 sec;[0m
    [34m[2022-09-27 14:34:23,983 INFO] Step 8900/40000; acc:  56.41; ppl:  7.28; xent: 1.99; lr: 0.00094; 19828/20057 tok/s;   7374 sec;[0m
    [34m[2022-09-27 14:35:49,838 INFO] Step 9000/40000; acc:  47.69; ppl: 11.59; xent: 2.45; lr: 0.00093; 20976/19257 tok/s;   7460 sec;[0m
    [34m[2022-09-27 14:37:16,669 INFO] Step 9100/40000; acc:  66.29; ppl:  4.12; xent: 1.42; lr: 0.00093; 24529/22163 tok/s;   7547 sec;[0m
    [34m[2022-09-27 14:38:40,608 INFO] Step 9200/40000; acc:  68.01; ppl:  3.92; xent: 1.37; lr: 0.00092; 22736/21959 tok/s;   7631 sec;[0m
    [34m[2022-09-27 14:40:03,038 INFO] Step 9300/40000; acc:  50.02; ppl:  9.63; xent: 2.27; lr: 0.00092; 20004/19151 tok/s;   7713 sec;[0m
    [34m[2022-09-27 14:41:21,788 INFO] Step 9400/40000; acc:  49.72; ppl:  9.82; xent: 2.28; lr: 0.00091; 20643/20088 tok/s;   7792 sec;[0m
    [34m[2022-09-27 14:42:42,318 INFO] Step 9500/40000; acc:  58.41; ppl:  5.97; xent: 1.79; lr: 0.00091; 22120/19765 tok/s;   7872 sec;[0m
    [34m[2022-09-27 14:44:03,272 INFO] Step 9600/40000; acc:  69.44; ppl:  3.25; xent: 1.18; lr: 0.00090; 26356/24212 tok/s;   7953 sec;[0m
    [34m[2022-09-27 14:45:19,023 INFO] Step 9700/40000; acc:  72.76; ppl:  2.86; xent: 1.05; lr: 0.00090; 23626/23119 tok/s;   8029 sec;[0m
    [34m[2022-09-27 14:46:38,082 INFO] Step 9800/40000; acc:  58.10; ppl:  6.54; xent: 1.88; lr: 0.00089; 22833/21490 tok/s;   8108 sec;[0m
    [34m[2022-09-27 14:47:58,944 INFO] Step 9900/40000; acc:  54.56; ppl:  7.38; xent: 2.00; lr: 0.00089; 20565/20075 tok/s;   8189 sec;[0m
    [34m[2022-09-27 14:49:20,795 INFO] Step 10000/40000; acc:  53.47; ppl:  8.43; xent: 2.13; lr: 0.00088; 19490/19926 tok/s;   8271 sec;[0m
    [34m[2022-09-27 14:49:20,796 INFO] valid's transforms: TransformPipe()[0m
    [34m[2022-09-27 14:49:20,906 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 14:49:20,910 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 14:49:20,911 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 14:49:41,174 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 14:49:41,174 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 14:49:41,175 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 14:50:02,053 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 14:50:11,178 INFO] Validation perplexity: 16.9486[0m
    [34m[2022-09-27 14:50:11,178 INFO] Validation accuracy: 46.7501[0m
    [34m[2022-09-27 14:50:11,221 INFO] Saving checkpoint /opt/ml/model/model.en-ja_step_10000.pt[0m
    [34m[2022-09-27 14:51:47,792 INFO] Step 10100/40000; acc:  63.18; ppl:  4.92; xent: 1.59; lr: 0.00088; 10990/11006 tok/s;   8418 sec;[0m
    [34m[2022-09-27 14:53:12,948 INFO] Step 10200/40000; acc:  60.47; ppl:  5.56; xent: 1.72; lr: 0.00088; 18994/19036 tok/s;   8503 sec;[0m
    [34m[2022-09-27 14:54:36,650 INFO] Step 10300/40000; acc:  66.80; ppl:  3.90; xent: 1.36; lr: 0.00087; 21293/21435 tok/s;   8587 sec;[0m
    [34m[2022-09-27 14:55:59,261 INFO] Step 10400/40000; acc:  58.12; ppl:  6.50; xent: 1.87; lr: 0.00087; 19331/19736 tok/s;   8669 sec;[0m
    [34m[2022-09-27 14:57:23,128 INFO] Step 10500/40000; acc:  55.69; ppl:  7.97; xent: 2.08; lr: 0.00086; 18305/19038 tok/s;   8753 sec;[0m
    [34m[2022-09-27 14:58:46,209 INFO] Step 10600/40000; acc:  52.87; ppl:  8.44; xent: 2.13; lr: 0.00086; 19353/19431 tok/s;   8836 sec;[0m
    [34m[2022-09-27 15:00:05,728 INFO] Step 10700/40000; acc:  56.24; ppl:  7.42; xent: 2.00; lr: 0.00085; 20088/20976 tok/s;   8916 sec;[0m
    [34m[2022-09-27 15:01:24,378 INFO] Step 10800/40000; acc:  49.41; ppl: 10.45; xent: 2.35; lr: 0.00085; 20848/20522 tok/s;   8995 sec;[0m
    [34m[2022-09-27 15:02:43,440 INFO] Step 10900/40000; acc:  49.15; ppl: 10.57; xent: 2.36; lr: 0.00085; 20561/20718 tok/s;   9074 sec;[0m
    [34m[2022-09-27 15:04:02,708 INFO] Step 11000/40000; acc:  47.19; ppl: 11.50; xent: 2.44; lr: 0.00084; 19610/20156 tok/s;   9153 sec;[0m
    [34m[2022-09-27 15:05:21,596 INFO] Step 11100/40000; acc:  52.76; ppl:  8.57; xent: 2.15; lr: 0.00084; 20207/20339 tok/s;   9232 sec;[0m
    [34m[2022-09-27 15:06:40,481 INFO] Step 11200/40000; acc:  52.36; ppl:  8.66; xent: 2.16; lr: 0.00084; 20570/20292 tok/s;   9311 sec;[0m
    [34m[2022-09-27 15:07:58,732 INFO] Step 11300/40000; acc:  52.84; ppl:  8.61; xent: 2.15; lr: 0.00083; 20029/20497 tok/s;   9389 sec;[0m
    [34m[2022-09-27 15:09:17,586 INFO] Step 11400/40000; acc:  54.08; ppl:  7.77; xent: 2.05; lr: 0.00083; 19981/19692 tok/s;   9468 sec;[0m
    [34m[2022-09-27 15:10:35,064 INFO] Step 11500/40000; acc:  48.73; ppl: 10.73; xent: 2.37; lr: 0.00082; 18100/19396 tok/s;   9545 sec;[0m
    [34m[2022-09-27 15:11:56,645 INFO] Step 11600/40000; acc:  66.24; ppl:  4.03; xent: 1.39; lr: 0.00082; 24852/23243 tok/s;   9627 sec;[0m
    [34m[2022-09-27 15:13:18,440 INFO] Step 11700/40000; acc:  74.22; ppl:  2.74; xent: 1.01; lr: 0.00082; 25638/24664 tok/s;   9709 sec;[0m
    [34m[2022-09-27 15:14:43,345 INFO] Step 11800/40000; acc:  61.57; ppl:  5.59; xent: 1.72; lr: 0.00081; 20930/20033 tok/s;   9794 sec;[0m
    [34m[2022-09-27 15:16:06,316 INFO] Step 11900/40000; acc:  47.27; ppl: 12.29; xent: 2.51; lr: 0.00081; 19475/16667 tok/s;   9876 sec;[0m
    [34m[2022-09-27 15:17:27,912 INFO] Step 12000/40000; acc:  42.76; ppl: 15.05; xent: 2.71; lr: 0.00081; 18662/17348 tok/s;   9958 sec;[0m
    [34m[2022-09-27 15:18:54,813 INFO] Step 12100/40000; acc:  50.74; ppl:  9.96; xent: 2.30; lr: 0.00080; 19308/19290 tok/s;  10045 sec;[0m
    [34m[2022-09-27 15:20:23,112 INFO] Step 12200/40000; acc:  64.06; ppl:  5.15; xent: 1.64; lr: 0.00080; 20569/20916 tok/s;  10133 sec;[0m
    [34m[2022-09-27 15:21:49,435 INFO] Step 12300/40000; acc:  55.40; ppl:  8.09; xent: 2.09; lr: 0.00080; 17923/17961 tok/s;  10220 sec;[0m
    [34m[2022-09-27 15:23:13,253 INFO] Step 12400/40000; acc:  50.78; ppl:  9.73; xent: 2.27; lr: 0.00079; 19378/19413 tok/s;  10303 sec;[0m
    [34m[2022-09-27 15:24:40,640 INFO] Step 12500/40000; acc:  55.75; ppl:  8.13; xent: 2.10; lr: 0.00079; 20200/18127 tok/s;  10391 sec;[0m
    [34m[2022-09-27 15:26:12,321 INFO] Step 12600/40000; acc:  55.14; ppl:  8.18; xent: 2.10; lr: 0.00079; 19020/18758 tok/s;  10482 sec;[0m
    [34m[2022-09-27 15:27:36,663 INFO] Step 12700/40000; acc:  51.09; ppl:  9.69; xent: 2.27; lr: 0.00078; 19153/19144 tok/s;  10567 sec;[0m
    [34m[2022-09-27 15:28:58,876 INFO] Step 12800/40000; acc:  50.46; ppl: 10.03; xent: 2.31; lr: 0.00078; 18219/18773 tok/s;  10649 sec;[0m
    [34m[2022-09-27 15:30:21,848 INFO] Step 12900/40000; acc:  50.43; ppl: 10.10; xent: 2.31; lr: 0.00078; 18046/18611 tok/s;  10732 sec;[0m
    [34m[2022-09-27 15:31:47,232 INFO] Step 13000/40000; acc:  60.71; ppl:  5.89; xent: 1.77; lr: 0.00078; 19353/19422 tok/s;  10817 sec;[0m
    [34m[2022-09-27 15:33:14,360 INFO] Step 13100/40000; acc:  72.04; ppl:  3.02; xent: 1.11; lr: 0.00077; 26137/23629 tok/s;  10905 sec;[0m
    [34m[2022-09-27 15:34:39,256 INFO] Step 13200/40000; acc:  76.80; ppl:  2.44; xent: 0.89; lr: 0.00077; 25029/23939 tok/s;  10989 sec;[0m
    [34m[2022-09-27 15:36:01,803 INFO] Step 13300/40000; acc:  67.94; ppl:  3.92; xent: 1.37; lr: 0.00077; 22336/21780 tok/s;  11072 sec;[0m
    [34m[2022-09-27 15:37:20,929 INFO] Step 13400/40000; acc:  53.93; ppl:  8.02; xent: 2.08; lr: 0.00076; 19850/20765 tok/s;  11151 sec;[0m
    [34m[2022-09-27 15:38:39,953 INFO] Step 13500/40000; acc:  50.55; ppl:  9.26; xent: 2.23; lr: 0.00076; 21733/21945 tok/s;  11230 sec;[0m
    [34m[2022-09-27 15:39:59,997 INFO] Step 13600/40000; acc:  58.17; ppl:  6.35; xent: 1.85; lr: 0.00076; 20775/20385 tok/s;  11310 sec;[0m
    [34m[2022-09-27 15:41:24,384 INFO] Step 13700/40000; acc:  72.53; ppl:  2.96; xent: 1.09; lr: 0.00076; 26682/24619 tok/s;  11395 sec;[0m
    [34m[2022-09-27 15:42:48,549 INFO] Step 13800/40000; acc:  73.84; ppl:  2.84; xent: 1.04; lr: 0.00075; 24158/23173 tok/s;  11479 sec;[0m
    [34m[2022-09-27 15:44:11,547 INFO] Step 13900/40000; acc:  53.89; ppl:  8.07; xent: 2.09; lr: 0.00075; 20412/20006 tok/s;  11562 sec;[0m
    [34m[2022-09-27 15:45:33,886 INFO] Step 14000/40000; acc:  51.10; ppl:  9.62; xent: 2.26; lr: 0.00075; 17898/18186 tok/s;  11644 sec;[0m
    [34m[2022-09-27 15:46:56,805 INFO] Step 14100/40000; acc:  46.75; ppl: 11.85; xent: 2.47; lr: 0.00074; 18059/18230 tok/s;  11727 sec;[0m
    [34m[2022-09-27 15:48:21,399 INFO] Step 14200/40000; acc:  52.19; ppl:  9.05; xent: 2.20; lr: 0.00074; 17832/18433 tok/s;  11812 sec;[0m
    [34m[2022-09-27 15:49:45,347 INFO] Step 14300/40000; acc:  57.45; ppl:  6.65; xent: 1.90; lr: 0.00074; 19698/19580 tok/s;  11896 sec;[0m
    [34m[2022-09-27 15:51:08,270 INFO] Step 14400/40000; acc:  66.36; ppl:  4.41; xent: 1.48; lr: 0.00074; 19975/20781 tok/s;  11978 sec;[0m
    [34m[2022-09-27 15:52:31,883 INFO] Step 14500/40000; acc:  53.46; ppl:  8.30; xent: 2.12; lr: 0.00073; 19664/19465 tok/s;  12062 sec;[0m
    [34m[2022-09-27 15:53:54,591 INFO] Step 14600/40000; acc:  58.81; ppl:  6.50; xent: 1.87; lr: 0.00073; 19267/19213 tok/s;  12145 sec;[0m
    [34m[2022-09-27 15:55:20,070 INFO] Step 14700/40000; acc:  58.97; ppl:  5.84; xent: 1.76; lr: 0.00073; 20720/20509 tok/s;  12230 sec;[0m
    [34m[2022-09-27 15:56:46,314 INFO] Step 14800/40000; acc:  67.74; ppl:  3.74; xent: 1.32; lr: 0.00073; 24239/22546 tok/s;  12316 sec;[0m
    [34m[2022-09-27 15:58:10,231 INFO] Step 14900/40000; acc:  67.87; ppl:  3.82; xent: 1.34; lr: 0.00072; 22052/21734 tok/s;  12400 sec;[0m
    [34m[2022-09-27 15:59:37,264 INFO] Step 15000/40000; acc:  66.97; ppl:  3.85; xent: 1.35; lr: 0.00072; 23673/22205 tok/s;  12487 sec;[0m
    [34m[2022-09-27 16:01:01,426 INFO] Step 15100/40000; acc:  75.72; ppl:  2.57; xent: 0.94; lr: 0.00072; 24447/23519 tok/s;  12572 sec;[0m
    [34m[2022-09-27 16:02:26,067 INFO] Step 15200/40000; acc:  60.62; ppl:  5.92; xent: 1.78; lr: 0.00072; 20337/19757 tok/s;  12656 sec;[0m
    [34m[2022-09-27 16:03:49,609 INFO] Step 15300/40000; acc:  55.18; ppl:  7.85; xent: 2.06; lr: 0.00071; 18749/18595 tok/s;  12740 sec;[0m
    [34m[2022-09-27 16:05:13,104 INFO] Step 15400/40000; acc:  53.86; ppl:  8.38; xent: 2.13; lr: 0.00071; 19475/19178 tok/s;  12823 sec;[0m
    [34m[2022-09-27 16:06:36,262 INFO] Step 15500/40000; acc:  59.68; ppl:  6.37; xent: 1.85; lr: 0.00071; 19360/19978 tok/s;  12906 sec;[0m
    [34m[2022-09-27 16:07:59,161 INFO] Step 15600/40000; acc:  56.80; ppl:  7.19; xent: 1.97; lr: 0.00071; 19636/20182 tok/s;  12989 sec;[0m
    [34m[2022-09-27 16:09:23,531 INFO] Step 15700/40000; acc:  55.36; ppl:  7.49; xent: 2.01; lr: 0.00071; 18778/18830 tok/s;  13074 sec;[0m
    [34m[2022-09-27 16:10:47,561 INFO] Step 15800/40000; acc:  62.48; ppl:  5.26; xent: 1.66; lr: 0.00070; 19626/19537 tok/s;  13158 sec;[0m
    [34m[2022-09-27 16:12:15,185 INFO] Step 15900/40000; acc:  71.11; ppl:  3.17; xent: 1.15; lr: 0.00070; 25279/22999 tok/s;  13245 sec;[0m
    [34m[2022-09-27 16:13:39,436 INFO] Step 16000/40000; acc:  76.21; ppl:  2.50; xent: 0.92; lr: 0.00070; 24879/23894 tok/s;  13330 sec;[0m
    [34m[2022-09-27 16:15:05,009 INFO] Step 16100/40000; acc:  68.19; ppl:  3.77; xent: 1.33; lr: 0.00070; 23541/22268 tok/s;  13415 sec;[0m
    [34m[2022-09-27 16:16:31,389 INFO] Step 16200/40000; acc:  64.03; ppl:  4.58; xent: 1.52; lr: 0.00069; 22037/20447 tok/s;  13502 sec;[0m
    [34m[2022-09-27 16:17:56,530 INFO] Step 16300/40000; acc:  76.55; ppl:  2.47; xent: 0.90; lr: 0.00069; 24656/23390 tok/s;  13587 sec;[0m
    [34m[2022-09-27 16:19:24,230 INFO] Step 16400/40000; acc:  57.94; ppl:  6.72; xent: 1.90; lr: 0.00069; 18897/18535 tok/s;  13674 sec;[0m
    [34m[2022-09-27 16:20:49,680 INFO] Step 16500/40000; acc:  56.96; ppl:  7.07; xent: 1.96; lr: 0.00069; 19148/19204 tok/s;  13760 sec;[0m
    [34m[2022-09-27 16:22:14,388 INFO] Step 16600/40000; acc:  60.97; ppl:  5.98; xent: 1.79; lr: 0.00069; 19435/19832 tok/s;  13845 sec;[0m
    [34m[2022-09-27 16:23:37,925 INFO] Step 16700/40000; acc:  67.05; ppl:  4.50; xent: 1.50; lr: 0.00068; 19861/20322 tok/s;  13928 sec;[0m
    [34m[2022-09-27 16:24:59,903 INFO] Step 16800/40000; acc:  49.53; ppl: 10.13; xent: 2.32; lr: 0.00068; 20116/19727 tok/s;  14010 sec;[0m
    [34m[2022-09-27 16:26:17,678 INFO] Step 16900/40000; acc:  52.43; ppl:  8.94; xent: 2.19; lr: 0.00068; 21027/20553 tok/s;  14088 sec;[0m
    [34m[2022-09-27 16:27:36,384 INFO] Step 17000/40000; acc:  55.05; ppl:  7.69; xent: 2.04; lr: 0.00068; 21502/21636 tok/s;  14167 sec;[0m
    [34m[2022-09-27 16:28:56,898 INFO] Step 17100/40000; acc:  49.51; ppl:  9.98; xent: 2.30; lr: 0.00068; 20654/20595 tok/s;  14247 sec;[0m
    [34m[2022-09-27 16:30:17,351 INFO] Step 17200/40000; acc:  49.02; ppl:  9.80; xent: 2.28; lr: 0.00067; 20303/20699 tok/s;  14328 sec;[0m
    [34m[2022-09-27 16:31:40,110 INFO] Step 17300/40000; acc:  51.44; ppl:  9.07; xent: 2.20; lr: 0.00067; 20224/20241 tok/s;  14410 sec;[0m
    [34m[2022-09-27 16:33:06,573 INFO] Step 17400/40000; acc:  62.37; ppl:  5.44; xent: 1.69; lr: 0.00067; 19643/19495 tok/s;  14497 sec;[0m
    [34m[2022-09-27 16:34:29,134 INFO] Step 17500/40000; acc:  63.77; ppl:  5.02; xent: 1.61; lr: 0.00067; 19896/20134 tok/s;  14579 sec;[0m
    [34m[2022-09-27 16:35:52,694 INFO] Step 17600/40000; acc:  56.00; ppl:  7.46; xent: 2.01; lr: 0.00067; 19374/19338 tok/s;  14663 sec;[0m
    [34m[2022-09-27 16:37:18,680 INFO] Step 17700/40000; acc:  52.37; ppl:  8.29; xent: 2.12; lr: 0.00066; 18928/19394 tok/s;  14749 sec;[0m
    [34m[2022-09-27 16:38:41,775 INFO] Step 17800/40000; acc:  53.20; ppl:  7.85; xent: 2.06; lr: 0.00066; 20225/20566 tok/s;  14832 sec;[0m
    [34m[2022-09-27 16:40:05,113 INFO] Step 17900/40000; acc:  57.04; ppl:  6.89; xent: 1.93; lr: 0.00066; 19654/19225 tok/s;  14915 sec;[0m
    [34m[2022-09-27 16:41:29,907 INFO] Step 18000/40000; acc:  49.31; ppl:  9.94; xent: 2.30; lr: 0.00066; 20586/19019 tok/s;  15000 sec;[0m
    [34m[2022-09-27 16:42:54,681 INFO] Step 18100/40000; acc:  51.80; ppl:  8.17; xent: 2.10; lr: 0.00066; 21834/20433 tok/s;  15085 sec;[0m
    [34m[2022-09-27 16:44:18,206 INFO] Step 18200/40000; acc:  57.53; ppl:  6.98; xent: 1.94; lr: 0.00066; 19005/19108 tok/s;  15168 sec;[0m
    [34m[2022-09-27 16:45:42,698 INFO] Step 18300/40000; acc:  52.11; ppl:  8.68; xent: 2.16; lr: 0.00065; 20210/19471 tok/s;  15253 sec;[0m
    [34m[2022-09-27 16:47:06,733 INFO] Step 18400/40000; acc:  61.19; ppl:  5.49; xent: 1.70; lr: 0.00065; 19609/19267 tok/s;  15337 sec;[0m
    [34m[2022-09-27 16:48:29,333 INFO] Step 18500/40000; acc:  72.81; ppl:  2.89; xent: 1.06; lr: 0.00065; 27223/24258 tok/s;  15419 sec;[0m
    [34m[2022-09-27 16:49:48,204 INFO] Step 18600/40000; acc:  77.75; ppl:  2.32; xent: 0.84; lr: 0.00065; 27568/26325 tok/s;  15498 sec;[0m
    [34m[2022-09-27 16:51:06,586 INFO] Step 18700/40000; acc:  77.11; ppl:  2.43; xent: 0.89; lr: 0.00065; 25988/24922 tok/s;  15577 sec;[0m
    [34m[2022-09-27 16:52:24,694 INFO] Step 18800/40000; acc:  63.04; ppl:  5.19; xent: 1.65; lr: 0.00064; 22522/23397 tok/s;  15655 sec;[0m
    [34m[2022-09-27 16:53:44,267 INFO] Step 18900/40000; acc:  58.75; ppl:  6.27; xent: 1.84; lr: 0.00064; 20848/21776 tok/s;  15734 sec;[0m
    [34m[2022-09-27 16:55:02,255 INFO] Step 19000/40000; acc:  63.65; ppl:  5.10; xent: 1.63; lr: 0.00064; 20145/20946 tok/s;  15812 sec;[0m
    [34m[2022-09-27 16:56:20,617 INFO] Step 19100/40000; acc:  60.05; ppl:  6.22; xent: 1.83; lr: 0.00064; 20791/20922 tok/s;  15891 sec;[0m
    [34m[2022-09-27 16:57:40,012 INFO] Step 19200/40000; acc:  57.10; ppl:  7.23; xent: 1.98; lr: 0.00064; 21064/20823 tok/s;  15970 sec;[0m
    [34m[2022-09-27 16:59:03,262 INFO] Step 19300/40000; acc:  64.61; ppl:  4.61; xent: 1.53; lr: 0.00064; 21813/21707 tok/s;  16053 sec;[0m
    [34m[2022-09-27 17:00:20,881 INFO] Step 19400/40000; acc:  63.11; ppl:  5.15; xent: 1.64; lr: 0.00063; 20476/20753 tok/s;  16131 sec;[0m
    [34m[2022-09-27 17:01:43,049 INFO] Step 19500/40000; acc:  57.86; ppl:  7.09; xent: 1.96; lr: 0.00063; 18067/19460 tok/s;  16213 sec;[0m
    [34m[2022-09-27 17:03:02,300 INFO] Step 19600/40000; acc:  59.35; ppl:  6.15; xent: 1.82; lr: 0.00063; 21217/21620 tok/s;  16292 sec;[0m
    [34m[2022-09-27 17:04:24,405 INFO] Step 19700/40000; acc:  61.42; ppl:  5.25; xent: 1.66; lr: 0.00063; 22432/20940 tok/s;  16375 sec;[0m
    [34m[2022-09-27 17:05:48,439 INFO] Step 19800/40000; acc:  77.51; ppl:  2.34; xent: 0.85; lr: 0.00063; 25425/24196 tok/s;  16459 sec;[0m
    [34m[2022-09-27 17:07:13,938 INFO] Step 19900/40000; acc:  68.47; ppl:  3.90; xent: 1.36; lr: 0.00063; 20676/20659 tok/s;  16544 sec;[0m
    [34m[2022-09-27 17:08:37,121 INFO] Step 20000/40000; acc:  59.44; ppl:  6.37; xent: 1.85; lr: 0.00062; 19145/19411 tok/s;  16627 sec;[0m
    [34m[2022-09-27 17:08:37,223 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 17:08:37,226 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 17:08:37,227 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 17:08:57,510 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 17:08:57,510 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 17:08:57,511 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 17:09:18,420 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 17:09:27,549 INFO] Validation perplexity: 10.6168[0m
    [34m[2022-09-27 17:09:27,549 INFO] Validation accuracy: 53.9178[0m
    [34m[2022-09-27 17:09:27,594 INFO] Saving checkpoint /opt/ml/model/model.en-ja_step_20000.pt[0m
    [34m[2022-09-27 17:10:47,879 INFO] Step 20100/40000; acc:  54.43; ppl:  8.00; xent: 2.08; lr: 0.00062; 11896/12188 tok/s;  16758 sec;[0m
    [34m[2022-09-27 17:12:07,072 INFO] Step 20200/40000; acc:  59.04; ppl:  6.58; xent: 1.88; lr: 0.00062; 19878/20038 tok/s;  16837 sec;[0m
    [34m[2022-09-27 17:13:29,242 INFO] Step 20300/40000; acc:  53.59; ppl:  8.16; xent: 2.10; lr: 0.00062; 18424/18934 tok/s;  16919 sec;[0m
    [34m[2022-09-27 17:14:53,450 INFO] Step 20400/40000; acc:  55.62; ppl:  7.45; xent: 2.01; lr: 0.00062; 18527/18810 tok/s;  17004 sec;[0m
    [34m[2022-09-27 17:16:18,347 INFO] Step 20500/40000; acc:  51.61; ppl:  9.22; xent: 2.22; lr: 0.00062; 18658/18901 tok/s;  17089 sec;[0m
    [34m[2022-09-27 17:17:43,073 INFO] Step 20600/40000; acc:  59.08; ppl:  6.53; xent: 1.88; lr: 0.00062; 18804/19297 tok/s;  17173 sec;[0m
    [34m[2022-09-27 17:19:04,043 INFO] Step 20700/40000; acc:  58.58; ppl:  6.53; xent: 1.88; lr: 0.00061; 20144/20060 tok/s;  17254 sec;[0m
    [34m[2022-09-27 17:20:24,317 INFO] Step 20800/40000; acc:  58.87; ppl:  6.37; xent: 1.85; lr: 0.00061; 19614/20271 tok/s;  17334 sec;[0m
    [34m[2022-09-27 17:21:43,381 INFO] Step 20900/40000; acc:  61.59; ppl:  5.66; xent: 1.73; lr: 0.00061; 20763/21189 tok/s;  17414 sec;[0m
    [34m[2022-09-27 17:23:05,094 INFO] Step 21000/40000; acc:  68.66; ppl:  4.24; xent: 1.44; lr: 0.00061; 19889/20765 tok/s;  17495 sec;[0m
    [34m[2022-09-27 17:24:28,054 INFO] Step 21100/40000; acc:  57.06; ppl:  6.98; xent: 1.94; lr: 0.00061; 19214/19026 tok/s;  17578 sec;[0m
    [34m[2022-09-27 17:25:51,185 INFO] Step 21200/40000; acc:  66.03; ppl:  4.44; xent: 1.49; lr: 0.00061; 18321/19827 tok/s;  17661 sec;[0m
    [34m[2022-09-27 17:27:16,522 INFO] Step 21300/40000; acc:  62.24; ppl:  5.58; xent: 1.72; lr: 0.00061; 17581/18427 tok/s;  17747 sec;[0m
    [34m[2022-09-27 17:28:39,047 INFO] Step 21400/40000; acc:  57.93; ppl:  6.70; xent: 1.90; lr: 0.00060; 18777/19380 tok/s;  17829 sec;[0m
    [34m[2022-09-27 17:30:01,447 INFO] Step 21500/40000; acc:  61.23; ppl:  5.42; xent: 1.69; lr: 0.00060; 19287/20380 tok/s;  17912 sec;[0m
    [34m[2022-09-27 17:31:24,259 INFO] Step 21600/40000; acc:  64.62; ppl:  4.60; xent: 1.53; lr: 0.00060; 19979/19592 tok/s;  17994 sec;[0m
    [34m[2022-09-27 17:32:47,609 INFO] Step 21700/40000; acc:  60.31; ppl:  5.62; xent: 1.73; lr: 0.00060; 19228/18858 tok/s;  18078 sec;[0m
    [34m[2022-09-27 17:34:10,827 INFO] Step 21800/40000; acc:  53.01; ppl:  8.24; xent: 2.11; lr: 0.00060; 18931/18925 tok/s;  18161 sec;[0m
    [34m[2022-09-27 17:35:34,067 INFO] Step 21900/40000; acc:  57.13; ppl:  6.97; xent: 1.94; lr: 0.00060; 19317/19473 tok/s;  18244 sec;[0m
    [34m[2022-09-27 17:36:57,416 INFO] Step 22000/40000; acc:  65.36; ppl:  4.77; xent: 1.56; lr: 0.00060; 19756/19680 tok/s;  18328 sec;[0m
    [34m[2022-09-27 17:38:19,139 INFO] Step 22100/40000; acc:  60.78; ppl:  6.06; xent: 1.80; lr: 0.00059; 17388/18335 tok/s;  18409 sec;[0m
    [34m[2022-09-27 17:39:42,906 INFO] Step 22200/40000; acc:  52.04; ppl:  8.75; xent: 2.17; lr: 0.00059; 19348/19991 tok/s;  18493 sec;[0m
    [34m[2022-09-27 17:41:05,904 INFO] Step 22300/40000; acc:  55.84; ppl:  7.32; xent: 1.99; lr: 0.00059; 18985/19182 tok/s;  18576 sec;[0m
    [34m[2022-09-27 17:42:30,336 INFO] Step 22400/40000; acc:  56.44; ppl:  7.23; xent: 1.98; lr: 0.00059; 19752/19016 tok/s;  18660 sec;[0m
    [34m[2022-09-27 17:43:54,309 INFO] Step 22500/40000; acc:  54.43; ppl:  7.79; xent: 2.05; lr: 0.00059; 19248/18958 tok/s;  18744 sec;[0m
    [34m[2022-09-27 17:45:17,132 INFO] Step 22600/40000; acc:  58.50; ppl:  6.58; xent: 1.88; lr: 0.00059; 18382/18600 tok/s;  18827 sec;[0m
    [34m[2022-09-27 17:46:40,146 INFO] Step 22700/40000; acc:  56.04; ppl:  7.33; xent: 1.99; lr: 0.00059; 19536/19354 tok/s;  18910 sec;[0m
    [34m[2022-09-27 17:48:05,462 INFO] Step 22800/40000; acc:  54.98; ppl:  7.78; xent: 2.05; lr: 0.00059; 19061/19231 tok/s;  18996 sec;[0m
    [34m[2022-09-27 17:49:29,792 INFO] Step 22900/40000; acc:  51.46; ppl:  8.48; xent: 2.14; lr: 0.00058; 19944/19855 tok/s;  19080 sec;[0m
    [34m[2022-09-27 17:50:53,355 INFO] Step 23000/40000; acc:  62.64; ppl:  5.43; xent: 1.69; lr: 0.00058; 19394/20057 tok/s;  19164 sec;[0m
    [34m[2022-09-27 17:52:13,951 INFO] Step 23100/40000; acc:  58.55; ppl:  6.46; xent: 1.87; lr: 0.00058; 18928/20185 tok/s;  19244 sec;[0m
    [34m[2022-09-27 17:53:33,021 INFO] Step 23200/40000; acc:  58.00; ppl:  6.60; xent: 1.89; lr: 0.00058; 18931/20003 tok/s;  19323 sec;[0m
    [34m[2022-09-27 17:54:51,154 INFO] Step 23300/40000; acc:  57.07; ppl:  6.77; xent: 1.91; lr: 0.00058; 20239/20656 tok/s;  19401 sec;[0m
    [34m[2022-09-27 17:56:09,758 INFO] Step 23400/40000; acc:  68.24; ppl:  3.99; xent: 1.38; lr: 0.00058; 19832/20430 tok/s;  19480 sec;[0m
    [34m[2022-09-27 17:57:32,150 INFO] Step 23500/40000; acc:  60.98; ppl:  5.41; xent: 1.69; lr: 0.00058; 19706/20266 tok/s;  19562 sec;[0m
    [34m[2022-09-27 17:58:57,958 INFO] Step 23600/40000; acc:  59.21; ppl:  5.99; xent: 1.79; lr: 0.00058; 21105/19574 tok/s;  19648 sec;[0m
    [34m[2022-09-27 18:00:23,401 INFO] Step 23700/40000; acc:  59.78; ppl:  5.91; xent: 1.78; lr: 0.00057; 20795/20002 tok/s;  19734 sec;[0m
    [34m[2022-09-27 18:01:46,373 INFO] Step 23800/40000; acc:  54.83; ppl:  7.61; xent: 2.03; lr: 0.00057; 19750/19602 tok/s;  19817 sec;[0m
    [34m[2022-09-27 18:03:09,321 INFO] Step 23900/40000; acc:  57.87; ppl:  6.58; xent: 1.88; lr: 0.00057; 19366/19741 tok/s;  19899 sec;[0m
    [34m[2022-09-27 18:04:27,476 INFO] Step 24000/40000; acc:  57.54; ppl:  6.85; xent: 1.92; lr: 0.00057; 19264/21071 tok/s;  19978 sec;[0m
    [34m[2022-09-27 18:05:47,714 INFO] Step 24100/40000; acc:  52.66; ppl:  8.58; xent: 2.15; lr: 0.00057; 19913/19694 tok/s;  20058 sec;[0m
    [34m[2022-09-27 18:07:13,380 INFO] Step 24200/40000; acc:  52.44; ppl:  7.51; xent: 2.02; lr: 0.00057; 25026/21741 tok/s;  20144 sec;[0m
    [34m[2022-09-27 18:08:40,035 INFO] Step 24300/40000; acc:  59.59; ppl:  4.96; xent: 1.60; lr: 0.00057; 25876/24350 tok/s;  20230 sec;[0m
    [34m[2022-09-27 18:10:06,980 INFO] Step 24400/40000; acc:  63.82; ppl:  4.01; xent: 1.39; lr: 0.00057; 24923/24608 tok/s;  20317 sec;[0m
    [34m[2022-09-27 18:11:31,230 INFO] Step 24500/40000; acc:  57.31; ppl:  6.27; xent: 1.84; lr: 0.00056; 21732/21451 tok/s;  20401 sec;[0m
    [34m[2022-09-27 18:12:52,168 INFO] Step 24600/40000; acc:  57.15; ppl:  6.48; xent: 1.87; lr: 0.00056; 20796/20243 tok/s;  20482 sec;[0m
    [34m[2022-09-27 18:14:13,104 INFO] Step 24700/40000; acc:  53.24; ppl:  8.14; xent: 2.10; lr: 0.00056; 20045/19965 tok/s;  20563 sec;[0m
    [34m[2022-09-27 18:15:33,555 INFO] Step 24800/40000; acc:  51.93; ppl:  8.47; xent: 2.14; lr: 0.00056; 20476/20823 tok/s;  20644 sec;[0m
    [34m[2022-09-27 18:16:54,592 INFO] Step 24900/40000; acc:  59.48; ppl:  5.37; xent: 1.68; lr: 0.00056; 23345/25201 tok/s;  20725 sec;[0m
    [34m[2022-09-27 18:18:18,155 INFO] Step 25000/40000; acc:  56.06; ppl:  6.81; xent: 1.92; lr: 0.00056; 20419/21151 tok/s;  20808 sec;[0m
    [34m[2022-09-27 18:19:38,472 INFO] Step 25100/40000; acc:  56.83; ppl:  6.80; xent: 1.92; lr: 0.00056; 21522/21383 tok/s;  20889 sec;[0m
    [34m[2022-09-27 18:20:59,694 INFO] Step 25200/40000; acc:  53.96; ppl:  7.56; xent: 2.02; lr: 0.00056; 21244/20941 tok/s;  20970 sec;[0m
    [34m[2022-09-27 18:22:21,288 INFO] Step 25300/40000; acc:  54.11; ppl:  7.37; xent: 2.00; lr: 0.00056; 20627/20160 tok/s;  21051 sec;[0m
    [34m[2022-09-27 18:23:45,446 INFO] Step 25400/40000; acc:  55.03; ppl:  6.95; xent: 1.94; lr: 0.00055; 20635/19773 tok/s;  21136 sec;[0m
    [34m[2022-09-27 18:25:10,155 INFO] Step 25500/40000; acc:  55.01; ppl:  7.09; xent: 1.96; lr: 0.00055; 20143/20095 tok/s;  21220 sec;[0m
    [34m[2022-09-27 18:26:35,208 INFO] Step 25600/40000; acc:  52.10; ppl:  8.58; xent: 2.15; lr: 0.00055; 19100/18686 tok/s;  21305 sec;[0m
    [34m[2022-09-27 18:28:00,295 INFO] Step 25700/40000; acc:  55.84; ppl:  6.98; xent: 1.94; lr: 0.00055; 19668/19370 tok/s;  21390 sec;[0m
    [34m[2022-09-27 18:29:25,512 INFO] Step 25800/40000; acc:  61.27; ppl:  5.47; xent: 1.70; lr: 0.00055; 21235/21391 tok/s;  21476 sec;[0m
    [34m[2022-09-27 18:30:49,898 INFO] Step 25900/40000; acc:  58.32; ppl:  6.30; xent: 1.84; lr: 0.00055; 20042/19947 tok/s;  21560 sec;[0m
    [34m[2022-09-27 18:32:13,651 INFO] Step 26000/40000; acc:  66.90; ppl:  3.96; xent: 1.38; lr: 0.00055; 21764/21799 tok/s;  21644 sec;[0m
    [34m[2022-09-27 18:33:34,910 INFO] Step 26100/40000; acc:  53.10; ppl:  7.74; xent: 2.05; lr: 0.00055; 21995/21488 tok/s;  21725 sec;[0m
    [34m[2022-09-27 18:34:56,189 INFO] Step 26200/40000; acc:  54.13; ppl:  7.42; xent: 2.00; lr: 0.00055; 20886/20717 tok/s;  21806 sec;[0m
    [34m[2022-09-27 18:36:18,676 INFO] Step 26300/40000; acc:  54.56; ppl:  7.32; xent: 1.99; lr: 0.00055; 21039/21028 tok/s;  21889 sec;[0m
    [34m[2022-09-27 18:37:38,417 INFO] Step 26400/40000; acc:  57.31; ppl:  6.52; xent: 1.87; lr: 0.00054; 20861/20928 tok/s;  21969 sec;[0m
    [34m[2022-09-27 18:38:59,598 INFO] Step 26500/40000; acc:  76.24; ppl:  2.51; xent: 0.92; lr: 0.00054; 22715/22541 tok/s;  22050 sec;[0m
    [34m[2022-09-27 18:40:22,923 INFO] Step 26600/40000; acc:  65.13; ppl:  4.41; xent: 1.48; lr: 0.00054; 20918/21131 tok/s;  22133 sec;[0m
    [34m[2022-09-27 18:41:47,635 INFO] Step 26700/40000; acc:  58.52; ppl:  6.03; xent: 1.80; lr: 0.00054; 19808/20200 tok/s;  22218 sec;[0m
    [34m[2022-09-27 18:43:15,325 INFO] Step 26800/40000; acc:  52.53; ppl:  8.25; xent: 2.11; lr: 0.00054; 19402/18788 tok/s;  22305 sec;[0m
    [34m[2022-09-27 18:44:42,804 INFO] Step 26900/40000; acc:  51.27; ppl:  8.52; xent: 2.14; lr: 0.00054; 18890/18417 tok/s;  22393 sec;[0m
    [34m[2022-09-27 18:46:08,266 INFO] Step 27000/40000; acc:  53.54; ppl:  7.75; xent: 2.05; lr: 0.00054; 20039/19128 tok/s;  22478 sec;[0m
    [34m[2022-09-27 18:47:28,827 INFO] Step 27100/40000; acc:  56.59; ppl:  6.62; xent: 1.89; lr: 0.00054; 21479/21151 tok/s;  22559 sec;[0m
    [34m[2022-09-27 18:48:48,664 INFO] Step 27200/40000; acc:  55.10; ppl:  7.39; xent: 2.00; lr: 0.00054; 21455/20434 tok/s;  22639 sec;[0m
    [34m[2022-09-27 18:50:09,115 INFO] Step 27300/40000; acc:  58.41; ppl:  6.30; xent: 1.84; lr: 0.00053; 20925/21121 tok/s;  22719 sec;[0m
    [34m[2022-09-27 18:51:29,445 INFO] Step 27400/40000; acc:  57.65; ppl:  6.55; xent: 1.88; lr: 0.00053; 20771/20302 tok/s;  22800 sec;[0m
    [34m[2022-09-27 18:52:50,309 INFO] Step 27500/40000; acc:  58.59; ppl:  6.34; xent: 1.85; lr: 0.00053; 21025/20605 tok/s;  22880 sec;[0m
    [34m[2022-09-27 18:54:09,025 INFO] Step 27600/40000; acc:  53.37; ppl:  8.01; xent: 2.08; lr: 0.00053; 20266/20751 tok/s;  22959 sec;[0m
    [34m[2022-09-27 18:55:30,826 INFO] Step 27700/40000; acc:  52.71; ppl:  7.49; xent: 2.01; lr: 0.00053; 23355/21868 tok/s;  23041 sec;[0m
    [34m[2022-09-27 18:56:52,492 INFO] Step 27800/40000; acc:  59.79; ppl:  5.04; xent: 1.62; lr: 0.00053; 25553/24577 tok/s;  23123 sec;[0m
    [34m[2022-09-27 18:58:14,798 INFO] Step 27900/40000; acc:  62.18; ppl:  5.01; xent: 1.61; lr: 0.00053; 21245/21772 tok/s;  23205 sec;[0m
    [34m[2022-09-27 18:59:36,220 INFO] Step 28000/40000; acc:  55.15; ppl:  7.09; xent: 1.96; lr: 0.00053; 20863/21508 tok/s;  23286 sec;[0m
    [34m[2022-09-27 19:00:53,967 INFO] Step 28100/40000; acc:  53.88; ppl:  8.04; xent: 2.08; lr: 0.00053; 20086/20797 tok/s;  23364 sec;[0m
    [34m[2022-09-27 19:02:13,586 INFO] Step 28200/40000; acc:  55.94; ppl:  6.85; xent: 1.92; lr: 0.00053; 21175/21028 tok/s;  23444 sec;[0m
    [34m[2022-09-27 19:03:33,123 INFO] Step 28300/40000; acc:  55.24; ppl:  7.40; xent: 2.00; lr: 0.00053; 20757/20435 tok/s;  23523 sec;[0m
    [34m[2022-09-27 19:04:54,784 INFO] Step 28400/40000; acc:  53.95; ppl:  7.90; xent: 2.07; lr: 0.00052; 19473/19727 tok/s;  23605 sec;[0m
    [34m[2022-09-27 19:06:21,279 INFO] Step 28500/40000; acc:  56.34; ppl:  7.28; xent: 1.98; lr: 0.00052; 17986/18590 tok/s;  23691 sec;[0m
    [34m[2022-09-27 19:07:48,990 INFO] Step 28600/40000; acc:  55.72; ppl:  7.22; xent: 1.98; lr: 0.00052; 19251/18941 tok/s;  23779 sec;[0m
    [34m[2022-09-27 19:09:18,211 INFO] Step 28700/40000; acc:  57.99; ppl:  6.28; xent: 1.84; lr: 0.00052; 18708/18975 tok/s;  23868 sec;[0m
    [34m[2022-09-27 19:10:43,539 INFO] Step 28800/40000; acc:  58.00; ppl:  6.28; xent: 1.84; lr: 0.00052; 19288/19539 tok/s;  23954 sec;[0m
    [34m[2022-09-27 19:12:08,322 INFO] Step 28900/40000; acc:  57.01; ppl:  6.44; xent: 1.86; lr: 0.00052; 20310/20586 tok/s;  24038 sec;[0m
    [34m[2022-09-27 19:13:30,626 INFO] Step 29000/40000; acc:  57.61; ppl:  6.49; xent: 1.87; lr: 0.00052; 19352/19348 tok/s;  24121 sec;[0m
    [34m[2022-09-27 19:14:59,207 INFO] Step 29100/40000; acc:  51.38; ppl:  9.47; xent: 2.25; lr: 0.00052; 21550/19625 tok/s;  24209 sec;[0m
    [34m[2022-09-27 19:16:30,868 INFO] Step 29200/40000; acc:  54.14; ppl:  7.19; xent: 1.97; lr: 0.00052; 28800/25937 tok/s;  24301 sec;[0m
    [34m[2022-09-27 19:18:05,317 INFO] Step 29300/40000; acc:  57.41; ppl:  5.89; xent: 1.77; lr: 0.00052; 27621/25371 tok/s;  24395 sec;[0m
    [34m[2022-09-27 19:19:36,732 INFO] Step 29400/40000; acc:  60.99; ppl:  4.89; xent: 1.59; lr: 0.00052; 26026/24886 tok/s;  24487 sec;[0m
    [34m[2022-09-27 19:21:06,371 INFO] Step 29500/40000; acc:  61.38; ppl:  4.84; xent: 1.58; lr: 0.00051; 27151/25483 tok/s;  24577 sec;[0m
    [34m[2022-09-27 19:22:32,257 INFO] Step 29600/40000; acc:  63.31; ppl:  4.66; xent: 1.54; lr: 0.00051; 23399/23244 tok/s;  24662 sec;[0m
    [34m[2022-09-27 19:23:57,680 INFO] Step 29700/40000; acc:  58.21; ppl:  6.52; xent: 1.88; lr: 0.00051; 19543/19757 tok/s;  24748 sec;[0m
    [34m[2022-09-27 19:25:24,341 INFO] Step 29800/40000; acc:  50.24; ppl:  9.68; xent: 2.27; lr: 0.00051; 19589/19608 tok/s;  24834 sec;[0m
    [34m[2022-09-27 19:26:50,599 INFO] Step 29900/40000; acc:  51.03; ppl:  8.46; xent: 2.14; lr: 0.00051; 20312/19385 tok/s;  24921 sec;[0m
    [34m[2022-09-27 19:28:16,749 INFO] Step 30000/40000; acc:  55.40; ppl:  7.11; xent: 1.96; lr: 0.00051; 19054/18831 tok/s;  25007 sec;[0m
    [34m[2022-09-27 19:28:16,852 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 19:28:16,858 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 19:28:16,859 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 19:28:37,129 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 19:28:37,129 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 19:28:37,133 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 19:28:57,998 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 19:29:07,155 INFO] Validation perplexity: 9.65143[0m
    [34m[2022-09-27 19:29:07,155 INFO] Validation accuracy: 54.5909[0m
    [34m[2022-09-27 19:29:07,197 INFO] Saving checkpoint /opt/ml/model/model.en-ja_step_30000.pt[0m
    [34m[2022-09-27 19:30:33,779 INFO] Step 30100/40000; acc:  58.71; ppl:  6.30; xent: 1.84; lr: 0.00051; 11555/12281 tok/s;  25144 sec;[0m
    [34m[2022-09-27 19:32:00,173 INFO] Step 30200/40000; acc:  55.27; ppl:  7.22; xent: 1.98; lr: 0.00051; 19430/19113 tok/s;  25230 sec;[0m
    [34m[2022-09-27 19:33:27,270 INFO] Step 30300/40000; acc:  57.16; ppl:  6.60; xent: 1.89; lr: 0.00051; 19610/19544 tok/s;  25317 sec;[0m
    [34m[2022-09-27 19:34:51,072 INFO] Step 30400/40000; acc:  63.00; ppl:  4.98; xent: 1.61; lr: 0.00051; 20307/20462 tok/s;  25401 sec;[0m
    [34m[2022-09-27 19:36:16,764 INFO] Step 30500/40000; acc:  59.42; ppl:  5.96; xent: 1.79; lr: 0.00051; 19640/19391 tok/s;  25487 sec;[0m
    [34m[2022-09-27 19:37:46,514 INFO] Step 30600/40000; acc:  87.71; ppl:  1.65; xent: 0.50; lr: 0.00051; 29233/24325 tok/s;  25577 sec;[0m
    [34m[2022-09-27 19:39:14,119 INFO] Step 30700/40000; acc:  70.87; ppl:  3.55; xent: 1.27; lr: 0.00050; 24032/21104 tok/s;  25664 sec;[0m
    [34m[2022-09-27 19:40:38,589 INFO] Step 30800/40000; acc:  59.76; ppl:  5.92; xent: 1.78; lr: 0.00050; 19989/19807 tok/s;  25749 sec;[0m
    [34m[2022-09-27 19:42:02,793 INFO] Step 30900/40000; acc:  57.96; ppl:  6.57; xent: 1.88; lr: 0.00050; 19364/20130 tok/s;  25833 sec;[0m
    [34m[2022-09-27 19:43:28,076 INFO] Step 31000/40000; acc:  61.77; ppl:  5.43; xent: 1.69; lr: 0.00050; 18857/19577 tok/s;  25918 sec;[0m
    [34m[2022-09-27 19:44:49,085 INFO] Step 31100/40000; acc:  57.25; ppl:  6.83; xent: 1.92; lr: 0.00050; 18996/20184 tok/s;  25999 sec;[0m
    [34m[2022-09-27 19:46:17,035 INFO] Step 31200/40000; acc:  71.98; ppl:  3.66; xent: 1.30; lr: 0.00050; 14009/25702 tok/s;  26087 sec;[0m
    [34m[2022-09-27 19:47:46,638 INFO] Step 31300/40000; acc:  79.82; ppl:  2.55; xent: 0.94; lr: 0.00050; 13805/29192 tok/s;  26177 sec;[0m
    [34m[2022-09-27 19:49:10,403 INFO] Step 31400/40000; acc:  79.94; ppl:  2.55; xent: 0.93; lr: 0.00050; 13892/31097 tok/s;  26261 sec;[0m
    [34m[2022-09-27 19:50:33,926 INFO] Step 31500/40000; acc:  80.44; ppl:  2.48; xent: 0.91; lr: 0.00050; 13436/31057 tok/s;  26344 sec;[0m
    [34m[2022-09-27 19:51:54,822 INFO] Step 31600/40000; acc:  76.23; ppl:  2.97; xent: 1.09; lr: 0.00050; 14368/28061 tok/s;  26425 sec;[0m
    [34m[2022-09-27 19:53:20,755 INFO] Step 31700/40000; acc:  51.68; ppl:  8.67; xent: 2.16; lr: 0.00050; 18823/19395 tok/s;  26511 sec;[0m
    [34m[2022-09-27 19:54:44,738 INFO] Step 31800/40000; acc:  63.91; ppl:  4.78; xent: 1.56; lr: 0.00050; 20497/20251 tok/s;  26595 sec;[0m
    [34m[2022-09-27 19:56:09,899 INFO] Step 31900/40000; acc:  52.05; ppl:  8.25; xent: 2.11; lr: 0.00049; 19185/19323 tok/s;  26680 sec;[0m
    [34m[2022-09-27 19:57:34,823 INFO] Step 32000/40000; acc:  55.94; ppl:  6.64; xent: 1.89; lr: 0.00049; 19769/20032 tok/s;  26765 sec;[0m
    [34m[2022-09-27 19:58:58,558 INFO] Step 32100/40000; acc:  63.34; ppl:  4.73; xent: 1.55; lr: 0.00049; 18928/19440 tok/s;  26849 sec;[0m
    [34m[2022-09-27 20:00:24,885 INFO] Step 32200/40000; acc:  62.94; ppl:  4.67; xent: 1.54; lr: 0.00049; 20368/19802 tok/s;  26935 sec;[0m
    [34m[2022-09-27 20:01:50,899 INFO] Step 32300/40000; acc:  59.35; ppl:  5.66; xent: 1.73; lr: 0.00049; 20649/20106 tok/s;  27021 sec;[0m
    [34m[2022-09-27 20:03:15,687 INFO] Step 32400/40000; acc:  58.60; ppl:  5.98; xent: 1.79; lr: 0.00049; 22202/19112 tok/s;  27106 sec;[0m
    [34m[2022-09-27 20:04:38,870 INFO] Step 32500/40000; acc:  90.51; ppl:  1.54; xent: 0.43; lr: 0.00049; 30462/20616 tok/s;  27189 sec;[0m
    [34m[2022-09-27 20:06:01,837 INFO] Step 32600/40000; acc:  92.08; ppl:  1.49; xent: 0.40; lr: 0.00049; 31254/22541 tok/s;  27272 sec;[0m
    [34m[2022-09-27 20:07:26,273 INFO] Step 32700/40000; acc:  72.74; ppl:  3.41; xent: 1.23; lr: 0.00049; 21811/22651 tok/s;  27356 sec;[0m
    [34m[2022-09-27 20:08:51,704 INFO] Step 32800/40000; acc:  60.75; ppl:  5.78; xent: 1.75; lr: 0.00049; 20522/19867 tok/s;  27442 sec;[0m
    [34m[2022-09-27 20:10:15,466 INFO] Step 32900/40000; acc:  55.89; ppl:  6.98; xent: 1.94; lr: 0.00049; 19727/19572 tok/s;  27526 sec;[0m
    [34m[2022-09-27 20:11:41,382 INFO] Step 33000/40000; acc:  56.87; ppl:  6.52; xent: 1.88; lr: 0.00049; 19666/18800 tok/s;  27612 sec;[0m
    [34m[2022-09-27 20:13:07,943 INFO] Step 33100/40000; acc:  54.30; ppl:  7.12; xent: 1.96; lr: 0.00049; 19485/18244 tok/s;  27698 sec;[0m
    [34m[2022-09-27 20:14:34,329 INFO] Step 33200/40000; acc:  53.10; ppl:  7.90; xent: 2.07; lr: 0.00049; 19949/19728 tok/s;  27784 sec;[0m
    [34m[2022-09-27 20:15:59,440 INFO] Step 33300/40000; acc:  56.48; ppl:  7.05; xent: 1.95; lr: 0.00048; 19897/19406 tok/s;  27870 sec;[0m
    [34m[2022-09-27 20:17:24,297 INFO] Step 33400/40000; acc:  56.18; ppl:  7.07; xent: 1.96; lr: 0.00048; 19398/18958 tok/s;  27954 sec;[0m
    [34m[2022-09-27 20:18:49,290 INFO] Step 33500/40000; acc:  56.04; ppl:  7.00; xent: 1.95; lr: 0.00048; 19390/19578 tok/s;  28039 sec;[0m
    [34m[2022-09-27 20:20:13,875 INFO] Step 33600/40000; acc:  55.09; ppl:  7.49; xent: 2.01; lr: 0.00048; 20347/19474 tok/s;  28124 sec;[0m
    [34m[2022-09-27 20:21:38,039 INFO] Step 33700/40000; acc:  50.38; ppl:  9.16; xent: 2.21; lr: 0.00048; 20528/18964 tok/s;  28208 sec;[0m
    [34m[2022-09-27 20:23:04,484 INFO] Step 33800/40000; acc:  55.98; ppl:  7.10; xent: 1.96; lr: 0.00048; 18521/19202 tok/s;  28295 sec;[0m
    [34m[2022-09-27 20:24:30,571 INFO] Step 33900/40000; acc:  60.95; ppl:  5.62; xent: 1.73; lr: 0.00048; 18958/19306 tok/s;  28381 sec;[0m
    [34m[2022-09-27 20:25:56,016 INFO] Step 34000/40000; acc:  53.36; ppl:  7.45; xent: 2.01; lr: 0.00048; 21772/20850 tok/s;  28466 sec;[0m
    [34m[2022-09-27 20:27:21,376 INFO] Step 34100/40000; acc:  51.63; ppl:  8.29; xent: 2.12; lr: 0.00048; 21148/20247 tok/s;  28552 sec;[0m
    [34m[2022-09-27 20:28:47,219 INFO] Step 34200/40000; acc:  49.56; ppl:  9.41; xent: 2.24; lr: 0.00048; 18883/18885 tok/s;  28637 sec;[0m
    [34m[2022-09-27 20:30:12,394 INFO] Step 34300/40000; acc:  54.18; ppl:  7.70; xent: 2.04; lr: 0.00048; 19206/18724 tok/s;  28723 sec;[0m
    [34m[2022-09-27 20:31:38,588 INFO] Step 34400/40000; acc:  57.97; ppl:  6.34; xent: 1.85; lr: 0.00048; 19272/19318 tok/s;  28809 sec;[0m
    [34m[2022-09-27 20:33:06,112 INFO] Step 34500/40000; acc:  57.82; ppl:  6.16; xent: 1.82; lr: 0.00048; 19657/19010 tok/s;  28896 sec;[0m
    [34m[2022-09-27 20:34:31,312 INFO] Step 34600/40000; acc:  56.94; ppl:  6.59; xent: 1.89; lr: 0.00048; 19152/18720 tok/s;  28981 sec;[0m
    [34m[2022-09-27 20:35:56,003 INFO] Step 34700/40000; acc:  52.79; ppl:  7.91; xent: 2.07; lr: 0.00047; 19786/18848 tok/s;  29066 sec;[0m
    [34m[2022-09-27 20:37:18,097 INFO] Step 34800/40000; acc:  57.52; ppl:  6.67; xent: 1.90; lr: 0.00047; 19389/19324 tok/s;  29148 sec;[0m
    [34m[2022-09-27 20:38:43,864 INFO] Step 34900/40000; acc:  67.31; ppl:  4.34; xent: 1.47; lr: 0.00047; 20005/19540 tok/s;  29234 sec;[0m
    [34m[2022-09-27 20:40:11,229 INFO] Step 35000/40000; acc:  79.01; ppl:  2.55; xent: 0.94; lr: 0.00047; 22763/21725 tok/s;  29321 sec;[0m
    [34m[2022-09-27 20:41:40,145 INFO] Step 35100/40000; acc:  97.73; ppl:  1.16; xent: 0.15; lr: 0.00047; 31518/25821 tok/s;  29410 sec;[0m
    [34m[2022-09-27 20:43:11,548 INFO] Step 35200/40000; acc:  96.55; ppl:  1.21; xent: 0.19; lr: 0.00047; 30258/26366 tok/s;  29502 sec;[0m
    [34m[2022-09-27 20:44:39,784 INFO] Step 35300/40000; acc:  71.08; ppl:  3.65; xent: 1.29; lr: 0.00047; 21535/20790 tok/s;  29590 sec;[0m
    [34m[2022-09-27 20:46:07,027 INFO] Step 35400/40000; acc:  52.77; ppl:  8.14; xent: 2.10; lr: 0.00047; 18469/18696 tok/s;  29677 sec;[0m
    [34m[2022-09-27 20:47:32,807 INFO] Step 35500/40000; acc:  50.90; ppl:  8.77; xent: 2.17; lr: 0.00047; 19036/18927 tok/s;  29763 sec;[0m
    [34m[2022-09-27 20:48:56,903 INFO] Step 35600/40000; acc:  49.52; ppl:  9.55; xent: 2.26; lr: 0.00047; 18320/18398 tok/s;  29847 sec;[0m
    [34m[2022-09-27 20:50:23,878 INFO] Step 35700/40000; acc:  54.17; ppl:  7.55; xent: 2.02; lr: 0.00047; 18787/18581 tok/s;  29934 sec;[0m
    [34m[2022-09-27 20:51:50,757 INFO] Step 35800/40000; acc:  50.88; ppl:  9.08; xent: 2.21; lr: 0.00047; 18305/18808 tok/s;  30021 sec;[0m
    [34m[2022-09-27 20:53:16,164 INFO] Step 35900/40000; acc:  53.13; ppl:  7.43; xent: 2.01; lr: 0.00047; 19889/20844 tok/s;  30106 sec;[0m
    [34m[2022-09-27 20:54:41,398 INFO] Step 36000/40000; acc:  57.76; ppl:  6.18; xent: 1.82; lr: 0.00047; 19986/19035 tok/s;  30192 sec;[0m
    [34m[2022-09-27 20:56:06,669 INFO] Step 36100/40000; acc:  51.68; ppl:  8.59; xent: 2.15; lr: 0.00047; 19183/19359 tok/s;  30277 sec;[0m
    [34m[2022-09-27 20:57:33,988 INFO] Step 36200/40000; acc:  55.10; ppl:  7.25; xent: 1.98; lr: 0.00046; 18494/18655 tok/s;  30364 sec;[0m
    [34m[2022-09-27 20:59:00,456 INFO] Step 36300/40000; acc:  55.99; ppl:  6.94; xent: 1.94; lr: 0.00046; 19800/19645 tok/s;  30451 sec;[0m
    [34m[2022-09-27 21:00:26,678 INFO] Step 36400/40000; acc:  56.52; ppl:  6.44; xent: 1.86; lr: 0.00046; 19345/19537 tok/s;  30537 sec;[0m
    [34m[2022-09-27 21:01:54,395 INFO] Step 36500/40000; acc:  53.81; ppl:  7.78; xent: 2.05; lr: 0.00046; 17315/17874 tok/s;  30625 sec;[0m
    [34m[2022-09-27 21:03:20,516 INFO] Step 36600/40000; acc:  53.59; ppl:  7.70; xent: 2.04; lr: 0.00046; 19522/19298 tok/s;  30711 sec;[0m
    [34m[2022-09-27 21:04:44,085 INFO] Step 36700/40000; acc:  57.61; ppl:  6.15; xent: 1.82; lr: 0.00046; 20393/20043 tok/s;  30794 sec;[0m
    [34m[2022-09-27 21:06:07,023 INFO] Step 36800/40000; acc:  67.43; ppl:  3.96; xent: 1.38; lr: 0.00046; 20767/19930 tok/s;  30877 sec;[0m
    [34m[2022-09-27 21:07:30,119 INFO] Step 36900/40000; acc:  55.30; ppl:  6.91; xent: 1.93; lr: 0.00046; 21073/20688 tok/s;  30960 sec;[0m
    [34m[2022-09-27 21:08:52,285 INFO] Step 37000/40000; acc:  54.48; ppl:  7.35; xent: 2.00; lr: 0.00046; 20827/20270 tok/s;  31042 sec;[0m
    [34m[2022-09-27 21:10:21,830 INFO] Step 37100/40000; acc:  55.24; ppl:  7.32; xent: 1.99; lr: 0.00046; 18900/18542 tok/s;  31132 sec;[0m
    [34m[2022-09-27 21:11:51,134 INFO] Step 37200/40000; acc:  53.77; ppl:  7.95; xent: 2.07; lr: 0.00046; 20078/19049 tok/s;  31221 sec;[0m
    [34m[2022-09-27 21:13:12,879 INFO] Step 37300/40000; acc:  50.97; ppl:  8.13; xent: 2.10; lr: 0.00046; 23944/21832 tok/s;  31303 sec;[0m
    [34m[2022-09-27 21:14:33,382 INFO] Step 37400/40000; acc:  51.44; ppl:  8.52; xent: 2.14; lr: 0.00046; 20998/20457 tok/s;  31384 sec;[0m
    [34m[2022-09-27 21:15:58,290 INFO] Step 37500/40000; acc:  55.18; ppl:  7.12; xent: 1.96; lr: 0.00046; 20417/19127 tok/s;  31468 sec;[0m
    [34m[2022-09-27 21:17:23,710 INFO] Step 37600/40000; acc:  52.67; ppl:  7.89; xent: 2.07; lr: 0.00046; 19947/18990 tok/s;  31554 sec;[0m
    [34m[2022-09-27 21:18:49,237 INFO] Step 37700/40000; acc:  53.54; ppl:  7.32; xent: 1.99; lr: 0.00046; 20048/19025 tok/s;  31639 sec;[0m
    [34m[2022-09-27 21:20:14,092 INFO] Step 37800/40000; acc:  53.05; ppl:  7.85; xent: 2.06; lr: 0.00045; 19953/19043 tok/s;  31724 sec;[0m
    [34m[2022-09-27 21:21:48,006 INFO] Step 37900/40000; acc:  66.33; ppl:  4.48; xent: 1.50; lr: 0.00045; 21493/20612 tok/s;  31818 sec;[0m
    [34m[2022-09-27 21:23:17,596 INFO] Step 38000/40000; acc:  64.25; ppl:  4.79; xent: 1.57; lr: 0.00045; 21448/21112 tok/s;  31908 sec;[0m
    [34m[2022-09-27 21:24:43,392 INFO] Step 38100/40000; acc:  53.45; ppl:  7.74; xent: 2.05; lr: 0.00045; 18253/17978 tok/s;  31994 sec;[0m
    [34m[2022-09-27 21:26:08,735 INFO] Step 38200/40000; acc:  54.00; ppl:  7.56; xent: 2.02; lr: 0.00045; 18512/18598 tok/s;  32079 sec;[0m
    [34m[2022-09-27 21:27:34,645 INFO] Step 38300/40000; acc:  53.27; ppl:  7.81; xent: 2.05; lr: 0.00045; 19088/18782 tok/s;  32165 sec;[0m
    [34m[2022-09-27 21:29:01,625 INFO] Step 38400/40000; acc:  55.37; ppl:  7.09; xent: 1.96; lr: 0.00045; 18651/18300 tok/s;  32252 sec;[0m
    [34m[2022-09-27 21:30:27,993 INFO] Step 38500/40000; acc:  53.00; ppl:  7.83; xent: 2.06; lr: 0.00045; 19038/18700 tok/s;  32338 sec;[0m
    [34m[2022-09-27 21:31:52,693 INFO] Step 38600/40000; acc:  53.13; ppl:  7.93; xent: 2.07; lr: 0.00045; 18450/18618 tok/s;  32423 sec;[0m
    [34m[2022-09-27 21:33:18,054 INFO] Step 38700/40000; acc:  50.92; ppl:  8.48; xent: 2.14; lr: 0.00045; 19202/18837 tok/s;  32508 sec;[0m
    [34m[2022-09-27 21:34:43,624 INFO] Step 38800/40000; acc:  53.63; ppl:  7.47; xent: 2.01; lr: 0.00045; 19350/18498 tok/s;  32594 sec;[0m
    [34m[2022-09-27 21:36:05,039 INFO] Step 38900/40000; acc:  51.86; ppl:  8.47; xent: 2.14; lr: 0.00045; 20616/19867 tok/s;  32675 sec;[0m
    [34m[2022-09-27 21:37:26,423 INFO] Step 39000/40000; acc:  50.85; ppl:  8.77; xent: 2.17; lr: 0.00045; 19372/19381 tok/s;  32757 sec;[0m
    [34m[2022-09-27 21:38:50,984 INFO] Step 39100/40000; acc:  50.73; ppl:  8.70; xent: 2.16; lr: 0.00045; 20050/18762 tok/s;  32841 sec;[0m
    [34m[2022-09-27 21:40:19,535 INFO] Step 39200/40000; acc:  53.90; ppl:  7.80; xent: 2.05; lr: 0.00045; 18899/18456 tok/s;  32930 sec;[0m
    [34m[2022-09-27 21:41:45,683 INFO] Step 39300/40000; acc:  56.29; ppl:  6.90; xent: 1.93; lr: 0.00045; 19367/18876 tok/s;  33016 sec;[0m
    [34m[2022-09-27 21:43:11,224 INFO] Step 39400/40000; acc:  53.20; ppl:  7.83; xent: 2.06; lr: 0.00045; 19141/18915 tok/s;  33101 sec;[0m
    [34m[2022-09-27 21:44:39,259 INFO] Step 39500/40000; acc:  58.37; ppl:  6.25; xent: 1.83; lr: 0.00044; 18639/18904 tok/s;  33189 sec;[0m
    [34m[2022-09-27 21:46:06,060 INFO] Step 39600/40000; acc:  58.69; ppl:  6.10; xent: 1.81; lr: 0.00044; 19633/19635 tok/s;  33276 sec;[0m
    [34m[2022-09-27 21:47:31,211 INFO] Step 39700/40000; acc:  58.05; ppl:  6.39; xent: 1.85; lr: 0.00044; 20261/20057 tok/s;  33361 sec;[0m
    [34m[2022-09-27 21:48:56,848 INFO] Step 39800/40000; acc:  63.59; ppl:  4.78; xent: 1.56; lr: 0.00044; 19926/19512 tok/s;  33447 sec;[0m
    [34m[2022-09-27 21:50:22,620 INFO] Step 39900/40000; acc:  56.00; ppl:  6.71; xent: 1.90; lr: 0.00044; 19270/19011 tok/s;  33533 sec;[0m
    [34m[2022-09-27 21:51:49,702 INFO] Step 40000/40000; acc:  68.42; ppl:  3.70; xent: 1.31; lr: 0.00044; 22116/22301 tok/s;  33620 sec;[0m
    [34m[2022-09-27 21:51:49,809 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 21:51:49,813 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 21:51:49,814 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 21:52:10,120 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 21:52:10,120 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 21:52:10,121 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 21:52:30,942 WARNING] The batch will be filled until we reach 1,its size may exceed 32 tokens[0m
    [34m[2022-09-27 21:52:40,044 INFO] Validation perplexity: 8.75019[0m
    [34m[2022-09-27 21:52:40,044 INFO] Validation accuracy: 55.978[0m
    [34m[2022-09-27 21:52:40,087 INFO] Saving checkpoint /opt/ml/model/model.en-ja_step_40000.pt[0m
    
    2022-09-27 21:52:49 Uploading - Uploading generated training model
    2022-09-27 21:58:06 Completed - Training job completed
    Training seconds: 34186
    Billable seconds: 34186



```python

   

```
