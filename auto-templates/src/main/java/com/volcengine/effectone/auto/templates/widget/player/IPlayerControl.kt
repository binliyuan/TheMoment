package com.volcengine.effectone.auto.templates.widget.player

/**
 * @author tyx
 * @description:
 * @date :2024/4/28 15:58
 */
interface IPlayerControl {
	fun start()
	fun pause()
	fun getDuration(): Long
	fun getPosition(): Long
	fun isPlaying(): Boolean
	fun seekTo(progress: Int, autoPlay: Boolean, callback: ((ret: Int) -> Unit)? = null)
	fun seeking(value: Int)
}

