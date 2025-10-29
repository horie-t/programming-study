# Exercise 2 in Chapter 2
# 体重50kgの人を高さ500mに持ち上げたときの位置エネルギーを求めるプログラムを作成せよ。
#             pass

def cal_position_energy(weight, height):
    """体重 (kg) と高さ (m) から位置エネルギー (J) を計算する関数"""
    g = 9.8  # 重力加速度 (m/s^2)
    potential_energy = weight * g * height  # 位置エネルギーの計算 (J)
    return potential_energy

def cal_velocity(energy, mass):
    """位置エネルギー (J) がすべて運動エネルギーに変換されたときの速度 (m/s) を計算する関数"""
    velocity = (2 * energy / mass) ** 0.5  # 速度の計算 (m/s)
    return velocity


def main():
    print("位置エネルギー: ", cal_position_energy(50, 500), "J")
    print("落下後の速度: ", cal_velocity(cal_position_energy(50, 500), 50), "m/s")

if __name__ == "__main__":
    main()