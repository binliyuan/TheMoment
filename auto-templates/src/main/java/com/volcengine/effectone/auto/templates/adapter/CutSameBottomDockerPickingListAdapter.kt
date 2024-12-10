package com.volcengine.effectone.auto.templates.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.ss.android.ugc.aweme.views.setGlobalDebounceOnClickListener
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.bean.CutSameMediaItem
import com.volcengine.effectone.auto.templates.vm.CutSameBottomDockerViewModel
import java.util.Locale
import kotlin.math.min

class CutSameBottomDockerPickingListAdapter(
    private val cutSameBottomDockerViewModel: CutSameBottomDockerViewModel,
    lifeCycleOwner: LifecycleOwner
) : RecyclerView.Adapter<CutSameBottomDockerPickingListAdapter.PickingItemHolder>() {
    var deleteBlock: ((position: Int) -> Unit)? = null
    var itemClickBlock: ((position: Int, empty: Boolean) -> Unit)? = null
    private val list = mutableListOf<CutSameMediaItem>()
    private var currentPickIndex = 0


    init {
        cutSameBottomDockerViewModel.processPickItem.value?.let {
            for (mediaItem in it) {
                list.add(mediaItem)
            }
        }

        cutSameBottomDockerViewModel.processPickItem.observe(lifeCycleOwner) {
            list.clear()
            it?.apply { list.addAll(it) }
            notifyItemRangeChanged(0, list.size)
        }

        cutSameBottomDockerViewModel.currentPickIndex.observe(lifeCycleOwner) {
            notifyItemChanged(currentPickIndex)// {zh} 刷新上次位置 {en} Refresh last location
            currentPickIndex = it ?: 0
            notifyItemChanged(currentPickIndex)// {zh} 刷新新选中位置 {en} Refresh newly selected location
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickingItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.cutsame_default_picker_segment,
            parent,
            false
        )
        return PickingItemHolder(parent, view)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PickingItemHolder, position: Int) {
        val data = list[position]
        holder.deleteLayout.tag = position
        holder.itemView.tag = position

        loadThumbnail(data.materialItem, holder.thumb) {
            val hasMedia = data.materialItem != null
            if (hasMedia) {
                holder.deleteLayout.visibility = View.VISIBLE
            } else {
                holder.deleteLayout.visibility = View.GONE
            }
            holder.itemView.isSelected = currentPickIndex == position
                    && cutSameBottomDockerViewModel.pickFull.value != true
            holder.durationTv.isSelected = hasMedia
        }

        // change number
        holder.templateNo.text = (1 + position).toString()

        // change solid circle's color
        val inGroup = cutSameBottomDockerViewModel.notInGroup(data.mediaItem)
        if (inGroup) {
            holder.pointCircle.alpha = 0f
        } else {
            holder.pointCircle.alpha = 1f
            val solidCircle: GradientDrawable = holder.pointCircle.background as GradientDrawable
            solidCircle.setColor(CutSameBottomDockerViewModel.getIdColor(data.mediaItem.getGroup()))
        }

        holder.durationTv.text =
            String.format(
                Locale.getDefault(), "%.1fs", data.mediaItem.duration.toFloat() / 1000
            )

        holder.itemView.setGlobalDebounceOnClickListener {
            itemClickBlock?.invoke(position, data.materialItem == null)
        }

        holder.deleteLayout.setGlobalDebounceOnClickListener(500) {
            deleteBlock?.invoke(position)
        }
        val empty = data.materialItem == null
        holder.ivSelector.visibility = if(position == currentPickIndex && empty)  View.VISIBLE else View.GONE
    }

    private fun loadThumbnail(
        media: IMaterialItem?,
        ivThumbnail: ImageView,
        // {zh} 图片加载有延迟，搞一个回调 {en} There is a delay in image loading, get a callback
        endAction: () -> Unit,
    ) {
        val uri = media?.uri
        if (uri == null || uri == Uri.EMPTY) {
            ivThumbnail.setTag(R.id.iv_cover, null)
            ivThumbnail.setImageResource(0)
            endAction.invoke()
        } else {
            val previousTag = ivThumbnail.getTag(R.id.iv_cover)
            if (null == previousTag || previousTag != uri) {
                ivThumbnail.setTag(R.id.iv_cover, uri)
                load(
                    uri,
                    ivThumbnail,
                    ivThumbnail.measuredWidth,
                    ivThumbnail.measuredHeight,
                    endAction
                )
            } else {
                endAction.invoke()
            }
        }
    }

    private fun load(
        uri: Uri,
        view: ImageView,
        resWidth: Int,
        resHeight: Int,
        endAction: () -> Unit
    ): Boolean {
        val requestOptions: RequestOptions = if (resWidth > 0 && resHeight > 0) {
            val overrideWidth = min(view.measuredWidth, resWidth)
            val overrideHeight = min(view.measuredHeight, resHeight)
            RequestOptions().override(overrideWidth, overrideHeight)
        } else {
            RequestOptions().override(view.measuredWidth, view.measuredHeight)
        }.transform(CenterCrop())

        Glide.with(view)
            .asBitmap()
            .load(uri)
            .apply(requestOptions)
            .into(object : CustomViewTarget<ImageView, Bitmap>(view) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    endAction.invoke()
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    view.setImageBitmap(resource)
                    endAction.invoke()
                }

                override fun onResourceCleared(placeholder: Drawable?) {

                }
            })
        return true
    }
    inner class PickingItemHolder(parent: ViewGroup, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val deleteLayout: View = itemView.findViewById(R.id.deleteLayout)
        val thumb: ImageView = itemView.findViewById(R.id.iv_cover)
        val durationTv: TextView = itemView.findViewById(R.id.durationTv)
        val templateNo: TextView = itemView.findViewById(R.id.templateNo)
        val pointCircle: View = itemView.findViewById(R.id.point_circle)
        val ivSelector: View = itemView.findViewById(R.id.iv_selector)
    }
}




