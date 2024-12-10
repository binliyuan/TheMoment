package com.volcengine.effectone.auto.templates.widget.player

/**
 * @author tyx
 * @description:
 * @date :2024/5/22 15:14
 */
open class BaseVideoControl(private val mVideoView: BaseVideoView) : IPlayerControl {
	override fun start() {
		mVideoView.start()
	}

	override fun pause() {
		mVideoView.pause()
	}

	override fun getDuration(): Long {
		return mVideoView.getDuration()
	}

	override fun getPosition(): Long {
		return mVideoView.getPosition()
	}

	override fun isPlaying(): Boolean {
		return mVideoView.isPlaying()
	}

	override fun seekTo(progress: Int, autoPlay: Boolean, callback: ((ret: Int) -> Unit)?) {
		mVideoView.seekTo(progress, callback)
		if (autoPlay) {
			start()
		}
	}

	override fun seeking(value: Int) {
		mVideoView.seeking(value)
	}
}