package com.volcengine.effectone.auto.templates.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.inflate

/**
 * @author tyx
 * @description:
 * @date :2024/4/29 15:09
 */
class CutSamePlayerBottomVideoListAdapter : RecyclerView.Adapter<CutSamePlayerBottomVideoListAdapter.CutSamePlayerBottomDockerHolder>() {

	private val mMediaItemList = mutableListOf<MediaItem>()
	private var mProgressIndex = 0
	var clickBlock: ((view: View, position: Int, data: MediaItem) -> Unit)? = null

	fun updateSelectIndex(index: Int): Boolean {
		if (mProgressIndex == index) return false
		notifyItemChanged(mProgressIndex)
		mProgressIndex = index
		notifyItemChanged(mProgressIndex)
		return true
	}

	fun updateItems(newMediaItems: List<MediaItem>) {
		val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
			override fun getOldListSize() = mMediaItemList.size
			override fun getNewListSize() = newMediaItems.size
			override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
				mMediaItemList[oldItemPosition].materialId == newMediaItems[newItemPosition].materialId

			override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = mMediaItemList[oldItemPosition] == newMediaItems[newItemPosition]
		})
		mMediaItemList.clear()
		mMediaItemList.addAll(newMediaItems)
		diffResult.dispatchUpdatesTo(this)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CutSamePlayerBottomDockerHolder {
		return CutSamePlayerBottomDockerHolder(parent.inflate(R.layout.item_cutsame_player_bottom_video))
	}

	override fun getItemCount(): Int {
		return mMediaItemList.size
	}

	override fun onBindViewHolder(holder: CutSamePlayerBottomDockerHolder, position: Int) {
		holder.bind(mMediaItemList[position], position)
	}

	inner class CutSamePlayerBottomDockerHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val mTvNum = view.findViewById<TextView>(R.id.tv_num)
		private val mIvCover = view.findViewById<ImageFilterView>(R.id.iv_cover)
		private val mTvTime = view.findViewById<TextView>(R.id.tv_time)
		private val mGroupEdit = view.findViewById<Group>(R.id.group_edit)
		private val mViewEdit = view.findViewById<View>(R.id.video_edit_bg)

		init {
			mIvCover.setDebounceOnClickListener {
				val pos = absoluteAdapterPosition
				clickBlock?.invoke(mIvCover, pos, mMediaItemList[pos])
			}
			mViewEdit.setDebounceOnClickListener {
				val pos = absoluteAdapterPosition
				clickBlock?.invoke(mViewEdit, pos, mMediaItemList[pos])
			}
		}

		@SuppressLint("SetTextI18n")
		fun bind(mediaItem: MediaItem, position: Int) {
			Glide.with(mIvCover)
				.load(mediaItem.source)
				.into(mIvCover)
			mTvNum.text = "${position + 1}"
			val duration = (1f * mediaItem.duration / 1000f)
			mTvTime.text = String.format("%.1fs", duration)
			mGroupEdit.visible = position == mProgressIndex
		}
	}
}