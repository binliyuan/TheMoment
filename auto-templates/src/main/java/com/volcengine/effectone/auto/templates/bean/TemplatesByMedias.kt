package com.volcengine.effectone.auto.templates.bean

import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_ui.MediaItem

/**
 * @author tyx
 * @description:
 * @date :2024/4/26 17:54
 */
data class TemplatesByMedias(
	var select: Boolean = false,
	val data: TemplateItem,
	val medias: List<MediaItem>
) {

	override fun equals(other: Any?): Boolean {
		other as TemplatesByMedias
		if (select != other.select) return false
		if (data != other.data) return false
		if (medias != other.medias) return false
		return super.equals(other)
	}

	override fun hashCode(): Int {
		var result = select.hashCode()
		result = 31 * result + data.hashCode()
		return result
	}
}