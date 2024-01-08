package ru.alox1d.customviewtest.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.alox1d.customviewtest.R
import ru.alox1d.ext.dpToPx

// @JvmOverloads переопределяет дефольные конструкторы нашей View
class AvatarImageViewMask @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet, defStyleAttr) {

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)

    @ColorInt
    private var borderColor: Int = DEFAULT_BORDER_COLOR
    private var initials: String = "??"

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private lateinit var resultBm: Bitmap
    private lateinit var maskBm: Bitmap
    private lateinit var srcBm: Bitmap

    init {
        if (attributeSet != null) {
            val ta = context.obtainStyledAttributes(attributeSet, R.styleable.AvatarImageViewMask)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageViewMask_aivm_borderWidth,
                context.dpToPx(DEFAULT_BORDER_WIDTH)
            )
            borderColor = ta.getColor(
                R.styleable.AvatarImageViewMask_aivm_borderColor,
                DEFAULT_BORDER_COLOR
            )
            initials = ta.getString(
                R.styleable.AvatarImageViewMask_aivm_initials,
            ) ?: "??"

            ta.recycle() // высвобождение атрибутов
        }

        scaleType = ScaleType.CENTER_CROP
        setup()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("AvatarImageViewMask", "onAttachedToWindow: ")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(
            "AvatarImageViewMask", """OnMeasure
            width: ${MeasureSpec.toString(widthMeasureSpec)}
            height: ${MeasureSpec.toString(heightMeasureSpec)}
        """.trimIndent()
        )

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)

        Log.d(
            "AvatarImageViewMask",
            "after onMeasure set size: $measuredWidth height: $measuredHeight"
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("AvatarImageViewMask", "onSizeChanged: ")

        if (w == 0) return
        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareBitmaps(w, h)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("AvatarImageViewMask", "onLayout: ")
    }

    override fun onDraw(canvas: Canvas) {
        // super.onDraw(canvas)
        Log.d("AvatarImageViewMask", "onDraw: ")
        // NO allocation for object, ONLY draw
        // do allocation in init block
        canvas.drawBitmap(resultBm, viewRect, viewRect, null)
        //resize rect
        val half = (borderWidth / 2).toInt()
        viewRect.inset(half, half)
        canvas.drawOval(viewRect.toRectF(), borderPaint)
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(DEFAULT_SIZE).toInt() // resolve default size

            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec) // from spec

            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun setup() {
        with(maskPaint) {
            color = Color.RED
            style = Paint.Style.FILL
        }

        with(borderPaint) {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }

    }

    private fun prepareBitmaps(w: Int, h: Int) {
        // prepare buffer

        maskBm = Bitmap.createBitmap(
            w,
            h,
            Bitmap.Config.ALPHA_8
        ) // only alpha channel, less memory usage
        resultBm = maskBm.copy(Bitmap.Config.ARGB_8888, true)

        val maskCanvas = Canvas(maskBm)
        maskCanvas.drawOval(viewRect.toRectF(), maskPaint)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        srcBm = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)

        val resultCanvas = Canvas(resultBm)

        resultCanvas.drawBitmap(maskBm, viewRect, viewRect, null)
        resultCanvas.drawBitmap(srcBm, viewRect, viewRect, maskPaint)
    }

    companion object {
        private const val DEFAULT_SIZE = 40
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
    }
}