package me.alexeyterekhov.vkfilter.GUI.Common

import android.graphics.*

public object RoundBitmap {
    infix fun make(b: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(b.width, b.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, b.width, b.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(b.width / 2f, b.height / 2f, b.width / 2f, paint)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(b, rect, rect, paint)
        return output
    }
}