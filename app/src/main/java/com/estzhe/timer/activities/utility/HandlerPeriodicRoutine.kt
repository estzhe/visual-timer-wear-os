package com.estzhe.timer.activities.utility

import android.content.Context
import android.os.Handler

class HandlerPeriodicRoutine(
    private val context: Context,
    private val intervalMs: Long,
    private val onTriggerListener: PeriodicRoutine.OnTriggerListener)
    : PeriodicRoutine
{
    companion object {
        private const val TRIGGER_MESSAGE_CODE = 0
    }

    private val handler: Handler

    private var startTimeMs: Long? = null

    private val isStarted: Boolean
        get() = startTimeMs != null

    init {
        check(intervalMs > 0) { "Interval has to be greater than zero." }

        handler = Handler { message ->
            when {
                message.what != TRIGGER_MESSAGE_CODE -> false

                !isStarted -> true

                else -> {
                    onTriggerListener.onTrigger()
                    scheduleNextTrigger()

                    true
                }
            }
        }
    }

    override fun start() {
        if (isStarted) return

        startTimeMs = System.currentTimeMillis()

        onTriggerListener.onTrigger()
        scheduleNextTrigger()
    }

    override fun stop() {
        if (!isStarted) return

        handler.removeMessages(TRIGGER_MESSAGE_CODE)
        startTimeMs = null
    }

    private fun scheduleNextTrigger() {
        val now = System.currentTimeMillis()
        val drift = (now - startTimeMs!!) % intervalMs
        val fromNow = intervalMs - drift

        handler.removeMessages(TRIGGER_MESSAGE_CODE)
        handler.sendEmptyMessageDelayed(TRIGGER_MESSAGE_CODE, fromNow)
    }

}