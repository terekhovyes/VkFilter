package me.alexeyterekhov.vkfilter.GUI.Common;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;

import org.jetbrains.annotations.NotNull;

import me.alexeyterekhov.vkfilter.R;

public class TripleSwitchView extends View {
    private static final float RATIO = 2f;
    private static final float BACK_SIZE = 0.5f;
    private static final float ROUND_SIZE = 0.9f;
    private static final float SPRING_SIZE = 0.15f;
    private static final float BLOCK_SIZE = 0.1f;

    public static final int STATE_MIDDLE = 0;
    public static final int STATE_LEFT = -1;
    public static final int STATE_RIGHT = 1;

    private int state = STATE_MIDDLE;
    private int centerY = 0;
    private int leftX = 0;
    private int centerX = 0;
    private int rightX = 0;
    private float backRadius = 0;
    private float roundRadius = 0;
    private RectF backRect = null;

    private float roundX = 0;
    private float touchDX = 0;
    private float touchX;
    private float touchY;

    private int leftColor = 0xFFEE7A67;
    private int midColor = 0xFF777777;
    private int rightColor = 0xFF009688;

    private Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private int iconRes = 0;
    private Bitmap icon = null;
    private OnSwitchChangeStateListener listener = null;

    private boolean alreadyMeasured = false;

