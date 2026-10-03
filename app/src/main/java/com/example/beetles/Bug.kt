package com.example.beetles

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Bug(
    val id: Int,
    val type: BugType,
    var logicalX: Float,
    var logicalY: Float,
    var vx: Float,
    var vy: Float,
    val bitmap: Bitmap,
    val size: Int
) {
    val rect = RectF()

    fun update(viewWidth: Int, viewHeight: Int) {
        logicalX += vx
        logicalY += vy

        val sizeXLogical = size.toFloat() / viewWidth
        val sizeYLogical = size.toFloat() / viewHeight

        if (logicalX < 0f) { logicalX = 0f; vx = -vx }
        if (logicalX + sizeXLogical > 1f) {
            logicalX = 1f - sizeXLogical; vx = -vx
        }
        if (logicalY < 0f) { logicalY = 0f; vy = -vy }
        if (logicalY + sizeYLogical > 1f) {
            logicalY = 1f - sizeYLogical; vy = -vy
        }

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