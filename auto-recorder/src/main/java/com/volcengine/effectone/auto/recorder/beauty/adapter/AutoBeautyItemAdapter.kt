package com.volcengine.effectone.auto.recorder.beauty.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.recorderui.beauty.BeautyItemAdapter
import com.volcengine.effectone.recorderui.beauty.OnItemClickListener
import com.volcengine.effectone.recorderui.beauty.data.ComposerCloseItem
import com.volcengine.effectone.recorderui.beauty.data.ComposerItem
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.inflate

class AutoBeautyItemAdapter(
    itemList: List<ComposerItem>,
    closeItem: ComposerCloseItem,
    listener: OnItemClickListener,
    showPoint: Boolean = true
) : BeautyItemAdapter(itemList, closeItem, listener, showPoint) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AutoViewHolder(parent.inflate(R.layout.auto_recorder_layout_beauty_item))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.bindCloseItem()
        } else {
            getItemByPosition(position)?.let {
                holder.bind(it)
                (holder as? AutoViewHolder)?.bindAuto(it)
            }
        }
    }

    inner class AutoViewHolder(itemView: View) : ViewHolder(itemView) {
        private val strokeImageView: ShapeableImageView =
            itemView.findViewById(R.id.auto_record_sticker_item_view_stroke_view)
        private val intensity: TextView =
            itemView.findViewById(R.id.eo_recorder_beauty_item_intensity)
        private val textView: TextView =
            itemView.findViewById(R.id.eo_recorder_beauty_item_text)

        fun bindAuto(it: ComposerItem) {
            strokeImageView.visibility = if (it.isSelected) View.VISIBLE else View.INVISIBLE
            bindIntensity(it)
            textView.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.auto_tab_tile_selected))
        }

        private fun bindIntensity(composerItem: ComposerItem) {
            intensity.visibility = View.VISIBLE
            var intensityValue =  composerItem.getIntensityValue()
            if (intensityValue == 0 && composerItem.hasChild()) {
                composerItem.items.firstOrNull { childItem -> childItem.isSelected }?.let {
                    intensityValue = it.getIntensityValue()
                }
            }
            intensity.text = "$intensityValue%"
        }

        private fun ComposerItem.getIntensityValue(): Int {
           return if (composeNode.value == -1F) {
                composeNode.getDefaultValue().times(100).toInt()
            }else{
               composeNode.value.times(100).toInt()
            }
        }

        private fun i(it: ComposerItem) =
            if (it.composeNode.value == -1F) {
                it.composeNode.getDefaultValue().times(100).toInt()
            } else {
                it.composeNode.value.times(100).toInt()
            }

        override fun bindCloseItem() {
            super.bindCloseItem()
            intensity.visibility = View.GONE
            strokeImageView.visibility = View.GONE
        }
    }
}