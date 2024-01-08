package ru.alox1d.customviewtest.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.core.animation.doOnRepeat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.alox1d.customviewtest.R
import ru.alox1d.ext.dpToPx
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.truncate

// @JvmOverloads переопределяет дефольные конструкторы нашей View
class AvatarImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet, defStyleAttr) {

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)

    @ColorInt
    private var borderColor: Int = DEFAULT_BORDER_COLOR
    private var initials: String = "??"
    private var isAvatarMode = true

    private val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private val borderRect = Rect()
    private var size = 0


    init {
        if (attributeSet != null) {
            val ta = context.obtainStyledAttributes(attributeSet, R.styleable.AvatarImageView)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageView_aiv_borderWidth,
                context.dpToPx(DEFAULT_BORDER_WIDTH)
            )
            borderColor = ta.getColor(
                R.styleable.AvatarImageView_aiv_borderColor,
                DEFAULT_BORDER_COLOR
            )
            initials = ta.getString(
                R.styleable.AvatarImageView_aiv_initials,
            ) ?: "??"
            ta.recycle() // высвобождение атрибутов
        }

        scaleType = ScaleType.CENTER_CROP
        setup()
        setOnLongClickListener {
            handleLongClick()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(
            "AvatarImageView", """OnMeasure
            width: ${MeasureSpec.toString(widthMeasureSpec)}
            height: ${MeasureSpec.toString(heightMeasureSpec)}
        """.trimIndent()
        )

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(max(initSize, size), max(initSize, size))

        Log.d(
            "AvatarImageView",
            "after onMeasure set size: $measuredWidth height: $measuredHeight"
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("AvatarImageView", "onSizeChanged: ")

        if (w == 0) return
        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareShader(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        // super.onDraw(canvas)
        Log.d("AvatarImageView", "onDraw: ")

        // NO allocation for object, ONLY draw
        // do allocation in init block

        if (drawable != null && isAvatarMode) {
            drawAvatar(canvas)
        } else {
            drawInitials(canvas)
        }

        //resize rect
        val half = (borderWidth / 2).toInt()
        borderRect.set(viewRect)
        borderRect.inset(half, half)
        canvas.drawOval(borderRect.toRectF(), borderPaint)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        if (isAvatarMode) prepareShader(width, height)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (isAvatarMode) prepareShader(width, height)
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (isAvatarMode) prepareShader(width, height)
    }

    fun setInitials(initials: String) {
        this.initials = initials
        if (!isAvatarMode) {
            invalidate()
        }
    }

    fun setBorderColor(@ColorInt color: Int) {
        borderColor = color
        borderPaint.color = borderColor
        invalidate()
    }

    fun setBorderWidth(@Dimension width: Int) {
        borderWidth = context.dpToPx(width)
        borderPaint.strokeWidth = borderWidth
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.isAvatarMode = isAvatarMode
        savedState.borderWidth = borderWidth
        savedState.borderColor = borderColor
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            isAvatarMode = state.isAvatarMode
            borderWidth = state.borderWidth
            borderColor = state.borderColor

            with(borderPaint) {
                color = borderColor
                strokeWidth = borderWidth
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun setup() {
        with(borderPaint) {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }

    private fun prepareShader(w: Int, h: Int) {
        // prepare shader

        if (w == 0 || drawable == null) return

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

    private fun drawAvatar(canvas: Canvas) {
        canvas.drawOval(viewRect.toRectF(), avatarPaint)
    }

    private fun drawInitials(canvas: Canvas) {
        initialsPaint.color = initialsToColor(initials)
        canvas.drawOval(viewRect.toRectF(), initialsPaint)
        with(initialsPaint) {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = height * 0.33f
        }
        val offsetY = abs(initialsPaint.descent() + initialsPaint.ascent()) / 2
        canvas.drawText(
            initials,
            viewRect.exactCenterX(),
            viewRect.exactCenterY() + offsetY,
            initialsPaint
        )
    }

    private fun initialsToColor(letters: String): Int {
        val b = letters[0].code.toByte()
        val len = bgColors.size
        val devidedChar = b / len.toDouble()
        val index = ((devidedChar - truncate(devidedChar)) * len).toInt()
        return bgColors[index]
    }

    private fun handleLongClick(): Boolean {
        changeModeViaAnimation()
        return true
    }

    private fun changeModeViaAnimation() {
        val va = ValueAnimator.ofInt(width, width * 2).apply {
            duration = 300
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1
        }
        va.addUpdateListener {
            size = it.animatedValue as Int
            requestLayout()
        }
        va.doOnRepeat { toggleMode() }
        va.start()
    }

    private fun toggleMode() {
        isAvatarMode = !isAvatarMode
        invalidate()
    }

    private class SavedState : BaseSavedState, Parcelable {
        @Px
        var borderWidth: Float = 0f

        @ColorInt
        var borderColor: Int = 0
        var isAvatarMode = true

        constructor(parcel: Parcel) : super(parcel) {
            borderWidth = parcel.readFloat()
            borderColor = parcel.readInt()
            isAvatarMode = parcel.readByte() != 0.toByte()
        }

        constructor(src: Parcel, loader: ClassLoader?) : super(src, loader) {
            isAvatarMode = src.readInt() == 1
            borderWidth = src.readFloat()
            borderColor = src.readInt()
        }

        constructor(superState: Parcelable?) : super(superState)


        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (isAvatarMode) 1 else 0)
            dst.writeFloat(borderWidth)
            dst.writeInt(borderColor)
        }

        override fun describeContents() = 0

        companion object {

            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<SavedState> = object :
                Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source, null)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)

                override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState =
                    SavedState(source, loader)
            }
        }

    }

    companion object {
        private const val DEFAULT_SIZE = 40
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE

        val bgColors = arrayOf(
            Color.parseColor("#7BC862"),
            Color.parseColor("#7BC862"),
            Color.parseColor("#7BC862"),
            Color.parseColor("#7BC862"),
            Color.parseColor("#7BC862"),
            Color.parseColor("#7BC862"),
            Color.parseColor("#7BC862"),
            Color.parseColor("#7BC862"),
        )
    }
}