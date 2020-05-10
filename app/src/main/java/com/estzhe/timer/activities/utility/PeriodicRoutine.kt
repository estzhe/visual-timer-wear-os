package com.estzhe.timer.activities.utility

interface PeriodicRoutine {
    fun start()
    fun stop()

    interface OnTriggerListener {
        fun onTrigger()
    }
}