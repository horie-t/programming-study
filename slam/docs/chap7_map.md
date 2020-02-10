# 7章 スキャンマッチング

LittleSLAMでのスキャンマッチングを行った場合のシーケンスは以下の通り。

![シーケンス](sequence_chap7.png)

ポイントは、

1. 参照スキャンと現在のスキャンと対応をとる(findCorrespondence)
2. 対応に合わせて、ロボットの位置・姿勢を推定(optimizePose)
3. 推定された位置、姿勢による地図を作成(growMap)

となるところ。
