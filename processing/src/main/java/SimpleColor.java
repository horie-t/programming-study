import processing.core.PApplet;

public class SimpleColor extends PApplet {

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void draw() {
        background(255);
        noStroke();

        fill(255, 0, 0);
        ellipse(20, 20, 16, 16);

        fill(127, 0, 0);
        ellipse(40, 20, 16, 16);

        fill(255, 200, 200);
        ellipse(60, 20, 16, 16);
    }

    public static void main(String[] args) {
        PApplet.main("SimpleColor");
    }
}
