package com.volcengine.effectone.auto.templates.bean

import com.cutsame.solution.template.model.TemplateItem

/**
 * @author tyx
 * @description:
 * @date :2024/5/30 16:29
 */
class TemplateLoadResult : BaseResultData<TemplateItem, TemplateLoadResult>() {
	override fun create(): TemplateLoadResult {
		return this
	}
}