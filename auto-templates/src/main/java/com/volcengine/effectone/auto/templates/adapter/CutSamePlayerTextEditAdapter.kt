package com.volcengine.effectone.auto.templates.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.PlayerTextEditItem
import com.volcengine.effectone.utils.inflate

/**
 * @author tyx
 * @description:
 * @date :2024/5/8 17:22
 */
class CutSamePlayerTextEditAdapter : RecyclerView.Adapter<CutSamePlayerTextEditAdapter.CutSamePlayerTextEditHolder>() {

	private val mItems = mutableListOf<PlayerTextEditItem>()
	private var mSelectIndex = 0
	var clickBlock: ((view: View, position: Int, data: PlayerTextEditItem) -> Unit)? = null
	private val mBitmapMap by lazy { mutableMapOf<Int, Bitmap?>() }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CutSamePlayerTextEditHolder {
		return CutSamePlayerTextEditHolder(parent.inflate(R.layout.item_cutsame_player_text_edit))
	}

	override fun getItemCount(): Int {
		return mItems.size
	}

	override fun onBindViewHolder(holder: CutSamePlayerTextEditHolder, position: Int, payloads: MutableList<Any>) {
		if (payloads.isNotEmpty()) {
			val bundle = payloads.firstOrNull() as? Bundle
			bundle?.let {
				val text = bundle.getString("text")
				holder.mTvText.text = text?.ifEmpty { "无内容" } ?: "无内容"
			}
		} else super.onBindViewHolder(holder, position, payloads)
	}

	override fun onBindViewHolder(holder: CutSamePlayerTextEditHolder, position: Int) {
		holder.bind(mItems[position], position)
	}

	fun updateItems(newTextItems: List<PlayerTextEditItem>) {
		val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
			override fun getOldListSize() = mItems.size
			override fun getNewListSize() = newTextItems.size
			override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
				mItems[oldItemPosition].textItem.materialId == newTextItems[newItemPosition].textItem.materialId

			override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = mItems[oldItemPosition] == newTextItems[newItemPosition]
		})
		mItems.clear()
		mItems.addAll(newTextItems)
		diffResult.dispatchUpdatesTo(this)
	}

	fun refreshCurrentItemText(text: String) {
		val bundle = Bundle()
		bundle.putString("text", text)
		notifyItemChanged(mSelectIndex, bundle)
	}

	fun addThumbBitmap(bitmap: MutableMap<Int, Bitmap?>) {
		releaseBitmap()
		mBitmapMap.putAll(bitmap)
		notifyItemRangeChanged(0, mItems.size)
	}

	fun releaseBitmap() {
		mBitmapMap.forEach {
			it.value?.recycle()
		}
		mBitmapMap.clear()
	}

	fun getData() = mItems

	fun updateSelectIndex(index: Int): Boolean {
		if (mSelectIndex == index) return false
		notifyItemChanged(mSelectIndex)
		mSelectIndex = index
		notifyItemChanged(mSelectIndex)
		return true
	}

	fun getCurrentSelectData(): PlayerTextEditItem? {
		return mItems.getOrNull(mSelectIndex)
	}

	inner class CutSamePlayerTextEditHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val mTvNum = view.findViewById<TextView>(R.id.tv_num)
		private val mIvCover = view.findViewById<ImageFilterView>(R.id.iv_cover)
		val mTvText = view.findViewById<TextView>(R.id.tv_text)
		private val mGroupEdit = view.findViewById<Group>(R.id.group_edit)
		private val mViewEdit = view.findViewById<View>(R.id.text_edit_bg)

		init {
			mIvCover.setDebounceOnClickListener {
				val pos = absoluteAdapterPosition
				updateSelectIndex(pos)
				clickBlock?.invoke(mIvCover, pos, mItems[pos])
			}
			mViewEdit.setDebounceOnClickListener {
				val pos = absoluteAdapterPosition
				clickBlock?.invoke(mViewEdit, pos, mItems[pos])
			}
		}

		@SuppressLint("SetTextI18n")
		fun bind(data: PlayerTextEditItem, position: Int) {
			val bitmap = mBitmapMap[data.startTime.toInt()]
			bitmap?.let {
				mIvCover.setImageBitmap(it)
			}
			mTvNum.text = "${position + 1}"
			mGroupEdit.visible = mSelectIndex == position
			mTvText.text = data.editText.ifEmpty { "无内容" }
			mTvText.visible = mSelectIndex != position
		}
	}
}