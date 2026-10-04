package com.example.beetles

import retrofit2.http.GET

interface CurrencyApi {

    @GET("daily_utf8.xml")
    suspend fun getDailyRates(): CurrencyResponse
}