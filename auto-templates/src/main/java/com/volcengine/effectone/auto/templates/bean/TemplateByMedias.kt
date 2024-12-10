package com.volcengine.effectone.auto.templates.bean

import android.os.Parcelable
import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_ui.MediaItem
import kotlinx.parcelize.Parcelize

/**
 *Author: gaojin
 *Time: 2024/6/4 15:07
 */

@Parcelize
data class TemplateByMedias(
    //模板
    val templateItem: TemplateItem,
    //上面模板需要的素材
    val mediaList: List<MediaItem>
) : Parcelable