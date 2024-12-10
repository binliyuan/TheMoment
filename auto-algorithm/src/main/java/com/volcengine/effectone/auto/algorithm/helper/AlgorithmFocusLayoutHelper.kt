package com.volcengine.effectone.auto.algorithm.helper

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.volcengine.effectone.auto.algorithm.viewmodel.AlgorithmViewModel
import com.volcengine.effectone.auto.recorder.helper.AutoRecordFocusLayoutHelper

class AlgorithmFocusLayoutHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner
) : AutoRecordFocusLayoutHelper(activity, owner) {
    companion object {
        private const val TAG = "AlgorithmFocusLayoutHelper"
    }
    override val cameraViewModel by lazy { AlgorithmViewModel.get(activity) }
}