package com.estzhe.timer.views.controls

import android.graphics.Canvas
import android.view.MotionEvent
import com.estzhe.timer.views.DialPicker
import kotlin.math.atan2

class DialCrudeOperations(dialPicker: DialPicker,
                          private val onCrudeOperationListener: OnCrudeOperationListener?)
    : DialPicker.BaseDialPickerControl(dialPicker) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> true

            MotionEvent.ACTION_UP -> {
                if (isDecrement(event.x, event.y)) {
                    onCrudeOperationListener?.onDecrement()
                }
                else {
                    onCrudeOperationListener?.onIncrement()
                }

                true
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Not drawn - touch handling only.
    }

    private fun isDecrement(x: Float, y: Float): Boolean {
        val (xDial, yDial) = canvasToDialCoordinates(x, y)
        val pointAngleRad: Float = atan2(yDial, xDial)
        val pointAngle: Float =
            normalizeAngle(90 - Math.toDegrees(pointAngleRad.toDouble()).toFloat())

        // Rotate coordnate system so that dialPicker.angle = 0.
        var pointAngleRotated = pointAngle - dialPicker.angle

        // Make sure pointAngleRotated is between -180 and +180 degrees.
        if(pointAngleRotated < -180) {
            pointAngleRotated += 360
        }
        else if (pointAngleRotated > 180) {
            pointAngleRotated -= 360
        }

        return pointAngleRotated < 0
    }

    interface OnCrudeOperationListener {
        fun onIncrement()
        fun onDecrement()
    }
}