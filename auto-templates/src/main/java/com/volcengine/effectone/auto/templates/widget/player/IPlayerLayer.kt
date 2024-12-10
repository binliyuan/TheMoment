package com.volcengine.effectone.auto.templates.widget.player

import android.content.Context
import android.view.View

/**
 * @author tyx
 * @description:
 * @date :2024/4/28 16:03
 */
interface IPlayerLayer<C : IPlayerControl> {
	fun bindPlayerControl(playControl: C)
	fun unBindPlayerControl()
	fun tag(): String
	fun getView(context: Context): View
	fun onProgress(progress: Long, duration: Long)
	fun onStateChange(state: Int)
	fun onVisible(visible: Boolean)
}