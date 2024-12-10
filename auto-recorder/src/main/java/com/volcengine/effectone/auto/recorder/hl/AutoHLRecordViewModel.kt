package com.volcengine.effectone.auto.recorder.hl

import androidx.fragment.app.FragmentActivity
import com.volcengine.ck.highlight.data.HLResult
import com.volcengine.effectone.recordersdk.RecordMediaItem
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 *Author: gaojin
 *Time: 2024/5/24 16:12
 */
class AutoHLRecordViewModel(activity: FragmentActivity) : BaseViewModel(activity) {

    companion object {
        fun get(activity: FragmentActivity): AutoHLRecordViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(AutoHLRecordViewModel::class.java)
        }
    }

    var hlRecordManager: HLRecordManager? = null

    private var recordVideoHLResultList = mutableListOf<HLResult>()

    fun addHLResult(hlResult: HLResult) {
        recordVideoHLResultList.add(hlResult)
    }

    fun groupHLResultByPath(): Map<String, List<HLResult>> {
        return recordVideoHLResultList.groupBy { it.path }
    }

    fun clear() {
        recordVideoHLResultList.clear()
    }
}
