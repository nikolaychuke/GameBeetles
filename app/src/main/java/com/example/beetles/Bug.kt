package com.example.beetles

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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

    var baseSpeedX: Float = kotlin.math.abs(vx)
    var baseSpeedY: Float = kotlin.math.abs(vy)

    fun update(viewWidth: Int, viewHeight: Int, bounce: Boolean = true) {
        logicalX += vx
        logicalY += vy

        val sizeXLogical = size.toFloat() / viewWidth
        val sizeYLogical = size.toFloat() / viewHeight

        if (bounce) {
            if (logicalX < 0f) { logicalX = 0f; vx = -vx }
            if (logicalX + sizeXLogical > 1f) {
                logicalX = 1f - sizeXLogical; vx = -vx
            }
            if (logicalY < 0f) { logicalY = 0f; vy = -vy }
            if (logicalY + sizeYLogical > 1f) {
                logicalY = 1f - sizeYLogical; vy = -vy
            }
        } else {
            if (logicalX < 0f) { logicalX = 0f; vx = 0f }
            if (logicalX + sizeXLogical > 1f) {
                logicalX = 1f - sizeXLogical; vx = 0f
            }
            if (logicalY < 0f) { logicalY = 0f; vy = 0f }
            if (logicalY + sizeYLogical > 1f) {
                logicalY = 1f - sizeYLogical; vy = 0f
            }
        }

        rect.set(
            logicalX * viewWidth,
            logicalY * viewHeight,
            logicalX * viewWidth + size,
            logicalY * viewHeight + size
        )
    }

    fun applyGravityMotion(tiltX: Float, tiltY: Float, gravityStrength: Float) {
        val len = kotlin.math.sqrt(tiltX * tiltX + tiltY * tiltY)
        if (len < 0.01f) {
            vx = 0f
            vy = 0f
            return
        }
        val nx = tiltX / len
        val ny = tiltY / len

        vx = nx * baseSpeedX * gravityStrength
        vy = ny * baseSpeedY * gravityStrength
    }

    fun restoreRandomDirection() {
        val speedX = baseSpeedX
        val speedY = baseSpeedY
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        vx = cos(angle) * speedX
        vy = sin(angle) * speedY
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)
    }
}