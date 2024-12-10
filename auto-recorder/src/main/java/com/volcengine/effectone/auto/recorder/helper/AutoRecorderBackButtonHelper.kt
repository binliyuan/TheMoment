package com.volcengine.effectone.auto.recorder.helper

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.widget.EOCommonDialog

open class AutoRecorderBackButtonHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    protected open val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }

    override fun initView(rootView: ViewGroup) {
        rootView.findViewById<View?>(R.id.auto_recorder_camera_back)?.apply {
            setNoDoubleClickListener {
                if (recordUIViewModel.isHighLight) {
                    //高光模式
                    showExistsDialog("退出高光拍摄") {
                        if (cameraViewModel.isRecording()) {
                            cameraViewModel.recordManager.stopRecord {
                                cameraViewModel.deleteAllVideos()
                                activity.finish()
                            }
                        } else {
                            cameraViewModel.deleteAllVideos()
                            activity.finish()
                        }
                    }
                } else {
                    recordUIViewModel.run {
                        if (isDrawerOpened()) {
                            toggleDrawerState.value = Unit
                            return@run
                        }
                        activity?.let {
                            cameraViewModel.existRecordPage(it, dialogShowAction = {
                                recordUIViewModel.rootViewVisible.value = false
                            }) {
                                recordUIViewModel.rootViewVisible.value = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showExistsDialog(dialogTitle: String, confirmAction: () -> Unit) {
        EOCommonDialog.Builder(activity)
            .setTitle(dialogTitle)
            .setContent("删除所有视频并退出高光拍摄？")
            .setConfirmText(com.volcengine.effectone.baseui.R.string.eo_base_popout_confirm)
            .setCancelText(com.volcengine.effectone.baseui.R.string.eo_base_popout_no)
            .setConfirmListener(object :
                EOCommonDialog.OnConfirmListener {
                override fun onClick() {
                    confirmAction()
                }
            })
            .show()
    }

}
