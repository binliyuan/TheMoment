package com.volcengine.effectone.auto.algorithm.viewmodel

import androidx.fragment.app.FragmentActivity
import com.volcengine.effectone.recorderui.viewmodel.RecoderEventTrackViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

class AlgorithmRecoderEventTrackViewModel(activity: FragmentActivity): RecoderEventTrackViewModel(activity) {
    companion object {
        fun get(activity: FragmentActivity): AlgorithmRecoderEventTrackViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(AlgorithmRecoderEventTrackViewModel::class.java)
        }
    }
    override val cameraViewModel = AlgorithmViewModel.get(activity)
}