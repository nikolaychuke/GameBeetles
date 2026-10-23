package com.example.beetles

enum class AbilityType(
    val displayName: String,
    val description: String,
    val maxCharges: Int = 2,
    val durationSeconds: Int,
    val color: Int
) {
    FREEZE(
        displayName = "Заморозка",
        description = "Останавливает всех жуков на 5 секунд",
        durationSeconds = 5,
        color = 0xFF64B5F6.toInt()
    ),
    BOMB(
        displayName = "Бомба",
        description = "Взрыв в точке тапа",
        durationSeconds = 0,
        color = 0xFFE57373.toInt()
    ),
    SLOW(
        displayName = "Замедление",
        description = "Снижает скорость жуков в 2 раза на 5 секунд",
        durationSeconds = 8,
        color = 0xFF81C784.toInt()
    ),
    MULTI_HIT(
        displayName = "Мультиудар",
        description = "1 попадание = 5 попаданий",
        durationSeconds = 0,
        color = 0xFFFFB74D.toInt()
    )
}