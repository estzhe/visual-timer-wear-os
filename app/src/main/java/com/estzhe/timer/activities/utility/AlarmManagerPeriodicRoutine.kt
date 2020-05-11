package com.estzhe.timer.activities.utility

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.util.*

class AlarmManagerPeriodicRoutine(
    private val context: Context,
    private val intervalMs: Long,
    private val onTriggerListener: PeriodicRoutine.OnTriggerListener)
    : PeriodicRoutine
{
    companion object {
        private const val INTENT_ACTION_PREFIX =
            "com.estzhe.timer.activities.utility.AlarmManagerPeriodicRoutine.action."
    }

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)!!
    private val broadcastIntentActionName: String
    private val broadcastPendingIntent: PendingIntent
    private val broadcastReceiver: BroadcastReceiver

    private var startTimeMs: Long? = null

    private val isStarted: Boolean
        get() = startTimeMs != null

    init {
        check(intervalMs > 0) { "Interval has to be greater than zero." }

        broadcastIntentActionName = INTENT_ACTION_PREFIX + UUID.randomUUID().toString()

        val intent = Intent(broadcastIntentActionName).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        broadcastPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (!isStarted) return

                onTriggerListener.onTrigger()
                scheduleNextTrigger()
            }
        }
    }

    override fun start() {
        if (isStarted) return

        context.registerReceiver(
            broadcastReceiver,
            IntentFilter(broadcastIntentActionName)
        )

        startTimeMs = System.currentTimeMillis()

        onTriggerListener.onTrigger()
        scheduleNextTrigger()
    }

    override fun stop() {
        if (!isStarted) return

        context.unregisterReceiver(broadcastReceiver)
        alarmManager.cancel(broadcastPendingIntent)

        startTimeMs = null
    }

    private fun scheduleNextTrigger() {
        val now = System.currentTimeMillis()
        val drift = (now - startTimeMs!!) % intervalMs
        val triggerTime = now + intervalMs - drift

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            broadcastPendingIntent)
    }
}