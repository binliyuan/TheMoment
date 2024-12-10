package com.volcengine.effectone.auto.moment.hl.adapter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bytedance.creativex.visibleOrGone
import com.volcengine.ck.highlight.data.HLResult
import com.volcengine.ck.highlight.utils.isImage
import com.volcengine.effectone.auto.moment.R
import com.volcengine.effectone.auto.moment.hl.data.AutoMaterialRecognizeMedia
import com.volcengine.effectone.auto.moment.hl.detail.AutoMaterialRecognizeDetailDiaLog.Companion.markCategoryInfo
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils

class AutoMaterialRecognizeTabAdapter : RecyclerView.Adapter<AutoMaterialRecognizeTabAdapter.AutoMaterialRecognizeTabViewHolder>() {
    var goneDetailAction:((AutoMaterialRecognizeMedia)->Unit)? = null
    var goHlExtractAction:((AutoMaterialRecognizeMedia)->Unit)? = null
    private val recognizeMedia = mutableListOf<AutoMaterialRecognizeMedia>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoMaterialRecognizeTabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_auto_material_recognize_item, parent, false)
        return  AutoMaterialRecognizeTabViewHolder(view)
    }

    override fun getItemCount() = recognizeMedia.size

    override fun onBindViewHolder(holder: AutoMaterialRecognizeTabViewHolder, position: Int) {
        holder.onBind(recognizeMedia[position])
    }

    override fun onViewRecycled(holder: AutoMaterialRecognizeTabViewHolder) {
        super.onViewRecycled(holder)
        holder.unBind()
    }
    private  val TAG = "AutoMaterialRecognizeTa"
     fun updateItems(newAutoMaterialRecognizeMedias: List<AutoMaterialRecognizeMedia>) {
        Log.d(TAG, "updateItems() called with: allData = ${newAutoMaterialRecognizeMedias.size}   ,recognizeMedia = ${recognizeMedia.map { it.hlResults?.size }}  newAutoMaterialRecognizeMedias = ${newAutoMaterialRecognizeMedias.map { it.hlResults?.size }}")
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = recognizeMedia.size
            override fun getNewListSize() = newAutoMaterialRecognizeMedias.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) :Boolean{
                val oldItem = recognizeMedia[oldItemPosition]
                val newItem = newAutoMaterialRecognizeMedias[newItemPosition]
                val oldSize = oldItem.hlResults?.size ?: -1
                val newSize = newItem.hlResults?.size ?: 0
                return  oldItem.recognizeMedia.id == newItem.recognizeMedia.id && oldSize == newSize
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) :Boolean{
                val oldItem = recognizeMedia[oldItemPosition]
                val newItem = newAutoMaterialRecognizeMedias[newItemPosition]
                val oldSize = oldItem.hlResults?.size ?: -1
                val newSize = newItem.hlResults?.size ?: 0
                return  oldSize == newSize
            }
        })
        recognizeMedia.clear()
        recognizeMedia.addAll(newAutoMaterialRecognizeMedias)
        diffResult.dispatchUpdatesTo(this)
    }

    fun updateItem(newItem: AutoMaterialRecognizeMedia) {
        recognizeMedia.indexOfFirst { oldItem ->
            val sameItem = newItem.recognizeMedia.id == oldItem.recognizeMedia.id
            val newHLResultsSize = newItem.hlResults?.size ?: 0
            val oldHLResultsSize = oldItem.hlResults?.size ?: 0
            sameItem && newHLResultsSize != oldHLResultsSize
        }.takeIf { it != -1 }?.run {
            notifyItemChanged(this)
        }
    }

    inner class AutoMaterialRecognizeTabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cover: ImageView? = itemView.findViewById(R.id.auto_material_recognize_item_cover)
        private val title: TextView? = itemView.findViewById(R.id.auto_material_recognize_item_title)
        private val state: TextView?  = itemView.findViewById(R.id.auto_material_recognize_item_state)
        private val category: TextView?  = itemView.findViewById(R.id.auto_material_recognize_item_category)
        private val loadingContainer: LinearLayout?  = itemView.findViewById(R.id.auto_material_recognize_loading)
        private val loadingView: ImageView?  = itemView.findViewById(R.id.auto_material_recognize_loading_view)
        private val loadingTip: TextView?  = itemView.findViewById(R.id.auto_material_recognize_loading_tip)
        private val goneDetail: View?  = itemView.findViewById(R.id.auto_material_recognize_go_detail)
        private val videoHl: View?  = itemView.findViewById(R.id.auto_material_recognize_hl_extract)
        private var animator: ObjectAnimator? = null
        private var attachStateChangeListener: View.OnAttachStateChangeListener? = null
        init {
            goneDetail?.setNoDoubleClickListener {
                goneDetailAction?.invoke( recognizeMedia[absoluteAdapterPosition])
            }
            videoHl?.setNoDoubleClickListener {
                goHlExtractAction?.invoke(recognizeMedia[absoluteAdapterPosition])
            }

            loadingView?.let {
                animator = ObjectAnimator.ofFloat(it, "rotation", 0f, 360f).apply {
                    duration = 1000
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.RESTART
                }
                attachStateChangeListener =  object :View.OnAttachStateChangeListener{
                    override fun onViewAttachedToWindow(v: View) = Unit

                    override fun onViewDetachedFromWindow(v: View) {
                        animator?.cancel()
                    }

                }
                it.addOnAttachStateChangeListener(attachStateChangeListener)
            }

        }

        fun onBind(autoMaterialRecognizeMedia: AutoMaterialRecognizeMedia) {
            val recognizeMedia = autoMaterialRecognizeMedia.recognizeMedia
            val hlResults = autoMaterialRecognizeMedia.hlResults
            val image = recognizeMedia.isImage()
            val loadFinished = autoMaterialRecognizeMedia.loadFinished()
            cover?.run {
                val option = RequestOptions()
                    .transform(CenterCrop(), RoundedCorners(EOUtils.sizeUtil.dp2px(8f)))
                    .format(DecodeFormat.PREFER_RGB_565)
                Glide.with(AppSingleton.instance)
                    .load(recognizeMedia.path)
                    .apply(option)
                    .into(this)
            }

            title?.run {
                text = recognizeMedia.path.substringAfterLast("/")
            }

            state?.run {
                text = "${if (image) "图片" else "视频"}-${if (loadFinished) "已提取" else "提取中..."}"
            }

            category?.run {
                text = if(autoMaterialRecognizeMedia.loadFinished()){
                    val hlResult = hlResults?.firstOrNull()
                    val ellipsizeNextLine = (hlResult?.c3?.size ?: 0) > 3
                    if(ellipsizeNextLine){
                        val ellipsizeC3 = hlResult?.c3?.subList(0, 2) ?: emptyList()
                        val ellipsizeHlResult = hlResult!!.let {  HLResult(id = it.id, ptsMs = it.ptsMs,c3 = ellipsizeC3, score = it.score, path = it.path) }
                        StringBuilder().append(listOf(ellipsizeHlResult).markCategoryInfo()).append("\n").append("...")
                    }else{
                        hlResults?.markCategoryInfo()
                    }
                }else{
                    "素材识别中..."
                }
            }

            loadingContainer?.run {
                visibleOrGone = loadFinished.not()
            }
            loadingView?.run {
                if (loadFinished) {
                    animator?.cancel()
                }else{
                    animator?.start()
                }
            }

            goneDetail?.run {
                visibleOrGone = loadFinished
            }

            videoHl?.run {
                visibleOrGone = image.not() && loadFinished
            }
        }

        private fun AutoMaterialRecognizeMedia.loadFinished():Boolean{
            return hlResults.isNullOrEmpty().not()
        }

        fun unBind() {
            loadingView?.removeOnAttachStateChangeListener(attachStateChangeListener)
            animator?.cancel()
        }

    }
}
