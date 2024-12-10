package com.volcengine.effectone.auto.recorder.base

import com.volcengine.effectone.auto.recorder.R
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.widget.IEOModelItem

const val EO_RECORD_MODEL_HIGH_LIGHT = 5

class RecordHighlightModel : IEOModelItem {
    override val id = EO_RECORD_MODEL_HIGH_LIGHT
    override val content = AppSingleton.instance.getString(R.string.auto_camera_modes_high_light)
    override val duration = 15 * 1000L
}