package me.alexeyterekhov.vkfilter.GUI.Common

import android.graphics.*

/**
 * Created by Alexey on 08.04.2015.
 */
public object RoundBitmap {
    fun make(b: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, b.getWidth(), b.getHeight())

        paint setAntiAlias true
        canvas.drawARGB(0, 0, 0, 0)
        paint setColor color
        canvas.drawCircle(b.getWidth() / 2f, b.getHeight() / 2f, b.getWidth() / 2f, paint)
        paint setXfermode PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(b, rect, rect, paint)
        return output
    }
}