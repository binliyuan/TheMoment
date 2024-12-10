package com.volcengine.effectone.auto.recorder.sticker

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.sticker.adapter.EOBaseStickerRecyclerAdapter
import com.volcengine.effectone.sticker.data.EOBaseStickerItemWrapper
import com.volcengine.effectone.utils.inflate
import java.util.Objects

class AutoStickerRecyclerAdapter(private val stickerList: List<EOBaseStickerItemWrapper>) :
    EOBaseStickerRecyclerAdapter(stickerList) {
        companion object{
           private val TYPE_NONE_HASH = Objects.hash(EOBaseStickerItemWrapper.TYPE_NONE)
           private val TYPE_EXPECTANCY_HASH = Objects.hash(EOBaseStickerItemWrapper.TYPE_EXPECTANCY)
        }
    var stickerOtherSelectedAction: ((item: EOBaseStickerItemWrapper, position: Int) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return  when (viewType) {
            TYPE_NONE_HASH -> {
                val itemView = parent.inflate(R.layout.auto_recorder_layout_sticker_none_item)
                return AutoStickerNoneItemViewHolder(itemView)
            }
            TYPE_EXPECTANCY_HASH -> {
                val itemView = parent.inflate(R.layout.auto_recorder_layout_sticker_expectoncy_item)
                return AutoStickerExpectancyItemViewHolder(itemView)
            }
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val type = Objects.hash(stickerList[position].type)
        when (type) {
            TYPE_NONE_HASH -> {
                (holder as? AutoStickerNoneItemViewHolder)?.bind(stickerList[position])
            }
            TYPE_EXPECTANCY_HASH -> {
                (holder as? AutoStickerExpectancyItemViewHolder)?.bind(stickerList[position])
            }
            else -> {
                super.onBindViewHolder(holder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val type = stickerList[position].type
        return Objects.hash(type)
    }

    inner class AutoStickerNoneItemViewHolder(itemView: View) : ViewHolder(itemView) {
        private val imageView: ShapeableImageView = itemView.findViewById(R.id.auto_record_sticker_item_view)
        private val selectedView: ImageView = itemView.findViewById(R.id.auto_record_sticker_item_view_stroke_view)
        init {
            itemView.setNoDoubleClickListener(0) {
                bindClickAction(absoluteAdapterPosition)
            }
        }
        fun bind(item: EOBaseStickerItemWrapper) {
            imageView.setImageResource(R.drawable.auto_record_item_none)

            if (!item.selected) {
                selectedView.visibility = View.INVISIBLE
            }else{
                selectedView.visibility = View.VISIBLE
            }
        }
    }

    inner class AutoStickerExpectancyItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.auto_record_sticker_item_view)
        private val selectedView: ImageView = itemView.findViewById(R.id.auto_record_sticker_item_view_stroke_view)
        init {
            itemView.setNoDoubleClickListener(0) {
                bindClickAction(absoluteAdapterPosition)
            }
        }
        fun bind(item: EOBaseStickerItemWrapper) {
            titleView.text = """敬请
                |期待""".trimMargin()
            if (!item.selected) {
                selectedView.visibility = View.INVISIBLE
            }else{
                selectedView.visibility = View.VISIBLE
            }
        }
    }

     fun bindClickAction( absoluteAdapterPosition: Int) {
        val position = absoluteAdapterPosition
        if (selectedPos != position) {
            stickerList.getOrNull(selectedPos)?.selected = false
        }
        val item = stickerList[position]
        stickerOtherSelectedAction?.invoke(item, position)
    }
}