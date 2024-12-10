package com.volcengine.effectone.auto.templates.widget

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import com.volcengine.effectone.utils.SizeUtil

/**
 * @author tyx
 * @description:
 * @date :2024/6/12 11:58
 */
class RvScrollbarItemDecoration : RecyclerView.ItemDecoration() {

	private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private var mPadding = SizeUtil.dp2px(10f)
	private var mStrokeWidth = SizeUtil.dp2px(3f)
	private var mIndicatorHeight = SizeUtil.dp2px(166f)

	init {
		mPaint.color = Color.parseColor("#353848")
		mPaint.style = Paint.Style.STROKE
		mPaint.strokeWidth = mStrokeWidth.toFloat()
		mPaint.strokeCap = Paint.Cap.ROUND
	}

	override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
		super.onDrawOver(c, parent, state)
		val scrollRange = parent.computeVerticalScrollRange()
		val scrollExtent = parent.computeVerticalScrollExtent()
		val scrollOffset = parent.computeVerticalScrollOffset()

		if (scrollExtent == scrollRange) return

		val yRadio = scrollOffset / 1F / (scrollRange - scrollExtent)
		val maxScrollH = scrollExtent - mIndicatorHeight
		val x = parent.width - SizeUtil.dp2px(9f).toFloat()
		val y = yRadio * maxScrollH

		c.drawLine(x, y + mPadding, x, y + mIndicatorHeight - mPadding, mPaint)
	}
}