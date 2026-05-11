package study.computationalphysics;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class DampedOscillationPlot {

    public static void main(String[] args) {
        double alpha = 0.3;
        double omega = 2.0 * Math.PI;
        double tMax = 8.0;
        int n = 800;

        LineStrip line = new LineStrip();
        for (int i = 0; i < n; i++) {
            double t = tMax * i / (n - 1);
            double y = Math.exp(-alpha * t) * Math.cos(omega * t);
            line.add(new Point(new Coord3d(t, y, 0)));
        }
        line.setWireframeColor(Color.RED);
        line.setWireframeWidth(2f);

        Chart chart = new EmulGLChartFactory().newChart(Quality.Advanced());
        chart.add(line);
        chart.getAxisLayout().setXAxisLabel("t");
        chart.getAxisLayout().setYAxisLabel("y = exp(-alpha*t) cos(omega*t)");
        chart.view2d();
        chart.open();
        chart.addMouse();
    }
}
