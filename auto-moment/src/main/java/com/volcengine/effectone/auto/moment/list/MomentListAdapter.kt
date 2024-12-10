package com.volcengine.effectone.auto.moment.list

import android.view.View
import android.view.ViewGroup
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
import com.volcengine.ck.moment.base.CKMoment
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.list.MomentListAdapter.MomentItemViewHolder
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.utils.inflate

/**
 *Author: gaojin
 *Time: 2024/5/16 11:43
 */

class MomentListAdapter : RecyclerView.Adapter<MomentItemViewHolder>() {

    private val momentList = mutableListOf<CKMoment>()
    var clickAction: ((Int, CKMoment) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MomentItemViewHolder {
        return MomentItemViewHolder(parent.inflate(R.layout.layout_moment_item))
    }

    override fun getItemCount() = momentList.size

    override fun onBindViewHolder(holder: MomentItemViewHolder, position: Int) {
        holder.bind(momentList[position])
    }

    fun updateItems(newMomentList: List<CKMoment>) {
        if (newMomentList.size == momentList.size) {
            return
        }
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = momentList.size
            override fun getNewListSize() = newMomentList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                momentList[oldItemPosition].id == newMomentList[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                momentList[oldItemPosition] == newMomentList[newItemPosition]
        })
        momentList.clear()
        momentList.addAll(newMomentList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class MomentItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val coverImage: ImageView = view.findViewById(R.id.auto_moment_item_cover)
        private val titleText: TextView = view.findViewById(R.id.auto_moment_item_title)

        init {
            itemView.setDebounceOnClickListener {
                val position = absoluteAdapterPosition
                clickAction?.invoke(position, momentList[position])
            }
        }

        fun bind(moment: CKMoment) {
            titleText.text = moment.title

            val radius = 12F

            val option = RequestOptions()
                .transform(CenterCrop(), RoundedCorners(EOUtils.sizeUtil.dp2px(radius)))
                .format(DecodeFormat.PREFER_RGB_565)

            val url = moment.cover.ifEmpty {
                moment.config.momentTemplates.firstOrNull()?.cover ?: ""
            }
            Glide.with(AppSingleton.instance)
                .load(url)
                .apply(option)
                .into(coverImage)
        }
    }
}