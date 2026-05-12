package study.computationalphysics.chap4;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.SwingChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class SinCurve {

    public static void main(String[] args) {
        LineStrip line = new LineStrip();
        for (double x = 0.0; x <= 2 * Math.PI; x += 0.1) {
            line.add(new Point(new Coord3d(x, Math.sin(x), 0)));
        }
        line.setWireframeColor(Color.RED);
        line.setWireframeWidth(2f);

        Chart chart = new SwingChartFactory().newChart(Quality.Advanced());
        chart.add(line);
        chart.getAxisLayout().setXAxisLabel("x");
        chart.getAxisLayout().setYAxisLabel("y = sin(x)");
        chart.view2d();
        chart.open();
        chart.addMouse();
    }
}
