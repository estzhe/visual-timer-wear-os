package com.estzhe.timer.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.estzhe.timer.views.controls.DialArrow
import com.estzhe.timer.views.controls.DialCrudeOperations
import com.estzhe.timer.views.controls.DialSubmitButton
import com.google.android.wearable.input.RotaryEncoderHelper
import kotlin.math.roundToInt
import kotlin.math.sign

class DialPicker(context: Context, attributes: AttributeSet)
    : DialView(context, attributes),
      View.OnGenericMotionListener {

    private val controls: Array<DialPickerControl>
    private val submitButton: DialSubmitButton?
    private val arrow: DialArrow?
    private var activeControl: DialPickerControl? = null
    private var onValuePickListener: OnValuePickListener? = null

    private var consecutiveRotaryEvents: Int = 0

    init {
        submitButton = DialSubmitButton(
            60f,
            this,
            object : DialSubmitButton.OnClickListener {
                override fun onClick(button: DialSubmitButton) {
                    onValuePickListener?.onPick(value)
                }
            }
        ).apply { text = value.toString() }

        arrow = DialArrow(
            angle,
            45,
            this,
            object : DialArrow.OnAngleChangeListener {
                override fun onAngleChange(newAngle: Float) {
                    value = (newAngle / 360f * (maxValue - minValue)).roundToInt()
                }
            })

        controls = arrayOf(
            submitButton,
            arrow,
            DialCrudeOperations(
                this,
                object : DialCrudeOperations.OnCrudeOperationListener {
                    override fun onIncrement() {
                        value = if (value + 1 > maxValue) minValue else (value + 1)
                    }

                    override fun onDecrement() {
                        value = if (value - 1 < minValue) maxValue else (value - 1)
                    }
                }))

        // For rotating side button to work, scroll view has to have focus.
        // https://developer.android.com/training/wearables/ui/rotary-input#tips
        focusable = FOCUSABLE
        isFocusableInTouchMode = true

        setOnGenericMotionListener(this)
    }

    fun setOnValuePickListener(value: OnValuePickListener) {
        onValuePickListener = value
    }

    override fun onValueChange(oldValue: Int, newValue: Int) {
        submitButton?.text = newValue.toString()
        arrow?.arrowAngle = angle

        super.onValueChange(oldValue, newValue)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        controls.reversed().forEach { it.onDraw(canvas) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeControl = controls.firstOrNull { it.onTouchEvent(event) }
                activeControl != null
            }

            else -> {
                activeControl?.onTouchEvent(event) ?: false
            }
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return true
    }

    override fun onGenericMotion(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL &&
            RotaryEncoderHelper.isFromRotaryEncoder(event)) {
            val deltaPixels: Float = -RotaryEncoderHelper.getRotaryAxisValue(event) *
                                      RotaryEncoderHelper.getScaledScrollFactor(context)

            // Every 7 events in same direction = value change by 1.
            val direction: Int = sign(deltaPixels).roundToInt()
            val previousDirection: Int = sign(consecutiveRotaryEvents.toFloat()).roundToInt()
            if (direction == previousDirection) {
                consecutiveRotaryEvents += direction
            }
            else {
                consecutiveRotaryEvents = direction
            }

            if (consecutiveRotaryEvents % 7 == 0) {
                // We invert direction here because:
                //  - rotating side buttons are usually located on the right
                //  - downward rotation direction (reported as negative direction here)
                //    matches with clockwise movement of the dial arrow, which intuitively
                //    is expected to increase value (positive direction).
                val newValue = value + -direction
                value = when {
                    newValue < minValue -> maxValue
                    newValue > maxValue -> minValue
                    else -> newValue
                }
            }

            return true
        }

        return false
    }

    interface OnValuePickListener {
        fun onPick(value: Int)
    }

    interface DialPickerControl {
        fun onTouchEvent(event: MotionEvent): Boolean
        fun onDraw(canvas: Canvas)
    }

    abstract class BaseDialPickerControl(protected val dialPicker: DialPicker)
        : DialPickerControl
    {
        /**
         * Returns an angle in degrees that is between 0 and 360.
         */
        protected fun normalizeAngle(angle: Float): Float {
            return angle % 360 + if (angle < 0) 360 else 0
        }

        protected fun canvasToDialCoordinates(x: Float, y: Float): Pair<Float, Float> {
            val bounds = dialPicker.dialBounds

            return Pair(
                x - bounds.centerX(),
                bounds.centerY() - y)
        }

        protected fun dialToCanvasCoordinates(xDial: Float, yDial: Float): Pair<Float, Float> {
            val bounds = dialPicker.dialBounds

            return Pair(
                bounds.centerX() + xDial,
                bounds.centerY() - yDial
            )
        }
    }
}