package com.volcengine.effectone.auto.templates.bean

import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.ss.android.ugc.cut_ui.MediaItem

data class CutSameMediaItem(var mediaItem: MediaItem) {
    var materialItem: IMaterialItem? = null
    var selected: Boolean = false
}