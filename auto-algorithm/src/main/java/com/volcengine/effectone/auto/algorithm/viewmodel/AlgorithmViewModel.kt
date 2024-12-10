package com.volcengine.effectone.auto.algorithm.viewmodel

import androidx.fragment.app.FragmentActivity
import com.volcengine.effectone.auto.recorder.AutoCameraViewModel
import com.volcengine.effectone.recordersdk.ARSDKRecordManager
import com.volcengine.effectone.recordersdk.RecordManager
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

open class AlgorithmViewModel(activity: FragmentActivity) : AutoCameraViewModel(activity) {
    companion object {

        private const val TAG = "AlgorithmViewModel"

        fun get(activity: FragmentActivity): AlgorithmViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(AlgorithmViewModel::class.java)
        }
    }

    private var sdkRecordManager : RecordManager? = null

    fun setAlgorithmOn(switch: Boolean) {
        val recordManager = sdkRecordManager as ARSDKRecordManager
        recordManager.setAlgorithmOn(switch)
    }

    override fun getEORecordManager(): RecordManager {
        sdkRecordManager = ARSDKRecordManager()
        return sdkRecordManager as ARSDKRecordManager
    }
}