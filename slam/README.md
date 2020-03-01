# SLAM

[SLAM入門: ロボットの自己位置推定と地図構築の技術](https://www.amazon.co.jp/dp/4274221660/)の内容をScalaで実装してみる。

## 廊下

オドメトリ

<img src="./docs/images/CorridorOdometry.png" width="400px" />

スキャン・マッチング

<img src="./docs/images/CorridorScanMatching.png" width="400px" />

点群を格子に分割して代表点でマッチング

<img src="./docs/images/CorridorGridRepresentativePoint.png" width="400px" />

スキャンを等間隔にする

<img src="./docs/images/CorridorResampling.png" width="400px" />

垂直距離をコスト関数にする

<img src="./docs/images/CorridorPerpendicularDistance.png" width="400px" />


## ホール

オドメトリ

<img src="./docs/images/HallOdometry.png" width="400px" />

スキャン・マッチング

<img src="./docs/images/HallScanMatching.png" width="400px" />

点群を格子に分割して代表点でマッチング

<img src="./docs/images/HallGridRepresentativePoint.png" width="400px" />

スキャンを等間隔にする

<img src="./docs/images/HallResampling.png" width="400px" />

垂直距離をコスト関数にする

<img src="./docs/images/HallPerpendicularDistance.png" width="400px" />


