package com.volcengine.effectone.auto.moment.utils

import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.bytedance.creativex.mediaimport.repository.api.isImage
import com.bytedance.creativex.mediaimport.repository.api.isVideo
import com.bytedance.creativex.mediaimport.util.getMediaFileAbsolutePath
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_ui.MediaItem
import com.volcengine.ck.highlight.config.HLMediaType
import com.volcengine.ck.highlight.data.HLTemplateMatchedInfo
import com.volcengine.ck.highlight.data.HLTemplateMatchedItem
import com.volcengine.ck.highlight.data.RecognizeMedia
import com.volcengine.ck.highlight.utils.isVideo
import com.volcengine.effectone.auto.templates.bean.TemplateByMedias

/**
 *Author: gaojin
 *Time: 2024/6/4 17:06
 */

fun HLTemplateMatchedItem.toMediaItem(): MediaItem {
    return MediaItem(
        width = media.width,
        height = media.height,
        materialId = media.id,
        isMutable = false,
        source = media.path,
        mediaSrcPath = media.path,
        type = if (media.isVideo()) MediaItem.TYPE_VIDEO else MediaItem.TYPE_PHOTO,
        duration = media.duration,
        sourceStartTime = if (media.isVideo()) extractorResult.startTime.toLong() else 0
    )
}

fun HLTemplateMatchedInfo.toTemplateByMedias(): TemplateByMedias {
    return TemplateByMedias(
        templateItem = this.template.any as TemplateItem,
        mediaList = mediaList.map { it.toMediaItem() }
    )
}

fun IMaterialItem.highlightType(): Int {
    return when {
        isImage() -> HLMediaType.IMAGE
        isVideo() -> HLMediaType.VIDEO
        else -> HLMediaType.UNKNOWN
    }
}

fun IMaterialItem.toRecognizeMedia() = RecognizeMedia(
    id = (this as IMediaItem).id.toString(),
    path = getMediaFileAbsolutePath(this),
    type = this.highlightType(),
    width = this.width,
    height = this.height,
    duration = this.duration,
    date = this.date
)