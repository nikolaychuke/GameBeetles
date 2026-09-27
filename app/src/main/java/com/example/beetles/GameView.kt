package com.example.beetles

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var gameSpeed: Int = 1
    var maxBeetles: Int = 5
    var roundDuration: Int = 30

    var onScoreChanged: ((Int) -> Unit)? = null
    var onTimeChanged: ((Int) -> Unit)? = null
    var onGameOver: ((Int) -> Unit)? = null

    private val beetles = mutableListOf<Beetle>()
    private var score = 0
    private var timeLeft = 0
    private var isRunning = false

    private val handler = Handler(Looper.getMainLooper())
    private val beetleBitmaps = mutableListOf<Bitmap>()
    private val beetleSize = 150

    init {
        beetleBitmaps.add(createBeetleBitmap(Color.rgb(180, 60, 60)))
        beetleBitmaps.add(createBeetleBitmap(Color.rgb(60, 140, 60)))
        beetleBitmaps.add(createBeetleBitmap(Color.rgb(60, 90, 180)))
        beetleBitmaps.add(createBeetleBitmap(Color.rgb(150, 80, 180)))
    }

    private fun createBeetleBitmap(bodyColor: Int): Bitmap {
        val size = beetleSize
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = size / 2f
        val s = size / 100f

        val dark = darken(bodyColor, 0.55f)
        val light = lighten(bodyColor, 0.25f)


        //Ноги
        paint.color = dark
        paint.strokeWidth = 4f * s
        paint.strokeCap = Paint.Cap.ROUND
        val legsY = listOf(35f, 50f, 65f).map { it * s }
        for (y in legsY) {
            c.drawLine(cx - 10f * s, y, cx - 35f * s, y + 10f * s, paint)
            c.drawLine(cx + 10f * s, y, cx + 35f * s, y + 10f * s, paint)
        }

        //Брюшко
        paint.color = dark
        c.drawOval(RectF(cx - 20f * s, 30f * s, cx + 20f * s, 90f * s), paint)

        //Крылья
        paint.color = bodyColor
        c.drawOval(RectF(cx - 25f * s, 30f * s, cx - 2f * s, 85f * s), paint)
        c.drawOval(RectF(cx + 2f * s, 30f * s, cx + 25f * s, 85f * s), paint)

        //Блики
        paint.color = light
        c.drawOval(RectF(cx - 20f * s, 35f * s, cx - 10f * s, 60f * s), paint)
        c.drawOval(RectF(cx + 10f * s, 35f * s, cx + 20f * s, 60f * s), paint)

        //Голова
        paint.color = dark
        c.drawCircle(cx, 28f * s, 12f * s, paint)

        //Усики
        paint.strokeWidth = 2f * s
        c.drawLine(cx - 5f * s, 20f * s, cx - 20f * s, 5f * s, paint)
        c.drawLine(cx + 5f * s, 20f * s, cx + 20f * s, 5f * s, paint)

        //Глаза
        paint.color = Color.WHITE
        c.drawCircle(cx - 5f * s, 25f * s, 2.5f * s, paint)
        c.drawCircle(cx + 5f * s, 25f * s, 2.5f * s, paint)

        return bmp
    }

    private fun darken(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun lighten(color: Int, factor: Float): Int {
        val r = (Color.red(color) + (255 - Color.red(color)) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) + (255 - Color.green(color)) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) + (255 - Color.blue(color)) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    fun startGame() {
        if (isRunning) return
        if (width == 0 || height == 0) {
            post { startGame() }
            return
        }
        handler.removeCallbacks(gameLoop)
        beetles.clear()
        score = 0
        timeLeft = roundDuration
        isRunning = true
        onScoreChanged?.invoke(score)
        onTimeChanged?.invoke(timeLeft)
        spawnBeetles()
        handler.post(gameLoop)
    }

    fun stopGame() {
        isRunning = false
        handler.removeCallbacks(gameLoop)
    }

    private fun spawnBeetles() {
        repeat(maxBeetles.coerceAtMost(beetleBitmaps.size)) {
            spawnOneBeetle()
        }
    }

    private fun spawnOneBeetle() {
        if (beetles.size >= maxBeetles) return
        if (width == 0 || height == 0) return

        val bmp = beetleBitmaps[Random.nextInt(beetleBitmaps.size)]
        val size = beetleSize
        val maxX = (width - size).toFloat().coerceAtLeast(1f)
        val maxY = (height - size).toFloat().coerceAtLeast(1f)
        val x = Random.nextFloat() * maxX
        val y = Random.nextFloat() * maxY

        val b = Beetle(x, y, bmp, size)
        val speed = gameSpeed * 5f

        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        b.vx = kotlin.math.cos(angle) * speed
        b.vy = kotlin.math.sin(angle) * speed

        beetles.add(b)
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return
            update()
            invalidate()
            handler.postDelayed(this, 16)
        }
    }

    private var tickCounter = 0

    private fun update() {
        for (b in beetles) b.update(width, height)

        tickCounter++
        if (tickCounter >= 60) {
            tickCounter = 0
            timeLeft--
            onTimeChanged?.invoke(timeLeft)

            if (timeLeft <= 0) {
                stopGame()
                onGameOver?.invoke(score)
                return
            }
        }

        if (beetles.size < maxBeetles) {
            spawnOneBeetle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (b in beetles) b.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isRunning) {
            val x = event.x
            val y = event.y
            var hit = false

            for (i in beetles.indices.reversed()) {
                val b = beetles[i]
                if (b.rect.contains(x, y)) {
                    hit = true
                    score += 1
                    beetles.removeAt(i)
                    onScoreChanged?.invoke(score)
                    break
                }
            }

            if (!hit) {
                score = (score - 1).coerceAtLeast(0)
                onScoreChanged?.invoke(score)
            }
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGame()
    }
}