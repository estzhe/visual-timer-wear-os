package com.estzhe.timer.views

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import com.estzhe.timer.R
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

open class DialView(context: Context, attributes: AttributeSet) : View(context, attributes) {

    val minValue: Int

    val maxValue: Int

    val rimSize: Int

    var rimColor: Int
        get() = rimPaint.color
        set(value) {
            if (value == rimPaint.color) return

            val oldValue = rimPaint.color
            rimPaint.color = value

            onColorChange(oldValue, value)
        }

    var value: Int = 0
        set(value) {
            check(value >= minValue) { "Value $value is outside of range $minValue..$maxValue." }
            check(value <= maxValue) { "Value $value is outside of range $minValue..$maxValue." }

            if (value == field) return

            val oldValue = field
            field = value

            onValueChange(oldValue, value)
        }

    /**
     * Angle from y-axis that corresponds to {value}.
     */
    val angle: Float
        get() = 360f * value / (maxValue - minValue)

    var dialBounds = RectF(0f, 0f, 0f, 0f)

    private var tickCoordinates: FloatArray = FloatArray(0)

    private var piePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private var rimPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
    }
    private var tickPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private var backgroundPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    init {
        context.theme.obtainStyledAttributes(
            attributes,
            R.styleable.DialView,
            0,
            0).apply {
            try {
                minValue = getInteger(R.styleable.DialView_minValue, 0)
                maxValue = getInteger(R.styleable.DialView_maxValue, 60)
                rimSize = getInteger(R.styleable.DialView_rimSize, 0)
                value = getInteger(R.styleable.DialView_value, minValue)

                check(minValue < maxValue) { "Minimum value must be smaller than maximum value." }
                check(value >= minValue) { "Value outside of range $minValue..$maxValue." }
                check(value <= maxValue) { "Value outside of range $minValue..$maxValue." }
                check(rimSize >= 0) { "Rim size must be zero or greater." }
            }
            finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // background
        canvas.drawPaint(backgroundPaint)

        // rim
        if (rimSize > 0) {
            val innerCircleRadius = dialBounds.width() / 2
            val outerCircleRadius = innerCircleRadius + rimSize

            canvas.drawCircle(
                dialBounds.centerX(),
                dialBounds.centerY(),
                outerCircleRadius,
                rimPaint)

            canvas.drawCircle(
                dialBounds.centerX(),
                dialBounds.centerY(),
                innerCircleRadius,
                backgroundPaint)
        }

        // pie
        val angle: Float = 360 * (value - minValue) / (maxValue - minValue).toFloat()
        canvas.drawArc(
            dialBounds,
            -90f,
            angle,
            true,
            piePaint
        )

        // ticks
        for (i in 0 until tickCoordinates.size / 4) {
            canvas.drawLine(
                tickCoordinates[4 * i + 0],
                tickCoordinates[4 * i + 1],
                tickCoordinates[4 * i + 2],
                tickCoordinates[4 * i + 3],
                tickPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        dialBounds = RectF(
            min(
                right - paddingRight,
                left + paddingLeft + rimSize).toFloat(),
            min(
                bottom - paddingBottom,
                top + paddingTop + rimSize).toFloat(),
            max(
                left + paddingLeft,
                right - paddingRight - rimSize).toFloat(),
            max(
                top + paddingTop,
                bottom - paddingBottom - rimSize).toFloat())

        tickCoordinates = calculateTickCoordinates(12, dialBounds)
    }

    protected open fun onValueChange(oldValue: Int, newValue: Int) {
        invalidate()
    }

    protected open fun onColorChange(oldValue: Int, newValue: Int) {
        invalidate()
    }

    /**
     * Calculates coordinates to draw tick lines.
     *
     * @param count - Number of ticks to be drawn on full circle.
     * @param bounds - Circle bounds.
     *
     * @return An array of start and end coordinates to draw tick lines.
     *         For each line there will be 4 coordinates in the array:
     *         start x, start y, end x, end y.
     */
    private fun calculateTickCoordinates(count: Int, bounds: RectF): FloatArray {
        val coordinates = FloatArray(count * 4)

        val radius = bounds.width() / 2
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()

        for (tick in 0 until count) {
            val angle: Double = tick * 360.0 / count

            val dx = cos(Math.toRadians(angle).toFloat()) * radius
            val dy = sin(Math.toRadians(angle).toFloat()) * radius

            coordinates[4 * tick + 0] = centerX + dx * 0.95f    // start x
            coordinates[4 * tick + 1] = centerY + dy * 0.95f    // start y
            coordinates[4 * tick + 2] = centerX + dx * 0.75f    // end x
            coordinates[4 * tick + 3] = centerY + dy * 0.75f    // end y
        }

        return coordinates
    }
}