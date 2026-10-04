package com.example.beetles.data

import android.content.Context

object CurrentPlayer {

    private const val PREFS = "beetles_session"
    private const val KEY_PLAYER_ID = "current_player_id"

    fun setPlayerId(context: Context, id: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_PLAYER_ID, id)
            .apply()
    }

    fun getPlayerId(context: Context): Long {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_PLAYER_ID, -1L)
    }
}