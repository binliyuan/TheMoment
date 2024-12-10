package com.volcengine.effectone.auto.templates.music

import com.google.gson.annotations.SerializedName
import java.io.File

/**
 *Author: gaojin
 *Time: 2023/12/25 19:16
 */

data class MusicItem(
	@SerializedName("id") val id: String = "",
	@SerializedName("name") val name: String = "",
	@SerializedName("path") val path: String = "",
	@SerializedName("icon") val icon: String = "",
	@SerializedName("hint") val hint: String = "",
	@SerializedName("tags") val tags: String = "",
	@SerializedName("duration") val duration: Long = 0,
) {
	var isSelected = false
	var isPreview = false

	companion object {
		const val DEFAULT_ID = "default"
	}

	override fun equals(other: Any?): Boolean {
		return super.equals(other)
	}
}

data class MusicResource(
	@SerializedName("list") val list: List<MusicItem> = emptyList(),
)

data class MusicResp(
	@SerializedName("type") val type: String = "",
	@SerializedName("resource") val resource: MusicResource? = null,
)


fun MusicItem.iconPath(): String {
	return "${MusicUtils.musicResRootPath}${File.separator}${icon}"
}

fun MusicItem.filePath(): String {
	val path = "${MusicUtils.musicResRootPath}${File.separator}${path}"
	val file = File(path)
	var musicFile: File? = null
	if (file.exists() && file.isDirectory) {
		musicFile = file.listFiles()?.firstOrNull()
	}
	return musicFile?.absolutePath ?: ""
}