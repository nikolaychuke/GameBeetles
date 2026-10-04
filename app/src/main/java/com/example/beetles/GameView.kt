package com.example.beetles

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val bonusBitmap: Bitmap = createBonusBitmap()
    private var bonus: Bonus? = null
    private var bonusTimerSeconds = 0
    var bonusIntervalSeconds: Int = 1
    private val bonusSpriteSize = 100
    private val sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var tiltX = 0f
    private var tiltY = 0f
    private var gravityActive = false
    private var gravitySecondsLeft = 0
    private val gravityDurationSeconds = 5
    private val gravityStrength = 3f

    private var goldenTimerSeconds = 0
    private val goldenIntervalSeconds = 20
    private val currencyRepository = CurrencyRepository()
    private var cachedYuanRate: Float = 0f

    var onYuanRateLoaded: ((Float) -> Unit)? = null

    var onGetYuanRate: (() -> Float)? = null

    private val soundPool: SoundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val screamSoundId: Int = soundPool.load(context, R.raw.beetle_scream, 1)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            tiltX = -event.values[0] / 9.8f
            tiltY = event.values[1] / 9.8f
            tiltX = tiltX.coerceIn(-1f, 1f)
            tiltY = tiltY.coerceIn(-1f, 1f)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun spawnBonus() {
        if (width == 0 || height == 0) return
        val size = bonusSpriteSize
        val sizeXLogical = size.toFloat() / width
        val sizeYLogical = size.toFloat() / height
        val lx = Random.nextFloat() * (1f - sizeXLogical).coerceAtLeast(0.01f)
        val ly = Random.nextFloat() * (1f - sizeYLogical).coerceAtLeast(0.01f)
        bonus = Bonus(lx, ly, bonusBitmap, size)
        bonus?.update(width, height)
    }

    private fun applyGravity() {
        if (width == 0 || height == 0) return

        val tiltMag = kotlin.math.sqrt(tiltX * tiltX + tiltY * tiltY)
        if (tiltMag < 0.05f) return

        for (b in bugs) {
            b.applyGravityMotion(tiltX, tiltY, gravityStrength)
        }
    }

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


    private fun createBonusBitmap(): Bitmap {
        val size = 100
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = size / 2f

        paint.color = Color.rgb(255, 215, 0)
        val path = Path()
        val outerR = cx
        val innerR = outerR / 2.5f
        val spikes = 5
        for (i in 0 until spikes * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = Math.PI / spikes * i - Math.PI / 2
            val x = cx + (r * Math.cos(angle)).toFloat()
            val y = cx + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        c.drawPath(path, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.rgb(180, 130, 0)
        c.drawPath(path, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        c.drawText("B", cx, cx + 10f, paint)

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
        goldenTimerSeconds = 0
        cachedYuanRate = 0f
        loadYuanRate()
        score = 0
        hits = 0
        misses = 0
        nextId = 0
        bonus = null
        bonusTimerSeconds = 0
        gravityActive = false
        gravitySecondsLeft = 0
        tiltX = 0f
        tiltY = 0f
        timeLeft = roundDuration
        isRunning = true
        onScoreChanged?.invoke(score)
        onTimeChanged?.invoke(timeLeft)
        spawnBugs()
        handler.post(gameLoop)
        accelerometer?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun saveState(): GameState {
        return GameState(
            score = score,
            hits = hits,
            misses = misses,
            timeLeft = timeLeft,
            bugs = bugs.toList(),
            isRunning = isRunning
        )
    }

    fun restoreState(state: GameState) {
        this.score = state.score
        this.hits = state.hits
        this.misses = state.misses
        this.timeLeft = state.timeLeft
        this.bugs.clear()
        this.bugs.addAll(state.bugs)
        this.isRunning = state.isRunning

        onScoreChanged?.invoke(score)
        onTimeChanged?.invoke(timeLeft)

        if (isRunning) {
            handler.post(gameLoop)
            accelerometer?.let {
                sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        invalidate()
    }

    fun stopGame() {
        isRunning = false
        handler.removeCallbacks(gameLoop)
        sensorManager?.unregisterListener(sensorListener)
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

        bug.baseSpeedX = logicalSpeedX
        bug.baseSpeedY = logicalSpeedY

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


    private fun spawnGoldenBug() {
        if (width == 0 || height == 0) return
        if (bugs.any { it.type == BugType.GOLDEN }) return

        val bmp = bugBitmaps[BugType.GOLDEN] ?: return
        val size = (baseSize * BugType.GOLDEN.sizeFactor).toInt()

        val sizeXLogical = size.toFloat() / width
        val sizeYLogical = size.toFloat() / height
        val logicalX = Random.nextFloat() * (1f - sizeXLogical).coerceAtLeast(0.01f)
        val logicalY = Random.nextFloat() * (1f - sizeYLogical).coerceAtLeast(0.01f)

        val pixelSpeed = gameSpeed * 5f * BugType.GOLDEN.speedFactor
        val logicalSpeedX = pixelSpeed / width
        val logicalSpeedY = pixelSpeed / height

        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val vx = cos(angle) * logicalSpeedX
        val vy = sin(angle) * logicalSpeedY

        val bug = Bug(nextId++, BugType.GOLDEN, logicalX, logicalY, vx, vy, bmp, size)
        bug.baseSpeedX = logicalSpeedX
        bug.baseSpeedY = logicalSpeedY

        bugs.add(bug)
    }

    private var tickCounter = 0

    private fun update() {
        val bounce = !gravityActive
        for (b in bugs) b.update(width, height, bounce)
        bonus?.update(width, height)

        if (gravityActive) applyGravity()

        tickCounter++
        if (tickCounter >= 60) {
            tickCounter = 0
            timeLeft--
            onTimeChanged?.invoke(timeLeft)

            goldenTimerSeconds++
            if (goldenTimerSeconds >= goldenIntervalSeconds) {
                goldenTimerSeconds = 0
                spawnGoldenBug()
            }

            if (bonus == null) {
                bonusTimerSeconds++
                if (bonusTimerSeconds >= bonusIntervalSeconds) {
                    bonusTimerSeconds = 0
                    spawnBonus()
                }
            }

            if (gravityActive) {
                gravitySecondsLeft--
                if (gravitySecondsLeft <= 0) {
                    gravityActive = false
                    tiltX = 0f
                    tiltY = 0f
                    for (b in bugs) b.restoreRandomDirection()
                }
            }

            if (timeLeft <= 0) {
                stopGame()
                val total = hits + misses
                val accuracy = if (total > 0) hits.toFloat() / total else 0f
                onGameOver?.invoke(GameResult(score, hits, misses, accuracy))
                return
            }
        }

        if (bugs.size < maxBeetles) spawnOneBug()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (b in bugs) b.draw(canvas)
        bonus?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isRunning) {
            val x = event.x
            val y = event.y

            val bonusRef = bonus
            if (bonusRef != null && bonusRef.rect.contains(x, y)) {
                gravityActive = true
                gravitySecondsLeft = gravityDurationSeconds
                soundPool.play(screamSoundId, 1f, 1f, 1, 0, 1f)
                bonus = null
                invalidate()
                return true
            }

            var hit = false

            for (i in bugs.indices.reversed()) {
                val bug = bugs[i]
                if (bug.rect.contains(x, y)) {
                    hit = true

                    val points = if (bug.type == BugType.GOLDEN) {
                        val rate = onGetYuanRate?.invoke() ?: 0f
                        if (rate > 0f) (rate).toInt() else bug.type.points
                    } else {
                        bug.type.points
                    }

                    score += points
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
        sensorManager?.unregisterListener(sensorListener)
        soundPool.release()
    }

    private fun loadYuanRate() {
        CoroutineScope(Dispatchers.IO).launch {
            val rate = currencyRepository.getYuanRate()
            withContext(Dispatchers.Main) {
                cachedYuanRate = rate ?: 0f
                onYuanRateLoaded?.invoke(cachedYuanRate)
            }
        }
    }

}