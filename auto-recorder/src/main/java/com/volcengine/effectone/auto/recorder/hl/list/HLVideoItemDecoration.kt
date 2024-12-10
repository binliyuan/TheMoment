package com.volcengine.effectone.auto.recorder.hl.list

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.volcengine.effectone.utils.EOUtils

/**
 *Author: gaojin
 *Time: 2023/4/20 12:01
 */

class HLVideoItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemPos = parent.getChildAdapterPosition(view)
        if (itemPos != 0) {
            outRect.top = EOUtils.sizeUtil.dp2px(16F)
        }
    }
}