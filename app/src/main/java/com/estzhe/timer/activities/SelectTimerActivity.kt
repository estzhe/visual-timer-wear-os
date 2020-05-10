package com.estzhe.timer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.core.view.children
import com.estzhe.timer.R

class SelectTimerActivity : BaseTimerActivity()
{
    companion object {
        private const val ADD_TIMER_REQUEST_CODE = 1
    }

    private lateinit var grid: GridLayout
    private lateinit var addTimerButton: ImageButton
    private lateinit var runningTimerButton: Button
    private lateinit var popularTimerButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        navigateToTimerIfAppLaunchAndTimerIsRunning()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_select_timer)
        grid = findViewById(R.id.grid)
        addTimerButton = findViewById<ImageButton>(R.id.addTimerButton).apply {
            setOnClickListener(addTimerButtonClickListener)
        }
        runningTimerButton = findViewById<Button>(R.id.runningTimerButton).apply {
            setOnClickListener(activeTimerButtonClickListener)
            setOnLongClickListener(activeTimerButtonLongClickListener)
        }
        popularTimerButtons = grid.children
                                  .filter {
                                      it.id != addTimerButton.id &&
                                      it.id != runningTimerButton.id
                                  }
                                  .map { (it as Button).apply {
                                      setOnClickListener(selectTimerButtonClickListener)
                                      setOnLongClickListener(selectTimerButtonLongClickListener)
                                  } }
                                  .toList()

        // For rotating side button to work, scroll view has to have focus.
        // https://developer.android.com/training/wearables/ui/rotary-input#focus
        findViewById<ScrollView>(R.id.scrollView).requestFocus()
    }

    override fun onResume() {
        super.onResume()
        updateTimerButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_TIMER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val minutes = data!!.getIntExtra(AddTimerActivity.RESULT_VALUE, -1).also {
                    check(it != -1) { "Dial picker did not return a value." }
                }

                startTimer(minutes)
            }
        }
    }

    private val addTimerButtonClickListener = View.OnClickListener {
        startActivityForResult(
            Intent(
                this,
                AddTimerActivity::class.java
            ),
            ADD_TIMER_REQUEST_CODE
        )
    }

    private val activeTimerButtonClickListener = View.OnClickListener {
        startActivity(Intent(this, TimerActivity::class.java))
    }

    private val activeTimerButtonLongClickListener = View.OnLongClickListener {
        timerManager.stopActiveTimer()
        updateTimerButtons()

        true
    }

    private val selectTimerButtonClickListener = View.OnClickListener { v ->
        val minutes = (v as Button).text.toString().toInt()
        startTimer(minutes)
    }

    private val selectTimerButtonLongClickListener = View.OnLongClickListener { v ->
        val minutes = (v as Button).text.toString().toInt()
        timerManager.forgetTimer(minutes)

        (v.parent as ViewGroup).removeView(v)

        true
    }

    private fun navigateToTimerIfAppLaunchAndTimerIsRunning() {
        val isAppLaunch = intent.action == Intent.ACTION_MAIN
        val isTimerRunning = timerManager.activeTimer != null

        if (isAppLaunch && isTimerRunning) {
            startActivity(Intent(this, TimerActivity::class.java))
        }
    }

    private fun startTimer(minutes: Int) {
        timerManager.startNewTimer(minutes)
        startActivity(Intent(this, TimerActivity::class.java))
    }

    private fun updateTimerButtons() {
        // Running timer.
        if (timerManager.activeTimer != null) {
            if (runningTimerButton.parent == null) {
                grid.addView(runningTimerButton, 1)
                grid.requestLayout()
            }
        }
        else {
            if (runningTimerButton.parent != null) {
                grid.removeView(runningTimerButton)
                grid.requestLayout()
            }
        }

        // Popular timers.
        timerManager.popularTimers
                    .asSequence()
                    .sortedByDescending { it.value }
                    .take(popularTimerButtons.size)
                    .forEachIndexed { index, timer ->
                        popularTimerButtons[index].text = timer.key.toString()
                        popularTimerButtons[index].visibility = View.VISIBLE
                    }

        // Hide buttons that are not used for popular timers.
        for (i in timerManager.popularTimers.size until popularTimerButtons.size) {
            // If we need to hide all the buttons, then the first button should be INVISIBLE instead
            // of GONE to keep grid layout consistent, otherwise it will collapse the right column.
            popularTimerButtons[i].visibility = if (i == 0) View.INVISIBLE else View.GONE
        }
    }
}