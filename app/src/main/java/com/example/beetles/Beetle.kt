package com.example.beetles

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Beetle(
    var x: Float,
    var y: Float,
    val bitmap: Bitmap,
    val size: Int
) {
    var vx: Float = 0f
    var vy: Float = 0f
    val rect = RectF(x, y, x + size, y + size)

    fun update(width: Int, height: Int) {
        x += vx
        y += vy

        if (x < 0) { x = 0f; vx = -vx }
        if (x + size > width) { x = width - size.toFloat(); vx = -vx }
        if (y < 0) { y = 0f; vy = -vy }
        if (y + size > height) { y = height - size.toFloat(); vy = -vy }

        rect.set(x, y, x + size, y + size)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)
    }
}