package com.estzhe.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.estzhe.timer.activities.TimerActivity
import java.time.Duration
import java.time.Instant

class TimerManager(private val context: Context) {

    val activeTimer: Timer?
        get() {
            if (timers.active == null) {
                return null
            }

            if (timers.active!!.isRunning &&
                timers.active!!.timeLeft < Duration.ZERO) {
                return null
            }

            return timers.active
        }

    val popularTimers: Map<Int, Int>
        get() = timers.popular

    private val store = TimerStore(PreferenceManager.getDefaultSharedPreferences(context))
    private var timers: Timers = store.read()

    fun startNewTimer(minutes: Int): Timer {
        check(minutes > 0) { "Minutes have to be greater than zero." }

        // Store
        val active = Timer.running(Instant.now().plusSeconds(60L * minutes))
        val popular = timers.popular.toMutableMap().also {
            it[minutes] = 1 + it.getOrDefault(minutes, 0)
        }

        timers = Timers(active, popular)
        store.save(timers)

        // Alarm
        scheduleAlarm(active)

        return active
    }

    fun pauseActiveTimer(): Timer {
        var timer: Timer = activeTimer!!

        check(activeTimer != null) { "There is no active timer to pause." }

        if (timer.isPaused) {
            return timer
        }

        timer = timer.toPaused()

        timers = timers.copy(active = timer)
        store.save(timers)

        cancelOutstandingAlarm()

        return timer
    }

    fun stopActiveTimer() {
        var timer: Timer = activeTimer!!

        check(activeTimer != null) { "There is no active timer to stop." }

        timers = timers.copy(active = null)
        store.save(timers)

        cancelOutstandingAlarm()
    }

    fun startTimer(timer: Timer) {
        check(timer.isPaused) { "Provided timer is already running." }

        val runningTimer = timer.toRunning()

        timers = timers.copy(active = runningTimer)
        store.save(timers)

        scheduleAlarm(runningTimer)
    }

    fun forgetTimer(minutes: Int) {
        val popular = timers.popular.toMutableMap().also {
            it.remove(minutes)
        }

        timers = timers.copy(popular = popular)
        store.save(timers)
    }

    fun refresh() {
        timers = store.read()
    }

    private fun scheduleAlarm(timer: Timer) {
        check(timer.isRunning) { "Alarm can only be scheduled for a running timer. The timer provided is paused." }

        val pendingIntent = getPendingIntentForAlarm(PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)!!
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timer.targetTime.toEpochMilli(),
            pendingIntent
        )
    }

    private fun cancelOutstandingAlarm() {
        val pendingIntent = getPendingIntentForAlarm(PendingIntent.FLAG_CANCEL_CURRENT)

        val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)!!
        alarmManager.cancel(pendingIntent)
    }

    private fun getPendingIntentForAlarm(flags: Int): PendingIntent {
        val intent = Intent(context, TimerActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra(TimerActivity.EXTRA_IS_ALARM, true)
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }
}