package com.volcengine.effectone.auto.recorder.helper

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.auto.recorder.hl.AutoHLRecordViewModel
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.recordersdk.base.RecordAction.DELETE_VIDEO
import com.volcengine.effectone.recordersdk.base.RecordAction.START_RECORD
import com.volcengine.effectone.recordersdk.base.RecordAction.STOP_RECORD
import java.util.Formatter
import java.util.Locale

/**
 *Author: gaojin
 *Time: 2023/11/18 20:50
 */

@Suppress("MagicNumber")
class AutoRecordDurationHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    companion object {
        private const val SPACE = " "
    }

    private val mFormatBuilder = StringBuilder()

    @SuppressLint("ConstantLocale")
    private val mFormatter = Formatter(mFormatBuilder, Locale.ENGLISH)

    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val autoHLRecordViewModel by lazy { AutoHLRecordViewModel.get(activity) }

    private var recordingDurationStub: ViewStub? = null
    private var recordingDuration: TextView? = null
    override fun initView(rootView: ViewGroup) {
        recordingDurationStub =
            rootView.findViewById<ViewStub?>(R.id.auto_recorder_record_duration_view)?.apply {
                setOnInflateListener { _, view ->
                    (view as? TextView)?.let {
                        recordingDuration = it
                    }
                }
            } ?: return
        cameraViewModel.recordTotalTime.observe(owner) {
            it?.let { duration ->
                if (recordingDuration?.visibility != View.VISIBLE) {
                    return@observe
                }
                val mode = recordUIViewModel.recordModelLiveData.value ?: return@observe
                if (mode.id == EO_RECORD_MODEL_HIGH_LIGHT) {
                    return@observe
                }
                if (mode.duration > 0L) {
                    val durationText = "${stringForTime(duration)}${SPACE}/${SPACE}${stringForTime(mode.duration)}"
                    recordingDuration?.text = durationText
                }
            }
        }

        cameraViewModel.currentRecordClipTime.observe(owner) {
            it?.let { duration ->
                if (recordingDuration?.visibility != View.VISIBLE) {
                    return@observe
                }
                val mode = recordUIViewModel.recordModelLiveData.value ?: return@observe
                if (mode.id == EO_RECORD_MODEL_HIGH_LIGHT) {
                    val totalDuration = autoHLRecordViewModel.hlRecordManager?.getMaxVideoLength() ?: 0L
                    if (totalDuration > 0L) {
                        val durationText = "${stringForTime(duration)}${SPACE}/${SPACE}${stringForTime(totalDuration)}"
                        recordingDuration?.text = durationText
                    }
                }
            }
        }

        cameraViewModel.recordAction.observe(owner) {
            it?.let { recordAction ->
                when (recordAction) {
                    START_RECORD -> {
                        showView(true)
                        recordingDurationStub?.visibility = View.VISIBLE
                        val duration = cameraViewModel.getCurrentTotalTime()
                        val modelDuration = recordUIViewModel.recordModelLiveData.value?.duration ?: 0L
                        if (modelDuration > 0L) {
                            val durationText = "${stringForTime(duration)}${SPACE}/${SPACE}${stringForTime(modelDuration)}"
                            recordingDuration?.text = durationText
                        }
                    }

                    DELETE_VIDEO -> {
                        if (cameraViewModel.isRecordListEmpty()) {
                            recordingDuration?.text = ""
                        }
                        showView(false)
                    }

                    STOP_RECORD -> {
                        showView(false)
                    }

                    else -> {
                        //do nothing
                    }
                }
            }
        }
    }

    private fun stringForTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        mFormatBuilder.setLength(0)
        return mFormatter.format("%01d:%02d", minutes, seconds).toString()
    }

    private fun showView(showView: Boolean) {
        recordingDurationStub?.run {
            visibility = if (showView) View.VISIBLE else View.GONE
        }
    }
}