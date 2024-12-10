package com.volcengine.effectone.auto.templates.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.aweme.views.setDebounceOnClickListener
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.utils.inflate
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author tyx
 * @description:
 * @date :2024/4/25 13:53
 */
class TemplatesItemAdapter : RecyclerView.Adapter<TemplatesItemAdapter.TemplatesItemHolder>() {

	private val mTemplatesList = mutableListOf<TemplateItem>()
	var clickBlock: ((view: View, position: Int, data: TemplateItem) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplatesItemHolder {
		return TemplatesItemHolder(parent.inflate(R.layout.item_templates))
	}

	override fun getItemCount(): Int = mTemplatesList.size

	override fun onBindViewHolder(holder: TemplatesItemHolder, position: Int) {
		holder.bind(mTemplatesList[position])
	}

	fun updateItems(newTemplateList: List<TemplateItem>) {
		val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
			override fun getOldListSize() = mTemplatesList.size
			override fun getNewListSize() = newTemplateList.size
			override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = mTemplatesList[oldItemPosition].id == newTemplateList[newItemPosition].id
			override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = mTemplatesList[oldItemPosition] == newTemplateList[newItemPosition]
		})
		mTemplatesList.clear()
		mTemplatesList.addAll(newTemplateList)
		diffResult.dispatchUpdatesTo(this)
	}

	inner class TemplatesItemHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val mTvInfo = view.findViewById<TextView>(R.id.tv_info)
		private val mIvCover = view.findViewById<ImageFilterView>(R.id.iv_cover)
		private val mTvName = view.findViewById<TextView>(R.id.tv_name)

		init {
			itemView.setDebounceOnClickListener {
				val position = absoluteAdapterPosition
				clickBlock?.invoke(itemView, position, mTemplatesList[position])
			}
		}

		fun bind(templateItem: TemplateItem) {
			mTvInfo.text = String.format("%d个素材", templateItem.fragmentCount)
			mTvName.text = templateItem.title

			Glide.with(mIvCover)
				.load(templateItem.cover?.url)
				.placeholder(R.drawable.iv_template_cover_holder)
				.error(R.drawable.iv_template_cover_holder)
				.into(mIvCover)
		}
	}
}