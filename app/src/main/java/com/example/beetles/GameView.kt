package com.example.beetles

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
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
    var onGameOver: ((GameResult) -> Unit)? = null

    private val bugs = mutableListOf<Bug>()
    private var score = 0
    private var hits = 0
    private var misses = 0
    private var timeLeft = 0
    private var isRunning = false
    private var nextId = 0

    private val handler = Handler(Looper.getMainLooper())
    private val bugBitmaps = mutableMapOf<BugType, Bitmap>()
    private val baseSize = 120

    init {
        BugType.values().forEach { type ->
            val size = (baseSize * type.sizeFactor).toInt()
            bugBitmaps[type] = createBugBitmap(type.color, size)
        }
    }

    private fun createBugBitmap(bodyColor: Int, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = size / 2f
        val s = size / 100f

        val dark = darken(bodyColor, 0.55f)
        val light = lighten(bodyColor, 0.25f)

        // Ноги
        paint.color = dark
        paint.strokeWidth = 4f * s
        paint.strokeCap = Paint.Cap.ROUND
        val legsY = listOf(35f, 50f, 65f).map { it * s }
        for (y in legsY) {
            c.drawLine(cx - 10f * s, y, cx - 35f * s, y + 10f * s, paint)
            c.drawLine(cx + 10f * s, y, cx + 35f * s, y + 10f * s, paint)
        }

        // Брюшко
        paint.color = dark
        c.drawOval(RectF(cx - 20f * s, 30f * s, cx + 20f * s, 90f * s), paint)

        // Крылья
        paint.color = bodyColor
        c.drawOval(RectF(cx - 25f * s, 30f * s, cx - 2f * s, 85f * s), paint)
        c.drawOval(RectF(cx + 2f * s, 30f * s, cx + 25f * s, 85f * s), paint)

        // Блики
        paint.color = light
        c.drawOval(RectF(cx - 20f * s, 35f * s, cx - 10f * s, 60f * s), paint)
        c.drawOval(RectF(cx + 10f * s, 35f * s, cx + 20f * s, 60f * s), paint)

        // Голова
        paint.color = dark
        c.drawCircle(cx, 28f * s, 12f * s, paint)

        // Усики
        paint.strokeWidth = 2f * s
        c.drawLine(cx - 5f * s, 20f * s, cx - 20f * s, 5f * s, paint)
        c.drawLine(cx + 5f * s, 20f * s, cx + 20f * s, 5f * s, paint)

        // Глаза
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
        bugs.clear()
        score = 0
        hits = 0
        misses = 0
        nextId = 0
        timeLeft = roundDuration
        isRunning = true
        onScoreChanged?.invoke(score)
        onTimeChanged?.invoke(timeLeft)
        spawnBugs()
        handler.post(gameLoop)
    }

    fun stopGame() {
        isRunning = false
        handler.removeCallbacks(gameLoop)
    }

    private fun spawnBugs() {
        repeat(maxBeetles.coerceAtMost(3)) {
            spawnOneBug()
        }
    }

    private fun spawnOneBug() {
        if (bugs.size >= maxBeetles) return
        if (width == 0 || height == 0) return

        val type = pickRandomType()
        val bmp = bugBitmaps[type] ?: return
        val size = (baseSize * type.sizeFactor).toInt()

        val sizeXLogical = size.toFloat() / width
        val sizeYLogical = size.toFloat() / height
        val logicalX = Random.nextFloat() * (1f - sizeXLogical).coerceAtLeast(0.01f)
        val logicalY = Random.nextFloat() * (1f - sizeYLogical).coerceAtLeast(0.01f)

        val pixelSpeed = gameSpeed * 5f * type.speedFactor
        val logicalSpeedX = pixelSpeed / width
        val logicalSpeedY = pixelSpeed / height

        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val vx = cos(angle) * logicalSpeedX
        val vy = sin(angle) * logicalSpeedY

        val bug = Bug(nextId++, type, logicalX, logicalY, vx, vy, bmp, size)
        bugs.add(bug)
    }

    private fun pickRandomType(): BugType {
        val total = BugType.values().sumOf { it.spawnWeight }
        var r = Random.nextInt(total)
        for (type in BugType.values()) {
            if (r < type.spawnWeight) return type
            r -= type.spawnWeight
        }
        return BugType.COMMON
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
        for (b in bugs) b.update(width, height)

        tickCounter++
        if (tickCounter >= 60) {
            tickCounter = 0
            timeLeft--
            onTimeChanged?.invoke(timeLeft)

            if (timeLeft <= 0) {
                stopGame()
                val total = hits + misses
                val accuracy = if (total > 0) hits.toFloat() / total else 0f
                onGameOver?.invoke(GameResult(score, hits, misses, accuracy))
                return
            }
        }

        if (bugs.size < maxBeetles) {
            spawnOneBug()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (b in bugs) b.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isRunning) {
            val x = event.x
            val y = event.y
            var hit = false

            for (i in bugs.indices.reversed()) {
                val b = bugs[i]
                if (b.rect.contains(x, y)) {
                    hit = true
                    score += b.type.points
                    hits++
                    bugs.removeAt(i)
                    onScoreChanged?.invoke(score)
                    break
                }
            }

            if (!hit) {
                score = (score - 1).coerceAtLeast(0)
                misses++
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