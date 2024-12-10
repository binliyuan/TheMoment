package com.volcengine.effectone.auto.recorder.hl

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.ss.android.vesdk.VEGetFrameSettings
import com.ss.android.vesdk.VEGetFrameSettings.VEGetFrameEffectType.NO_EFFECT
import com.ss.android.vesdk.VEGetFrameSettings.VEGetFrameFitMode.CENTER_CROP
import com.ss.android.vesdk.VEGetFrameSettings.VEGetFrameType.NORMAL_GET_FRAME_MODE
import com.ss.android.vesdk.VEGetFrameSettings.VEResultType.RGBA_ARRAY
import com.volcengine.ck.highlight.data.HLResult
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.base.AutoRecordScope
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.auto.recorder.config.AutoRecordConstant
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.recordersdk.base.RecordAction.START_RECORD
import com.volcengine.effectone.recordersdk.base.RecordAction.STOP_RECORD
import com.volcengine.effectone.recorderui.vesdk.VESDKRecordManager
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

/**
 *Author: gaojin
 *Time: 2024/5/24 16:13
 */

class AutoHLRecordViewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    companion object {
        private const val TAG = "AutoHLRecordViewHelper"
    }

    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val autoHLRecordViewModel by lazy { AutoHLRecordViewModel.get(activity) }

    private var hlTipsContainer: View? = null
    private var hlCategoryInfo: TextView? = null
    private var hlScoreInfo: TextView? = null

    private var hlTipsDot: View? = null
    private var hlTipTextView: TextView? = null

    private var autoRecordScope = AutoRecordScope()

    private val hlDependAbility = object : HLDependAbility {

        override suspend fun getRecordFrameData(): PreviewData? {
            return vesdkRecordFrameData()
        }

        override fun startRecord() {
            if (recordUIViewModel.isHighLight) {
                LogKit.i(TAG, "startRecord")
                if (cameraViewModel.recordAction.value != START_RECORD) {
                    recordUIViewModel.performClickRecordButton.value = Unit
                } else {
                    LogKit.i(TAG, "startRecord error current recordState: ${cameraViewModel.recordAction.value}")
                }
            } else {
                LogKit.i(TAG, "hlStartRecord not highlight mode")
            }

        }

        override fun stopRecord() {
            if (cameraViewModel.recordAction.value == START_RECORD) {
                recordUIViewModel.performClickRecordButton.value = Unit
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onFrameRecognized(result: HLResult?) {
            if (recordUIViewModel.isHighLight) {
                result?.let {
                    hlCategoryInfo?.text = it.getC3PrintInfo()
                    hlScoreInfo?.text = "画质打分: ${it.score?.score}"
                    LogKit.i(AutoRecordConstant.HL_RECORD_TAG, "onFrameRecognized| pts:${it.ptsMs} score:${it.score?.score}")
                    if (it.ptsMs != -1) {
                        it.path = getCurrentRecordingMediaPath()
                        autoHLRecordViewModel.addHLResult(it)
                    }
                }
            }
        }
    }

    private val hlRecordManager = HLRecordManager(autoRecordScope, hlDependAbility)

    override fun initView(rootView: ViewGroup) {
        //暂时先这么写
        autoHLRecordViewModel.hlRecordManager = hlRecordManager

        hlTipsContainer = rootView.findViewById(R.id.auto_recorder_highlight_container)
        hlCategoryInfo = rootView.findViewById(R.id.auto_record_hl_category)
        hlScoreInfo = rootView.findViewById(R.id.auto_record_hl_score)

        hlTipsDot = rootView.findViewById(R.id.record_highlight_tips_dot)
        hlTipTextView = rootView.findViewById(R.id.record_highlight_tips_title)

        cameraViewModel.currentRecordClipTime.observe(owner) {
            hlRecordManager.recordingTime(it)
        }

        cameraViewModel.recordAction.observe(owner) {
            it?.let { action ->
                when (action) {
                    START_RECORD -> {
                        hlTipsDot?.background = ContextCompat.getDrawable(activity, R.drawable.auto_record_highlight_tips_title_orange)
                        hlTipTextView?.text = "高光拍摄中..."
                    }

                    STOP_RECORD -> {
                        hlTipsDot?.background = ContextCompat.getDrawable(activity, R.drawable.auto_record_highlight_tips_title_green)
                        hlTipTextView?.text = "高光拍摄检测中..."
                        if (recordUIViewModel.isHighLight) {
                            hlRecordManager.realStop()
                        }
                    }

                    else -> {
                        hlTipsContainer?.visibility = View.GONE
                    }
                }
            }
        }

        recordUIViewModel.recordModelLiveData.observe(owner) {
            it?.let { modelItem ->
                //发生模式切换，就清空数据
                autoHLRecordViewModel.clear()
                when (modelItem.id) {
                    EO_RECORD_MODEL_HIGH_LIGHT -> {
                        hlTipsContainer?.visibility = View.VISIBLE
                        hlTipTextView?.text = "高光拍摄检测中..."
                        hlRecordManager.start()
                    }

                    else -> {
                        hlTipsContainer?.visibility = View.GONE
                        hlRecordManager.stop()
                    }
                }
            }
        }
    }

    /**
     * 利用了VESDK拍摄视频生成文件名的规则
     */
    private fun getCurrentRecordingMediaPath(): String {
        val recordedMediaSize = cameraViewModel.recordManager.getRecordedVideoPath().size
        val rootPath = cameraViewModel.recordManager.getWorkSpace().absolutePath + File.separator
        return "${rootPath}segments${File.separator}${recordedMediaSize + 1}_frag_v"
    }

    private suspend fun vesdkRecordFrameData() = suspendCancellableCoroutine<PreviewData?> {
        val recorder = (cameraViewModel.recordManager as? VESDKRecordManager)?.getVERecorder()
        if (recorder == null) {
            it.resume(null)
        }
        val veSize = recorder?.videoEncodeSettings?.videoRes
        val previewSettings = VEGetFrameSettings.Builder()
            .setGetFrameType(NORMAL_GET_FRAME_MODE)
            .setEffectType(NO_EFFECT)
            .setFitMode(CENTER_CROP)
            .setResultType(RGBA_ARRAY)
            .setTargetResolution(veSize)
            .setGetFrameCallback { data, width, height ->
                if (cameraViewModel.isRecording()) {
                    it.resume(PreviewData(width, height, cameraViewModel.currentRecordClipTime.value ?: 0L, data))
                } else {
                    it.resume(PreviewData(width, height, -1L, data))
                }
            }
            .build()
        recorder?.getPreviewFrame(previewSettings)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (recordUIViewModel.isHighLight) {
            LogKit.i(AutoRecordConstant.HL_RECORD_TAG, "onResume start")
            hlRecordManager.start()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (recordUIViewModel.isHighLight) {
            LogKit.i(AutoRecordConstant.HL_RECORD_TAG, "onPause stop")
            hlRecordManager.stop()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        autoRecordScope.close()
    }
}