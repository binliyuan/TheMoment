package com.volcengine.effectone.auto.moment.hl.data

import com.volcengine.ck.highlight.data.HLResult
import com.volcengine.ck.highlight.data.RecognizeMedia
import java.io.Serializable

data class AutoMaterialRecognizeMedia(
    val recognizeMedia: RecognizeMedia,
    var hlResults: List<HLResult>? = null
) :Serializable
