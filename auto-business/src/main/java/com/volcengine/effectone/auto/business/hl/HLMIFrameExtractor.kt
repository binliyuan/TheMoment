package com.volcengine.effectone.auto.business.hl

import android.preference.PreferenceManager
import com.volcengine.ck.highlight.api.IFrameExtractor
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.singleton.AppSingleton

/**
 *Author: gaojin
 *Time: 2023/9/14 14:36
 */

class HLMIFrameExtractor : IFrameExtractor {
    /**
     * @return IntArray 视频进行抽帧的时间戳
     */
    override fun extract(videoTime: Long): LongArray {
        val frameCount = (videoTime / frameGap()).toInt()
        val intArray = LongArray(frameCount + 1) { i ->
            frameGap() * i * 1L
        }
        return intArray
    }

    override fun frameGap(): Int {
        var gap = 1000
        try {
            val gapString = PreferenceManager.getDefaultSharedPreferences(AppSingleton.instance)
                .getString("preference_hl_frame_extractor", "1000") ?: "1000"
            gap = gapString.toInt()
        } catch (e: NumberFormatException) {
            LogKit.e("HLMIFrameExtractor", "", e)
        }
        return gap
    }
}