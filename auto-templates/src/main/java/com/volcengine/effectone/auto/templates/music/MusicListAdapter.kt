package com.volcengine.effectone.auto.templates.music

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.music.MusicItem.Companion
import com.volcengine.effectone.auto.templates.music.MusicListAdapter.MusicViewHolder
import com.volcengine.effectone.auto.templates.utils.TimeUtil
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.utils.inflate

/**
 *Author: gaojin
 *Time: 2023/12/25 19:24
 */

class MusicListAdapter : RecyclerView.Adapter<MusicViewHolder>() {

	var clickAction: ((view: View, item: MusicItem) -> Unit)? = null

	val mMusicItems: MutableList<MusicItem> = mutableListOf()
	private var currentSelectedIndex = 0
	private var currentPreviewIndex = -1

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
		return MusicViewHolder(parent.inflate(R.layout.layout_music_item))
	}

	override fun getItemCount() = mMusicItems.size

	override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
		holder.bind(mMusicItems[position])
	}

	fun selectItem(position: Int) {
		val oldIndex = currentSelectedIndex
		currentSelectedIndex = position

		mMusicItems.getOrNull(oldIndex)?.isSelected = false
		mMusicItems.getOrNull(position)?.isSelected = true

		notifyItemChanged(oldIndex)
		notifyItemChanged(position)
	}

	fun previewItem(position: Int) {
		val oldIndex = currentPreviewIndex
		currentPreviewIndex = position

		mMusicItems.getOrNull(oldIndex)?.isPreview = false
		mMusicItems.getOrNull(position)?.isPreview = true

		notifyItemChanged(oldIndex)
		notifyItemChanged(position)
	}

	fun updateItems(newMusicItems: List<MusicItem>) {
		val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
			override fun getOldListSize() = mMusicItems.size
			override fun getNewListSize() = newMusicItems.size
			override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
				mMusicItems[oldItemPosition].id == newMusicItems[newItemPosition].id

			override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = mMusicItems[oldItemPosition] == newMusicItems[newItemPosition]
		})
		mMusicItems.clear()
		mMusicItems.addAll(newMusicItems)
		currentSelectedIndex = mMusicItems.indexOfFirst { it.isSelected }
		diffResult.dispatchUpdatesTo(this)
	}

	inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

		private val layoutContainer = itemView.findViewById<View>(R.id.layout_container)
		private val iconView: ImageView = itemView.findViewById(R.id.music_icon)
		private val musicName: TextView = itemView.findViewById(R.id.music_name)
		private val musicDuration: TextView = itemView.findViewById(R.id.music_duration)
		private val musicSelected: View = itemView.findViewById(R.id.music_selected_icon)
		private val btnConfirm: Button = itemView.findViewById(R.id.btn_confirm)

		init {
			itemView.setDebounceOnClickListener {
				val position = absoluteAdapterPosition
				if (position == currentPreviewIndex) return@setDebounceOnClickListener
				if (mMusicItems[position].isSelected) return@setDebounceOnClickListener
				previewItem(position)
			}
			btnConfirm.setDebounceOnClickListener {
				val position = absoluteAdapterPosition
				if (position == currentSelectedIndex) return@setDebounceOnClickListener
				previewItem(-1)
				selectItem(position)
				clickAction?.invoke(btnConfirm, mMusicItems[position])
			}
		}

		fun bind(item: MusicItem) {
			val option = RequestOptions()
				.transform(CenterCrop(), RoundedCorners(EOUtils.sizeUtil.dp2px(8f)))
				.format(DecodeFormat.PREFER_RGB_565)
			Glide.with(iconView)
				.load(if (item.id == Companion.DEFAULT_ID) R.drawable.icon_template_music_def else item.iconPath())
				.apply(option)
				.into(iconView)
			musicName.text = item.name
			btnConfirm.text = if (item.id == MusicItem.DEFAULT_ID) "还原" else "使用"
			musicDuration.text = TimeUtil.stringForTime(item.duration)

			layoutContainer.setBackgroundResource(if (item.isPreview) R.color.color_1B1D26 else 0)
			btnConfirm.visible = item.isPreview
			if (item.isSelected) {
				musicSelected.visibility = View.VISIBLE
			} else {
				musicSelected.visibility = View.GONE
			}
		}
	}
}