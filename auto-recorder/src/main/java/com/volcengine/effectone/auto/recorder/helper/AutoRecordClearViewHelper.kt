package com.volcengine.effectone.auto.recorder.helper

import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.filter.FilterViewModel
import com.volcengine.effectone.sticker.EOBaseStickerViewModel
import com.volcengine.effectone.widget.AutoDismissTextView

/**
 *Author: gaojin
 *Time: 2023/11/22 18:50
 */

class AutoRecordClearViewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val filterViewModel by lazy { FilterViewModel.get(activity) }
    private val stickerViewModel by lazy { EOBaseStickerViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }

    private var clearViewStub: ViewStub? = null
    private var filterNameView: AutoDismissTextView? = null
    private var stickerTipView: AutoDismissTextView? = null

    override fun initView(rootView: ViewGroup) {
        clearViewStub = rootView.findViewById<ViewStub?>(R.id.auto_recorder_root_tips_view_stub)?.apply {
            setOnInflateListener { _, inflated ->
                inflated?.let {
                    inflatedInitView(it)
                }
            }
        }

        filterViewModel.filterChanged.observe(owner) {
            it?.let {
                filterNameView?.showText(it.name())
            }
        }

        stickerViewModel.selectedItem.observe(owner) {
            it?.let {
                val selectedTips = it.resource.tips ?: ""
                stickerTipView?.showTextWithBlink(selectedTips, 1000L, 4)
            }
        }
        recordUIViewModel.rootViewVisible.observe(owner) {
            it?.let {
                showView(true)
            }
        }
    }

    private fun inflatedInitView(inflated: View) {
        filterNameView = inflated.findViewById(R.id.auto_recorder_filter_name_tips)
        stickerTipView = inflated.findViewById(R.id.auto_recorder_sticker_tips)
        setTipViewWidth()
    }

    private fun setTipViewWidth() {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        val targetWidth = (displayMetrics.widthPixels * 0.74).toInt()
        val layoutParams = stickerTipView?.layoutParams
        layoutParams?.width = targetWidth
        stickerTipView?.layoutParams = layoutParams
    }

    private fun showView(showView: Boolean) {
        clearViewStub?.run {
            val originVisible = visibility == View.VISIBLE
            if (showView == originVisible) {
                return
            }
            visibility = if (showView) View.VISIBLE else View.GONE

        }
    }
}