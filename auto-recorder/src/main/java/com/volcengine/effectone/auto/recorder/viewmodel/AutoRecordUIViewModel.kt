package com.volcengine.effectone.auto.recorder.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.volcengine.effectone.auto.recorder.base.EO_RECORD_MODEL_HIGH_LIGHT
import com.volcengine.effectone.recorderui.base.EO_RECORD_MODEL_PHOTO
import com.volcengine.effectone.recorderui.viewmodel.RecordUIViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

class AutoRecordUIViewModel(activity: FragmentActivity) : RecordUIViewModel(activity) {
    companion object {
        fun get(activity: FragmentActivity): AutoRecordUIViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(AutoRecordUIViewModel::class.java)
        }
    }

    val toggleDrawerState = MutableLiveData<Unit>()
    val drawerViewState = MutableLiveData<Boolean>()
    val selectShot30SModel = MutableLiveData<Unit>()
    fun isDrawerOpened() = drawerViewState.value == true

    //左侧控制栏alpha值，根据drawer offset相反
    val leftDockerViewAlpha = MutableLiveData<Float>()

    //当前是否是拍照模式
    val isPhotoMode get() = recordModelLiveData.value?.id == EO_RECORD_MODEL_PHOTO

    val isHighLight get() = recordModelLiveData.value?.id == EO_RECORD_MODEL_HIGH_LIGHT
}