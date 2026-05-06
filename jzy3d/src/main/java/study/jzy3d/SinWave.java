package study.jzy3d;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class SinWave {
    public static void main(String[] args) {
        // sin(x) のサンプル点を生成 ([-2π, 2π] を 400分割)
        int n = 400;
        LineStrip line = new LineStrip();
        for (int i = 0; i < n; i++) {
            double x = -2 * Math.PI + 4 * Math.PI * i / (n - 1);
            double y = Math.sin(x);
            line.add(new Point(new Coord3d(x, y, 0))); // z=0 固定で2D
        }
        line.setWireframeColor(Color.BLUE);
        line.setWireframeWidth(2f);

        // チャート作成 → 2Dビューに切り替え
        Chart chart = new EmulGLChartFactory().newChart(Quality.Advanced());
        chart.add(line);
        chart.getAxisLayout().setXAxisLabel("x");
        chart.getAxisLayout().setYAxisLabel("sin(x)");
        chart.view2d();    // ← 2Dビュー
        chart.open();
        chart.addMouse();  // マウスでパン・ズーム可能に
    }
}