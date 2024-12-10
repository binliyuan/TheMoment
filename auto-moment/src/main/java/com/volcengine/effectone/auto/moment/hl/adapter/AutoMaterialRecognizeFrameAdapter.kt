package com.volcengine.effectone.auto.moment.hl.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bytedance.creativex.visibleOrGone
import com.bytedance.ies.cutsame.util.VEUtils
import com.volcengine.ck.highlight.data.HLResult
import com.volcengine.ck.highlight.utils.isImage
import com.volcengine.ck.highlight.utils.isVideo
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.config.AutoMomentScope
import com.volcengine.effectone.auto.moment.hl.data.AutoMaterialRecognizeMedia
import com.volcengine.effectone.singleton.AppSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoMaterialRecognizeFrameAdapter :
    RecyclerView.Adapter<AutoMaterialRecognizeFrameAdapter.AutoMaterialRecognizeFrameViewHolder>() {
    private val hlResults = mutableListOf<HLResult>()
    private lateinit var autoMaterialRecognizeMedia:AutoMaterialRecognizeMedia
    private val autoScope by lazy { AutoMomentScope() }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AutoMaterialRecognizeFrameViewHolder {
        return AutoMaterialRecognizeFrameViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_auto_material_recognize_frame_item, parent, false)
        )
    }

    fun updateItem(item: AutoMaterialRecognizeMedia) {
        autoMaterialRecognizeMedia = item
        val newItems = item.hlResults?:return
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = hlResults.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = hlResults[oldItemPosition]
                val newItem = newItems[newItemPosition]
                return oldItem.id == newItem.id && oldItem.ptsMs == newItem.ptsMs
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                hlResults[oldItemPosition] == newItems[newItemPosition]

        })
        hlResults.clear()
        hlResults.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = hlResults.size

    override fun onBindViewHolder(holder: AutoMaterialRecognizeFrameViewHolder, position: Int) {
        holder.onBind(hlResults[position])
    }

    inner class AutoMaterialRecognizeFrameViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val cover = itemView.findViewById<AppCompatImageView>(R.id.cover)
        private val pos = itemView.findViewById<TextView>(R.id.pos)
        fun onBind(hlResult: HLResult) {

            cover.run {
                val recognizeMedia =
                    autoMaterialRecognizeMedia.recognizeMedia
                val image =
                    recognizeMedia.isImage()
                if (image) {
                    val option = RequestOptions()
                        .transform(CenterCrop())
                        .format(DecodeFormat.PREFER_RGB_565)

                    Glide.with(AppSingleton.instance)
                        .load(recognizeMedia.path)
                        .apply(option)
                        .into(this)
                    return
                }
                autoScope.launch(Dispatchers.IO) {
                    val path = autoMaterialRecognizeMedia.recognizeMedia.path
                    VEUtils.getVideoFrames(path, intArrayOf(hlResult.ptsMs)) { frame, width, height, ptsMs ->
                        if (ptsMs != -1 && width != -1 && height != -1) {
                            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bmp.copyPixelsFromBuffer(frame)
                            autoScope.launch(Dispatchers.Main){
                               loadBitmap(this@run, bmp)
                           }
                        }
                        false
                    }
                }

            }
            val isVideo = autoMaterialRecognizeMedia.recognizeMedia.isVideo()
            pos.run {
                text = absoluteAdapterPosition.inc().toString()
                visibleOrGone = isVideo
            }
        }

        private fun loadBitmap(targetView: ImageView, bitmap: Bitmap) {
            val option = RequestOptions()
                .transform(CenterCrop())
                .format(DecodeFormat.PREFER_RGB_565)

            Glide.with(AppSingleton.instance)
                .load(bitmap)
                .apply(option)
                .into(targetView)
        }

    }
}
