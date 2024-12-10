package com.volcengine.effectone.auto.templates.cutsame

import com.cutsame.solution.Constants
import com.cutsame.solution.template.model.Cover
import com.cutsame.solution.template.model.TemplateFragment
import com.cutsame.solution.template.model.TemplateItem
import com.cutsame.solution.template.model.UrlModel
import org.json.JSONObject

/**
 * @author tyx
 * @description:
 * @date :2024/6/11 16:51
 */
object AutoTemplateResourceHelper {

	fun readJsonStringToTemplateResource(jsonString: String): MutableList<TemplateItem> {
		val list = mutableListOf<TemplateItem>()
		val jsonObject = JSONObject(jsonString)
		val data = jsonObject.getJSONObject("data")
		val templateList = data.getJSONArray("template_list")
		val length = templateList.length()
		for (i in 0 until length) {
			val template = templateList.getJSONObject(i)
			val templatesZip = template.optString("template_url")
			val title = template.optString("title")

			val cover = template.getJSONObject("cover").run {
				val url = optString("url")
				val width = optInt("width")
				val height = optInt("height")
				Cover(url, width, height)
			}

			val videoInfo = template.getJSONObject("video_info").run {
				val videoUrl = optString("url")
				UrlModel(videoUrl)
			}

			val fragmentsJsonArray = template.getJSONArray("fragments")
			val fragmentLength = fragmentsJsonArray.length()
			var totalDuration = 0L
			val fragments = mutableListOf<TemplateFragment>()
			for (index in 0 until fragmentLength) {
				val fragment = fragmentsJsonArray.getJSONObject(index)
				val duration = fragment.optInt("duration")
				totalDuration += duration
				fragments.add(TemplateFragment(duration.toLong()))
			}
			//必须有template_tags 不然就Crash
			val templatesTags = template.getString("template_tags")

			val c3Info = template.optString("c3_info") ?: ""

			val templateItem = TemplateItem(
				title = title,
				zipPath = templatesZip,
				templateUrl = templatesZip,
				cover = cover,
				templateType = Constants.TEMPLATE_TYPE_NLE,
				videoInfo = videoInfo,
				fragmentCount = fragments.size,
				duration = totalDuration,
				templateTags = templatesTags,
				fragments = fragments,
				category = c3Info
			)
			list.add(templateItem)
		}
		return list
	}

	fun parseZipName(zipPath: String): String {
		val startFix = "//"
		val endFix = "/"
		val index = zipPath.indexOf(startFix)
		if (index == -1) return zipPath
		val lastIndex = zipPath.lastIndexOf(endFix)
		if (lastIndex == -1) return zipPath
		return zipPath.substring(index + startFix.length, lastIndex)
	}
}