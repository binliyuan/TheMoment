package com.volcengine.effectone.auto.recorder.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.recordersdk.base.RecordAction.START_RECORD
import com.volcengine.effectone.recordersdk.base.RecordAction.STOP_RECORD

open class AutoRecordCameraSwitchHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {
    protected open val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private var cameraView: ImageView? = null

    override fun initView(rootView: ViewGroup) {
        cameraView = rootView.findViewById(R.id.auto_recorder_camera_flip)
        cameraView?.setOnClickListener {
            cameraViewModel.switchCamera()
            cameraView?.let {
                val value = it.rotation - 180
                it.animate()?.rotation(value)?.start()
            }
        }

        cameraViewModel.recordAction.observe(owner) {
            it?.let { recordAction ->
                when (recordAction) {
                    START_RECORD -> {
                        cameraView?.visibility = View.INVISIBLE
                    }

                    STOP_RECORD -> {
                        cameraView?.visibility = View.VISIBLE
                    }

                    else -> {
                        cameraView?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}
