package com.volcengine.effectone.auto.album.view

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorBottomView
import com.bytedance.creativex.mediaimport.view.internal.IMaterialSelectorViewModel
import com.bytedance.creativex.mediaimport.view.internal.base.BaseMaterialSelectorView
import com.bytedance.creativex.visibleOrGone
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.auto.album.R
import com.volcengine.effectone.singleton.AppSingleton

class AutoAlbumMediaSelectorBottomView(
    root: ViewGroup,
    lifecycleOwner: LifecycleOwner,
    private val albumConfig: AlbumConfig,
    selectorViewModel: IMaterialSelectorViewModel<IMaterialItem>?,
    enableIndexedConfirm: Boolean = true,
    confirmText: String,
) : IMaterialSelectorBottomView<IMaterialItem>, BaseMaterialSelectorView<IMaterialItem>(
    root, lifecycleOwner, selectorViewModel, enableIndexedConfirm, confirmText
) {

    private var selectCountTipsView: TextView? = null

    override fun init() {
        super.init()
        initView()
        initObserver()
    }

    private fun initView() {
        val size = selectorViewModel?.selectedMaterials?.value?.size ?: 0
        setConfirmTextViewState(size != 0)
        contentView.visibleOrGone = size != 0
        confirmTextView?.text = ""
        selectCountTipsView = contentView.findViewById(R.id.auto_docker_select_count_tips)
    }

    private fun initObserver() {
        selectorViewModel?.selectedMaterials?.observe(lifecycleOwner) {
            contentView.visibleOrGone = it.isNotEmpty()
            selectCountTipsView?.apply {
                val color = ContextCompat.getColor(this.context, R.color.color_FF53BB)
                val textInfo = AppSingleton.instance.getString(R.string.auto_album_docker_selected_count, it.size)
                text = SpannableString(textInfo).apply {
                    val index = indexOf("${it.size}")
                    setSpan(ForegroundColorSpan(color),index,index+it.size.toString().length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        selectorViewModel?.confirmEnable?.observe(lifecycleOwner) {
            setConfirmTextViewState(it)
        }
    }

    private fun setConfirmTextViewState(confirmEnable: Boolean) {
        if (confirmEnable) {
            confirmTextView?.run {
                isEnabled = true
                setBackgroundResource(R.drawable.auto_next_bg_enable)
            }
        } else {
            confirmTextView?.run {
                isEnabled = false
                setBackgroundResource(R.drawable.auto_next_bg_un_enable)
            }
        }
    }

    override fun updateHintText(hintText: String) {
        //do nothing
    }


    override fun provideContentView(root: ViewGroup): ViewGroup {
        return LayoutInflater.from(root.context).inflate(R.layout.auto_album_import_selector_view, root, true) as ViewGroup
    }
}