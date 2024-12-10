package com.volcengine.effectone.auto.templates.cutsame

import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.effectone.data.IEffectOneConfig

/**
 * @author tyx
 * @description:
 * @date :2024/5/30 15:04
 */
class TemplateConfig : IEffectOneConfig {

	var templateResourceProvider: ITemplateResourceProvider = OfflineTemplateResourceProvider()
	fun getTemplateList(): List<TemplateItem> {
		return templateResourceProvider.getTemplateList()
	}

	suspend fun loadTemplateResource(templateItem: TemplateItem, callback: ILoadTemplatesResourceCallback? = null): TemplateItem {
		return templateResourceProvider.loadTemplatesResource(templateItem, callback)
	}
}