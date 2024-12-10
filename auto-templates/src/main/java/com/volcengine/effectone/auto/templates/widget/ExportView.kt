package com.volcengine.effectone.auto.templates.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:
 * @date :2024/5/20 10:07
 */
class ExportView : View {

	private lateinit var mPaint: Paint
	private lateinit var mLinePaint: Paint
	private var mLineColor = ContextCompat.getColor(context, R.color.tab_indicator_start_color)
	private var mLineWidth = SizeUtil.dp2px(2.5f)
	private var mProgressColor = Color.parseColor("#4CFFFFFF")
	private var mProgress = 0f
	private val mRectF by lazy { RectF() }
	private var mPadding = SizeUtil.dp2px(36f).toFloat()
	private var isEnd = false

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		initPaint()
	}

	private fun initPaint() {
		mPaint = Paint()
		mPaint.run {
			isAntiAlias = true
			isDither = true
			style = Paint.Style.FILL
			color = mProgressColor
		}
		mLinePaint = Paint()
		mLinePaint.run {
			isDither = true
			isAntiAlias = true
			color = mLineColor
			style = Paint.Style.FILL_AND_STROKE
			strokeWidth = mLineWidth.toFloat()
		}
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		val left = mProgress * measuredWidth
		mRectF.set(left, mPadding, measuredWidth.toFloat(), measuredHeight - mPadding)
		canvas.drawRect(mRectF, mPaint)
		if (!isEnd) {
			val x = mRectF.left - mLineWidth / 2f
			canvas.drawLine(x, 0f, x, measuredHeight.toFloat(), mLinePaint)
		}
	}

	fun updateProgress(progress: Float) {
		mProgress = progress
		invalidate()
	}

	fun updateCanvasSize(videoWidth: Int, videoHeight: Int) {
		val lp = layoutParams
		layoutParams = lp.apply {
			width = videoWidth
			height = videoHeight + (mPadding * 2).toInt()
		}
	}

	fun updateState(isEnd: Boolean) {
		this.isEnd = isEnd
		invalidate()
	}
}