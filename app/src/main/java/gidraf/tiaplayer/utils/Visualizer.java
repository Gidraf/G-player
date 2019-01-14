package gidraf.tiaplayer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.chibde.BaseVisualizer;
import com.chibde.visualizer.CircleBarVisualizer;

import gidraf.tiaplayer.R;

public class Visualizer extends BaseVisualizer {
    private float[] points;
    private Paint circlePaint;
    private int radius;

    public Visualizer(Context context) {
        super(context);
    }

    public Visualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Visualizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint = new Paint();
        radius = -1;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        if (radius == -1) {
            radius = getHeight() < getWidth() ? getHeight() : getWidth();
            radius = (int) (radius * 0.65 / 2);
            double circumference = 2 * Math.PI * radius;
            paint.setStrokeWidth((float) (circumference / 100));
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeWidth(4);
        }
        circlePaint.setColor(R.color.red_one);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, circlePaint);
        if (bytes != null) {
            if (points == null || points.length < bytes.length * 4) {
                points = new float[bytes.length * 4];
            }
            double angle = 0;

            for (int i = 0; i < 120; i++, angle += 3) {
                int x = (int) Math.ceil(i * 8.5);
                int t = ((byte) (-Math.abs(bytes[x]) + 350)) * (getHeight() / 4) / 350;

                points[i * 4] = (float) (getWidth() / 2
                        + radius
                        * Math.cos(Math.toRadians(angle)));

                points[i * 4 + 1] = (float) (getHeight() / 2
                        + radius
                        * Math.sin(Math.toRadians(angle)));

                points[i * 4 + 2] = (float) (getWidth() / 2
                        + (radius + t)
                        * Math.cos(Math.toRadians(angle)));

                points[i * 4 + 3] = (float) (getHeight() / 2
                        + (radius + t)
                        * Math.sin(Math.toRadians(angle)));
            }

            canvas.drawLines(points, paint);
        }
        super.onDraw(canvas);
    }
}
