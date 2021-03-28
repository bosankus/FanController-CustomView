package tech.androidplay.fancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**Created by
Author: Ankush Bose
Date: 28,March,2021
 **/

/**
 * @JvmOverloads constructor instructs the Kotlin compiler
 * to generate overloads for this function that substitute
 * default parameter values.
 */
class FanController @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    /**
     * Default values
     * [radius]:Radius of the circle.
     * [fanSpeed]:The active selection.
     * [pointPosition]:Position variable which will be used to draw label and indicator
     * [paint]:Paint object along with these styles are initialized here to help speed
     * up the drawing step.
     */
    private var radius = 0.0f
    private var fanSpeed = FanSpeed.OFF
    private val pointPosition: PointF = PointF(0.0f, 0.0f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    /**
     * Default color values for fan speed levels 0,1,2
     * */
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSeedMaxColor = 0

    init {
        // enables the view to accept user input
        isClickable = true

        // Create custom styling from user provided attributes
        context.withStyledAttributes(attributeSet, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    /**
     * method handles onClick event by the user
     */
    override fun performClick(): Boolean {
        // accepts user's click event
        if (super.performClick()) return true

        // changes the fan speed to next() onClick
        fanSpeed = fanSpeed.next()

        // changes the fan speed level text to the next speed label
        contentDescription = resources.getString(fanSpeed.label)

        // forces a call to onDraw() to redraw the view of changed state
        invalidate()
        return true
    }

    /**
     * method is called when view renders it's contents
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Set dial background color to green if selection not off.

        /*paint.color = if (fanSpeed == FanSpeed.OFF) Color.GRAY else Color.GREEN*/

        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSeedMaxColor
        }

        // Draw the dial
        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // Draw the indicator circle
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        // Draw the text labels
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas?.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }

    /**
     * method is called to control view's size when it first appears
     * and appears and each time it's size.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    /**
     * This extension function on the PointF class calculates the X, Y
     * coordinates on the screen for the text label and current indicator
     * (0, 1, 2, or 3), given the current FanSpeed position and radius of the dial.
     * we'll use this in onDraw().
     */
    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    companion object {
        private const val RADIUS_OFFSET_INDICATOR = -35
        private const val RADIUS_OFFSET_LABEL = 30
    }
}

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}