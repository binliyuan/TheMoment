package com.volcengine.effectone.auto.recorder.hl.utils

import com.volcengine.ck.highlight.config.HLMediaType
import com.volcengine.ck.highlight.data.RecognizeMedia
import com.volcengine.effectone.recordersdk.RecordMediaItem
import java.util.UUID

/**
 *Author: gaojin
 *Time: 2024/6/6 00:57
 */

fun RecordMediaItem.toRecognizeMedia(): RecognizeMedia {
    return RecognizeMedia(
        id = UUID.randomUUID().toString(),
        path = this.path,
        type = HLMediaType.VIDEO,
        width = 1080,
        height = 608,
        duration = this.duration,
        date = System.currentTimeMillis()
    )
}