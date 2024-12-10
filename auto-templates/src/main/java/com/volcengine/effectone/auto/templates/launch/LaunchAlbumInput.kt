package com.volcengine.effectone.auto.templates.launch

import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_ui.MediaItem

/**
 * @author tyx
 * @description:
 * @date :2024/5/15 15:57
 */
data class LaunchAlbumInput(
	val mediaItems: ArrayList<MediaItem>,
	val templateItem: TemplateItem
)
