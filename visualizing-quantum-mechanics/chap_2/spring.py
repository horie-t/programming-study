# バネのシミュレーション


from vpython import *

# シーンの設定
scene = canvas(title="バネのシミュレーション", width=800, height=400)
scene.background = color.white
scene.center = vector(0, 0, 0)
scene.range = 10
scene.forward = vector(-1, -0.3, -1)
scene.up = vector(0, 1, 0)
scene.caption = "バネのシミュレーション\n"
scene.caption += "質量がバネに取り付けられ、バネの伸縮運動を示します。\n"
scene.caption += "バネの力はフックの法則に従います。\n"
scene.caption += "時間の経過とともに位置が更新されます。\n"
scene.caption += "プログラムを終了するには、ウィンドウを閉じてください。\n"

# バネ定数、質量、初期位置の設定
k = 5.0  # バネ定数 (N/m)
mass_value = 1.0  # 質量 (kg)
initial_position = vector(5, 0, 0)  # 初期位置

# 質量を表す球とバネの作成
mass = sphere(pos=initial_position, radius=0.5, color=color.blue, make_trail=True)
spring = helix(pos=vector(0, 0, 0), axis=mass.pos, radius=0.2, coils=15, thickness=0.1, color=color.green)
mass.mass = mass_value
mass.velocity = vector(0, 0, 0)  # 初期速度

# 時間の設定
dt = 0.01  # タイムステップ
t = 0      # 初期時間
omega = (k / mass.mass) ** 0.5  # 角周波数
amplitude = mag(initial_position)  # 振幅
period = 2 * pi / omega  # 周期
num_cycles = 5  # シミュレーションする周期数
total_time = num_cycles * period  # 総シミュレーション時間
time_elapsed = 0  # 経過時間

# グラフの設定（時間に対する位置・速度・エネルギー）
# 位置と速度のグラフ
traj_graph = graph(title="位置と速度 vs 時間", xtitle="t [s]", ytitle="x [m], v [m/s]", width=800, height=250, background=color.white, foreground=color.black, legend=True)
pos_curve = gcurve(graph=traj_graph, color=color.blue, label="x(t)")
vel_curve = gcurve(graph=traj_graph, color=color.red, label="v(t)")

# エネルギーのグラフ（運動エネルギー、位置エネルギー、ハミルトニアン）
energy_graph = graph(title="エネルギー vs 時間", xtitle="t [s]", ytitle="E [J]", width=800, height=250, background=color.white, foreground=color.black, legend=True)
ke_curve = gcurve(graph=energy_graph, color=color.green, label="運動エネルギー T")
pe_curve = gcurve(graph=energy_graph, color=color.orange, label="位置エネルギー V")
h_curve = gcurve(graph=energy_graph, color=color.black, label="ハミルトニアン H=T+V")

# メインループ
while time_elapsed < total_time:
    rate(100)  # 1秒間に100回の更新

    # バネの力の計算 (フックの法則)
    spring_force = -k * (mag(mass.pos) - 0) * norm(mass.pos)

    # 運動方程式に基づく加速度の計算
    acceleration = spring_force / mass.mass

    # 速度と位置の更新
    mass.velocity += acceleration * dt
    mass.pos += mass.velocity * dt
    spring.axis = mass.pos  # バネの位置更新
    time_elapsed += dt  # 経過時間の更新

    # グラフに描画
    x = mass.pos.x
    v = mass.velocity.x
    T = 0.5 * mass.mass * mag(mass.velocity)**2
    V = 0.5 * k * mag(mass.pos)**2
    H = T + V
    pos_curve.plot(pos=(time_elapsed, x))
    vel_curve.plot(pos=(time_elapsed, v))
    ke_curve.plot(pos=(time_elapsed, T))
    pe_curve.plot(pos=(time_elapsed, V))
    h_curve.plot(pos=(time_elapsed, H))



