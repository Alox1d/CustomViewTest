package ru.alox1d.customviewtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TestView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private val paint = Paint()

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.RED
        canvas.drawCircle(width.toFloat() / 2, height.toFloat() / 2, 100f, paint)

        paint.color = Color.GRAY
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    }
}