from vpython import canvas, sphere, color, vector

scene = canvas(title="Quantum Mechanics Visualization")
s = sphere(pos=vector(0,0,0), radius=1, color=color.blue)

def main():
    print("Hello from visualizing-quantum-mechanics!")


if __name__ == "__main__":
    main()
