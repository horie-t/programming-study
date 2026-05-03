import processing.core.PApplet;

public class DrawEllipse extends PApplet {

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void draw() {
        if (mousePressed) {
            fill(0);
        } else {
            fill(255);
        }
        ellipse(mouseX, mouseY, 80, 80);
    }

    public static void main(String[] args) {
        PApplet.main("DrawEllipse");
    }

    @Override
    public String toString() {
        return "DrawEllipse";
    }
}
