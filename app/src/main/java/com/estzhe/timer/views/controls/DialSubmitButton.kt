package com.estzhe.timer.views.controls

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import com.estzhe.timer.views.DialPicker
import kotlin.math.atan2
import kotlin.math.cos

class DialSubmitButton : DialPicker.BaseDialPickerControl {

    var text: String? = null
        set(value) {
            field = value
            dialPicker.invalidate()
        }

    private val radius: Float
    private val onClickListener: OnClickListener?

    private var isPressed: Boolean = false

    private val buttonFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val buttonStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val buttonPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
    }
    private val textPaint: Paint

    constructor(radius: Float,
                dialPicker: DialPicker,
                onClickListener: OnClickListener?) : super(dialPicker) {
        check(radius > 0) { "Button radius must be greater than zero." }

        this.radius = radius
        this.onClickListener = onClickListener

        this.textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = radius
            isFakeBoldText = true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> intersectsWith(event.x, event.y).also { intersects ->
                if (intersects) {
                    isPressed = true
                    dialPicker.invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                intersectsWith(event.x, event.y).let { intersects ->
                    if (isPressed != intersects) {
                        isPressed = intersects
                        dialPicker.invalidate()
                    }
                }

                true
            }

            MotionEvent.ACTION_UP -> {
                if (intersectsWith(event.x, event.y)) {
                    isPressed = false
                    dialPicker.invalidate()

                    onClickListener?.onClick(this)
                }

                true
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        val (centerX, centerY) = dialToCanvasCoordinates(0f, 0f)

        if (!isPressed) {
            canvas.drawCircle(centerX, centerY, radius, buttonFillPaint)
            canvas.drawCircle(centerX, centerY, radius, buttonStrokePaint)
        }
        else {
            canvas.drawCircle(centerX, centerY, radius, buttonPressedPaint)
        }

        if (text != null) {
            val textBounds = Rect()
            textPaint.getTextBounds(text, 0, text!!.length, textBounds)

            canvas.drawText(
                text!!,
                centerX - textBounds.width() / 2,
                centerY + textBounds.height() / 2,
                textPaint)
        }
    }

    private fun intersectsWith(x: Float, y: Float): Boolean {
        val (xDial, yDial) = canvasToDialCoordinates(x, y)

        // Point's polar coordinates in original coordinate system.
        val pointAngleRad: Float = atan2(yDial, xDial)
        val pointDistance: Float = xDial / cos(pointAngleRad)

        return pointDistance <= radius
    }

    interface OnClickListener {
        fun onClick(button: DialSubmitButton)
    }
}