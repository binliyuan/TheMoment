package com.volcengine.effectone.auto.recorder.helper

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.bytedance.creativex.visibleOrGone
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.common.widget.AutoLoadingDialog
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.auto.recorder.viewmodel.AutoImageViewModel
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.recordersdk.base.RecordAction
import com.volcengine.effectone.recordersdk.base.RecordAction.START_RECORD
import com.volcengine.effectone.recordersdk.base.RecordAction.STOP_RECORD
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.widget.EOCommonDialog
import com.volcengine.effectone.widget.EOToaster
import java.util.Locale

/**
 *Author: gaojin
 *Time: 2023/11/17 15:50
 */

class AutoRecordVideoControlHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val firstImageViewModel by lazy { AutoImageViewModel.get(activity) }

    private var deleteVideoButton: View? = null
    private var toNextButton: View? = null
    private var isNextButtonClicked = false
    private val loadingDialog by lazy { AutoLoadingDialog.Builder(activity).show() }

    override fun initView(rootView: ViewGroup) {
        initInflatedTopView(rootView)
        initInflatedBottomView(rootView)
        cameraViewModel.recordAction.observe(owner) {
            it?.let { action ->
                when (action) {
                    START_RECORD -> {
                        showView(true)
                        deleteVideoButton?.visibleOrGone = false
                    }

                    STOP_RECORD -> {
                        if (!cameraViewModel.isRecordListEmpty()) {
                            showView(true)
                        } else {
                            showView(false)
                        }
                        if (isNextButtonClicked) {
                            toNextPage()
                        }
                    }

                    RecordAction.DELETE_VIDEO -> {
                        if (!cameraViewModel.isRecordListEmpty()) {
                            showView(true)
                        } else {
                            showView(false)
                        }
                    }

                    else -> {
                        // do nothing
                    }
                }
            }
        }
        cameraViewModel.concatVideoStateWithResult.observe(owner) { result ->
            result?.let {
                val pattern = activity.getString(if (recordUIViewModel.isPhotoMode) R.string.auto_recorder_photo else R.string.auto_recorder_video)
                if (result.first.not()) { //非loading态
                    loadingDialog.dismiss()
                    cameraViewModel.deleteAllVideos()
                    showView(false)
                    val pathUri = result.second
                    val saveSuc = pathUri != null
                    if (saveSuc) {
                        firstImageViewModel.updateAlbumIcon(pathUri.toString())
                        EOToaster.show(activity, String.format(Locale.ENGLISH, activity.getString(R.string.auto_recorder_save_album_suc), pattern))
                    } else {
                        EOToaster.show(activity, String.format(Locale.ENGLISH, activity.getString(R.string.auto_recorder_save_album_failed), pattern))
                    }
                } else {
                    if (recordUIViewModel.isPhotoMode) {
                        //拍照模式不需要loading，有闪屏了
                        return@observe
                    }
                    loadingDialog.setTipMessage(String.format(Locale.ENGLISH, activity.getString(R.string.auto_recorder_saving_media_tip), pattern)).show()
                }
            }
        }

        recordUIViewModel.finishLiveData.observe(owner) {
            if (it.isNullOrEmpty().not()) {
                cameraViewModel.saveRecordMedias(AppSingleton.instance, it)
            }
        }

        recordUIViewModel.recordModelLiveData.observe(owner) {
            it?.let {
                when (it.id) {
                    EO_RECORD_MODEL_HIGH_LIGHT -> {
                        showView(false)
                    }

                    else -> {
                        showView(false)
                    }
                }
            }
        }
    }

    private fun initInflatedTopView(rootView: View) {
        deleteVideoButton = rootView.findViewById<View?>(R.id.auto_recorder_delete_video).apply {
            visibility = View.GONE
        }
        deleteVideoButton?.setNoDoubleClickListener {
            if (recordUIViewModel.isHighLight) {
                //高光模式
                showExistsDialog("退出高光拍摄模式", content = "删除所有视频并退出高光拍摄？") {
                    if (cameraViewModel.isRecording()) {
                        cameraViewModel.recordManager.stopRecord {
                            cameraViewModel.deleteAllVideos()
                            recordUIViewModel.selectShot30SModel.value = Unit
                            recordUIViewModel.stopRecord.value = Unit
                            showView(!cameraViewModel.isRecordListEmpty())
                        }
                    } else {
                        cameraViewModel.deleteAllVideos()
                        recordUIViewModel.selectShot30SModel.value = Unit
                        showView(!cameraViewModel.isRecordListEmpty())
                    }
                }
            } else {
                if (!cameraViewModel.isRecordListEmpty()) {
                    showExistsDialog(activity.getString(com.volcengine.effectone.recorderui.R.string.eo_base_popup_discardclip)) {
                        cameraViewModel.deleteLastVideo()
                    }
                }
            }
        }
    }

    private fun showExistsDialog(dialogTitle: String, content:String = "", confirmAction: () -> Unit) {
        EOCommonDialog.Builder(activity)
            .setTitle(dialogTitle)
            .setContent(content)
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

    private fun initInflatedBottomView(inflated: View) {
        toNextButton = inflated.findViewById(R.id.auto_recorder_to_next)
        toNextButton?.setNoDoubleClickListener {
            isNextButtonClicked = true
            if (cameraViewModel.isRecording()) {
                recordUIViewModel.stopRecord.value = Unit
            } else {
                toNextPage()
            }
        }
    }

    private fun toNextPage() {
        recordUIViewModel.finishRecord(cameraViewModel.recordManager.getRecordedVideoPath())
        isNextButtonClicked = false
    }

    private fun showView(showView: Boolean) {
        if (recordUIViewModel.isHighLight) {
            toNextButton?.visibility = View.GONE
            deleteVideoButton?.visibility = View.VISIBLE
        } else {
            deleteVideoButton?.run {
                visibility = if (showView) View.VISIBLE else View.GONE
            }
            toNextButton?.run {
                visibility = if (showView) View.VISIBLE else View.GONE
            }
        }
    }
}