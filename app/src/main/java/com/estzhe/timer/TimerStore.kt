package com.estzhe.timer

import android.content.SharedPreferences
import com.estzhe.timer.gson.GsonDurationTypeConverter
import com.estzhe.timer.gson.GsonInstantTypeConverter
import com.google.gson.*
import java.time.Duration
import java.time.Instant

class TimerStore(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val KEY_TIMERS = "timers"
    }

    private val gson: Gson =
        GsonBuilder()
            .registerTypeAdapter(Instant::class.java, GsonInstantTypeConverter())
            .registerTypeAdapter(Duration::class.java, GsonDurationTypeConverter())
            .create()

    fun save(timers: Timers) {
        val json = gson.toJson(timers)

        sharedPreferences.edit().apply {
            putString(KEY_TIMERS, json)
            commit()
        }
    }

    fun read(): Timers {
        val json = sharedPreferences.getString(KEY_TIMERS, null)

        return if (json == null) {
            Timers(null, emptyMap())
        }
        else {
            gson.fromJson(json, Timers::class.java)
        }
    }
}
