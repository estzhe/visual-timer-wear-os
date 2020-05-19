package com.estzhe.timer.activities

import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.wear.ambient.AmbientModeSupport
import com.estzhe.timer.*
import com.estzhe.timer.activities.utility.*
import com.estzhe.timer.activities.utility.AmbientCallback
import com.estzhe.timer.views.DialView
import java.time.Instant
import kotlin.math.ceil
import kotlin.math.roundToInt

// TODO: ambient burn-in protection
// TODO: ambient antialiasing on for lowbit / off otherwise
// TODO: sound

class TimerActivity
      : BaseTimerActivity(),
        AmbientModeSupport.AmbientCallbackProvider,
        AmbientCallback.OnAmbientListener,
        View.OnLongClickListener
{
    companion object {
        const val EXTRA_IS_ALARM = "is_alarm"
    }

    private lateinit var ambientModeController: AmbientModeSupport.AmbientController
    private lateinit var dial: DialView
    private lateinit var pauseOverlay: View

    private lateinit var scheduler: PeriodicRoutine
    private lateinit var activeModeScheduler: HandlerPeriodicRoutine
    private lateinit var ambientModeScheduler: AlarmManagerPeriodicRoutine

    /**
     * Indicates whether an alarm notification was received.
     * If true, this activity is showing alarm animation (blinking, vibrating, etc).
     * If false, this activity is showing visual countdown timer.
     */
    private val alarmReceived: Boolean get() = intent.extras?.getBoolean(EXTRA_IS_ALARM) ?: false
    private lateinit var alarmAnimation: AlarmAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_timer)
        ambientModeController = AmbientModeSupport.attach(this)
        dial = findViewById<DialView>(R.id.dial).apply {
            setOnLongClickListener(this@TimerActivity)
        }
        pauseOverlay = findViewById(R.id.pauseOverlay)

        activeModeScheduler = HandlerPeriodicRoutine(
            this,
            1000,
            object : PeriodicRoutine.OnTriggerListener {
                override fun onTrigger() = onSchedulerTrigger()
            })
        ambientModeScheduler = AlarmManagerPeriodicRoutine(
            this,
            5000,
            object : PeriodicRoutine.OnTriggerListener {
                override fun onTrigger() = onSchedulerTrigger()
            })
        scheduler = activeModeScheduler

        alarmAnimation = AlarmAnimation(this, dial)
    }

    override fun onResume() {
        super.onResume()

        if (alarmReceived) {
            alarmAnimation.start()
        }

        scheduler.start()
    }

    override fun onPause() {
        super.onPause()
        scheduler.stop()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onLongClick(v: View): Boolean {
        if (alarmReceived) {
            return false
        }

        onUserAction()

        val timer: Timer = timerManager.activeTimer ?: return false

        if (timer.isPaused) {
            pauseOverlay.visibility = View.INVISIBLE
            timerManager.startTimer(timer)
        }
        else {
            pauseOverlay.visibility = View.VISIBLE
            timerManager.pauseActiveTimer()
        }

        return true
    }

    override fun onBackPressed() {
        alarmAnimation.end()
        super.onBackPressed()
    }

    override fun onEnterAmbient(ambientDetails: Bundle) {
        scheduler.stop()
        scheduler = ambientModeScheduler

        // onEnterAmbient/onExitAmbient can get called when state is PAUSED.
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            scheduler.start()
        }
    }

    override fun onExitAmbient() {
        scheduler.stop()
        scheduler = activeModeScheduler

        // onEnterAmbient/onExitAmbient can get called when state is PAUSED.
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            scheduler.start()
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return AmbientCallback(this)
    }

    private fun onSchedulerTrigger() {
        if (alarmReceived) {
            dial.value = 0
            pauseOverlay.visibility = View.INVISIBLE
        }
        else {
            val timer = timerManager.activeTimer

            dial.value = when (timer) {
                null -> 0
                else -> ceil(dial.maxValue * timer.timeLeft.seconds / 3600.0).roundToInt()
            }

            pauseOverlay.visibility = when {
                timer == null -> View.INVISIBLE
                timer.isRunning -> View.INVISIBLE
                else -> View.VISIBLE
            }
        }

        alarmAnimation.animateOnceIfStarted()
    }

    private class AlarmAnimation(
        private val context: TimerActivity,
        private val dial: DialView)
    {
        var started: Boolean = false
            private set

        private var animationEndTime: Instant? = null
        private val vibrator: Vibrator = context.getSystemService(Vibrator::class.java)!!

        init {
            dial.setOnClickListener {
                if (started) {
                    context.onUserAction()
                    end()
                }
            }
        }

        fun start() {
            if (started) return

            started = true
            animationEndTime = Instant.now().plusSeconds(60)

            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(
                    0,

                    100, 200, 100, 200, 100, 200, 100, 200,
                    100, 100, 100, 100, 100, 200, 100, 200, 100, 200, 100, 200,
                    100, 100, 100, 100, 100, 200, 100, 200, 100, 200, 100, 200,
                    100, 100, 100, 100, 100, 100, 100, 100, 100, 100,  50,  50, 100, 800), -1),
                AudioAttributes.Builder().setFlags(AudioAttributes.USAGE_ALARM).build())
        }

        fun animateOnceIfStarted() {
            if (!started) return

            dial.rimColor = Color.GREEN

            if (animationEndTime!! < Instant.now()) {
                end()
            }
        }

        fun end() {
            if (!started) return

            vibrator.cancel()

            val intent = Intent(context, SelectTimerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            context.startActivity(intent)
            context.finish()
        }
    }
}