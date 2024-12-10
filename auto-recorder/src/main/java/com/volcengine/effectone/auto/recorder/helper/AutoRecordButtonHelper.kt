package com.volcengine.effectone.auto.recorder.helper

import android.graphics.Color
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.recordersdk.RecordMediaItem
import com.volcengine.effectone.recordersdk.RecordMediaType.IMAGE
import com.volcengine.effectone.recordersdk.api.EOPictureCallback
import com.volcengine.effectone.recordersdk.base.FlashState
import com.volcengine.effectone.recordersdk.base.RecordAction
import com.volcengine.effectone.recorderui.api.EORecordTakePicListener
import com.volcengine.effectone.recorderui.api.EORecordTakeVideoListener
import com.volcengine.effectone.recorderui.base.EO_RECORD_MODEL_30S
import com.volcengine.effectone.recorderui.base.EO_RECORD_MODEL_PHOTO
import com.volcengine.effectone.recorderui.widget.EORecordButton
import com.volcengine.effectone.recorderui.widget.RecordMode.HIGHLIGHT
import com.volcengine.effectone.recorderui.widget.RecordMode.PICTURE
import com.volcengine.effectone.recorderui.widget.RecordMode.VIDEO
import com.volcengine.effectone.recorderui.widget.RecordingConfig
import com.volcengine.effectone.utils.SizeUtil

/**
 *Author: gaojin
 *Time: 2023/11/15 15:55
 */

open class AutoRecordButtonHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    companion object {
        private const val TAG = "AutoRecordButtonHelper"
        private const val IMAGE_DURATION = 3000L
    }

    protected open val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    protected lateinit var recordButton: EORecordButton

    override fun initView(rootView: ViewGroup) {
        recordButton = rootView.findViewById(R.id.auto_recorder_start_record)

        initObserver()
        recordButton.setOnTakePicListener(object : EORecordTakePicListener {
            override fun takePic() {
                cameraViewModel.recordAction.value = RecordAction.TAKE_PIC_START
                cameraViewModel.recordManager.takePicture(cameraViewModel.flashState.value == FlashState.ON, object : EOPictureCallback {
                    override fun onPictureTaken(path: String) {
                        cameraViewModel.recordAction.value = RecordAction.TAKE_PIC_FINISH
                        recordUIViewModel.finishRecord(listOf(RecordMediaItem(path, IMAGE, IMAGE_DURATION, 1F)))
                    }

                    override fun onTakenFailed() {
                        Toast.makeText(activity, "拍照失败", Toast.LENGTH_SHORT).show()
                        cameraViewModel.recordAction.value = RecordAction.TAKE_PIC_FINISH
                    }
                })
            }
        })

        recordButton.setOnTakeVideoListener(object : EORecordTakeVideoListener {

            override fun startRecord() {
                LogKit.i(TAG, "startRecord")
                cameraViewModel.recordManager.startRecord(cameraViewModel.recordSpeed.value ?: 1F, cameraViewModel.flashState.value == FlashState.ON)
            }

            override fun stopRecord() {
                LogKit.i(TAG, "stopRecord")
                cameraViewModel.recordManager.stopRecord()
            }
        })
    }

    private fun initObserver() {
        recordUIViewModel.recordModelLiveData.observe(owner) {
            it?.let { modelItem ->
                when (modelItem.id) {
                    EO_RECORD_MODEL_PHOTO -> {
                        //拍照模式
                        recordButton.isEnabled = true
                        recordButton.changeRecordMode(PICTURE)
                        recordButton.setTotalDuration(0L)
                        recordButton.defaultUIStyle()
                    }

                    EO_RECORD_MODEL_30S -> {
                        //录制模式
                        recordButton.isEnabled = true
                        recordButton.changeRecordMode(VIDEO)
                        recordButton.defaultUIStyle()
                        if (modelItem.duration != 0L) {
                            recordButton.setTotalDuration(modelItem.duration)
                        }
                    }

                    EO_RECORD_MODEL_HIGH_LIGHT -> {
                        //高光拍摄模式
                        recordButton.changeRecordMode(HIGHLIGHT)
                        recordButton.setTotalDuration(0L)
                        recordButton.isEnabled = false
                        //开始拍摄时的样式
                        val startConfig = RecordingConfig().apply {
                            panelRadius = SizeUtil.dp2pxF(72F) / 2
                            innerPanelRadius = SizeUtil.dp2pxF(28F) / 2
                            innerPanelCorners = SizeUtil.dp2pxF(4F)
                            borderWidth = SizeUtil.dp2pxF(4F)
                            recordingBgPanelPaintColor = Color.WHITE
                        }
                        //结束拍摄时的样式
                        val stopConfig = startConfig.copy().apply {
                            innerPanelRadius = SizeUtil.dp2pxF(28F)
                            innerPanelCorners = SizeUtil.dp2pxF(28F)
                        }
                        recordButton.setCurrentRecordingConfig(startConfig, stopConfig)
                    }
                }
                // 恢复曝光至默认值
                cameraViewModel.recordManager.setExposureValue(50)
            }
        }

        recordUIViewModel.stopRecord.observe(owner) {
            recordButton.stopRecord()
        }

        recordUIViewModel.performClickRecordButton.observe(owner) {
            recordButton.performClick()
        }

        cameraViewModel.recordTotalTime.observe(owner) {
            it?.let { time ->
                recordButton.setProgress(time)
            }
        }

        cameraViewModel.recordAction.observe(owner) {
            it?.let { recordAction ->
                when (recordAction) {
                    RecordAction.STOP_RECORD -> {
                        recordButton.markEndPoint(cameraViewModel.getCurrentTotalTime())
                    }

                    RecordAction.DELETE_VIDEO -> {
                        recordButton.deleteLastVideo()
                    }

                    else -> {
                        //do nothing
                    }
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        // 如果在录制中，需要触发停止录制
        if (cameraViewModel.recordAction.value == RecordAction.START_RECORD) {
            recordUIViewModel.stopRecord.value = Unit
        }
    }
}