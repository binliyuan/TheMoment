package com.volcengine.effectone.auto.templates.cutsame

import com.cutsame.solution.template.model.TemplateItem
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author tyx
 * @description: 离线素材提供
 * @date :2024/6/11 16:49
 */
class OfflineTemplateResourceProvider : ITemplateResourceProvider {

	var offlineResSrcDir = "template"
	var jsonName = "auto_templates.json"

	override fun getTemplateList(): List<TemplateItem> {
		val list = mutableListOf<TemplateItem>()
		val jsonString = EOUtils.fileUtil.readJsonFromAssets(AppSingleton.instance, "$offlineResSrcDir/$jsonName")
		list.addAll(AutoTemplateResourceHelper.readJsonStringToTemplateResource(jsonString))
		return list
	}

	override suspend fun loadTemplatesResource(templateItem: TemplateItem, callback: ILoadTemplatesResourceCallback?): TemplateItem {
		var zipPath = templateItem.zipPath
		if (zipPath.startsWith("file://")) {
			val pathName = AutoTemplateResourceHelper.parseZipName(zipPath)
			val offlinePath = zipPath.substringAfter("file://")
			val rootFile = EOUtils.pathUtil.internalResource("template")
			val zipFile = File(rootFile, offlinePath)
			if (zipFile.exists().not()) {
				withContext(Dispatchers.IO) {
					EOUtils.fileUtil.copyAssets(
						AppSingleton.instance.assets, "$offlineResSrcDir/$pathName", pathName, rootFile.absolutePath
					)
				}
			}
			zipPath = zipFile.absolutePath
		}
		return templateItem.copy(
			templateUrl = zipPath,
			zipPath = zipPath
		)
	}
}