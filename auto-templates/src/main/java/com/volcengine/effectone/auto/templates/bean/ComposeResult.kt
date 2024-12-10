package com.volcengine.effectone.auto.templates.bean

import com.ss.android.ugc.cut_ui.MediaItem

/**
 * @author tyx
 * @description:
 * @date :2024/5/8 10:11
 */
class ComposeResult : BaseResultData<ArrayList<MediaItem>, ComposeResult>() {
	override fun create(): ComposeResult {
		return this
	}
}