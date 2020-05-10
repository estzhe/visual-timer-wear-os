package com.estzhe.timer.views.controls

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import com.estzhe.timer.views.DialPicker
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Movable dial arrow to be overlaid on DialPicker.
 */
class DialArrow : DialPicker.BaseDialPickerControl {

    private val arrowWidth: Float
    private val onAngleChangeListener: OnAngleChangeListener?
    private val paint: Paint

    var arrowAngle: Float   // degrees from y-axis

    constructor(angle: Float,   // degrees from y-axis
                width: Int,
                dialPicker: DialPicker,
                onAngleChangeListener: OnAngleChangeListener?)
            : super(dialPicker) {
        check(width > 0) { "Width must be greater than zero." }

        this.arrowAngle = normalizeAngle(angle)
        this.arrowWidth = width.toFloat()
        this.onAngleChangeListener = onAngleChangeListener

        this.paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.TRANSPARENT
            style = Paint.Style.STROKE
            strokeWidth = width.toFloat()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> intersectsWith(event.x, event.y)

            MotionEvent.ACTION_MOVE -> {
                val (xDial, yDial) = canvasToDialCoordinates(event.x, event.y)

                val newAngle = 90f - Math.toDegrees(atan2(yDial, xDial).toDouble()).toFloat()
                arrowAngle = normalizeAngle(newAngle)

                onAngleChangeListener?.onAngleChange(arrowAngle)

                true
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        val arrowAngleRad = getAngleInRadians()
        val radius = dialPicker.dialBounds.width() / 2

        val (startX, startY) = dialToCanvasCoordinates(0f, 0f)
        val (endX, endY) = dialToCanvasCoordinates(
            radius * cos(arrowAngleRad),
            radius * sin(arrowAngleRad))

        canvas.drawLine(startX, startY, endX, endY, paint)
    }

    private fun getAngleInRadians(): Float {
        return Math.toRadians(90.0 - arrowAngle).toFloat()
    }

    private fun intersectsWith(x: Float, y: Float): Boolean {
        val (xDial, yDial) = canvasToDialCoordinates(x, y)

        // Point's polar coordinates in original coordinate system.
        val pointAngleRad: Float = atan2(yDial, xDial)
        val pointDistance: Float = xDial / cos(pointAngleRad)

        // Point's cartesian coordinates in rotated coordinate system.
        val rotationAngleRad: Float = -getAngleInRadians()  // rotate towards negative
        val pointXRotated: Float = pointDistance * cos(pointAngleRad + rotationAngleRad)
        val pointYRotated: Float = pointDistance * sin(pointAngleRad + rotationAngleRad)

        // Arrow is a rectangle from (0, arrowWidth) to (arrowLength, -arrowWidth).
        val arrowLength: Float = dialPicker.dialBounds.width() / 2

        return (pointXRotated in 0f..arrowLength) and
               (pointYRotated in -arrowWidth / 2 .. arrowWidth / 2)
    }

    interface OnAngleChangeListener {
        fun onAngleChange(newAngle: Float)
    }
}