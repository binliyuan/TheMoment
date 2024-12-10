package com.volcengine.effectone.auto.templates.widget.player

import android.view.View

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 13:44
 */
interface IRenderView {

	companion object {
		val SCREEN_SCALE_DEFAULT = 0
		val SCREEN_SCALE_16_9 = 1
		val SCREEN_SCALE_4_3 = 2
		val SCREEN_SCALE_MATCH_PARENT = 3
		val SCREEN_SCALE_ORIGINAL = 4
		val SCREEN_SCALE_CENTER_CROP = 5
	}

	fun attachToPlayer(player: IVideoPlayer)
	fun setVideoSize(videoWidth: Int, videoHeight: Int)
	fun setVideoRotation(degree: Int)
	fun setScaleType(scaleType: Int)
	fun getView(): View
	fun release()
}