package ru.alox1d.customviewtest.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.alox1d.customviewtest.R
import ru.alox1d.ext.dpToPx

// @JvmOverloads переопределяет дефольные конструкторы нашей View
class AvatarImageViewShader @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet, defStyleAttr) {

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)

    @ColorInt
    private var borderColor: Int = DEFAULT_BORDER_COLOR
    private var initials: String = "??"

    private val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()

    init {
        if (attributeSet != null) {
            val ta = context.obtainStyledAttributes(attributeSet, R.styleable.AvatarImageViewShader)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageViewShader_aivs_borderWidth,
                context.dpToPx(DEFAULT_BORDER_WIDTH)
            )
            borderColor = ta.getColor(
                R.styleable.AvatarImageViewShader_aivs_borderColor,
                DEFAULT_BORDER_COLOR
            )
            initials = ta.getString(
                R.styleable.AvatarImageViewShader_aivs_initials,
            ) ?: "??"

            ta.recycle() // высвобождение атрибутов
        }

        scaleType = ScaleType.CENTER_CROP
        setup()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("AvatarImageViewShader", "onAttachedToWindow: ")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(
            "AvatarImageViewShader", """OnMeasure
            width: ${MeasureSpec.toString(widthMeasureSpec)}
            height: ${MeasureSpec.toString(heightMeasureSpec)}
        """.trimIndent()
        )

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)

        Log.d(
            "AvatarImageViewShader",
            "after onMeasure set size: $measuredWidth height: $measuredHeight"
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("AvatarImageViewShader", "onSizeChanged: ")

        if (w == 0) return
        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareShader(w, h)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("AvatarImageViewShader", "onLayout: ")
    }

    override fun onDraw(canvas: Canvas) {
        // super.onDraw(canvas)
        Log.d("AvatarImageViewShader", "onDraw: ")
        // NO allocation for object, ONLY draw
        // do allocation in init block
        canvas.drawOval(viewRect.toRectF(), avatarPaint)
        //resize rect
        val half = (borderWidth / 2).toInt()
        viewRect.inset(half, half)
        canvas.drawOval(viewRect.toRectF(), borderPaint)
    }

    private fun setup() {
        with(borderPaint){
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }

    private fun prepareShader(w: Int, h: Int) {
        // prepare shader

        val srcBm = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)
        avatarPaint.shader = BitmapShader(srcBm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(DEFAULT_SIZE).toInt() // resolve default size

            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec) // from spec

            else -> MeasureSpec.getSize(spec)
        }
    }

    companion object {
        private const val DEFAULT_SIZE = 40
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
    }
}