package com.volcengine.effectone.auto.algorithm.helper

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmViewModel
import com.volcengine.effectone.auto.recorder.helper.AutoRecordCameraSwitchHelper

class AlgorithmCameraSwitchHelper (
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : AutoRecordCameraSwitchHelper(activity, owner) {
    companion object {
        private const val TAG = "AlgorithmCameraSwitchHelper"
    }
    override val cameraViewModel by lazy { AlgorithmViewModel.get(activity) }
}