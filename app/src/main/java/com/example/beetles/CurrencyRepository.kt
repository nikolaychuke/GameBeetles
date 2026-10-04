package com.example.beetles

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CurrencyRepository {

    suspend fun getYuanRate(): Float? = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("Currency", "→ Запрос к API...")
            val response = RetrofitClient.api.getDailyRates()

            val valutes = response.valutes ?: emptyList()
            android.util.Log.d("Currency", "← Получено валют: ${valutes.size}")

            val yuan = valutes.find { it.charCode == "CNY" }

            if (yuan == null) {
                android.util.Log.e("Currency", "CNY не найден в списке")
                return@withContext null
            }

            android.util.Log.d("Currency", "CNY: ${yuan.value}")

            yuan.value?.trim()?.replace(",", ".")?.toFloatOrNull()
        } catch (e: Exception) {
            android.util.Log.e("Currency", "ОШИБКА: ${e.message}", e)
            null
        }
    }
}