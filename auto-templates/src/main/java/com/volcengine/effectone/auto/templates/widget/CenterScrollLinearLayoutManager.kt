package com.volcengine.effectone.auto.templates.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * @author tyx
 * @description: 横向滑动选中居中
 * @date :2024/5/6 16:36
 */
class CenterScrollLinearLayoutManager : LinearLayoutManager {
	constructor(context: Context?) : super(context)
	constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

	override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
		val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
			override fun calculateDtToFit(viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int): Int {
				return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
			}
		}
		smoothScroller.targetPosition = position
		startSmoothScroll(smoothScroller)
	}
}
