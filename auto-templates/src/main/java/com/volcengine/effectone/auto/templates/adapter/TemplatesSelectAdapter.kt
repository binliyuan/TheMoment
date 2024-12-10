package com.volcengine.effectone.auto.templates.adapter

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.TemplatesByMedias
import com.volcengine.effectone.utils.inflate

/**
 * @author tyx
 * @description:
 * @date :2024/4/26 17:47
 */
class TemplatesSelectAdapter : RecyclerView.Adapter<TemplatesSelectAdapter.TemplatesSelectViewHolder>() {

	val mTemplatesList = mutableListOf<TemplatesByMedias>()
	var clickBlock: ((view: View, position: Int, data: TemplatesByMedias) -> Unit)? = null

	fun updateItems(newTemplateList: List<TemplatesByMedias>) {
		val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
			override fun getOldListSize() = mTemplatesList.size
			override fun getNewListSize() = newTemplateList.size
			override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
				mTemplatesList[oldItemPosition].data.id == newTemplateList[newItemPosition].data.id

			override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = mTemplatesList[oldItemPosition] == newTemplateList[newItemPosition]
			override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Bundle? {
				val bundle = Bundle()
				val new = newTemplateList[newItemPosition]
				bundle.putBoolean("isSelect", new.select)
				if (bundle.isEmpty) return null
				return bundle
			}
		})
		mTemplatesList.clear()
		mTemplatesList.addAll(newTemplateList)
		diffResult.dispatchUpdatesTo(this)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplatesSelectViewHolder {
		return TemplatesSelectViewHolder(parent.inflate(R.layout.item_templates_select))
	}

	override fun getItemCount(): Int = mTemplatesList.size

	override fun onBindViewHolder(holder: TemplatesSelectViewHolder, position: Int) {
		holder.bind(mTemplatesList[position])
	}

	override fun onBindViewHolder(holder: TemplatesSelectViewHolder, position: Int, payloads: MutableList<Any>) {
		if (payloads.isNotEmpty()) {
			val bundle = payloads.firstOrNull() as? Bundle
			bundle?.let {
				val isSelect = bundle.getBoolean("isSelect")
				holder.mBorder.visibility = if (isSelect) View.VISIBLE else View.GONE
				holder.mTvTempName.visibility = if (isSelect) View.GONE else View.VISIBLE
			}
		} else super.onBindViewHolder(holder, position, payloads)
	}

	inner class TemplatesSelectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		internal val mTvTempName = view.findViewById<TextView>(R.id.tv_temp_name)
		internal val mIvCover = view.findViewById<ImageFilterView>(R.id.iv_cover)
		internal val mBorder = view.findViewById<View>(R.id.view_border)

		init {
			itemView.setDebounceOnClickListener {
				val pos = absoluteAdapterPosition
				clickBlock?.invoke(itemView, pos, mTemplatesList[pos])
			}
		}

		fun bind(templatesByMedias: TemplatesByMedias) {
			Glide.with(mIvCover)
				.load(templatesByMedias.data.cover?.url)
				.into(mIvCover)
			mTvTempName.text = templatesByMedias.data.title
			mTvTempName.visibility = if (templatesByMedias.select) View.GONE else View.VISIBLE
			mBorder.visibility = if (templatesByMedias.select) View.VISIBLE else View.GONE
		}
	}
}