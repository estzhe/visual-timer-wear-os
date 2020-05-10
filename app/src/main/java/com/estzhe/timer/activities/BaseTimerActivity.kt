package com.estzhe.timer.activities

import androidx.fragment.app.FragmentActivity
import com.estzhe.timer.TimerManager

abstract class BaseTimerActivity : FragmentActivity()
{
    private val timerManagerLazy: Lazy<TimerManager> = lazy { TimerManager(this) }
    protected val timerManager: TimerManager by timerManagerLazy

    override fun onResume() {
        super.onResume()

        if (timerManagerLazy.isInitialized()) {
            timerManagerLazy.value.refresh()
        }
    }
}