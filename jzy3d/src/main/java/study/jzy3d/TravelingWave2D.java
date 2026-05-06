package study.jzy3d;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.EmulGLChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class TravelingWave2D {
    public static void main(String[] args) {
        final int n = 400;
        final double xMin = -2 * Math.PI;
        final double xMax =  2 * Math.PI;
        final double omega = 2.0;   // 角周波数 ω [rad/s]

        // 初期波形を構築
        LineStrip line = new LineStrip();
        for (int i = 0; i < n; i++) {
            double x = xMin + (xMax - xMin) * i / (n - 1);
            line.add(new Point(new Coord3d(x, Math.sin(x), 0)));
        }
        line.setWireframeColor(Color.BLUE);
        line.setWireframeWidth(2f);

        // チャート作成
        Chart chart = new EmulGLChartFactory().newChart(Quality.Advanced());
        chart.add(line);
        chart.getAxisLayout().setXAxisLabel("x");
        chart.getAxisLayout().setYAxisLabel("sin(x - ωt)");

        // 軸範囲を固定(自動スケールによる軸のジッタを防ぐ)
        chart.getView().setBoundManual(new BoundingBox3d(
                (float) xMin, (float) xMax, -1.2f, 1.2f, -0.1f, 0.1f));
        chart.view2d();
        chart.open();
        chart.addMouse();

        // アニメーションスレッド
        Thread anim = new Thread(() -> {
            long t0 = System.currentTimeMillis();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    double t = (System.currentTimeMillis() - t0) / 1000.0;
                    double phase = omega * t;

                    // 各点のy座標だけを位相ずらしで更新(in-place)
                    for (int i = 0; i < n; i++) {
                        double x = xMin + (xMax - xMin) * i / (n - 1);
                        double y = Math.sin(x - phase);
                        line.getPoints().get(i).xyz = new Coord3d(x, y, 0);
                    }
                    chart.getView().shoot();   // 再描画を要求
                    Thread.sleep(16);          // 約60 FPS
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "wave-animator");
        anim.setDaemon(true);   // ウィンドウを閉じればJVMが終了するように
        anim.start();
    }
}