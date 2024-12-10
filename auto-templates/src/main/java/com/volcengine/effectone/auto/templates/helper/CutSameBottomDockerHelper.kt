package com.volcengine.effectone.auto.templates.helper

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.mediaimport.view.internal.MaterialSelectedState
import com.ss.android.ugc.aweme.views.setGlobalDebounceOnClickListener
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.templates.R
import com.volcengine.effectone.auto.templates.cutsame.CutSameDurationCheck
import com.volcengine.effectone.auto.templates.vm.CutSameAlbumViewModel
import com.volcengine.effectone.auto.templates.vm.CutSameBottomDockerViewModel
import com.volcengine.effectone.auto.templates.widget.CustomButtonLayout

class CutSameBottomDockerHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) :IUIHelper {

    private val cutSameViewModel by lazy { CutSameAlbumViewModel.get(activity) }
    private val cutSameBottomDockerViewModel by lazy { CutSameBottomDockerViewModel.get(activity) }

    private lateinit var rootViewTips: View
    private lateinit var tvPickTips: TextView
    private lateinit var rootNextText: CustomButtonLayout
    override fun initView(rootView: ViewGroup) {
        rootViewTips = rootView.findViewById(R.id.cutsame_album_root_tips)
        tvPickTips = rootView.findViewById(R.id.tv_pick_tips)
        rootNextText = rootView.findViewById(R.id.tv_export)
        rootView.findViewById<View>(R.id.point_circle)?.run{
            val solidCircle: GradientDrawable = background as GradientDrawable
            solidCircle.setColor(CutSameBottomDockerViewModel.getThemeColor())
        }

        initObserver()
    }

    private fun initObserver() {
        cutSameBottomDockerViewModel.pickFull.observe(owner){
            updatePickTipView()
        }

        rootNextText.setGlobalDebounceOnClickListener {
            cutSameBottomDockerViewModel.viewConfirmAction(activity)
        }

        cutSameBottomDockerViewModel.pickFull.observe(owner) {
            var isFull = it == true
            // feat：点击选择了一个素材 也可以下一步
            isFull = if (cutSameBottomDockerViewModel.isDebug())
                cutSameBottomDockerViewModel.isFull2()
            else isFull
            rootNextText.isSelected = isFull
            rootNextText.setCheckState(isFull)
            rootNextText.alpha = if (isFull) 1.0f else 0.5f
        }
        initMediaSelectorViewModelObserver()
    }
    private fun updatePickTipView() {
        tvPickTips.apply {
           text =  cutSameBottomDockerViewModel.updatePickTipView(activity)
        }
    }

    private fun initMediaSelectorViewModelObserver() {
        val mediaSelectorViewModel =
            cutSameViewModel.mediaSelectViewModel?.mediaSelectorViewModel ?: return
        mediaSelectorViewModel.selectionChanged.observe(owner) { it ->
            val materialItems = it.first
            val selected = it.second
            val selectedMaterialItem =
                materialItems.takeIf { selected == MaterialSelectedState.SELECTED }?.firstOrNull()
                    ?: return@observe
            cutSameBottomDockerViewModel.processCutSamePickItem.firstOrNull { it.selected } ?: return@observe
            cutSameBottomDockerViewModel.pickOne(selectedMaterialItem)
        }
        val cutSameDurationCheck = CutSameDurationCheck(activity)
        //add  mediaCountCheckValidator
        mediaSelectorViewModel.addPreSelectValidator(cutSameDurationCheck)
        //添加素材后置检查
        mediaSelectorViewModel.addPostSelectValidator(cutSameDurationCheck)
    }

}