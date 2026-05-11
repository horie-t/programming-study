package study.computationalphysics;

import processing.core.PApplet;

public class SimpleHarmonicMotion extends PApplet {

    private static final float AMPLITUDE = 150f;
    private static final float ANGULAR_FREQUENCY = 2f;

    @Override
    public void settings() {
        size(800, 400);
    }

    @Override
    public void setup() {
        frameRate(60);
    }

    @Override
    public void draw() {
        background(240);

        float centerY = height / 2f;
        float t = millis() / 1000f;
        float x = width / 2f + AMPLITUDE * cos(ANGULAR_FREQUENCY * t);

        stroke(180);
        line(0, centerY, width, centerY);

        stroke(50);
        line(width / 2f, centerY, x, centerY);

        noStroke();
        fill(220, 60, 60);
        ellipse(x, centerY, 40, 40);
    }

    public static void main(String[] args) {
        PApplet.main(SimpleHarmonicMotion.class.getName());
    }
}
