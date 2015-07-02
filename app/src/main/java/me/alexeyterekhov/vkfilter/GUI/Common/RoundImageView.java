package me.alexeyterekhov.vkfilter.GUI.Common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import me.alexeyterekhov.vkfilter.R;

public class RoundImageView extends ImageView {
    private final int OFFSET_PX = 1;
    private float shadowSizePx = 0;
    private Bitmap image = null;
    private Paint backPaint = new Paint();
    private Paint imagePaint = new Paint();
    private int canvasSize;

    public RoundImageView(final Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        backPaint.setAntiAlias(true);
        imagePaint.setAntiAlias(true);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView, defStyle, 0);
        int backgroundColor = attributes.getColor(R.styleable.RoundImageView_roundBackgroundColor, 0xffffff);
        backPaint.setColor(backgroundColor | 0xFF000000);
        shadowSizePx = attributes.getDimensionPixelSize(R.styleable.RoundImageView_shadowSize, 0);
        attributes.recycle();

        setLayerType(View.LAYER_TYPE_SOFTWARE, backPaint);
        backPaint.setShadowLayer(shadowSizePx * 2 / 3f, 0, shadowSizePx / 3f, 0xff555555);
    }

    public void setRoundColor(int color) {
        backPaint.setColor(color);
        invalidate();
    }

    public void setRoundColorRes(int colorRes) {
        int color = getContext().getResources().getColor(colorRes);
        setRoundColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap image = getBitmap();

        canvasSize = Math.min(getWidth(), getHeight());
        float areaSize = Math.min(getWidth(), getHeight()) - OFFSET_PX * 2;
        float circleSize = areaSize - shadowSizePx * 2;

        canvas.drawCircle(
                getWidth() / 2f,
                getHeight() / 2f,
                circleSize / 2f,
                backPaint
        );

        if (image != null) {
            Matrix m = new Matrix();
            m.setTranslate(shadowSizePx, shadowSizePx);
            BitmapShader shader = new BitmapShader(
                    image,
                    Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP
            );
            shader.setLocalMatrix(m);
            imagePaint.setShader(shader);
            imagePaint.setColor(0xff004499);
            canvas.drawCircle(
                    getWidth() / 2f,
                    getHeight() / 2f,
                    circleSize / 2f,
                    imagePaint
            );
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // The parent has not imposed any constraint on the child.
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize;
        }

        return (result + 2);
    }

    private Bitmap getBitmap() {
        Drawable d = getDrawable();

        if (d == null) {
            return null;
        } else if (d instanceof BitmapDrawable) {
            return cropBitmap(((BitmapDrawable) d).getBitmap(), getWidth() - shadowSizePx * 2);
        } else if (d.getIntrinsicWidth() == -1) {
            Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            d.draw(new Canvas(b));
            return b;
        } else {
            Bitmap b = Bitmap.createBitmap(
                    d.getIntrinsicWidth(),
                    d.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            d.draw(new Canvas(b));
            return cropBitmap(b, getWidth() - shadowSizePx * 2);
        }
    }

    private Bitmap cropBitmap(Bitmap src, float size) {
        float coefficient = Math.min(src.getWidth(), src.getHeight()) / size;
        Bitmap decreasedBitmap = Bitmap.createScaledBitmap(
                src,
                (int) (src.getWidth() / coefficient),
                (int) (src.getHeight() / coefficient),
                false
        );
        if (decreasedBitmap.getWidth() > decreasedBitmap.getHeight()) {
            return Bitmap.createBitmap(
                    decreasedBitmap,
                    decreasedBitmap.getWidth() / 2 - decreasedBitmap.getHeight() / 2,
                    0,
                    decreasedBitmap.getHeight(),
                    decreasedBitmap.getHeight()
            );
        } else if (decreasedBitmap.getWidth() < decreasedBitmap.getHeight()){
            return Bitmap.createBitmap(
                    decreasedBitmap,
                    decreasedBitmap.getHeight() / 2 - decreasedBitmap.getWidth() / 2,
                    0,
                    decreasedBitmap.getWidth(),
                    decreasedBitmap.getWidth()
            );
        } else return decreasedBitmap;
    }
}
