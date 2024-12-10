package com.volcengine.effectone.auto.recorder.hl.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.hl.list.AutoHLRecordVideoListAdapter.HLVideoItemHolder
import com.volcengine.effectone.recordersdk.RecordMediaItem
import com.volcengine.effectone.utils.inflate

/**
 *Author: gaojin
 *Time: 2024/5/29 14:33
 */

class AutoHLRecordVideoListAdapter : RecyclerView.Adapter<HLVideoItemHolder>() {

    private val data = mutableListOf<RecordMediaItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HLVideoItemHolder {
        return HLVideoItemHolder(parent.inflate(R.layout.auto_record_layout_highlight_video_list_item))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: HLVideoItemHolder, position: Int) {
        data.getOrNull(position)?.let {
            holder.bind(it)
        }
    }

    fun updateItems(newList: List<RecordMediaItem>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = data.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                data[oldItemPosition].path == newList[newItemPosition].path

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                data[oldItemPosition].path == newList[newItemPosition].path
        })
        data.clear()
        data.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class HLVideoItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ShapeableImageView = itemView.findViewById(R.id.auto_record_hl_image_view)

        fun bind(item: RecordMediaItem) {
            EffectOneSdk.imageLoader.loadImageView(imageView, item.path)
        }
    }
}