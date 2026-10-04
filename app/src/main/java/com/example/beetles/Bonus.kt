package com.example.beetles

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Bonus(
    var logicalX: Float,
    var logicalY: Float,
    val bitmap: Bitmap,
    val size: Int
) {
    val rect = RectF()

    fun update(viewWidth: Int, viewHeight: Int) {
        rect.set(
            logicalX * viewWidth,
            logicalY * viewHeight,
            logicalX * viewWidth + size,
            logicalY * viewHeight + size
        )
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)
    }
}