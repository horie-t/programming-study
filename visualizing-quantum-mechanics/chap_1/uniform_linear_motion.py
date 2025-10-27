# 等速直線運動を可視化するプログラム
from vpython import *

# シーンの設定
scene = canvas(title="等速直線運動の可視化", width=800, height=400)
scene.background = color.white
scene.center = vector(0, 0, 0)
scene.range = 10
scene.forward = vector(-1, -0.3, -1)
scene.up = vector(0, 1, 0)
scene.caption = "等速直線運動のシミュレーション\n"
scene.caption += "青い球が一定速度で直線上を移動します。\n"
scene.caption += "速度ベクトルは赤い矢印で示されています。\n"
scene.caption += "時間の経過とともに位置が更新されます。\n"
scene.caption += "プログラムを終了するには、ウィンドウを閉じてください。\n"
# 球の初期位置と速度
ball = sphere(pos=vector(-8, 0, 0), radius=0.5, color=color.blue, make_trail=True)
velocity = vector(2, 0, 0)  # 速度ベクトル
# 速度ベクトルを示す矢印
velocity_arrow = arrow(pos=ball.pos, axis=velocity.norm()*2, color=color.red)
# 時間の設定
dt = 0.01  # タイムステップ
t = 0  # 初期時間
# メインループ
while True:
    rate(100)  # 1秒間に100回の更新
    # 位置の更新
    ball.pos = ball.pos + velocity * dt
    # 速度ベクトル矢印の位置更新
    velocity_arrow.pos = ball.pos
    t += dt
    # 画面外に出たら位置をリセット
    if ball.pos.x > 10:
        ball.pos = vector(-8, 0, 0)
        t = 0
        ball.clear_trail()
