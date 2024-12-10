package com.volcengine.effectone.auto.recorder.beauty.item

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AutoBeautyItemDecoration(private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position: Int = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: return
        val layoutManager = parent.layoutManager ?: return
        when (layoutManager) {
            is GridLayoutManager -> {
                adapterGridLayout(outRect, position, itemCount, layoutManager)
            }
            else -> {}
        }
    }

    private fun adapterGridLayout(
        outRect: Rect,
        position: Int,
        itemCount: Int,
        layoutManager: GridLayoutManager,
    ) {
        val spanCount = layoutManager.spanCount
        val column = position % spanCount
        val horizontalSpacing = spacing / spanCount
        val verticalSpacing = spacing
        if (includeEdge) {
            outRect.left = horizontalSpacing - column * horizontalSpacing
            outRect.right = (column + 1) * horizontalSpacing - horizontalSpacing
        } else {
            outRect.left = column * horizontalSpacing
            outRect.right = spanCount - (column + 1) * horizontalSpacing
        }
        outRect.top = verticalSpacing
        outRect.bottom = verticalSpacing
    }
}