package com.estzhe.timer.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.estzhe.timer.R
import com.estzhe.timer.views.DialPicker

class AddTimerActivity
    : BaseTimerActivity(),
      DialPicker.OnValuePickListener
{
    companion object {
        const val RESULT_VALUE = "value"
    }

    private lateinit var dialPicker: DialPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_timer)

        dialPicker = findViewById<DialPicker>(R.id.dialPicker).apply {
            setOnValuePickListener(this@AddTimerActivity)

            // For rotating side button to work, dial picker view has to have focus.
            // https://developer.android.com/training/wearables/ui/rotary-input#focus
            requestFocus()
        }
    }

    override fun onPick(value: Int) {
        onUserAction()

        if (value == dialPicker.minValue) {
            Toast.makeText(
                this@AddTimerActivity,
                "Select a real time interval, wouldya?",
                Toast.LENGTH_SHORT).show()

            return
        }

        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(RESULT_VALUE, value)
            })

        finish()
    }
}