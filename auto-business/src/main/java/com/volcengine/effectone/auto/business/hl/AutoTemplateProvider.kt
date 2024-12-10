package com.volcengine.effectone.auto.business.hl

import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.ck.highlight.api.ITemplateProvider
import com.volcengine.ck.moment.base.CKMomentTemplate
import com.volcengine.effectone.InnerEffectOneConfigList
import com.volcengine.effectone.auto.templates.cutsame.TemplateConfig

/**
 *Author: gaojin
 *Time: 2023/7/17 17:14
 */

class AutoTemplateProvider : ITemplateProvider {

    private val mTemplateConfig: TemplateConfig? = InnerEffectOneConfigList.getConfig()

    /**
     * 获取模板信息
     */
    override fun provideTemplateList(): List<CKMomentTemplate> {
        return mutableListOf<CKMomentTemplate>().apply {
            mTemplateConfig?.getTemplateList()?.filter { it.templateTags != "测试模板" }?.let {
                addAll(templateListToCKMoment(it))
            }
        }
    }

    private fun templateListToCKMoment(templateList: List<TemplateItem>): List<CKMomentTemplate> {
        return templateList.map { templateItem ->
            val categoryList = templateItem.category.trim().split(" ").filter { it.isNotEmpty() }
            CKMomentTemplate(
                id = templateItem.id.toString(),
                cover = templateItem.cover?.url ?: "",
                title = templateItem.shortTitle,
                segmentDurations = templateItem.fragments.map { it.duration },
                category = categoryList,
                extra = "",
            ).apply {
                any = templateItem
            }
        }
    }
}