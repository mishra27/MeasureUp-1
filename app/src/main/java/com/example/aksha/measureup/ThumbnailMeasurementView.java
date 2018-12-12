package com.example.aksha.measureup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class ThumbnailMeasurementView extends AppCompatImageView {
    private Paint paint;
    private float[] points;
    private float[] matrix = new float[9];

    public ThumbnailMeasurementView(Context context) {
        super(context);
        init();
    }

    public ThumbnailMeasurementView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThumbnailMeasurementView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setARGB(255, 255, 0, 0);
        paint.setStrokeWidth(2);

        points = new float[] {0, 0, 0, 0};
    }

    public void setPoints(float... points) {
        if (points.length < 4) return;

        this.points[0] = points[0];
        this.points[1] = points[1];
        this.points[2] = points[2];
        this.points[3] = points[3];

        this.invalidate();
    }

    public void setPoints(double... points) {
        float[] floatPoints = new float[points.length];

        for (int i = 0; i < points.length; i++) {
            floatPoints[i] = (float) points[i];
        }

        setPoints(floatPoints);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable drawable = this.getDrawable();
        Rect bounds = drawable.getBounds();

        this.getImageMatrix().getValues(matrix);

        float left = matrix[Matrix.MTRANS_X];
        float top = matrix[Matrix.MTRANS_Y];

        float width = bounds.width() * matrix[Matrix.MSCALE_X];
        float height = bounds.height() * matrix[Matrix.MSCALE_Y];

        canvas.drawLine(points[0] * width + left, points[1] * height + top,
                points[2] * width + left, points[3] * height + top, paint);
    }
}
