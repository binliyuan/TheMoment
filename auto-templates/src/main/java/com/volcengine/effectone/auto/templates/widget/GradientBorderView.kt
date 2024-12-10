package com.volcengine.effectone.auto.templates.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.bytedance.ies.cutsame.util.SizeUtil
import com.volcengine.effectone.auto.templates.R

/**
 * @author tyx
 * @description:
 * @date :2024/4/28 11:37
 */
class GradientBorderView : View {

	private lateinit var mPaint: Paint
	private var mBorderStartColor = ContextCompat.getColor(context, R.color.tab_indicator_start_color)
	private var mBorderEndColor = ContextCompat.getColor(context, R.color.tab_indicator_end_color)
	private var mRadius = SizeUtil.dp2px(6f)
	private var mBorderWidth = SizeUtil.dp2px(1.5f)
	private lateinit var mLinearGradient: LinearGradient

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		attrs?.let {
			val attributes = context.obtainStyledAttributes(R.styleable.GradientBorderView)
			mBorderStartColor = attributes.getColor(R.styleable.GradientBorderView_border_start_color, mBorderStartColor)
			mBorderEndColor = attributes.getColor(R.styleable.GradientBorderView_border_end_color, mBorderEndColor)
			mRadius = attributes.getDimensionPixelSize(R.styleable.GradientBorderView_border_radius, mRadius)
			mBorderWidth = attributes.getDimensionPixelSize(R.styleable.GradientBorderView_border_width, mBorderWidth)
			attributes.recycle()
		}
		initPaint()
	}

	private fun initPaint() {
		mPaint = Paint()
		mPaint.isDither = true
		mPaint.isAntiAlias = true
		mPaint.style = Paint.Style.STROKE
		mPaint.strokeWidth = mBorderWidth.toFloat()
	}

	@SuppressLint("DrawAllocation")
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		mLinearGradient =
			LinearGradient(
				0f, 0f,
				measuredWidth.toFloat(),
				0f,
				intArrayOf(mBorderStartColor, mBorderEndColor), null,
				Shader.TileMode.CLAMP
			)
	}

	private val mRectF by lazy { RectF() }

	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		canvas ?: return
		mPaint.setShader(mLinearGradient)
		mRectF.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
		mRectF.inset(mBorderWidth / 2f, mBorderWidth / 2f)
		canvas.drawRoundRect(mRectF, mRadius.toFloat(), mRadius.toFloat(), mPaint)
	}
}