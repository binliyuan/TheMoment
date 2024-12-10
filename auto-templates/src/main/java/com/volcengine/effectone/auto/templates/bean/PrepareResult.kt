package com.volcengine.effectone.auto.templates.bean

import com.cutsame.solution.template.model.TemplateItem
import com.ss.android.ugc.cut_ui.MediaItem
import com.ss.android.ugc.cut_ui.TextItem

/**
 * @author tyx
 * @description:
 * @date :2024/4/26 10:24
 */
class PrepareResult : BaseResultData<PrepareResultData, PrepareResult>() {
	var hasSwitch: Boolean = false
	fun setHasSwitch(hasSwitch: Boolean): PrepareResult {
		this.hasSwitch = hasSwitch
		return this
	}

	override fun reset(): PrepareResult {
		hasSwitch = false
		return super.reset()
	}

	override fun create(): PrepareResult {
		return this
	}
}

data class PrepareResultData(
	var templateItem: TemplateItem,
	var mediaItemList: List<MediaItem>,
	var textItems: List<TextItem>
)

