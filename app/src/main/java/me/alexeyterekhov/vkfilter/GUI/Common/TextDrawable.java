package me.alexeyterekhov.vkfilter.GUI.Common;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class TextDrawable extends Drawable {

    private final String text;
    private final Paint fontPainter;
    private final Paint backgroundPainter;
    private float toDPCoefficient = 1;

    public TextDrawable(String text, float densityFromResources) {
        this.text = text;
        toDPCoefficient = densityFromResources;

        this.fontPainter = new Paint();
        fontPainter.setTextSize(14 * toDPCoefficient);
        fontPainter.setTextAlign(Paint.Align.CENTER);
        fontPainter.setAntiAlias(true);
        fontPainter.setStyle(Paint.Style.FILL);

        backgroundPainter = new Paint();
        backgroundPainter.setStyle(Paint.Style.FILL);
    }

    public void setTextColor(int color) {
        fontPainter.setColor(color);
    }

    public void setBackgroundColor(int color) {
        backgroundPainter.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPainter);
        canvas.drawText(text, canvas.getWidth() / 2, 20 * toDPCoefficient, fontPainter);
    }

    @Override
    public void setAlpha(int alpha) {
        fontPainter.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        fontPainter.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
