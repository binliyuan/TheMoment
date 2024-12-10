package com.volcengine.effectone.auto.templates.bean

import com.cutsame.solution.template.model.TemplateCategory

/**
 * @author tyx
 * @description:
 * @date :2024/5/30 16:29
 */
class TemplateCategoryResult : BaseResultData<MutableList<TemplateCategory>, TemplateCategoryResult>() {
	override fun create(): TemplateCategoryResult {
		return this
	}
}