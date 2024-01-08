package ru.alox1d.customviewtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2

class TestView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    var listener: Listener? = null

    private val paint = Paint()
    private val paintC = Paint()
    private val startAngle = 0f
    private val colors = listOf(
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA,
    )
    private val sweepAngle = 360f / colors.size
    private var buttonClicked = 0

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        paintC.style = Paint.Style.FILL
        paintC.color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircleButton(canvas)
    }

    private fun drawCircleButton(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width.coerceAtMost(height) / 2f

        for (i in colors.indices) {
            paintC.color = colors[i]
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                startAngle + sweepAngle * i,
                sweepAngle,
                true,
                paintC
            )
        }

        paintC.color = colors[buttonClicked]
        canvas.drawCircle(
            centerX,
            centerY,
            radius / 1.7f,
            paintC
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val centerX = width / 2f
        val centerY = height / 2f
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val angle = (Math.toDegrees(
                    atan2(
                        y - centerY,
                        x - centerX
                    ).toDouble()
                ) + 360) % 360
                buttonClicked = (angle / (360 / colors.size)).toInt()
                listener?.onClick(buttonClicked)
                Log.d("MyLog", "Angle: $angle")
                invalidate()
            }
        }
        return true
    }

    interface Listener {
        fun onClick(index: Int)
    }
}