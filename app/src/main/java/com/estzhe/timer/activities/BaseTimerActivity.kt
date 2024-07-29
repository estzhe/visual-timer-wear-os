package com.estzhe.timer.activities

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.FragmentActivity
import com.estzhe.timer.TimerManager

abstract class BaseTimerActivity : FragmentActivity()
{
    private val timerManagerLazy: Lazy<TimerManager> = lazy { TimerManager(this) }
    protected val timerManager: TimerManager by timerManagerLazy

    private lateinit var userFeedbackVibrator: Vibrator
    private val tickVibrationEffect = VibrationEffect.createOneShot(20, 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userFeedbackVibrator = getSystemService(Vibrator::class.java)!!
    }

    override fun onResume() {
        super.onResume()

        if (timerManagerLazy.isInitialized()) {
            timerManagerLazy.value.refresh()
        }
    }

    protected open fun onUserAction() {
        userFeedbackVibrator.vibrate(tickVibrationEffect)
    }
}