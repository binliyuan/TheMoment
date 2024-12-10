package com.volcengine.effectone.auto.recorder.helper

import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.hl.AutoRecordHLVideoListHelper
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.data.EditorSurfacePlaceHolder
import com.volcengine.effectone.recordersdk.base.RecordAction

class AutoRecordLeftDockerHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }

    private var leftDockerView: ViewGroup? = null
    override fun initView(rootView: ViewGroup) {
        leftDockerView = rootView.findViewById(R.id.auto_recorder_root_left_docker)
        addActivityCallBack()
        addFirstFrameBitmapListener()

        recordUIViewModel.rootViewVisible.observe(owner) {
            it?.let { show ->
                showView(show)
            }
        }

        recordUIViewModel.leftDockerViewAlpha.observe(owner) {
            it?.let {
                updateAlpha(it)
            }
        }
    }

    private fun addActivityCallBack() {
        activity.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cameraViewModel.existRecordPage(activity, dialogShowAction = {
                    recordUIViewModel.rootViewVisible.value = false
                }) {
                    recordUIViewModel.rootViewVisible.value = true
                }
            }
        })
    }

    private fun addFirstFrameBitmapListener() {
        cameraViewModel.recordManager.firstFrameCallback {
            EditorSurfacePlaceHolder.setPlaceHolder(it)
        }
    }

    private fun showView(showView: Boolean) {
        leftDockerView?.run {
            visibility = if (showView) View.VISIBLE else View.GONE
            alpha = 1f
        }
    }

    private fun updateAlpha(alphaValue: Float) {
        leftDockerView?.run {
            alpha = alphaValue
            takeIf { alpha > 0 && visibility != View.VISIBLE }?.let { visibility = View.VISIBLE }
        }
    }
}
