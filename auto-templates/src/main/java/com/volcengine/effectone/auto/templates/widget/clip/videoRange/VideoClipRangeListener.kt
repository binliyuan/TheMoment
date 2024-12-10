package com.volcengine.effectone.auto.templates.widget.clip.videoRange

/**
 * @author tyx
 * @description:
 * @date :2024/5/13 11:09
 */
interface VideoClipRangeListener {
	fun onStartScroll() {}
	fun onScroll(startTime: Int) {}
	fun onEndScroll(startTime: Int) {}
}