package com.volcengine.effectone.auto.recorder.hl

import android.preference.PreferenceManager
import com.volcengine.effectone.singleton.AppSingleton

/**
 *Author: gaojin
 *Time: 2023/10/16 17:47
 */

class HLRecordConfig {

    //检测时间间隔
    var checkTimeInterval = 1000L

    var checkFrameTotalCount = 5

    var checkFrameLegalCount = 4

    var maxVideoLength = 15000L
    var minVideoLength = 5000

    var minScoreRequire = 0.6F
    val c3Category = mutableListOf<String>()

    fun initConfig(): HLRecordConfig {
        try {
            checkTimeInterval = getString("hl_recorder_time_interval", "$checkTimeInterval").toLong()
        } catch (e: NumberFormatException) {
            //do nothing
        }

        try {
            checkFrameTotalCount = getString("hl_recorder_frame_total_count", "$checkFrameTotalCount").toInt()
        } catch (e: NumberFormatException) {
            //do nothing
        }

        try {
            checkFrameLegalCount = getString("hl_recorder_frame_legal_count", "$checkFrameLegalCount").toInt()
        } catch (e: NumberFormatException) {
            //do nothing
        }

        try {
            maxVideoLength = getString("hl_recorder_max_video_length", "$maxVideoLength").toLong()
        } catch (e: NumberFormatException) {
            //do nothing
        }

        try {
            minVideoLength = getString("hl_recorder_min_video_length", "$minVideoLength").toInt()
        } catch (e: NumberFormatException) {
            //do nothing
        }

        try {
            minScoreRequire = getString("hl_recorder_af_score", "$minScoreRequire").toFloat()
        } catch (e: NumberFormatException) {
            //do nothing
        }

        val c3Content = getString("hl_recorder_c3_require", "")
        if (c3Content.isNotEmpty()) {
            val list = c3Content.split(" ")
            list.forEach {
                c3Category.add(it.trim())
            }
        }
        return this
    }

    private fun getString(key: String, default: String): String {
        return PreferenceManager.getDefaultSharedPreferences(AppSingleton.instance)
            .getString(key, default) ?: default
    }
}