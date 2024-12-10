package com.volcengine.effectone.auto.templates.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.SizeUtil
import kotlin.math.roundToInt

/**
 * @author tyx
 * @description:
 * @date :2024/5/9 21:33
 */
class ThumbTextSeekBar : AppCompatSeekBar {

	private lateinit var mTextPaint: Paint
	private var mTextSize = SizeUtil.sp2px(18f)
	private var mTextColor = ContextCompat.getColor(context, R.color.color_EEE)
	private var mThumbSize = SizeUtil.dp2px(21f)
	private var mProgressTextMargin = SizeUtil.dp2px(2f)
	private var mProgressText = "100"

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		initPaint()
		max = 200
		setPadding(mThumbSize / 2, 0, mThumbSize / 2, 0)
	}

	private fun initPaint() {
		mTextPaint = Paint()
		mTextPaint.isAntiAlias = true
		mTextPaint.isDither = true
		mTextPaint.textSize = mTextSize.toFloat()
		mTextPaint.color = mTextColor
	}

	private val mBound by lazy { Rect() }

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		val width = MeasureSpec.getSize(widthMeasureSpec)
		val height = MeasureSpec.getSize(heightMeasureSpec)
		mTextPaint.getTextBounds(mProgressText, 0, mProgressText.length, mBound)
		setMeasuredDimension(width, height + mProgressTextMargin + mBound.height())
	}

	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		val progressRatio = progress / 1f / max
		mProgressText = maxOf(0, (progressRatio * max).roundToInt()).toString()
		mTextPaint.getTextBounds(mProgressText, 0, mProgressText.length, mBound)
		val fontMetrics = mTextPaint.fontMetrics
		val textCenterY = measuredHeight / 2f - mThumbSize / 2f - mProgressTextMargin - mBound.height() / 2f
		val baseLine = textCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
		val offset = ((paddingLeft + paddingRight) - mBound.width()) / 2 - (paddingLeft + paddingRight) * progressRatio
		val startX = measuredWidth * progressRatio + offset
		val textX = minOf(startX, measuredWidth - paddingRight - mBound.width().toFloat())
		canvas?.drawText(mProgressText, textX, baseLine, mTextPaint)
	}
}