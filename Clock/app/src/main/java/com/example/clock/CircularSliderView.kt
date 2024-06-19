package com.example.clock
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularSliderView : View {

    // Slider attributes
    private val strokeWidths = 100f
    private val knobRadius = 55f
    val arcStartAngle = 135f
    val arcSweepAngle = 270f
    private var knobAngle = arcStartAngle
    private var currentAngle = 135f

    // Paint objects
    private val sliderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidths
        color = Color.argb(255,0,0,0)
    }
    private val knobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(255,255,255,255)
    }
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 55.0f
        color = Color.argb(255,105,105,105)
    }

    // Callback for slider value change
    var onSliderChangeListener: ((Int) -> Unit)? = null

    // Flag to track if timer is running
    var isTimerRunning: Boolean = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) - strokeWidths / 2f

        val arc = android.graphics.RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        val arcFilled = android.graphics.RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(arc, arcStartAngle, arcSweepAngle, false, sliderPaint)
        canvas.drawArc(arcFilled, arcStartAngle, knobAngle-135, false, whitePaint)

        val knobX = centerX + radius * cos(Math.toRadians(knobAngle.toDouble())).toFloat()
        val knobY = centerY + radius * sin(Math.toRadians(knobAngle.toDouble())).toFloat()
        canvas.drawCircle(knobX, knobY, knobRadius, knobPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        currentAngle = knobAngle
        if (!isTimerRunning) {
            event?.let {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        updateKnobPosition(event.x, event.y)
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        updateKnobPosition(event.x, event.y)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        return true
                    }
                    else -> return false
                }
            }
        }
        return super.onTouchEvent(event)
    }


    private fun updateKnobPosition(touchX: Float, touchY: Float) {
        val centerX = width / 2f
        val centerY = height / 2f

        // Calculate angle of touch point relative to center
        val angle = Math.toDegrees(atan2(touchY - centerY, touchX - centerX).toDouble()).toFloat()

        // Normalize angle to be within arcStartAngle and arcStartAngle + arcSweepAngle
        var newAngle = if (angle < 0) angle + 360f else angle
        val minAngle = arcStartAngle
        val maxAngle = arcStartAngle + arcSweepAngle

        // Adjust for wrapping around
        if (newAngle < minAngle) {
            newAngle += 360f
        }

        knobAngle = newAngle.coerceIn(minAngle, maxAngle)

        invalidate()

        // Notify listener about slider value change
        val percentage = ((knobAngle - arcStartAngle) / arcSweepAngle * 100).toInt()
        onSliderChangeListener?.invoke(percentage)
    }

    fun setValue(angle: Float){
        knobAngle = angle
        invalidate()
    }

    fun resetSlider(){
        knobAngle = arcStartAngle
        invalidate()
    }

}
