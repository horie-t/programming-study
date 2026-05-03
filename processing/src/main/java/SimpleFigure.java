import processing.core.PApplet;

public class SimpleFigure extends PApplet {

    public void settings() {
        size(800, 600);
    }

    @Override
    public void draw() {
        background(255);

        // 800x600 を 4x2 のセル（各 200x300）に分割し、各図形を1セルずつに配置
        // 上段
        point(100, 150);
        line(250, 100, 350, 200);
        rectMode(CENTER);
        rect(500, 150, 100, 100);
        ellipse(700, 150, 100, 100);

        // 下段
        triangle(50, 525, 150, 525, 100, 375);
        quad(240, 380, 360, 380, 380, 520, 220, 520);
        arc(500, 450, 120, 120, 0, PI);
        bezier(620, 550, 680, 350, 720, 350, 780, 550);
    }

    public static void main(String[] args) {
        PApplet.main("SimpleFigure");
    }
}