    @NotNull
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle out = new Bundle();
        out.putParcelable("superState", super.onSaveInstanceState());
        out.putInt("state", state);
        out.putInt("leftColor", leftColor);
        out.putInt("midColor", midColor);
        out.putInt("rightColor", rightColor);
        out.putInt("iconRes", iconRes);
        out.putParcelable("icon", icon);
        return out;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable savedState) {
        if (savedState instanceof Bundle) {
            Bundle b = (Bundle) savedState;
            state = b.getInt("state");
            leftColor = b.getInt("leftColor");
            midColor = b.getInt("midColor");
            rightColor = b.getInt("rightColor");
            iconRes = b.getInt("iconRes");
            icon = b.getParcelable("icon");
            super.onRestoreInstanceState(b.getParcelable("superState"));
        } else
            super.onRestoreInstanceState(savedState);
    }

    public TripleSwitchView(Context context) {
        super(context);
        init();
    }

    public TripleSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.TripleSwitchView, 0, 0);
        try {
            setIconRes(a.getResourceId(R.styleable.TripleSwitchView_iconRes, 0));
            setRoundColor(a.getColor(R.styleable.TripleSwitchView_roundColor, 0x007788));
            setRoundColorLeft(a.getColor(R.styleable.TripleSwitchView_roundColorLeft, 0x007788));
            setRoundColorMid(a.getColor(R.styleable.TripleSwitchView_roundColorMid, 0x007788));
            setRoundColorRight(a.getColor(R.styleable.TripleSwitchView_roundColorRight, 0x007788));
            setBackgroundColor(a.getColor(R.styleable.TripleSwitchView_backgroundColor, 0x337788));
        } finally {
            a.recycle();
        }
    }

    private void init() {
        roundPaint.setStyle(Paint.Style.FILL);
        backPaint.setColor(0xFF337788);
        backPaint.setStyle(Paint.Style.FILL);
    }

    public float getRoundX() {
        return roundX;
    }

    public void setRoundX(float roundX) {
        this.roundX = roundX;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(backRect, backRadius, backRadius, backPaint);
        if (roundX >= leftX && roundX <= centerX)
            roundPaint.setColor(blendColors(midColor, leftColor, (roundX - leftX) / (float) (centerX - leftX)));
        else
            roundPaint.setColor(blendColors(midColor, rightColor, (rightX - roundX) / (float) (rightX - centerX)));
        canvas.drawCircle(roundX, centerY, roundRadius, roundPaint);
        if (icon != null)
            canvas.drawBitmap(icon, roundX - roundRadius, getHeight() / 2 - roundRadius, bitmapPaint);
    }

    @Override
    protected void onMeasure(int w, int h) {
        super.onMeasure(w, h);
        w = resolveSizeAndState(getSuggestedMinimumWidth(), w, 1);
        h = resolveSizeAndState(getSuggestedMinimumHeight(), h, 1);
        int width, height;
        if (h * RATIO > w) {
            width = w;
            height = (int)(w / RATIO);
        } else {
            width = (int)(h * RATIO);
            height = h;
        }
        setMeasuredDimension(width, height);
        recountValues(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean upOrCancel = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                touchDX = event.getX() - roundX;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - touchX) > getHeight() * BLOCK_SIZE) {
                    ViewParent parent = getParent();
                    if (parent != null)
                        parent.requestDisallowInterceptTouchEvent(true);
                }
                if (state == STATE_MIDDLE) {
                    roundX = event.getX() - touchDX;
                    if (roundX < leftX)
                        roundX = leftX;
                    else if (roundX > rightX)
                        roundX = rightX;
                } else {
                    roundX = event.getX() - touchDX;
                    if (roundX > rightX)
                        roundX = rightX;
                    if (roundX < leftX)
                        roundX = leftX;
                    if (state * roundX < centerX * state) {
                        float maxDx = getWidth() * SPRING_SIZE;
                        float dx = state * (centerX - roundX);
                        roundX = centerX - state * maxDx * dx / (getWidth() + dx);
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                upOrCancel = true;
                break;
            case MotionEvent.ACTION_UP:
                upOrCancel = true;
                break;
        }
        if (upOrCancel) {
            ViewParent parent2 = getParent();
            if (parent2 != null)
                parent2.requestDisallowInterceptTouchEvent(false);
            float toLeft = roundX - leftX;
            float toRight = rightX - roundX;
            float toMiddle = Math.abs(centerX - roundX);
            float aimX;
            if (toLeft < toMiddle) {
                aimX = leftX;
                if (state != STATE_LEFT) {
                    state = STATE_LEFT;
                    if (listener != null)
                        listener.onChangeState(STATE_LEFT);
                }
            } else if (toRight < toMiddle) {
                aimX = rightX;
                if (state != STATE_RIGHT) {
                    state = STATE_RIGHT;
                    if (listener != null)
                        listener.onChangeState(STATE_RIGHT);
                }
            } else {
                aimX = centerX;
                if (state != STATE_MIDDLE) {
                    state = STATE_MIDDLE;
                    if (listener != null)
                        listener.onChangeState(STATE_MIDDLE);
                }
            }
            animateMovingTo((int) aimX);
        }
        return true;
    }

    private void animateMovingTo(int x) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "roundX", roundX, x);
        animator.setDuration(100);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        invalidate();
    }

    private Bitmap loadIcon(int roundRadius) {
        if (iconRes == 0)
            return null;
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), iconRes);
        if (bitmap == null)
            return null;
        if (bitmap.getWidth() > bitmap.getHeight())
            return Bitmap.createScaledBitmap(
                    bitmap,
                    roundRadius * 2,
                    (int) ((float) bitmap.getHeight() / (float) bitmap.getWidth() * roundRadius * 2),
                    true);
        else
            return Bitmap.createScaledBitmap(
                    bitmap,
                    (int) ((float) bitmap.getWidth() / (float) bitmap.getHeight() * roundRadius * 2),
                    roundRadius * 2,
                    true);
    }

    private void recountValues(int width, int height) {
        alreadyMeasured = true;
        roundRadius = height * ROUND_SIZE / 2;
        centerY = height / 2;
        roundX = (int) (width * (0.5f + 0.25f * state));
        leftX = (int) (0.5f * height);
        centerX = width / 2;
        rightX = (int) (width - 0.5f * height);
        backRadius = height * BACK_SIZE / 2;
        float backTop = height * (1 - BACK_SIZE) / 2;
        float backBottom = height * (1 - BACK_SIZE / 2);
        backRect = new RectF(leftX - backRadius, backTop, rightX + backRadius, backBottom);
        if (iconRes != 0 && width > 0 && height > 0)
            icon = loadIcon((int) roundRadius);
        float shadow = height * (1 - ROUND_SIZE) / 2;
        setLayerType(LAYER_TYPE_SOFTWARE, roundPaint);
        roundPaint.setShadowLayer(shadow * 2 / 3, 0, shadow / 3, 0xFF555555);
        invalidate();
    }

    private int multiplyHex(int value, float multiplier, int mask, int offset) {
        int withOffset = (value & mask) >>> (offset * 8);
        int result = (int) (withOffset * multiplier);
        return result << (offset * 8);
    }

    private int blendColors(int c1, int c2, float ratio) {
        int alpha = multiplyHex(c1, ratio, 0xFF000000, 3) + multiplyHex(c2, 1 - ratio, 0xFF000000, 3);
        int r = multiplyHex(c1, ratio, 0xFF0000, 2) + multiplyHex(c2, 1 - ratio, 0xFF0000, 2);
        int g = multiplyHex(c1, ratio, 0x00FF00, 1) + multiplyHex(c2, 1 - ratio, 0x00FF00, 1);
        int b = multiplyHex(c1, ratio, 0x0000FF, 0) + multiplyHex(c2, 1 - ratio, 0x0000FF, 0);
        return alpha + r + g + b;
    }

    public void setIconRes(int res) {
        iconRes = res;
        if (alreadyMeasured)
            measure(getWidth(), getHeight());
    }

    public void setListener(OnSwitchChangeStateListener l) {
        listener = l;
    }

    public void setStateWithListener(int state, boolean animation) {
        setStateWithoutListener(state, animation);
        if (listener != null)
            listener.onChangeState(state);
    }

    public void setStateWithoutListener(int state, boolean animation) {
        this.state = state;
        switch (state) {
            case STATE_LEFT:
                if (animation)
                    animateMovingTo(leftX);
                else {
                    roundX = leftX;
                    invalidate();
                }
                break;
            case STATE_MIDDLE:
                if (animation)
                    animateMovingTo(centerX);
                else {
                    roundX = centerX;
                    invalidate();
                }
                break;
            case STATE_RIGHT:
                if (animation)
                    animateMovingTo(rightX);
                else {
                    roundX = rightX;
                    invalidate();
                }
                break;
        }
    }

    public void setRoundColor(int color) {
        setRoundColorLeft(color);
        setRoundColorMid(color);
        setRoundColorRight(color);
    }

    public void setRoundColorLeft(int color) {
        leftColor = handleColor(color);
        invalidate();
    }

    public void setRoundColorMid(int color) {
        midColor = handleColor(color);
        invalidate();
    }

    public void setRoundColorRight(int color) {
        rightColor = handleColor(color);
        invalidate();
    }

    public void setBackgroundColor(int color) {
        backPaint.setColor(handleColor(color));
        invalidate();
    }

    private int handleColor(int color) {
        if (color == 0x00000000)
            return color;
        if ((color & 0xFF000000) == 0x00000000)
            return color | 0xFF000000;
        return color;
    }

    public interface OnSwitchChangeStateListener {
        public void onChangeState(int newState);
    }
}
