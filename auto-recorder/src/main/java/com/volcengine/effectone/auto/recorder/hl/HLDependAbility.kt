package com.volcengine.effectone.auto.recorder.hl

import com.volcengine.ck.highlight.data.HLResult

/**
 *Author: gaojin
 *Time: 2024/5/24 14:47
 */

interface HLDependAbility {
    suspend fun getRecordFrameData(): PreviewData?
    fun startRecord()
    fun stopRecord()
    fun onFrameRecognized(result: HLResult?)
}