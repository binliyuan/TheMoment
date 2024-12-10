package com.volcengine.effectone.auto.templates.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.volcengine.effectone.auto.templates.R

/**
 * @author tyx
 * @description:
 * @date :2024/4/30 10:10
 */
class TopRoundFrameLayout : FrameLayout {
	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		val attributes = context.obtainStyledAttributes(R.styleable.TopRoundFrameLayout)
		mRadius = attributes.getDimensionPixelSize(R.styleable.TopRoundFrameLayout_ts_round_radius, mRadius.toInt()).toFloat()
		attributes.recycle()
	}

	private val mRectF by lazy { RectF() }
	private var mRadius = 0f
	private val mPath by lazy { Path() }

	fun setRadius(radius: Float) {
		mRadius = radius
		invalidate()
	}

	override fun dispatchDraw(canvas: Canvas?) {
		canvas ?: return
		canvas.save()
		mRectF.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat() + mRadius)
		mPath.reset()
		mPath.addRoundRect(mRectF, mRadius, mRadius, Path.Direction.CCW)
		canvas.clipPath(mPath)
		super.dispatchDraw(canvas)
		canvas.restore()
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		return true
	}
}