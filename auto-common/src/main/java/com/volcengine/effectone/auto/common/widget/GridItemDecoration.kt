package com.volcengine.effectone.auto.common.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 14:02
 */
class GridItemDecoration(private val mSpanCount: Int, private val mColumnSpacing: Int, private val mRowSpacing: Int) : RecyclerView.ItemDecoration() {
	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		super.getItemOffsets(outRect, view, parent, state)
		val position = parent.getChildAdapterPosition(view)
		val column = position % mSpanCount
		outRect.left = column * mColumnSpacing / mSpanCount
		outRect.right = mColumnSpacing - (column + 1) * mColumnSpacing / mSpanCount
		outRect.bottom = mRowSpacing
	}
}