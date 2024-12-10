package com.volcengine.effectone.auto.templates.cutsame

import com.cutsame.solution.template.model.TemplateItem

/**
 * @author tyx
 * @description:
 * @date :2024/5/30 15:15
 */
interface ITemplateResourceProvider {
	fun getTemplateList(): List<TemplateItem>
	suspend fun loadTemplatesResource(templateItem: TemplateItem, callback: ILoadTemplatesResourceCallback? = null): TemplateItem
}

interface ILoadTemplatesResourceCallback {
	fun onProgress(progress: Int) {}
}