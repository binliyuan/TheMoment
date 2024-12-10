package com.volcengine.effectone.auto.moment.hl.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import com.volcengine.effectone.auto.moment.R
import kotlin.math.max
import kotlin.math.roundToInt

class AutoHighLightSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AppCompatSeekBar(context, attrs, defStyleAttr) {
    private val highLightBgPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = highLightBgColor
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.FILL
        }
    }

    private var highLightBgColor = ContextCompat.getColor(context, R.color.Primary)
    private val highLightRangeRect = RectF()
    private var highLightRangeStart = 0f
    private var highLightRangeEnd = 0f
    private var drawHighLightBgAvailable = false

    init {
        var typedArray:TypedArray? = null
        try {
             typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.AutoHighLightSeekBar,
                defStyleAttr,
                0
            )
            typedArray.getColor(
                R.styleable.AutoHighLightSeekBar_hlBgColor,
                ContextCompat.getColor(context, R.color.Primary)
            ).also {
                highLightBgColor = it
                highLightBgPaint.color = it
            }
        } finally {
            typedArray?.recycle()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.takeIf {
            drawHighLightBgAvailable
        }?.run {
            drawHighLightRange(this)
        }
        super.onDraw(canvas)
    }

    private fun drawHighLightRange(canvas: Canvas) {
        (progressDrawable as? LayerDrawable)?.let {
            val backgroundProgress = it.findDrawableByLayerId(android.R.id.background) ?: return
            val bounds = backgroundProgress.bounds
            val paddingStart = max(paddingStart, paddingLeft)
            val paddingEnd = max(paddingEnd, paddingRight)
            val hlStart =
                (highLightRangeStart * bounds.width()).roundToInt().coerceAtLeast(0) + paddingStart
            val hlEnd = (highLightRangeEnd * bounds.width()).roundToInt()
                .coerceAtMost(bounds.width()) + paddingEnd
            val cornerRadius = bounds.height().toFloat()/2

            highLightRangeRect.set(
                hlStart.toFloat(),
                bounds.top.toFloat() + paddingTop,
                hlEnd.toFloat(),
                bounds.bottom.toFloat() + paddingTop
            )
            canvas.drawRoundRect(highLightRangeRect,cornerRadius,cornerRadius, highLightBgPaint)
        }
    }

    override fun getMax(): Int {
        return super.getMax()
    }

    override fun getMin(): Int {
        return super.getMin()
    }

    /**
     * 转换成两位小数
     */
    fun setHighLightRange(
        @FloatRange(from = 0.0) highLightRangeStart: Float,
        highLightRangeEnd: Float
    ) {
        drawHighLightBgAvailable =
            highLightRangeEnd >= 0f && highLightRangeStart < highLightRangeEnd
        if (drawHighLightBgAvailable.not()) {
            return
        }
        //转换成百分比小数
        this.highLightRangeStart = highLightRangeStart.format(2).toFloat()
        this.highLightRangeEnd = highLightRangeEnd.format(2).toFloat()
        invalidate()
    }

    fun Float.format(digits: Int) = "%.${digits}f".format(this)
    fun Int.format(digits: Int) = this.toFloat().format(digits)
}