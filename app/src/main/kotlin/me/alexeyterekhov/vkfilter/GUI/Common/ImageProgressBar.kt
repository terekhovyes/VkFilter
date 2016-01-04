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
        val a = context.theme.obtainStyledAttributes(
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
        paintProgress.color = colorProgress
        paintProgress.alpha = 0xff
        paintProgress.style = Paint.Style.FILL
        paintProgress.isAntiAlias = true

        paintImage.color = 0xf0f0f0.toInt()
        paintImage.alpha = 0xff
        paintImage.style = Paint.Style.FILL
        paintImage.isAntiAlias = true

        paintCross.color = colorButton
        paintCross.alpha = 0xff
        paintCross.style = Paint.Style.FILL
        paintCross.isAntiAlias = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return if (isInCloseZone(event.x, event.y))
                    true
                else
                    super.onTouchEvent(event)
            }
            MotionEvent.ACTION_UP -> {
                return if (isInCloseZone(event.x, event.y)) {
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
        val width = View.resolveSizeAndState(suggestedMinimumWidth, w, 1)
        val height = View.resolveSizeAndState(suggestedMinimumWidth, h, 1)

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
        val coefficient = Math.min(src.width, src.height) / size
        if (coefficient < 1)
            return src
        val decreasedBitmap = Bitmap.createScaledBitmap(
                src,
                (src.width / coefficient).toInt(),
                (src.height / coefficient).toInt(),
                false
        )
        val croppedBitmap = if (decreasedBitmap.width > decreasedBitmap.height) {
            Bitmap.createBitmap(
                    decreasedBitmap,
                    decreasedBitmap.width / 2 - decreasedBitmap.height / 2,
                    0,
                    decreasedBitmap.height,
                    decreasedBitmap.height
            )
        } else {
            Bitmap.createBitmap(
                    decreasedBitmap,
                    0,
                    decreasedBitmap.height / 2 - decreasedBitmap.width / 2,
                    decreasedBitmap.width,
                    decreasedBitmap.width
            )
        }
        return croppedBitmap
    }

    private fun drawProgress(canvas: Canvas) {
        canvas.drawArc(
                RectF(
                    PADDING_PIXELS.toFloat(),
                    PADDING_PIXELS.toFloat(),
                    width - PADDING_PIXELS.toFloat(),
                    height - PADDING_PIXELS.toFloat()
                ),
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

    private fun computeCenterX() = width / 2f
    private fun computeCenterY() = height / 2f
    private fun computeProgressRadius() = (Math.min(width, height)) / 2f - PADDING_PIXELS
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
    private fun computeButtonCenterX() = width - PADDING_PIXELS - computeButtonRadius()
    private fun computeButtonCenterY() = PADDING_PIXELS + computeButtonRadius()

    private fun getBitmap(): Bitmap? {
        val drawable = drawable

        return when {
            drawable == null -> null
            drawable is BitmapDrawable -> decreaseAndCropBitmap(drawable.bitmap, width.toFloat())
            drawable.intrinsicWidth == -1 -> {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                drawable.draw(Canvas(bitmap))
                bitmap
            }
            else -> {
                val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                )
                drawable.draw(Canvas(bitmap))
                decreaseAndCropBitmap(bitmap, width.toFloat())
            }
        }
    }

    private fun isInCloseZone(x: Float, y: Float) = x > width * 2 / 3 && y < height / 3
}