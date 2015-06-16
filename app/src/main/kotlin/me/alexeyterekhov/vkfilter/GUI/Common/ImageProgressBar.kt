package me.alexeyterekhov.vkfilter.GUI.Common

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import me.alexeyterekhov.vkfilter.R

public class ImageProgressBar: ImageView {
    private val PADDING_PIXELS = 3
    private val PROGRESS_SIZE_PERCENTS = 10

    private val paintProgress = Paint()
    private val paintImage = Paint()
    private val paintCross = Paint()

    private var valueMaxProgress = 100
    private var valueCurProgress = 0
    private var colorProgress = 0x009688
    private var colorButton = 0xf0f0f0
    private var closeListener: View.OnClickListener? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ImageProgressBar, 0, 0)
        try {
            colorProgress = a.getColor(R.styleable.ImageProgressBar_progressColor, 0x009688)
            colorButton = a.getColor(R.styleable.ImageProgressBar_buttonColor, 0xf0f0f0)
        } finally {
            a.recycle()
        }

        setup()
    }
    constructor(context: Context) : super(context) {
        setup()
    }

    fun setMaxProgress(value: Int) {
        valueMaxProgress = value
        invalidate()
    }

    fun setCurrentProgress(value: Int) {
        valueCurProgress = value
        invalidate()
    }

    fun setOnCloseListener(l: View.OnClickListener?) {
        closeListener = l
    }

    override fun onSaveInstanceState(): Parcelable {
        val out = Bundle()
        with (out) {
            putParcelable("superState", super.onSaveInstanceState())
            putInt("valueMaxProgress", valueMaxProgress)
            putInt("valueCurProgress", valueCurProgress)
            putInt("colorProgress", colorProgress)
            putInt("colorButton", colorButton)
        }
        return out
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            valueMaxProgress = state.getInt("valueMaxProgress")
            valueCurProgress = state.getInt("valueCurProgress")
            colorProgress = state.getInt("colorProgress")
            colorButton = state.getInt("colorButton")
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else
            super.onRestoreInstanceState(state)
    }

    private fun setup() {
        paintProgress.setColor(colorProgress)
        paintProgress.setAlpha(0xff)
        paintProgress.setStyle(Paint.Style.FILL)
        paintProgress.setAntiAlias(true)

        paintImage.setColor(0xf0f0f0.toInt())
        paintImage.setAlpha(0xff)
        paintImage.setStyle(Paint.Style.FILL)
        paintImage.setAntiAlias(true)

        paintCross.setColor(colorButton)
        paintCross.setAlpha(0xff)
        paintCross.setStyle(Paint.Style.FILL)
        paintCross.setAntiAlias(true)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                return if (isInCloseZone(event.getX(), event.getY()))
                    true
                else
                    super.onTouchEvent(event)
            }
            MotionEvent.ACTION_UP -> {
                return if (isInCloseZone(event.getX(), event.getY())) {
                    closeListener?.onClick(this)
                    true
                } else
                    super.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(w: Int, h: Int) {
        super.onMeasure(w, h)
        val width = View.resolveSizeAndState(getSuggestedMinimumWidth(), w, 1)
        val height = View.resolveSizeAndState(getSuggestedMinimumWidth(), h, 1)

        if (width > height)
            setMeasuredDimension(height, height)
        else
            setMeasuredDimension(width, width)
    }

    override fun onDraw(canvas: Canvas) {
        drawProgress(canvas)
        drawImage(canvas)
        drawButton(canvas)
    }

    private fun decreaseAndCropBitmap(src: Bitmap, size: Float): Bitmap {
        val coefficient = Math.min(src.getWidth(), src.getHeight()) / size
        val decreasedBitmap = Bitmap.createScaledBitmap(
                src,
                (src.getWidth() / coefficient).toInt(),
                (src.getHeight() / coefficient).toInt(),
                false
        )
        val croppedBitmap = if (decreasedBitmap.getWidth() > decreasedBitmap.getHeight()) {
            Bitmap.createBitmap(
                    decreasedBitmap,
                    decreasedBitmap.getWidth() / 2 - decreasedBitmap.getHeight() / 2,
                    0,
                    decreasedBitmap.getHeight(),
                    decreasedBitmap.getHeight()
            )
        } else {
            Bitmap.createBitmap(
                    decreasedBitmap,
                    0,
                    decreasedBitmap.getHeight() / 2 - decreasedBitmap.getWidth() / 2,
                    decreasedBitmap.getWidth(),
                    decreasedBitmap.getWidth()
            )
        }
        return croppedBitmap
    }

    private fun drawProgress(canvas: Canvas) {
        canvas.drawArc(
                PADDING_PIXELS.toFloat(),
                PADDING_PIXELS.toFloat(),
                getWidth() - PADDING_PIXELS.toFloat(),
                getHeight() - PADDING_PIXELS.toFloat(),
                315f,
                computeSweepAngle(),
                true,
                paintProgress
        )
    }
    private fun drawImageStub(canvas: Canvas) {
        canvas.drawCircle(
                computeCenterX(),
                computeCenterY(),
                computeImageRadius(),
                paintImage
        )
    }
    private fun drawImage(canvas: Canvas) {
        val bitmap = getBitmap()
        if (bitmap == null)
            drawImageStub(canvas)
        else {
            val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paintImage.setShader(shader)
            canvas.drawCircle(
                    computeCenterX(),
                    computeCenterY(),
                    computeImageRadius(),
                    paintImage
            )
        }
    }
    private fun drawButton(canvas: Canvas) {
        val butX = computeButtonCenterX()
        val butY = computeButtonCenterY()
        val butRad = computeButtonRadius()
        canvas.drawCircle(
                butX,
                butY,
                butRad,
                paintProgress
        )
        canvas.rotate(-45f, butX, butY)
        canvas.drawRect(
                butX - butRad * 0.1f,
                butY - butRad * 0.6f,
                butX + butRad * 0.1f,
                butY + butRad * 0.6f,
                paintCross
        )
        canvas.rotate(90f, butX, butY)
        canvas.drawRect(
                butX - butRad * 0.1f,
                butY - butRad * 0.6f,
                butX + butRad * 0.1f,
                butY + butRad * 0.6f,
                paintCross
        )
        canvas.restore()
    }

    private fun computeCenterX() = getWidth() / 2f
    private fun computeCenterY() = getHeight() / 2f
    private fun computeProgressRadius() = (Math.min(getWidth(), getHeight())) / 2f - PADDING_PIXELS
    private fun computeImageRadius() = (
            computeProgressRadius()
                    * (100 - PROGRESS_SIZE_PERCENTS)
                    / 100f
            )
    private fun computeSweepAngle() = 360f * valueCurProgress / valueMaxProgress
    private fun computeButtonRadius() = (
            ((Math.sqrt(2.0) * computeProgressRadius() - computeImageRadius())
                    / (Math.sqrt(2.0) + 1)).toFloat()
            )
    private fun computeButtonCenterX() = getWidth() - PADDING_PIXELS - computeButtonRadius()
    private fun computeButtonCenterY() = PADDING_PIXELS + computeButtonRadius()

    private fun getBitmap(): Bitmap? {
        val drawable = getDrawable()
        if (drawable == null)
            return null
        if (drawable is BitmapDrawable)
            return decreaseAndCropBitmap(drawable.getBitmap(), getWidth().toFloat())

        val bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return decreaseAndCropBitmap(bitmap, getWidth().toFloat())
    }

    private fun isInCloseZone(x: Float, y: Float) = x > getWidth() * 2 / 3 && y < getHeight() / 3
}