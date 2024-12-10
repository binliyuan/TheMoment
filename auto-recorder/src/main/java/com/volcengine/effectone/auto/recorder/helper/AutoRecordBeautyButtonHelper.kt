package com.volcengine.effectone.auto.recorder.helper

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.common.helper.api.IUIHelper
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.auto.recorder.viewmodel.AutoRecordUIViewModel
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.recordersdk.base.RecordAction.START_RECORD
import com.volcengine.effectone.recordersdk.base.RecordAction.STOP_RECORD

class AutoRecordBeautyButtonHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : IUIHelper {

    private val recordUIViewModel by lazy { AutoRecordUIViewModel.get(activity) }
    private val cameraViewModel by lazy { AutoCameraViewModel.get(activity) }
    private var beautyButton: View? = null

    override fun initView(rootView: ViewGroup) {
        beautyButton = rootView.findViewById(R.id.auto_recorder_beauty_drawer)
        beautyButton?.setNoDoubleClickListener {
            recordUIViewModel.toggleDrawerState.value = Unit
        }

        cameraViewModel.recordAction.observe(owner) {
            it?.let { recordAction ->
                when (recordAction) {
                    START_RECORD -> {
                        beautyButton?.visibility = View.INVISIBLE
                    }

                    STOP_RECORD -> {
                        if (!recordUIViewModel.isHighLight) {
                            beautyButton?.visibility = View.VISIBLE
                        }
                    }

                    else -> {
                        if (!recordUIViewModel.isHighLight) {
                            beautyButton?.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        recordUIViewModel.recordModelLiveData.observe(owner) {
            it?.let {
                when (it.id) {
                    EO_RECORD_MODEL_HIGH_LIGHT -> {
                        beautyButton?.visibility = View.INVISIBLE
                    }

                    else -> {
                        beautyButton?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}
